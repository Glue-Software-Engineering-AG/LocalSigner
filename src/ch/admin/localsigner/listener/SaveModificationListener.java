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
import ch.admin.localsigner.gui.common.YesNoDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.admin.localsigner.utils.BoxPosition;
import ch.glue.securitytools.pdf.FieldCreator;
import ch.glue.securitytools.pdf.PdfSignerException;
import com.lowagie.text.DocumentException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class is instantiated by pressing the 'create' button in the main view.
 * It tests the given input data and initiates the actual creating process.
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class SaveModificationListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(SaveModificationListener.class);

  private final MainGUI maingui;

  private byte[] input;

  private boolean promptName;

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   */
  public SaveModificationListener(final MainGUI maingui, boolean promptName)
  {
    this.maingui = maingui;
    this.promptName = promptName;
  }

  @Override
  public void handleEvent(final Event event)
  {

    try
    {
      input = maingui.getInputFile();
      byte[] output = input;
      LOGGER.debug("saving document ");


      // add pending signature fields
      for (BoxPosition box : maingui.getDocument().getSigFields())
      {
        output = this.addSigField(box);
        // write output to input for next signature field
        input = output;
      }

      final String filepath;
      try {
        filepath = this.writeFile(output);
      } catch (FileWriteException e){
        LOGGER.error("Error save Modification", e);
        FileExceptionHandler.showAppropriateErrorMessage(e);
        return;
      }
      if (filepath == null)
      {
        LOGGER.debug("User cancelled save");
        return;
      }

      // delete sig fields
      maingui.getDocument().setSigFields(null);

      // show created file

      maingui.setInputFileAndCheck(filepath, true);
      maingui.setOutputFile(maingui.getDocument().proposeOutputNameFinal(), false);
      // switch to sign mode
      maingui.switchMode(GuiMode.sign);
    } catch (Exception e)
    {
      LOGGER.error("cannot add signature field", e);
    }
  }

  private String writeFile(byte[] output) throws FileWriteException
  {
    Path outfile = Paths.get(maingui.getOutputFile());
    if (promptName)
    {
      // ask user for a name (save as...)
      FileDialog dialog = new FileDialog(maingui.getMainshell(), SWT.SAVE);
      String path = dialog.open();
      if (path == null)
      {
        return null;
      }
      outfile = Paths.get(path);
    }

    if (outfile.toFile().exists())
    {
      final YesNoDialog dialog = new YesNoDialog(maingui.getMainshell(),
              LocalSigner.i18n("warning"), LocalSigner.i18n("fileExists"));

      LOGGER.debug("user decision: " + dialog.isUserDecision());
      if (dialog.isUserDecision())
      {
        // ok, overwrite file
        LOGGER.debug("user allowed overwriting file");
      }
      else
      {
        // don't continue
        return null;
      }
    }

    maingui.getDocument().getInputFile().write(outfile,output);
    LOGGER.info("written file to " + outfile.toAbsolutePath());
    return outfile.toAbsolutePath().toString();
  }

  private byte[] addSigField(final BoxPosition position)
          throws PdfSignerException, IOException, DocumentException
  {
    LOGGER.info("create signature field " + position.getName() + " on page "
            + position.getPage());
    byte[] out = new FieldCreator().createSignatureField(new ByteArrayInputStream(input),
            position.getName(), position.getPage(), position.getX(), position.getY(),
            position.getWidth(), position.getHeight());
    return out;
  }

}
