/*
 * Copyright 2020 The Federal Authorities of the Swiss Confederation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package ch.admin.localsigner.config;

/**
 * This class is a helper class for the timestamp configuration.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */

public class TsaConfiguration
{
  private String url;

  /** Key is needed for configuring system tsas. */
  private String lookupKey;

  /** Text to be displayed in GUI. */
  private String displayText;

  private String username;

  private String password;

  public TsaConfiguration(String url, String lookupKey, String username, String password,
      String displayText)
  {
    this.url = url;
    this.lookupKey = lookupKey;
    this.username = username;
    this.password = password;
    this.displayText = displayText;
  }

  public TsaConfiguration(String url, String lookupKey, String displayText)
  {
    this.url = url;
    this.lookupKey = lookupKey;
    this.displayText = displayText;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public String getLookupKey()
  {
    return lookupKey;
  }

  public void setLookupKey(String lookupKey)
  {
    this.lookupKey = lookupKey;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }

  public String getDisplayText()
  {
    return displayText;
  }

  public void setDisplayText(String displayText)
  {
    this.displayText = displayText;
  }

}
