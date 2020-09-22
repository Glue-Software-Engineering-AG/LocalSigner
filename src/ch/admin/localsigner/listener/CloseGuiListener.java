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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.viewer.ViewerPanel;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Listener to close the main SWT window.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class CloseGuiListener implements DisposeListener
{
  private static final Logger LOGGER = Logger.getLogger(CloseGuiListener.class);

  private final MainGUI maingui;

  public CloseGuiListener(MainGUI maingui)
  {
    this.maingui = maingui;
  }

  @Override
  public void widgetDisposed(final DisposeEvent e)
  {
    if (maingui.getDocument()!=null && maingui.getDocument().getInputFile()!=null) {
      maingui.getDocument().getInputFile().unlockFile();
    }
    // save current window position
    org.eclipse.swt.graphics.Rectangle bounds = maingui.getMainshell().getBounds();
    String pos = bounds.x + "/" + bounds.y + "/" + bounds.width + "/" + bounds.height;
    if (maingui.getMainshell().getMaximized())
    {
      pos = "max";
    }
    try
    {
      LocalSigner.appConfig.setValue(Config.WINDOW_POSITION, pos);
    } catch (ConfigurationException ex)
    {
      LOGGER.error("Cannot save window position", ex);
    }

    LOGGER.info("exit LocalSigner");

    Browser b = maingui.getBrowser();
    if (b != null)
    {
      b.stop();
      b.close();
      b.dispose();
      LOGGER.debug("browser closed");
    }

    // close the current pdf file
    final ViewerPanel v = maingui.getPdfViewerPane();
    if (v != null)
    {
      v.closePdf();
    }
  }

}
