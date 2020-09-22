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
package ch.admin.localsigner.gui;

import ch.admin.localsigner.config.Config;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.gui.common.ButtonsDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.securitytools.keystore.PINInvalidException;
import ch.glue.securitytools.keystore.PINWrongException;
import ch.glue.securitytools.pkcs11.PKCS11Token;
import ch.glue.securitytools.pkcs11.PKCS11TokenSearch;
import ch.glue.securitytools.pkcs11.PKCS11TokenSearch.Token;
import ch.glue.securitytools.pkcs11.SunPKCS11Factory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import sun.security.pkcs11.wrapper.PKCS11Exception;

/**
 * Helps to find certificates using PKCS11.
 *
 * @author unknown
 * @author $Author$
 * @version $Revision$
 */
public class Pkcs11Helper
{

  private static final Logger LOGGER = Logger.getLogger(Pkcs11Helper.class);

  private final Shell parent;

  public Pkcs11Helper(Shell parent)
  {
    this.parent = parent;
  }

  public PKCS11Token findCertPkcs11() throws KeyStoreException, PINWrongException,
    PINInvalidException, UserCanceledException
  {
    // check if multiple PKCS11 tokens can be used
    List<String> libs = new ArrayList<String>();
    libs.add(LocalSigner.appConfig.getPkcs11Lib());

    // TODO: add other available libs
    Map<String, String> detectedPkcs11Libs = LocalSigner.appConfig.detectPkcs11Lib();
    for (String lib : detectedPkcs11Libs.keySet())
    {
      if (libs.contains(lib))
      {
        continue;
      }
      libs.add(lib);
    }

    LOGGER.debug("Searching PKCS11 libs: " + libs);
    List<Token> tokens = PKCS11TokenSearch.listActiveTokens(libs);
    LOGGER.debug("Tokens found: " + tokens);

    if (tokens.isEmpty())
    {
      return null;
    }

    if (tokens.size() == 2)
    {
      Token t1 = tokens.get(0);
      Token t2 = tokens.get(1);
      // Button 1: Certificate on slot 1 (same lib)
      // Button 2: Certificate on default slot (same lib)
      // -> remove certificate 2 because it is the same as the first!

      if (t1.getLibrary().equals(t2.getLibrary()) && t2.getSlot() == SunPKCS11Factory.DEFAULT_SLOT)
      {
        LOGGER.debug("Removing default slot from token list");
        tokens.remove(t2);
      }
    }

    // remove empty slots and filter unwanted
    String readerMatchPattern = LocalSigner.appConfig.getReaderMatchRegexp();
    LOGGER.debug("Reader name must match "+
      (StringUtils.isEmpty(readerMatchPattern)?"no special pattern":"exactly '"+readerMatchPattern+"'"));

    List<Token> activeTokens = new LinkedList<Token>();
    for (Token t : tokens)
    {
      if (t.getSlot() != SunPKCS11Factory.DEFAULT_SLOT)
      {
        if (StringUtils.isNotEmpty(readerMatchPattern))
        { // there is a pattern, so it needs to match
          if (t.getReaderName().matches(readerMatchPattern))
          {
            LOGGER.debug("'"+t.getReaderName()+"' matches regular expression '"+readerMatchPattern+"'");
            activeTokens.add(t);
          } else
          {
            LOGGER.debug("'"+t.getReaderName()+"' doesn't match regular expression '"+readerMatchPattern+"'");
          }
        } else
        { // no pattern, add always
          activeTokens.add(t);
        }
      }
    }

    HashMap<String, Token> uniqueTokens = new HashMap<String, Token>();
    for (Token t : activeTokens)
    {
      if (!uniqueTokens.containsKey(getTokenUniqueName(t)))
      { // we want the first one to stay in the map
        uniqueTokens.put(getTokenUniqueName(t), t);
      }
    }

    // the order of a set is not guaranteed
    List<String> uniqueTokenKeys = new LinkedList<String>(uniqueTokens.keySet());

    int selection = 0;
    if (uniqueTokens.size() > 1)
    {
      List<String> names = new ArrayList<String>();
      for (String key : uniqueTokenKeys)
      {
        names.add(getTokenDisplayName(uniqueTokens.get(key)));
      }
      // more than 1; ask user which one to use
      ButtonsDialog dialog = new ButtonsDialog(parent, LocalSigner.i18n("pkcs11TokenTitle"), names,
        LocalSigner.i18n("pkcs11TokenText"));

      selection = dialog.getUserDecision();
      if (selection < 0)
      {
        // cancel button; abort
        throw new UserCanceledException();
      }
    }

    if (uniqueTokens.values().isEmpty())
    {
      return null;
    }

    Token tk = uniqueTokens.get(uniqueTokenKeys.get(selection));
    if (tk.getSlot() == -1)
    {
      LOGGER.debug("Invalid slot number");
      return null;
    }

    String name = "";
    if (StringUtils.isNotBlank(tk.getName()))
    {
      name += tk.getName();
    }
    if (StringUtils.isNotBlank(name))
    {
      name += ", ";
    }
    if (StringUtils.isNotBlank(tk.getReaderName()))
    {
      name += tk.getReaderName();
    }

    writePkcs11Lib(tk.getLibrary());

    LOGGER.debug("Loading PKCS11 token for library: " + tk.getLibrary() +
        " in slot: " + tk.getSlot() + " with for name: " + name);

    PKCS11Token token = new PKCS11Token(tk.getLibrary(), tk.getSlot(),
      PKCS11Token.getSwtPinCallback(LocalSigner.getLocale(), name, parent));

    return token;
  }

  private void writePkcs11Lib(String lib)
  {
    try
    {
      LocalSigner.appConfig.setValue(Config.PKCS11_LIB, lib);
    } catch (ConfigurationException e)
    {
      LOGGER.error("Cannot write config", e);
    }
  }

  private String getTokenUniqueName(Token t)
  {
    return t.getName() + " - " + t.getReaderName() + " - " + t.getSlot();
  }

  private String getTokenDisplayName(Token t)
  {
    return t.getName() + " - " + t.getReaderName();
  }


  /**
   * Versucht die zugrundeliegende Exceptions des Kartentreibers zu finden. In der PKCS11Exception ist dann eine etwas
   * aussagekräftigere Message zu finden. z.B.: CKR_PIN_LOCKED. Falls dieser KeyStoreException keine PKCS11Exception
   * zugrunde liegt, wird die Nachricht (getMessage()) der mitgegebenen KeyStoreException zurückgeliefert.
   *
   * @param kse Die Keystore Exception, welche beim Öffnen/Laden des KeyStores auf dem Token geworfen wurde.
   * @return Die Message der zugrundeliegenden PKCS11Exception oder die Message der mitgegebenen KeyStoreException,
   * falls dieser keine PKCS11Exception zugrunde liegt.
   */
  public static String getPkcs11Exception(KeyStoreException kse)
  {
    Throwable currEx = kse;

    while (currEx != null) {
      if (currEx instanceof PKCS11Exception)
      {
        return ((PKCS11Exception) currEx).getMessage();
      } else
      {
        currEx = currEx.getCause();
      }
    };

    // Keine PKCS11Exception enthalten, Message der originalen Exception zurückliefern.
    return kse.getMessage();
  }

  @SuppressWarnings("serial")
  public static class UserCanceledException extends Exception
  {
    // just a marker class
  }

}
