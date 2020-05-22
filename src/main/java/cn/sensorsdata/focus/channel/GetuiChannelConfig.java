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

import com.sensorsdata.focus.channel.ChannelConfig;
import com.sensorsdata.focus.channel.annotation.ConfigField;
import com.sensorsdata.focus.channel.annotation.SfChannelConfig;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@SfChannelConfig
@Data
public class GetuiChannelConfig extends ChannelConfig {

  @ConfigField(cname = "请求地址", desc = "个推请求地址", defaultValue = "http://sdk.open.api.igexin.com/apiex.htm")
  private String url = "http://sdk.open.api.igexin.com/apiex.htm";

  @ConfigField(cname = "推送类型", desc = "推送类型")
  private String pushType;

  @ConfigField(cname = "AppID", desc = "平台 AppID，可在平台配置页面获取")
  @NotBlank
  @Size(min = 22, max = 22)
  private String appId;

  @ConfigField(cname = "AppKey", desc = "平台 AppKey，可在平台配置页面获取")
  @NotBlank
  @Size(min = 22, max = 22)
  private String appKey;

  @ConfigField(cname = "MasterSecret", desc = "平台 MasterSecret，可在平台配置页面获取")
  @NotBlank
  @Size(min = 22, max = 22)
  private String masterSecret;

  @ConfigField(cname = "安卓推送 Intent 模板", desc = "可选配置。设置第三方厂商通道使用的 Intent 模板。")
  private String intentTemplate;
}
