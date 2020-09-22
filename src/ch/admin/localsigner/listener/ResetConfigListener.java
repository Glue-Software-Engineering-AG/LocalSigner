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

import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.Helper;
import java.io.File;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * This listener resets the application config to default values.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class ResetConfigListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(
          ResetConfigListener.class);

  private final Shell shell;

  /**
   * Constructor
   * 
   * @param shell
   *          Parent shell
   */
  public ResetConfigListener(final Shell shell)
  {
    this.shell = shell;
  }

  /**
   * Handle listener event.
   * 
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {
    MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.YES
            | SWT.NO);

    messageBox.setText(LocalSigner.i18n("configReset"));
    messageBox.setMessage(LocalSigner.i18n("configResetText"));
    int buttonID = messageBox.open();

    if (buttonID == SWT.YES)
    {
      Helper.backup(new File(LocalSigner.appConfig.getUserConfig()));
      Helper.backup(new File(LocalSigner.appConfig.getUserConfigCacheFolder()));
      Helper.backup(new File(LocalSigner.appConfig.getUserProfileCacheFolder()));

      try
      {
        LocalSigner.appConfig = new ApplicationConfiguration();
      } catch (Exception e)
      {
        LOGGER.error("Cannot init user configuration", e);
      }
    }
  }

}
