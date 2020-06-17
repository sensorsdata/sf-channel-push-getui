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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetuiClientTest extends TestCase {

  // 个推账号
  private static final String APP_ID = "aaaaaaaaaaaaaaaaaaaaaa";
  private static final String APP_KEY = "bbbbbbbbbbbbbbbbbbbbbb";
  private static final String MASTER_SECRET = "cccccccccccccccccccccc";
  // 测试设备的推送 ID
  private static final String CLIENT_ID = "dddddddddddddddddddddddddddddddd";

  @Test
  @Ignore
  public void testSend() throws Exception {
    GetuiChannelConfig getuiChannelConfig = new GetuiChannelConfig();
    getuiChannelConfig.setAppId(APP_ID);
    getuiChannelConfig.setAppKey(APP_KEY);
    getuiChannelConfig.setMasterSecret(MASTER_SECRET);

    List<MessagingTask> messagingTaskList = new ArrayList<>();
    for (int i=0; i<3; i++){

      PushTask pushTask = new PushTask();
      pushTask.setLandingType(LandingType.CUSTOMIZED);
      Map<String, String> testMap = new LinkedHashMap<>();
      testMap.put("aaa", "bb");
      pushTask.setCustomized(testMap);
      pushTask.setMsgContent("content111"+i);
      pushTask.setMsgTitle("title123");
      pushTask.setClientId(CLIENT_ID);
      MessagingTask messagingTask = new MessagingTask();
      messagingTask.setPushTask(pushTask);
      messagingTaskList.add(messagingTask);
      messagingTaskList.add(messagingTask);
    }

    GetuiClient getuiClient = new GetuiClient();
    getuiClient.initChannelClient(getuiChannelConfig);


    getuiClient.send(messagingTaskList);
    getuiClient.close();
  }
}