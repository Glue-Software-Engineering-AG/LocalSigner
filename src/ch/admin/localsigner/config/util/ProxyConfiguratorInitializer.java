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
package ch.admin.localsigner.config.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.proxylibrary.core.enums.LogLevel;
import ch.glue.proxylibrary.core.enums.ProxyConfigurationKind;
import ch.glue.proxylibrary.core.integration.ConfigAdapter;
import ch.glue.proxylibrary.core.integration.LanguageAdapter;
import ch.glue.proxylibrary.core.integration.LogAdapter;
import ch.glue.proxylibrary.core.util.Bootstrapper;

/**
 * This class initialize the adapters for the proxylibrary and provides a boolean to make a restart.
 *
 * @author keller
 */
public class ProxyConfiguratorInitializer
{

  private static final String PROXYCONFIGURATIONGUI_CLASS = "PROXYCONFIGURATIONGUI class: ";

  private static final Logger LOGGER = Logger.getLogger(ProxyConfiguratorInitializer.class);

  private static boolean restartNeeded = false;

  /**
   * This method performs an anonymous implementation of the apaters from the proxylibrary via the bootstrapper class.
   */
  @SuppressWarnings("serial")
  public ProxyConfiguratorInitializer()
  {
    Bootstrapper.initialize(new LogAdapter()
    {
      @Override
      public void log(
          Class<?> type, LogLevel ll, String string)
      {
        switch (ll)
        {
          case DEBUG:
            LOGGER.debug(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string);
            break;
          case INFO:
            LOGGER.info(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string);
            break;
          case WARNING:
            LOGGER.warn(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string);
            break;
          case TRACE:
            LOGGER.trace(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string);
            break;
          default:
            LOGGER.error(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string);
        }
      }

      @Override
      public void log(
          Class<?> type, LogLevel ll, String string, Throwable thrwbl)
      {
        switch (ll)
        {
          case DEBUG:
            LOGGER.debug(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string, thrwbl);
            break;
          case INFO:
            LOGGER.info(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string, thrwbl);
            break;
          case WARNING:
            LOGGER.warn(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string, thrwbl);
            break;
          case TRACE:
            LOGGER.trace(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string, thrwbl);
            break;
          default:
            LOGGER.error(PROXYCONFIGURATIONGUI_CLASS + type + " " + ll + " " + string, thrwbl);
        }
      }
    }, new ConfigAdapter()
    {

      @Override
      public String getHttpProxyHost()
      {
        return LocalSigner.appConfig.getProxyHttpHost();
      }

      @Override
      public String getHttpProxyPort()
      {
        return LocalSigner.appConfig.getProxyHttpPort();
      }

      @Override
      public String getSslProxyHost()
      {
        return LocalSigner.appConfig.getProxyHttpsHost();
      }

      @Override
      public String getSslProxyPort()
      {
        return LocalSigner.appConfig.getProxyHttpsPort();
      }

      @Override
      public String getProxyExclusionValue()
      {
        return LocalSigner.appConfig.getProxyExclusions();
      }

      @Override
      public String getPacURI()
      {
        return LocalSigner.appConfig.getPacURI();
      }

      @Override
      public ProxyConfigurationKind getProxyConfigurationKind()
      {
        if (StringUtils.isBlank(LocalSigner.appConfig.getProxyConfigurationKindStr()))
        {
          if (StringUtils.isBlank(LocalSigner.appConfig.getProxyHttpHost()))
          {
            return ProxyConfigurationKind.AUTO_PROXY;
          }
          else
          {
            return ProxyConfigurationKind.MANUAL_PROXY;
          }
        }
        else
        {
          return ProxyConfigurationKind.value(LocalSigner.appConfig.getProxyConfigurationKindStr());
        }
      }

      @Override
      public boolean getIsConfigEditable()
      {
        return LocalSigner.appConfig.isEditable(Config.PROXY_EDIT);
      }

      @Override
      public void setHttpProxyHost(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_HTTP_HOST, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setHttpProxyPort(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_HTTP_PORT, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setSslProxyHost(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_HTTPS_HOST, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setSslProxyPort(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_HTTPS_PORT, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setProxyExclusion(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_EXCLUSIONS, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setPacURI(String string)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PAC_URI, string);
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setProxyConfigurationKind(ProxyConfigurationKind pck)
      {
        try
        {
          LocalSigner.appConfig.setValue(Config.PROXY_CONFIGURATION_KIND, pck.toString());
        } catch (ConfigurationException ce)
        {
          showSaveConfigurationErrorMessage(ce);
        }
      }

      @Override
      public void setRestartNeeded()
      {
        restartNeeded = true;
      }

    }, new LanguageAdapter()
    {
      @Override
      public String getTextForInvalidHttpPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxyHttpPortTextForErrorMessage");
      }

      @Override
      public String getTextForInvalidSslPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxySslPortTextForErrorMessage");
      }

      @Override
      public String getWindowTitle()
      {
        return LocalSigner.i18n("proxyGUI.proxyconfig");
      }

      @Override
      public String getLabelForNoProxy()
      {
        return LocalSigner.i18n("proxyGUI.noProxy");
      }

      @Override
      public String getLabelForManualProxy()
      {
        return LocalSigner.i18n("proxyGUI.manualProxy");
      }

      @Override
      public String getLabelForSystemProxy()
      {
        return LocalSigner.i18n("proxyGUI.systemProxy");
      }

      @Override
      public String getLabelForAutoProxy()
      {
        return LocalSigner.i18n("proxyGUI.automaticDetectProxy");
      }

      @Override
      public String getLabelForPacProxy()
      {
        return LocalSigner.i18n("proxyGUI.pacScriptProxy");
      }

      @Override
      public String getLabelForHttpProxyHost()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxyHttpHost");
      }

      @Override
      public String getLabelForHttpProxyPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxyPort");
      }

      @Override
      public String getLabelForSslProxyHost()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxySslHost");
      }

      @Override
      public String getLabelForSslProxyPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxyPort");
      }

      @Override
      public String getLabelForProxyExclusion()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.proxyExclusions");
      }

