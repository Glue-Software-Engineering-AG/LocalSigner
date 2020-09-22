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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.TextDialog;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.BoxPosition;
import ch.glue.securitytools.pdf.FieldCreator;
import ch.glue.securitytools.pdf.PdfSignerException;

/**
 * This class creates a text field used to add a verbal to a PDF document.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class AddTextFieldListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(
          AddTextFieldListener.class);

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
  public AddTextFieldListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void handleEvent(final Event event)
  {
    final PropertiesGUI propertiesGui = maingui.getPropertiesGui();

    try (PdfReader reader = new PdfReader(maingui.getInputFile()))
    {

      // check input file for certification level, if the file is certified
      // and changes are not allowed we inform the user and return
      if (reader.getCertificationLevel()
              == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED)
      {
        Message.warning(maingui.getMainshell(), LocalSigner.i18n(
                "documentCertified"));
        LOGGER.debug("document is certified");
        return;
      }

      final BoxPosition position = new BoxPosition(propertiesGui.
              getSignaturePageSignature(reader.getNumberOfPages()),
          propertiesGui.getLeftPosInPdfUnits(), propertiesGui.getTopPosPdfUnit(),
          propertiesGui.getBoxWidthInPdfUnits(), propertiesGui.getBoxHeightInPdfUnits());

      // create text field
      Set<String> names = reader.getAcroFields().getFields().keySet();
      int num = 1;
      String base = "LSTextfield";
      String name = base + num;
      while (names.contains(name))
      {
        num++;
        name = base + num;
      }

      TextDialog textDialog = new TextDialog(maingui,
              LocalSigner.i18n("textField"), LocalSigner.i18n("textFieldPrompt"),
              propertiesGui.getBoxWidth(), propertiesGui.getBoxHeight());
      String text = textDialog.getInput();
      if (text == null)
      {
        LOGGER.debug("Cancel text field content");
        return;
      }

      LOGGER.debug("add text field " + name + " to " + maingui.getInputFileName());

      byte[] output = this.addTextField(name, position, text);

      maingui.getDocument().getInputFile().setTemporaryFile(output);

      // set box position for next box below current box
      maingui.getPropertiesGui().setTopPos(position.getY()
              + position.getHeight() + 5);

      maingui.reloadInputFile(true);
    } catch (IOException e)
    {
      LOGGER.error("cannot open file", e);
    } catch (PdfSignerException e)
    {
      LOGGER.error("cannot add text field", e);
    } catch (DocumentException e)
    {
      LOGGER.error("cannot fill text field", e);
    }
  }

  private byte[] addTextField(final String name, final BoxPosition position,
          String text)
          throws PdfSignerException, IOException, DocumentException
  {
    int page = position.getPage();
    LOGGER.info("create text field " + name + " on page " + page);
    byte[] out = new FieldCreator().fillTextField(
        new ByteArrayInputStream(maingui.getInputFile()), name, page, position.getX(),
        position.getY(), position.getWidth(), position.getHeight(), text);
    return out;
  }

}
