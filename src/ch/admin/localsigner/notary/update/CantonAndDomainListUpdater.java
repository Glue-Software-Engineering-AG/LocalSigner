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
package ch.admin.localsigner.notary.update;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import ch.admin.localsigner.config.ZulabConfiguration;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.OnlineServices;

/**
 * This class checks online for updates of canton and domain list for
 * Zulassungsbestätigung of UPReg/ZulaB. If there is an update available, the
 * user gets informed and the file updated if desired.
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public class CantonAndDomainListUpdater implements Runnable
{

  private static final Logger LOGGER = Logger.getLogger(CantonAndDomainListUpdater.class);

  private final MainGUI maingui;

  private final String url;

  public CantonAndDomainListUpdater(MainGUI maingui, String url)
  {
    this.maingui = maingui;
    this.url = url;
  }

  @Override
  public void run()
  {
    LOGGER.info("Checking for canton and domain list udpate using url: " + url);

    try
    {
      HttpURLConnection conn = new OnlineServices(LocalSigner.appConfig).getConfiguredSslConnection(url);

      LOGGER.debug("Status code for canton and domain list update request: " + conn.getResponseCode());

      InputStream updateXml = conn.getInputStream();
      final byte[] xmlData = IOUtils.toByteArray(updateXml);

      // check if XML response is not empty and is the expected XML file
      if (!new String(xmlData).contains("<config version="))
      {
        LOGGER.warn("no XML response, giving up");
        return;
      }

      ZulabConfiguration availableUpdate = new ZulabConfiguration(new ByteArrayInputStream(xmlData));

      if (isUpdateAvailable(availableUpdate, LocalSigner.appConfig.getZulabListsVersion()))
      {
        StringBuilder builder = new StringBuilder();
        builder.append(LocalSigner.i18n("zulab.updateDialog.updateFound")).append("\n\n")
            .append(LocalSigner.i18n("zulab.updateDialog.currentVersion")).append(": ")
            .append(
                humanReadableDateFromVersionDate(LocalSigner.appConfig.getZulabListsVersion(), LocalSigner.getLocale()))
            .append("\n").append(LocalSigner.i18n("zulab.updateDialog.newVersion")).append(": ")
            .append(humanReadableDateFromVersionDate(availableUpdate.getVersion(), LocalSigner.getLocale()));
        final String msg = builder.toString();

        LOGGER.debug("show update dialog");

        Listener updateDesiredListener = new Listener()
        {
          @Override
          public void handleEvent(Event event)
          {
            try
            {
              FileOutputStream fos = new FileOutputStream(LocalSigner.appConfig.getZulabListsFile());
              fos.write(xmlData);
              fos.close();

              LocalSigner.appConfig.reloadUpdatedZulabConfiguration();
            } catch (Exception e)
            {
              LOGGER.warn("Failed to write ZulaB lists update (" + e.getMessage() + ")");

              Message.warning(maingui.getMainshell(), LocalSigner.i18n("zulab.updateDialog.networkWarning"));
            }
          }
        };

        new Message(maingui.getMainshell(), SWT.ICON_INFORMATION | SWT.ON_TOP | SWT.OK | SWT.CANCEL,
            LocalSigner.i18n("warning"), msg, updateDesiredListener);

      }
    } catch (Exception e)
    {
      LOGGER.info("Failed to update ZulaB lists (" + e.getMessage() + ")");
      LOGGER.debug("Problem occured while updating ZulaB list: " + e.getMessage());

      Message.warning(maingui.getMainshell(), LocalSigner.i18n("networkWarning"));
    }
  }

  protected boolean isUpdateAvailable(ZulabConfiguration remoteConfiguration, String localVersion)
  {
    String remoteVersion = remoteConfiguration.getVersion();
    Calendar remoteVersionDate = DatatypeConverter.parseDateTime(remoteVersion);

    Calendar localVersionDate = null;

    if (ZulabConfiguration.VERSION_NOT_AVAILABLE.equals(localVersion))
    {
      LOGGER.info("Update to " + remoteVersion + " available (installed: " + localVersion + ")");

      // There is only the dummy configuration initialized by the
      // ZulabConfiguration. So we create a new Calendar and
      // set it to the most ancient date. This will cause an update in
      // subsequent steps.
      localVersionDate = Calendar.getInstance();
      localVersionDate.set(Calendar.YEAR, localVersionDate.getMinimum(Calendar.YEAR));
    }
    else
    {
      localVersionDate = DatatypeConverter.parseDateTime(localVersion);
    }

    if (remoteVersionDate != null && localVersionDate != null
        && remoteVersionDate.getTimeInMillis() > localVersionDate.getTimeInMillis())
    {
      LOGGER.info("Update to " + remoteVersion + " available (installed: " + localVersion + ")");
      return true;
    }
    else
    {
      LOGGER.info(
          "No update for zulab lists available (installed: " + localVersion + ", available" + remoteVersion + ")");
      return false;
    }
  }

  protected String humanReadableDateFromVersionDate(String date, Locale locale)
  {
    if (ZulabConfiguration.VERSION_NOT_AVAILABLE.equals(date))
    {
      return date;
    }
    Calendar calendar = DatatypeConverter.parseDateTime(date);

    DateFormat df = null;
    if (locale == null)
    {
      df = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
    }
    else
    {
      df = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale);
    }
    return df.format(calendar.getTime());
  }
}
