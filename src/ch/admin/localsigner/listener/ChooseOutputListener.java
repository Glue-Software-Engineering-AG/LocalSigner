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

import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Choose output file action.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ChooseOutputListener implements Listener {

  private static final Logger LOGGER = Logger
      .getLogger(ChooseOutputListener.class);

  private final Shell shell;
  private final MainGUI maingui;
  private final PropertiesGUI propertiesGui;

  /**
   * Constructor
   * @param gui
   *          The main GUI
   */
  public ChooseOutputListener(final MainGUI gui) {
    this.shell = gui.getMainshell();
    this.maingui = gui;
    this.propertiesGui = gui.getPropertiesGui();
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    final FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
    dialog.setFilterNames(new String[] { "PDF" });
    dialog.setFilterExtensions(new String[] { "*.pdf" });
    dialog.open();

    if (StringUtils.isEmpty(dialog.getFileName())) {
      // nothing selected
      return;
    }

    // get filename (path) chosen by the user
    String chosenFile = dialog.getFileName();
    // add ".pdf" to the file name if it does not end with ".pdf"
    if (!chosenFile.endsWith(".pdf")) {
      chosenFile = chosenFile + ".pdf";
    }

    LOGGER.debug("Chosen file: " + chosenFile);

    // if there is no output path specified, the input folder is the output
    // folder.
    // otherwise the output path is the one specified by the user, the filename
    // is
    // the one specified in the above dialog.
    if (StringUtils.isEmpty(propertiesGui.getOutputDir())) {
      maingui.setOutputFile(dialog.getFilterPath() + File.separator
          + chosenFile, false);
    } else {
      String outputdir = propertiesGui.getOutputDir();
      // append file separator if necessary
      if (!outputdir.endsWith(File.separator)) {
        outputdir = outputdir + File.separator;
      }
      maingui.setOutputFile(outputdir + chosenFile, false);
    }
  }
}
