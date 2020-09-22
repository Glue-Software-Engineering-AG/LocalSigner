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
package ch.admin.localsigner.notary.cantonal.seal.info.client;

import java.io.FileOutputStream;
import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.config.ZulabConfiguration;
import ch.admin.localsigner.main.LocalSigner;

/**
 * This class checks online for updates of canton and domain list for Zulassungsbestätigung of UPReg/ZulaB.
 * If there is an update available, the user gets informed and the file updated if desired.
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public class CantonalSealInfoUpdater
{

  private static final Logger LOGGER = Logger.getLogger(CantonalSealInfoUpdater.class);

  private final ApplicationConfiguration config;

  public CantonalSealInfoUpdater(ApplicationConfiguration config)
  {
    this.config = config;
  }

  /**
   * Updates the XML with the cantonal seal endpoints information cached locally.
   */
  public void updateCantonalSealConfig() throws CantonalSealInfoException
  {
    try
    {
      CantonalSealInfoWsClient wsClient = new CantonalSealInfoWsClient(config);

      if (wsClient.isUpdateRequired() || !isXmlFileCached())
      {
        reloadAndCacheXml(wsClient);
      }
    } catch (CantonalSealInfoException ex)
    {
      throw ex;
    } catch (Exception e)
    {
      String message = "A problem occured while updating the cantonal seal configuration";

      LOGGER.error(message, e);

      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_WRITABLE, message, e);
    }
  }

  private boolean isXmlFileCached()
  {
    boolean fileExists = LocalSigner.appConfig.getCantonalSealFile().exists();

    LOGGER.debug("Cantonal seal config file (" + LocalSigner.appConfig.getCantonalSealFile() + ") is "
        + (fileExists ?  "" : "not ") + "cached");

    return fileExists;
  }

  private void reloadAndCacheXml(CantonalSealInfoWsClient wsClient) throws CantonalSealInfoException
  {
    try
    {
      byte[] xml = wsClient.loadEndpoints();

      FileOutputStream fos = new FileOutputStream(config.getCantonalSealFile());
      fos.write(xml);
      fos.close();

      LOGGER.debug("Updated cantonal seal configuration to "+config.getCantonalSealFile());

      config.setCantonalSealLastUpdateTimestamp(wsClient.getHttpLastModifiedHeader());
    } catch (Exception e)
    {
      String message = "The cantonal seal configuration could not be written to local file: "
          + config.getCantonalSealFile();
      LOGGER.error(message, e);

      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_WRITABLE, message, e);
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

      // There is only the dummy configuration initialized by the ZulabConfiguration. So we create a new Calendar and
      // set it to the most ancient date. This will cause an update in subsequent steps.
      localVersionDate = Calendar.getInstance();
      localVersionDate.set(Calendar.YEAR, localVersionDate.getMinimum(Calendar.YEAR));
    } else
    {
      localVersionDate = DatatypeConverter.parseDateTime(localVersion);
    }

    if (remoteVersionDate != null && localVersionDate != null && remoteVersionDate.getTimeInMillis() > localVersionDate.
        getTimeInMillis())
    {
      LOGGER.info("Update to " + remoteVersion + " available (installed: " + localVersion + ")");
      return true;
    } else
    {
      LOGGER.info("No update for zulab lists available (installed: " + localVersion + ", available" + remoteVersion
          + ")");
      return false;
    }
  }
}
