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
import ch.admin.localsigner.utils.Constants;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import java.io.File;
import java.io.IOException;

/**
 * Choose PDF extension action.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ChoosePdfExtensionListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(
          ChoosePdfExtensionListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   * @param maingui
   *          The main GUI
   */
  public ChoosePdfExtensionListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {
    // open a dialog for pdf file loading
    final FileDialog dialog = new FileDialog(maingui.getMainshell(), SWT.OPEN);
    dialog.setFilterNames(new String[]
            {
              "PDF"
            });
    dialog.setFilterExtensions(new String[]
            {
              "*.pdf"
            });
    dialog.open();

    // if the user did not select a file just return
    if (StringUtils.isEmpty(dialog.getFileName()))
    {
      return;
    }

    // if a correct file has been chosen (SHOULD!)
    if (dialog.getFileName().toLowerCase().endsWith(Constants.PDF_FILE_SUFFIX))
    {
      final String chosenFile = dialog.getFilterPath() + File.separator
              + dialog.getFileName();

      if (attachmentHasErrors(chosenFile))
      {
        return;
      }

      // finally set pdf into property window
      maingui.getPropertiesGui().setPdfAttachment(chosenFile);
    }
    else
    {
      // this should not happen, as we set an input filter in the file dialog,
      // but with drag and drop...
      Message.warning(maingui.getMainshell(), LocalSigner.i18n("errorNotPdf"));

      LOGGER.debug("no pdf file selected");
    }
  }

  /**
   * Checks if the specified PDF attachment is valid and contains exactly one
   * page.
   * @return
   */
  private boolean attachmentHasErrors(final String attachment)
  {
    PdfReader reader;

    // check for general errors
    try
    {
      reader = new PdfReader(attachment);
    } catch (IOException e)
    {
      LOGGER.debug("error opening pdf attachment", e);
      Message.warning(maingui.getMainshell(),
              LocalSigner.i18n("errorInvalidPdf"));
      return true;
    }

    // attachment must have exactly one page
    if (reader.getNumberOfPages() != 1)
    {
      LOGGER.debug("pdf must contain exactly one page");
      Message.warning(maingui.getMainshell(), LocalSigner.i18n(
              "errorPdfMustHaveOnePage"));
      return true;
    }

    // check if the file is encrypted and no modifications are allowed
    if (reader.isEncrypted())
    {
      LOGGER.debug("pdf is encrypted");
      Message.warning(maingui.getMainshell(), LocalSigner.i18n("pdfEncrypted"));
      return true;
    }

    // check input file for certification level, if the file is certified
    // and changes are not allowed we inform the user and return
    if (reader.getCertificationLevel()
            == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED)
    {
      Message.warning(maingui.getMainshell(), LocalSigner.i18n(
              "documentCertified"));
      return true;
    }

    // everything ok
    return false;
  }

}
