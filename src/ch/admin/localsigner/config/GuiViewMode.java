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
 * Definition of the GUI-mode as configured in configuration files (guiViewMode).
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public enum GuiViewMode
{

  SIMPLE_MODE("simple"),
  PROFESSIONAL_MODE("professional"),
  MINIMAL_MODE("minimal"),
  UNKNOWN("unknown");

  private final String configValue;

  GuiViewMode(String configValue)
  {
    this.configValue = configValue;
  }

  public String getConfigurationValue()
  {
    return configValue;
  }

  public static GuiViewMode fromConfigurationValue(String configuredValue)
  {
    if (SIMPLE_MODE.getConfigurationValue().equals(configuredValue))
    {
      return SIMPLE_MODE;
    } if (PROFESSIONAL_MODE.getConfigurationValue().equals(configuredValue))
    {
      return PROFESSIONAL_MODE;
    } if (MINIMAL_MODE.getConfigurationValue().equals(configuredValue))
    {
      return MINIMAL_MODE;
    } else
    {
      return UNKNOWN;
    }
  }

  public boolean isUnknown()
  {
    return (GuiViewMode.UNKNOWN.getConfigurationValue().equals(getConfigurationValue()));
  }

  public boolean isSimpleMode()
  {
    return (GuiViewMode.SIMPLE_MODE.getConfigurationValue().equals(getConfigurationValue()));
  }

  public boolean isMinimalMode()
  {
    return (GuiViewMode.MINIMAL_MODE.getConfigurationValue().equals(getConfigurationValue()));
  }

  public boolean isProfessionalMode()
  {
    return (GuiViewMode.PROFESSIONAL_MODE.getConfigurationValue().equals(getConfigurationValue()));
  }
}
