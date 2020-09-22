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
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileOpenException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import java.io.File;

/**
 * This class represents the listener which is called, when the user loads a new
 * pdf as input by clicking on the "choose" button. The user can choose the file
 * with a file chooser dialog box.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ChooseInputListener implements Listener {

  private static final Logger LOGGER = Logger
      .getLogger(ChooseInputListener.class);

  private final MainGUI gui;
  private final Shell shell;

  /**
   * Constructor
   * @param gui
   *          The main GUI
   */
  public ChooseInputListener(final MainGUI gui) {
    this.gui = gui;
    this.shell = gui.getMainshell();
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    // extract current path
    String curPath = gui.getOutputFile();
    LOGGER.debug("current path: " + curPath);
    final int index = curPath.lastIndexOf(File.separator);
    if (index != -1) {
      LOGGER.debug("-> use path from current file");
      curPath = curPath.substring(0, index);
    } else {
      LOGGER.debug("-> use path from configuration");
      curPath = LocalSigner.appConfig.getInputpath(); // default
    }
    LOGGER.debug("file chooser directory: " + curPath);

    // open a dialog for pdf file loading
    final FileDialog dialog = new FileDialog(this.shell, SWT.OPEN);
    dialog.setFilterPath(curPath);
    dialog.setFilterNames(new String[] { "PDF" });
    dialog.setFilterExtensions(new String[] { "*.pdf" });
    dialog.open();

    // if the user did not select a file just return
    if (dialog.getFileName().equals(StringUtils.EMPTY)) {
      return;
    }
    String filename = dialog.getFilterPath() + File.separator + dialog.getFileName();
    gui.closeInputFile();
    try {
      gui.setInputFileAndCheck(filename, true);
    } catch (FileOpenException e) {
      FileExceptionHandler.showAppropriateErrorMessage(e, filename);
    }
  }
}