      @Override
      public String getTooltipForProxyHost()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.tooltipProxyhost");
      }

      @Override
      public String getTooltipForProxyPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.tooltipProxyport");
      }

      @Override
      public String getTooltipForProxyExclusions()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.tooltipProxyExclusions");
      }

      @Override
      public String getTooltipForPacScriptURI()
      {
        return LocalSigner.i18n("proxyGUI.pnlPacProxy.tooltipPacScript");
      }

      @Override
      public String getTextForCancelButton()
      {
        return LocalSigner.i18n("cancel");
      }

      @Override
      public String getTextForOkButton()
      {
        return LocalSigner.i18n("ok");
      }

      @Override
      public String getTextForConfigurationErrorAllFieldsEmptyMessage()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.configErrorProxy");
      }

      @Override
      public String getTextForConfigErrorProxyMissingPacScript()
      {
        return LocalSigner.i18n("proxyGUI.pnlPacProxy.configErrorProxyPacScript");
      }

      @Override
      public String getTextFormatForConfigErrorProxyEmptyTextfield()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.configErrorProxyEmptyTextfield");
      }

      @Override
      public String getTextFormatForConfigErrorProxyInvalidHost()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.configErrorProxyInvalidHost");
      }

      @Override
      public String getTextFormatForConfigErrorProxyInvalidPort()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.configErrorProxyInvalidPort");
      }

      @Override
      public String getTextForConfigErrorProxyInvalidHostExclusionSupplement()
      {
        return LocalSigner.i18n("proxyGUI.pnlManualProxy.configErrorProxyInvalidHostExclusionSupplement");
      }

      @Override
      public String getTextForConfigurationWarningMessageTitle()
      {
        return LocalSigner.i18n("configerror");
      }
    });
  }

  /**
   * Displays an error message indicating that the configuration cannot be saved.
   *
   * @param ce The exception by saving the configuration
   */
  private void showSaveConfigurationErrorMessage(ConfigurationException ce)
  {
    LOGGER.error(ce);
    Shell shell = new Shell();
    // present error message to user
    Message.error(shell, LocalSigner.i18n("errorsaveconfig"));
  }

  /**
   * This method is used to query if a reboot is required.
   *
   * @return true if a reboot is required
   */
  public static boolean isRestartNeeded()
  {
    return restartNeeded;
  }
}
