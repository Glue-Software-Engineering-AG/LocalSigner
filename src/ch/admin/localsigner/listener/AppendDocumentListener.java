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
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.securitytools.pdf.PdfAttacher;
import com.lowagie.text.pdf.PdfReader;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;

import java.io.IOException;

/**
 * This class adds another PDF document at the end of a PDF document.
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class AppendDocumentListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(
          AppendDocumentListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   * @param maingui
   *          Parent shell
   */
  public AppendDocumentListener(final MainGUI maingui)
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

    maingui.switchMode(GuiMode.appendDocument);
    try
    {
      // read file
      PdfReader attachmentReader = this.loadFile();
      if (attachmentReader == null)
      {
        LOGGER.debug("no document selected");
        return;
      }

      PdfAttacher attacher = new PdfAttacher();
      byte[] output = attacher.attachDocument(new PdfReader(maingui.getInputFile()),
          attachmentReader, 0);

      maingui.getDocument().getInputFile().setTemporaryFile(output);
      maingui.reloadInputFile(true);
    } catch (IOException e)
    {
      LOGGER.error("Cannot attach document", e);
    }
  }

  private PdfReader loadFile()
  {
    FileDialog dialog = new FileDialog(maingui.getMainshell(), SWT.OPEN);
    String path = dialog.open();

    if (path == null)
    {
      // cancel button
      return null;
    }

    try
    {
      return new PdfReader(path);
    } catch (Exception e)
    {
      LOGGER.debug("Not a PDF document");
      Message.warning(maingui.getMainshell(), LocalSigner.i18n("errorNotPdf"));
      return null;
    }
  }

}
