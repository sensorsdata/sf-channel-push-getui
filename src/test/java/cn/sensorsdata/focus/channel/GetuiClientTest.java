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

import com.sensorsdata.focus.channel.entry.LandingType;
import com.sensorsdata.focus.channel.entry.MessagingTask;
import com.sensorsdata.focus.channel.entry.PushTask;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GetuiClientTest extends TestCase {

  // 个推账号
  private static final String APP_ID = "geO2qJxEb99qGLGniiyyi4";
  private static final String APP_KEY = "CJE44wmIfm5OffjboZ1iO3";
  private static final String MASTER_SECRET = "mExfQVhKfv6K64HLnegs64";
  // 测试设备的推送 ID
  private static final String CLIENT_ID = "5b976ac30a942baf04a1c21d068656e1";

  @Test
  @Ignore
  public void testSend() throws Exception {
//    testDistinctPushType(null);
    testDistinctPushType("passthrough");
//    testDistinctPushType("notification");
  }

  private void testDistinctPushType(String pushType) throws Exception {
    GetuiChannelConfig getuiChannelConfig = new GetuiChannelConfig();
    getuiChannelConfig.setAppId(APP_ID);
    getuiChannelConfig.setAppKey(APP_KEY);
    getuiChannelConfig.setMasterSecret(MASTER_SECRET);
    getuiChannelConfig.setPushType(pushType);
//    getuiChannelConfig.setIntentTemplate("intent:w w ww ;end");

    PushTask pushTask = new PushTask();
    pushTask.setLandingType(LandingType.CUSTOMIZED);
    Map<String, String> testMap = new LinkedHashMap<>();
    testMap.put("aaa", "bb");
    pushTask.setCustomized(testMap);
    pushTask.setMsgContent("content7" + pushType);
    pushTask.setMsgTitle("title7" + pushType);
    pushTask.setClientId(CLIENT_ID);
    pushTask.setSfData(
        "{\"sf_link_url\":\"http://sensorsdata.cn\",\"sf_landing_type\":\"CUSTOMIZED\",\"sf_msg_id\":\"83ddb764-e163-453e-a715-d68621170b71\",\"sf_plan_id\":\"3\",\"sf_audience_id\":5,\"sf_plan_strategy_id\":\"0\",\"sf_strategy_unit_id\":\"100\",\"sf_plan_type\":\"运营计划\",\"customized\":{ \"book_id\":\"12345\",\"news_id\":\"678\"}}");

    MessagingTask messagingTask = new MessagingTask();
    messagingTask.setPushTask(pushTask);

    GetuiClient getuiClient = new GetuiClient();
    getuiClient.initChannelClient(getuiChannelConfig);
    getuiClient.send(Collections.singletonList(messagingTask));
    getuiClient.close();
  }
}