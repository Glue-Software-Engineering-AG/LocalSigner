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
package ch.admin.localsigner.notary;

import ch.admin.bj.upreg.fn.client.core.webservice.FNWebserviceWrapper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.Pkcs11Helper;
import ch.admin.localsigner.gui.common.InputDialog;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import java.security.KeyStoreException;
import org.eclipse.swt.widgets.Shell;
import ch.glue.securitytools.keystore.PINInvalidException;
import ch.glue.securitytools.keystore.PINWrongException;
import ch.glue.securitytools.pkcs11.PKCS11Token;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.util.Enumeration;
import org.apache.log4j.Logger;

/**
 * Kapselt das erstellen des WebService-Clients für den Funktionsnachweis. Dieser wird sowohl für die Aktivierung der
 * Funktion, als auch für das Anbringen des Funktionnachweises benötigt.
 *
 * @author Adrian Greiler
 */
public class WebServiceBuilder
{

  private static final Logger LOGGER = Logger.getLogger(WebServiceBuilder.class);

  private static final String TRUST_STORE_FILE_NAME = "httpsTrustStore.jks";

  private static WebServiceBuilder instance = null;

  private MainGUI mainGUI = null;

  private WebServiceBuilder(MainGUI mainGUI)
  {
    this.mainGUI = mainGUI;
  }

  public static WebServiceBuilder instance()
  {
    if (instance == null)
    {
      instance = new WebServiceBuilder(LocalSigner.mainGui);
    }
    return instance;
  }

  /**
   * Initialisiert den FNWebserviceWrapper mit Trust- und Keystores (PKCS#11 und ggf. PKCS#12) und setzt das
   * Debugging-Flag der Bibliothek zulab-client, falls der LocalSigner im Debug-Modus ausgeführt wird.
   */
  public FNWebserviceWrapper initWebservice() throws Pkcs11Helper.UserCanceledException
  {
    KeyStore keystore = getKeystore();

    InputStream trustStore = getClass().getClassLoader().getResourceAsStream(TRUST_STORE_FILE_NAME);

    FNWebserviceWrapper client = new FNWebserviceWrapper(
        keystore, LocalSigner.appConfig.getFunktionsnachweisUrl(), trustStore, "123456".toCharArray());
    client.setDebugging(LocalSigner.isInDebugMode());

    return client;
  }

  /**
   * Lädt den Keystore (PKCS#11 falls vorhanden sonst PKCS#12) und setzt das
   */
  public KeyStore getKeystore() throws Pkcs11Helper.UserCanceledException
  {
    KeyStore keystore = getPkcs11Keystore(mainGUI.getMainshell());
    if (keystore == null)
    {
      keystore = getPkcs12Keystore();
      if (keystore == null)
      {
        throw new IllegalStateException("No cert found");
      }
    }

    return keystore;
  }

  private KeyStore getPkcs11Keystore(Shell mainShell) throws Pkcs11Helper.UserCanceledException
  {
    PKCS11Token token = null;
    try
    {
      token = new Pkcs11Helper(mainShell).findCertPkcs11();
    } catch (Pkcs11Helper.UserCanceledException cancel)
    {
      LOGGER.info("user cancelled reader choice");
      throw cancel;
    } catch (PINWrongException pinEx)
    {
      LOGGER.info("Attention: PIN was wrong");
      Message.error(mainShell, LocalSigner.i18n("wrongPin"));
      throw new Pkcs11Helper.UserCanceledException();
    } catch (PINInvalidException e)
    {
      LOGGER.debug("user pressed cancel in PKCS11 PIN dialog");
      throw new Pkcs11Helper.UserCanceledException();
    } catch (KeyStoreException e)
    {
      // this happens if no device is plugged in, not accessible or the token is locked or the like
      LOGGER.debug("exception on accessing certificate", e);

      Message.error(mainShell,
          String.format(LocalSigner.i18n("errorKeyStoreNotLoaded"), Pkcs11Helper.getPkcs11Exception(e)));

      throw new Pkcs11Helper.UserCanceledException();
    }
    if (token == null)
    {
      return null;
    } else
    {
      return token.getKeystore();
    }
  }

  private KeyStore getPkcs12Keystore()
  {
    String pkcs12File = LocalSigner.appConfig.getPkcs12File();
    if (pkcs12File == null)
    {
      LOGGER.info("no PKCS12 Keystore configured");
      return null;
    }

    File file = new File(pkcs12File);
    if (!file.exists())
    {
      LOGGER.info("configured PKCS12 Keystore does not exist");
      return null;
    }

    LOGGER.info("loading software certificate from " + file.getAbsolutePath());

    String pkcs12Password = null;

    // try load password from settings
    pkcs12Password = LocalSigner.appConfig.getPkcs12Password();
    if (pkcs12Password == null)
    {
      // ask user with dialog
      InputDialog pwDialog = new InputDialog(mainGUI, file.getName(), LocalSigner.i18n("pkcs12Password") + ":", true);
      pkcs12Password = pwDialog.getInput();
    }

    try (InputStream pkcs12IS = Files.newInputStream(file.toPath()))
    {
      KeyStore ks = java.security.KeyStore.getInstance("PKCS12", "BC");
      ks.load(pkcs12IS, pkcs12Password.toCharArray());

      LOGGER.debug("PKCS12 keystore size: " + ks.size());

      LOGGER.info("PKCS12 Keystore contains keys with following aliases:");
      for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();)
      {
        String alias = e.nextElement();
        LOGGER.info("    " + alias);
      }
      return ks;
    } catch (Exception e)
    {
      LOGGER.error("Cannot load PKCS12 certificate " + file.getName() + " (" + e.getMessage() + ")");
      return null;
    }
  }
}
