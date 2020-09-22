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
import ch.admin.localsigner.main.LocalSigner;
import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Class implementing the listener which is called when the user clicks on
 * "choose output directory".
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ChooseOutputDirListener implements Listener {

  private static final Logger LOGGER = Logger
      .getLogger(ChooseOutputDirListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   * @param maingui
   *          The main GUI
   */
  public ChooseOutputDirListener(final MainGUI maingui) {
    this.maingui = maingui;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    final DirectoryDialog dialog = new DirectoryDialog(maingui.getMainshell());
    dialog.setText(LocalSigner.i18n("chooseDir"));
    final String dir = dialog.open();

    if (StringUtils.isEmpty(dir)) {
      // nothing selected
      return;
    }

    // set filename into GUI
    LOGGER.debug("chosen output dir: " + dir);
    maingui.getPropertiesGui().setOutputDir(dir);

    // if an output file is already set replace its output directory
    String outputFile = maingui.getOutputFile();
    if (StringUtils.isNotEmpty(outputFile)) {
      final int index = outputFile.lastIndexOf(File.separator);
      if (index > 0) {
        outputFile = outputFile.substring(index); // with "/"
        maingui.setOutputFile(dir + outputFile, false);
      }
    }
  }
}
