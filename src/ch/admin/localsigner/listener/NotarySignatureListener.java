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
package ch.admin.localsigner.listener;

import ch.admin.bj.upreg.fn.client.core.exception.FNClientException;
import ch.admin.bj.upreg.fn.client.core.exception.FNWebserviceException;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.Pkcs11Helper;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.notary.ExceptionMapper;
import ch.admin.localsigner.notary.Funktionsnachweis;
import ch.glue.localsigner.cantonal.seal.exception.CantonalSealException;
import com.sun.jersey.api.client.ClientHandlerException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Listener that adds the Zulassungsbestaetigung to the signed PDF
 *
 * @author Weisskopf
 * @author Blaser
 * @author Greiler
 * @author $Author$
 * @version $Revision$
 */
public class NotarySignatureListener implements Listener
{
  private final static Logger LOGGER = Logger.getLogger(NotarySignatureListener.class);

  private final MainGUI gui;

  public NotarySignatureListener(MainGUI gui)
  {
    this.gui = gui;
  }

  @Override
  public void handleEvent(final Event event)
  {
    gui.getMainshell().getDisplay().asyncExec(new Runnable()
    {

      @Override
      public void run()
      {
        LOGGER.info("Notary signature in progress");

        String fileName = null;
        try
        {
          if (gui.canDraw())
          { // opened the file right now
            fileName = new Funktionsnachweis(gui)
                .signNotary(gui.getInputFileName(), gui.getInputFile());
          } else
          { // apply Zulassunbsgestätigung right after signing
            // Initialize lock indirectly
            gui.setInputFileAndCheck(gui.getOutputFile(), false);
            gui.setOutputFile(gui.getOutputFile(), false);
              fileName = new Funktionsnachweis(gui)
                  .signNotary(gui.getOutputFile(), gui.getInputFile());
          }
        } catch (Pkcs11Helper.UserCanceledException cancel)
        {
          // user cancelled, the token is locked, or no certificates could be found
        } catch (FNClientException ex)
        {
          LOGGER.warn("FN Client-side error code: " + ex.getErrorCode(), ex);
          Message.error(gui.getMainshell(),
              LocalSigner.i18n("fn.ws.error.intro")
                  + ExceptionMapper.convertCodeToMsg(ex.getErrorCode()) + "\n\nCode:\n"
                  + ex.getErrorCode());
        } catch (FNWebserviceException ex)
        {
          LOGGER.warn("FN Webservice returned an error code: " + ex.getErrorCode(), ex);
          Message.error(gui.getMainshell(),
              LocalSigner.i18n("fn.ws.error.intro")
                  + ExceptionMapper.convertCodeToMsg(ex.getErrorCode()) + "\n\nCode:\n"
                  + ex.getErrorCode());
        } catch (ClientHandlerException networkEx)
        {
          LOGGER.warn("Https network connection could not be established", networkEx);
          Message.error(gui.getMainshell(), LocalSigner.i18n("fn.ws.error.https_connection"));
        } catch (IllegalArgumentException iex)
        {
          new Message(gui.getMainshell(), SWT.ICON_ERROR | SWT.ON_TOP,
              LocalSigner.i18n("notarySign.preConditionsNotMet"), iex.getMessage(), null);
        } catch (CantonalSealException csex)
        {
          String localizedMessage = LocalSigner.i18n(csex.getI18nKey());
          LOGGER.warn("An error occured while adding the cantonal seal: "+localizedMessage, csex);

          new Message(gui.getMainshell(), SWT.ICON_ERROR | SWT.ON_TOP,
              LocalSigner.i18n("error.CantonalSealException.title"), localizedMessage, null);
        } catch (IllegalStateException ise)
        {
          LOGGER.error("No certificate found", ise);
          Message.error(gui.getMainshell(), LocalSigner.i18n("noCertificatesFound"));
        } catch (Exception ex)
        {
          LOGGER.error("Another exception happend while trying to add the notary signature: "+ex.getMessage(), ex);
          Message.error(gui.getMainshell(), LocalSigner.i18n("fn.error.unexpected") + ex.toString());
        }

        if (fileName != null)
        {
          gui.showSignedFile(fileName);
        }
      }
    });
  }
}
