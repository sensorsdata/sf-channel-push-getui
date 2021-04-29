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

import com.sensorsdata.focus.channel.entry.MessagingTask;
import com.sensorsdata.focus.channel.entry.PushTask;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetuiClientTest extends TestCase {

  // 个推账号
  private static final String APP_ID = "";
  private static final String APP_KEY = "";
  private static final String MASTER_SECRET = "";
  // 测试设备的推送 ID
  private static final String CLIENT_ID = "";  // canva-android
//  private static final String CLIENT_ID = ""; // canva-IOS

  @Test
  @Ignore
  public void testSend() throws Exception {
    GetuiChannelConfig getuiChannelConfig = new GetuiChannelConfig();
    getuiChannelConfig.setAppId(APP_ID);
    getuiChannelConfig.setAppKey(APP_KEY);
    getuiChannelConfig.setMasterSecret(MASTER_SECRET);
    getuiChannelConfig.setIntentTemplate("intent:#Intent;action=android.intent.action.oppopush;package=cn.canva.editor;component=cn.canva.editor/com.canva.app.editor.inappmessage.GetuiPushActivity;S.key={\"webUrl\":\"{sf_customized.webUrl}\",\"analyticPushId\":\"{sf_customized.analyticPushId}\",\"action2\":\"{sf_customized.action2}\"};end");

    List<MessagingTask> messagingTaskList = new ArrayList<>();

    PushTask pushTask = new PushTask();

    // 测试OPEN_APP
//    pushTask.setLandingType(LandingType.OPEN_APP);
//    pushTask.setSfData("{\"customized\":{},\"sf_landing_type\":\"OPEN_APP\",\"sf_msg_id\":\"b205ab0d-8606-42a8-8617-97042508d396\",\"sf_plan_id\":\"-1\",\"sf_audience_id\":\"-1\",\"sf_plan_strategy_id\":\"-1\",\"sf_strategy_unit_id\":null,\"sf_plan_type\":\"运营计划\"})");

    // 测试 LINK
//    pushTask.setLandingType(LandingType.LINK);
//    pushTask.setLinkUrl("https://www.baidu.com");
//    pushTask.setSfData("{\"customized\":{},\"sf_link_url\":\"https://www.baidu.com\",\"sf_landing_type\":\"LINK\",\"sf_msg_id\":\"e8fe3077-b9ca-4d83-a288-83eeb69d5f67\",\"sf_plan_id\":\"-1\",\"sf_audience_id\":\"-1\",\"sf_plan_strategy_id\":\"-1\",\"sf_strategy_unit_id\":null,\"sf_plan_type\":\"运营计划\"}");

    // 测试 CUSTOMIZED
//    pushTask.setLandingType(LandingType.CUSTOMIZED);
    Map<String, String> testMap = new LinkedHashMap<>();
    testMap.put("payload", "{\"action\":{\"action2\":\"search\",\"searchQuery2\":\"今日推荐\"}}");
    testMap.put("webUrl", "https://www.canva.cn/search/templates?q=今日推荐");
    testMap.put("analyticPushId", "20200721_test");
    testMap.put("action2", "");
    testMap.put("logoUrl", "https://wx4.sinaimg.cn/mw690/e04cc7a6gy1gfknluab27j20u00u0hdt.jpg");
//    pushTask.setSfData("{\"customized\":{\"payload\":\"{\\\"action\\\":{\\\"action2\\\":\\\"search\\\",\\\"searchQuery2\\\":\\\"今日推荐\\\"}}\",\"webUrl\":\"https://www.canva.cn/search/templates?q=今日推荐\",\"analyticPushId\":\"20200721_test\",\"action2\":\"\",\"logoUrl\":\"https://wx4.sinaimg.cn/mw690/e04cc7a6gy1gfknluab27j20u00u0hdt.jpg\"},\"sf_landing_type\":\"CUSTOMIZED\",\"sf_msg_id\":\"755c2363-2dfe-4193-86b6-49692906d460\",\"sf_plan_id\":\"-1\",\"sf_audience_id\":\"-1\",\"sf_plan_strategy_id\":\"-1\",\"sf_strategy_unit_id\":null,\"sf_plan_type\":\"运营计划\"}");
//    pushTask.setCustomized(testMap);


    pushTask.setMsgContent("content123");
    pushTask.setMsgTitle("title123");
    pushTask.setClientId(CLIENT_ID);

    MessagingTask messagingTask = new MessagingTask();
    messagingTask.setPushTask(pushTask);
    messagingTaskList.add(messagingTask);

    GetuiClient getuiClient = new GetuiClient();
    getuiClient.initChannelClient(getuiChannelConfig);


//    getuiClient.send(messagingTaskList);
    getuiClient.close();
  }
}