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

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.admin.localsigner.gui.common.InputDialog;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.BoxPosition;

/**
 * This class is instantiated by pressing the 'create' button in the main view.
 * It tests the given input data and initiates the actual creating process.
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class AddSigFieldListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(
          AddSigFieldListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   */
  public AddSigFieldListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  @Override
  public void handleEvent(final Event event)
  {
    if (maingui.getGuiMode() == GuiMode.addTextField)
    {
      // add text field
      new AddTextFieldListener(maingui).handleEvent(null);
      return;
    }

    final PropertiesGUI propertiesGui = maingui.getPropertiesGui();

    try (final PdfReader reader = new PdfReader(maingui.getInputFile()))
    {
      // check input file for certification level, if the file is certified
      // and changes are not allowed we inform the user and return
      if (reader.getCertificationLevel() == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED)
      {
        Message.warning(maingui.getMainshell(), LocalSigner.i18n( "documentCertified"));
        LOGGER.debug("document is certified");
        return;
      }

      final BoxPosition position = new BoxPosition(
              propertiesGui.getSignaturePageSignature(reader.getNumberOfPages()),
          propertiesGui.getLeftPosInPdfUnits(), propertiesGui.getTopPosPdfUnit(),
          propertiesGui.getBoxWidthInPdfUnits(), propertiesGui.getBoxHeightInPdfUnits());

      // check for intersection, don't draw signature position on top of
      // another!
      for (BoxPosition p : maingui.getDocument().getSigFields())
      {
        if (p.getPage() != position.getPage())
        {
          // not the same page, therefore no intersection
          continue;
        }

        final Rectangle rect1 = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        final Rectangle rect2 =
          new Rectangle(position.getX(), position.getY(), position.getWidth(), position.getHeight());

        if (rect1.intersects(rect2))
        {
          // show warning
          Message.warning(maingui.getMainshell(), LocalSigner.i18n("sigField"), LocalSigner.i18n("sigFieldIntersects"));
          return;
        }
      }

      // prompt for a name
      InputDialog dialog = new InputDialog(maingui, LocalSigner.i18n("sigField"),
              LocalSigner.i18n("sigFieldChooseName"), false);
      String name = dialog.getInput();

      // resolve name conflict
      ArrayList<String> existingNames = new ArrayList<String>();

      // signed names
      for (Object s : reader.getAcroFields().getSignatureNames())
      {
        existingNames.add(((String) s).toLowerCase());
      }

      // unsigned names
      for (Object s : reader.getAcroFields().getBlankSignatureNames())
      {
        existingNames.add(((String) s).toLowerCase());
      }

      // temporary names
      for (BoxPosition p : maingui.getDocument().getSigFields())
      {
        existingNames.add(p.getName().toLowerCase());
      }

      if (name == null || name.length() == 0 || existingNames.contains(name.toLowerCase()))
      {
        // show warning
        Message.warning(maingui.getMainshell(), LocalSigner.i18n("sigField"), LocalSigner.i18n("sigFieldNameInvalid"));
        return;
      }

      position.setName(name);
      maingui.getDocument().getSigFields().add(position);
      LOGGER.debug("added signature field " + name + ", now "
              + maingui.getDocument().getSigFields().size() + " fields");
      // set box position for next box below current box
      maingui.getPropertiesGui().setTopPos(position.getY() + position.getHeight() + 5);

      maingui.reloadInputFile(true);
    } catch (IOException e)
    {
      LOGGER.error("cannot open file", e);
    }
  }

}
