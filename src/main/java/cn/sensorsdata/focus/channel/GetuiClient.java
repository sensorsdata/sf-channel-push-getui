/*
 * Copyright 2019 Sensors Data Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.sensorsdata.focus.channel;

import com.sensorsdata.focus.channel.ChannelClient;
import com.sensorsdata.focus.channel.ChannelConfig;
import com.sensorsdata.focus.channel.annotation.SfChannelClient;
import com.sensorsdata.focus.channel.entry.MessagingTask;
import com.sensorsdata.focus.channel.entry.PushTask;
import com.sensorsdata.focus.channel.push.PushTaskUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.gexin.fastjson.JSONObject;
import com.gexin.rp.sdk.base.IBatch;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.ListMessage;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.notify.Notify;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.dto.GtReq;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.AbstractTemplate;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.gexin.rp.sdk.template.style.Style0;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SfChannelClient(version = "v0.1.1", desc = "SF 个推推送客户端")
@Slf4j
public class GetuiClient extends ChannelClient {

  private static final long MESSAGE_OFFLINE_EXPIRE_TIME = 24 * 3600 * 1000L;

  // 每次批量发送请求最多包含多少推送 ID
  private static final int BATCH_SIZE = 100;

  // 第三方 intent 字段模板
  private String intentTemplate;

  private IGtPush pushClient;

  private String appKey;
  private String appId;

  private static final String STR_SF_DATA = "sf_data";

  @Override
  public void initChannelClient(ChannelConfig channelConfig) {
    GetuiChannelConfig getuiChannelConfig = (GetuiChannelConfig) channelConfig;

    appId = getuiChannelConfig.getAppId();
    appKey = getuiChannelConfig.getAppKey();
    String getuiApiHost = getuiChannelConfig.getUrl();
    String masterSecret = getuiChannelConfig.getMasterSecret();

    // 这个 client 的 close 还要发 http 请求，比较奇怪，先不管
    pushClient = new IGtPush(getuiApiHost, appKey, masterSecret);

    this.intentTemplate = getuiChannelConfig.getIntentTemplate();
  }

  @Override
  public void send(List<MessagingTask> messagingTasks) throws Exception {
    // 将推送内容相同的任务分到一组，后面按组批量推送
    Collection<List<MessagingTask>> taskGroups = PushTaskUtils.groupByTaskContent(messagingTasks, BATCH_SIZE);

    List<Pair<SingleMessage, MessagingTask>> singleMessageBatch = new ArrayList<>();

    for (List<MessagingTask> taskList : taskGroups) {
      MessagingTask messagingTask = taskList.get(0);
      PushTask pushTask = messagingTask.getPushTask();

      AbstractTemplate template = constructTemplate2(pushTask);
      template.setAppId(appId);
      template.setAppkey(appKey);

      if (taskList.size() == 1) {
        // 一组只有一条的话，那么可能是推给很多人，内容不同的情况，使用批量单推的方式
        SingleMessage message = new SingleMessage();
        message.setData(template);
        message.setOffline(true);
        message.setOfflineExpireTime(MESSAGE_OFFLINE_EXPIRE_TIME);

        singleMessageBatch.add(Pair.of(message, messagingTask));
        log.debug("add push task into batch. [task='{}']", messagingTask);

      } else {
        List<Target> targets = new ArrayList<>();
        for (MessagingTask task : taskList) {
          Target target = new Target();
          target.setAppId(appId);
          target.setClientId(task.getPushTask().getClientId());
          targets.add(target);
        }

        ListMessage message = new ListMessage();
        message.setData(template);
        // 设置消息离线，并设置离线时间
        message.setOffline(true);
        // 离线有效时间，单位为毫秒，可选
        message.setOfflineExpireTime(MESSAGE_OFFLINE_EXPIRE_TIME);

        String failReason = null;
        try {
          // http://docs.getui.com/getui/server/java/push/
          // 2.3.2 pushMessageToList-对指定用户列表推送消息
          String taskId = pushClient.getContentId(message);
          IPushResult ret = pushClient.pushMessageToList(taskId, targets);
          log.debug("finish push process. [tasks='{}', response='{}']", messagingTasks, ret.getResponse());

          // 如果发送多条数据，只要有一条成功，这个接口就返回 ok
          String result = (String) ret.getResponse().get("result");
          if (!"ok".equals(result)) {
            failReason = result;
          }
        } catch (Exception e) {
          log.warn("push with exception. [tasks='{}']", taskList);
          log.warn("exception detail:", e);
          failReason = ExceptionUtils.getMessage(e);
        }

        for (MessagingTask task : taskList) {
          task.setSuccess(failReason == null);
          task.setFailReason(failReason);
        }
      }
    }

    flushSingleMessageBatch(singleMessageBatch);
  }

  private void flushSingleMessageBatch(List<Pair<SingleMessage, MessagingTask>> singleMessageBatch) {
    for (int from = 0; from < singleMessageBatch.size(); from += BATCH_SIZE) {
      int to = Math.min(from + BATCH_SIZE, singleMessageBatch.size());
      flushSingleMessageBatchInner(singleMessageBatch.subList(from, to));
    }
  }

  private void flushSingleMessageBatchInner(List<Pair<SingleMessage, MessagingTask>> singleMessageBatch) {
    IBatch batch = pushClient.getBatch();
    for (Pair<SingleMessage, MessagingTask> taskPair : singleMessageBatch) {
      MessagingTask messagingTask = taskPair.getRight();

      Target target = new Target();
      target.setAppId(appId);
      target.setClientId(messagingTask.getPushTask().getClientId());

      try {
        batch.add(taskPair.getLeft(), target);
      } catch (Exception e) {
        log.warn("add message into batch with exception. [task='{}']", messagingTask);
        log.warn("exception detail:", e);
      }
    }

    IPushResult pushResult;
    try {
      pushResult = batch.submit();
    } catch (Exception e) {
      log.warn("getui batch submit failed. [tasks='{}']", singleMessageBatch.stream().map(Pair::getRight).collect(
          Collectors.toList()));
      log.warn("exception detail:", e);

      String failReason = ExceptionUtils.getMessage(e);
      for (Pair<SingleMessage, MessagingTask> taskPair : singleMessageBatch) {
        taskPair.getRight().setFailReason(failReason);
      }
      return;
    }

    Map<String, Object> response = pushResult.getResponse();
    log.debug("finish batch push process. [response='{}']", response);

    // 取推送结果
    JSONObject infoJson = (JSONObject) response.get("info");
    if (infoJson != null) {
      for (int i = 0; i < singleMessageBatch.size(); ++i) {
        MessagingTask messagingTaskTask = singleMessageBatch.get(i).getRight();
        String resultKey = Integer.toString(i + 1);
        JSONObject taskInfo = (JSONObject) infoJson.get(resultKey);
        String result = (String) taskInfo.get("result");
        if ("ok".equals(result)) {
          messagingTaskTask.setSuccess(true);
        } else {
          messagingTaskTask.setSuccess(false);
          messagingTaskTask.setFailReason(result);
        }
      }
    }
  }

  private static Style0 constructStyle(PushTask pushTask) {
    Style0 style = new Style0();
    // 设置通知栏标题与内容
    style.setTitle(pushTask.getMsgTitle());
    style.setText(pushTask.getMsgContent());
    // 配置通知栏图标
    style.setLogo("");
    // 配置通知栏网络图标
    style.setLogoUrl("");
    // 设置通知是否响铃，震动，或者可清除
    style.setRing(true);
    style.setVibrate(true);
    style.setClearable(true);
    return style;
  }

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static {
    OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
  }

  /**
   * 构造透传内容，可修改此函数实现自定义格式的透传内容
   */
  private String constructTransmissionContent(PushTask pushTask) {
    return pushTask.getSfData();
  }

  /**
   * 使用透传模板构造消息
   */
  private TransmissionTemplate constructTemplate2(PushTask pushTask) {
    TransmissionTemplate transmissionTemplate = new TransmissionTemplate();
    // 透传消息设置，1 为强制启动应用，客户端接收到消息后就会立即启动应用；2 为等待应用启动
    transmissionTemplate.setTransmissionType(2);
    // 这里也可以传消息，但如果目的只是打开 App，可以不传
    String transmissionContent = constructTransmissionContent(pushTask);
    transmissionTemplate.setTransmissionContent(transmissionContent);

    String intent = null;
    if (StringUtils.isNotBlank(intentTemplate)) {
      intent = PushTaskUtils.generateIntentFromTemplate(pushTask, intentTemplate);
      Notify notify = new Notify();
      notify.setContent(pushTask.getMsgContent());
      notify.setTitle(pushTask.getMsgTitle());
      notify.setIntent(intent);
      notify.setType(GtReq.NotifyInfo.Type._intent);
      transmissionTemplate.set3rdNotifyInfo(notify);
    }

    APNPayload apnPayload = new APNPayload();
    APNPayload.DictionaryAlertMsg alertMsg = new APNPayload.DictionaryAlertMsg();
    alertMsg.setTitle(pushTask.getMsgTitle());
    alertMsg.setBody(pushTask.getMsgContent());
    apnPayload.setAlertMsg(alertMsg);
    apnPayload.addCustomMsg(STR_SF_DATA, pushTask.getSfData());
    transmissionTemplate.setAPNInfo(apnPayload);

    log.debug("construct template. [content='{}', intent='{}', cid='{}']", transmissionContent, intent,
        pushTask.getClientId());
    return transmissionTemplate;
  }

  /**
   * 使用通知模板构造消息。默认使用 constructTemplate2 而不是本函数
   */
  private NotificationTemplate constructTemplate(PushTask pushTask) { // NOSONAR 暂时保留这种方式
    NotificationTemplate notificationTemplate = new NotificationTemplate();
    // 透传消息设置，1 为强制启动应用，客户端接收到消息后就会立即启动应用；2 为等待应用启动
    notificationTemplate.setTransmissionType(1);
    // 这里也可以传消息，但如果目的只是打开 App，可以不传
    notificationTemplate.setStyle(constructStyle(pushTask));
    notificationTemplate.setTransmissionContent(pushTask.getSfData());

    APNPayload apnPayload = new APNPayload();
    APNPayload.DictionaryAlertMsg alertMsg = new APNPayload.DictionaryAlertMsg();
    alertMsg.setTitle(pushTask.getMsgTitle());
    alertMsg.setBody(pushTask.getMsgContent());
    apnPayload.setAlertMsg(alertMsg);
    apnPayload.addCustomMsg(STR_SF_DATA, pushTask.getSfData());
    notificationTemplate.setAPNInfo(apnPayload);

    return notificationTemplate;
  }
}
