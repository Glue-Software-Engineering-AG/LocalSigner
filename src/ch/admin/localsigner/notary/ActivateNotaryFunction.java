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
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.gui.Pkcs11Helper;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;


public class ActivateNotaryFunction implements Listener
{
  private final static Logger LOGGER = Logger.getLogger(ActivateNotaryFunction.class);

  private final Shell mainShell;

  public ActivateNotaryFunction(Shell mainShell)
  {
    this.mainShell = mainShell;
  }

  @Override
  public void handleEvent(Event event)
  {
    boolean canLogin;
    try
    {
      FNWebserviceWrapper client = WebServiceBuilder.instance().initWebservice();

      canLogin = client.login();
    } catch (Pkcs11Helper.UserCanceledException cancel)
    {
      // the user cancelled, the token is locked or no certificates could be found
      return;
    } catch (ClientHandlerException ex)
    {
      Message.error(mainShell, LocalSigner.i18n("fn.ws.error.https_connection"));
      LOGGER.debug("Cannot login to notary webservice", ex);
      return;
    }

    if (canLogin)
    {
      // activate
      try
      {
        LocalSigner.appConfig.setValue(Config.FUNKTIONSNACHWEIS_AKTIV, "true");

        new Message(mainShell, SWT.ICON_INFORMATION,
            LocalSigner.i18n("notarySign.activatedTitle"),
            LocalSigner.i18n("notarySign.activated"), null);
      } catch (ConfigurationException e)
      {
        throw new UnhandledException(e);
      }
    }
    else
    {
      Message.error(mainShell, LocalSigner.i18n("notarySign.notRegistered"));
    }
  }
}
