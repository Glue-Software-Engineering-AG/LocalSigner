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
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.glue.securitytools.pdf.PdfAttacher;
import com.lowagie.text.pdf.PdfReader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

import java.io.File;
import java.io.IOException;

/**
 * This class inserts a document into the PDF as attachment.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class InsertAttachmentListener implements Listener
{

  private static final Logger LOGGER = Logger.getLogger(InsertAttachmentListener.class);

  private final MainGUI maingui;

  private String filename;

  /**
   * Constructor
   * 
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   * @param maingui
   *          Parent shell
   */
  public InsertAttachmentListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  @Override
  public void handleEvent(final Event event)
  {
    MenuItem item = (MenuItem) event.widget;
    if (!item.getSelection())
    {
      // deselect menu item, don't do it again!
      return;
    }

    maingui.switchMode(GuiMode.insertAttachment);
    try
    {
      // load file to attach
      byte[] data = this.loadFile();
      if (data == null)
      {
        LOGGER.debug("no document selected");
        return;
      }

      PdfAttacher attacher = new PdfAttacher();
      byte[] output = attacher.insertAttachment(new PdfReader(maingui.getInputFile()),
          data, filename, "");

      maingui.getDocument().getInputFile().setTemporaryFile(output);
      maingui.reloadInputFile(true);
    } catch (IOException e)
    {
      LOGGER.error("Cannot insert attachment", e);
    }
  }

  private byte[] loadFile()
  {
    try
    {
      FileDialog dialog = new FileDialog(maingui.getMainshell(), SWT.OPEN);
      String attachment = dialog.open();

      if (attachment == null)
      {
        // cancel button
        return null;
      }

      File file = new File(attachment);
      filename = file.getName();
      return FileUtils.readFileToByteArray(file);
    } catch (IOException e)
    {
      LOGGER.error("Cannot read file", e);
    }
    return null;
  }

}
