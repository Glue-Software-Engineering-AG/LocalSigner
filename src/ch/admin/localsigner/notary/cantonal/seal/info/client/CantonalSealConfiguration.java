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

import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;

/**
 * Aktualisiert ggf. die Konfiguration der Kantonalen Siegel, parsed die
 * XML-Datei und gibt die EndPoint Konfiguration zurück.
 *
 * @author greiler
 */
public class CantonalSealConfiguration
{
  private static final Logger LOGGER = Logger.getLogger(CantonalSealConfiguration.class);

  /**
   * Aktualisiert ggf. die Konfiguration der Kantonalen Siegel, parsed die
   * XML-Datei und gibt die EndPoint Konfiguration zurück.
   * 
   * @param mainGUI
   *          MainGUI für die Applikations-Konfiguration und die SWT-Shell um
   *          ggf. Fehler anzuzeigen.
   * @return Die aktualisierten EndPoints.
   */
  public static EndPoints getCantonalSealConfiguration() throws CantonalSealInfoException
  {
    ApplicationConfiguration appConfig = LocalSigner.appConfig;

    // updaten falls nötig
    CantonalSealInfoUpdater updater = new CantonalSealInfoUpdater(appConfig);
    updater.updateCantonalSealConfig();

    // auslesen und parsen
    try (InputStream is = new FileInputStream(appConfig.getCantonalSealFile()))
    {
      EndPoints epConfig = CantonalSealInfoParser.parseEndpointsXml(is);

      return epConfig;
    } catch (Exception ex)
    {
      if (ex instanceof CantonalSealInfoException)
      {
        throw (CantonalSealInfoException) ex;
      }
      else
      {
        String msg = "Not able to load EndPoint configuration for cantonal seals.";
        LOGGER.error(msg, ex);
        throw new CantonalSealInfoException(
            CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_LOADABLE, msg, ex);
      }
    }
  }
}
