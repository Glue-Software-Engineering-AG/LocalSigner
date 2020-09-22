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
package ch.admin.localsigner.update;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.listener.LinkListener;
import ch.admin.localsigner.main.LocalSigner;

/**
 * This class checks online for updates of LocalSigner.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class UpdateQuery implements Runnable
{
  private static final Logger LOGGER = Logger.getLogger(UpdateQuery.class);

  private final MainGUI maingui;

  private final String url;

  private static ConnectionStatus connStatus = ConnectionStatus.UNKNOWN;

  public UpdateQuery(MainGUI maingui, String url)
  {
    this.maingui = maingui;
    this.url = url;
  }

  @Override
  public void run()
  {
    LOGGER.debug("Checking Internet connection and update");
    LOGGER.debug("Using url: " + url);
    connStatus = ConnectionStatus.UNKNOWN;

    try
    {
      HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setReadTimeout(10000);
      conn.setConnectTimeout(10000);

      int statusCode = conn.getResponseCode();
      LOGGER.debug("Status code: " + statusCode);
      InputStream updateXml = conn.getInputStream();
      byte[] xmlData = IOUtils.toByteArray(updateXml);
      LOGGER.info("Working Internet connection");
      connStatus = ConnectionStatus.AVAILABLE;

      // check if XML response is not empty
      if (!new String(xmlData).contains("<release>"))
      {
        LOGGER.debug("no XML response");
        return;
      }

      // check if an update is available
      UpdateChecker checker = new UpdateChecker();
      checker.parseXml(new ByteArrayInputStream(xmlData), LocalSigner.getLocale(),
          System.getProperty("os.name"));

      String currentVersion = GuiHelper.getVersion();
      if (checker.hasUpdate(currentVersion))
      {
        final String newVersion = checker.getVersion();
        final String date = checker.getDate();
        final String descString = checker.getDescription();
        final String dlPage = checker.getDownloadPage();
        LOGGER.info("Update to " + newVersion + " available");

        StringBuilder builder = new StringBuilder();
        builder.append(LocalSigner.i18n("updateFound")).append("\n\n");
        builder.append(LocalSigner.i18n("currentVersion")).append(": ")
            .append(currentVersion).append("\n");
        builder.append(LocalSigner.i18n("newVersion")).append(": ").append(newVersion)
            .append(" (").append(date).append(")\n\n");
        builder.append(descString);
        final String msg = builder.toString();

        LOGGER.debug("show update dialog");

        new Message(maingui.getMainshell(), SWT.ICON_INFORMATION | SWT.ON_TOP | SWT.OK
            | SWT.CANCEL, LocalSigner.i18n("warning"), msg, new LinkListener(
            maingui.getMainshell(), dlPage));
      }
    } catch (Exception e)
    {
      LOGGER.info("No Internet connection (" + e.getMessage() + ")");
      connStatus = ConnectionStatus.UNAVAILABLE;

      Message.warning(maingui.getMainshell(), LocalSigner.i18n("networkWarning"));
    }
  }

  public static ConnectionStatus getConnectionStatus()
  {
    return connStatus;
  }

  public enum ConnectionStatus
  {
    UNKNOWN, AVAILABLE, UNAVAILABLE

  }
}
