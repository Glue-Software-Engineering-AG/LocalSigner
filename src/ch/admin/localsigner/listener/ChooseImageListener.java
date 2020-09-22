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

import java.io.File;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.gui.profile.PropertiesGUI;

/**
 * This listener is called when the user clicks the 'choose' button to find the
 * background image for the signature.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ChooseImageListener implements Listener {

  private static final Logger LOGGER = Logger.getLogger(ChooseImageListener.class);

  private final PropertiesGUI propertiesGui;
  private final Shell shell;

  /**
   * Constructor
   * @param propertiesGui
   *          properties GUI
   * @param shell
   *          Parent shell
   */
  public ChooseImageListener(final PropertiesGUI propertiesGui,
      final Shell shell) {
    this.propertiesGui = propertiesGui;
    this.shell = shell;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    LOGGER.debug("choose image listener");

    // open a dialog for pdf file loading
    final FileDialog dialog = new FileDialog(this.shell, SWT.OPEN);
    dialog.setFilterNames(new String[] { "JPEG" });
    dialog.setFilterExtensions(new String[] { "*.jpg" });
    dialog.open();

    // if the user did not select a file just return
    if (dialog.getFileName().equals("")) {
      return;
    }

    final String path = dialog.getFilterPath() + File.separator
        + dialog.getFileName();

    // store into gui
    propertiesGui.setBackgroundImage(path);
  }

}
