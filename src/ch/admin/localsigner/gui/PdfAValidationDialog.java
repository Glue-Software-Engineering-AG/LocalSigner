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
package ch.admin.localsigner.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.validation.PdfAnalyzer;
import ch.admin.localsigner.validation.PdfAValidationResults;

/**
 * This class displays a dialog containing a validation report whether this PDF
 * is PDF/A conform or not.
 *
 * @author Adrian Greiler
 * @author Roland Keller
 * @author $Author$
 * @version $Revision$
 */
public class PdfAValidationDialog
{

  private static final String TITLE = "Open eGov LocalSigner";

  private final MainGUI mainGUI;

  public PdfAValidationDialog(final MainGUI mainGUI)
  {
    this.mainGUI = mainGUI;

    if (mainGUI.getDocument().getInputFile() == null)
    {
      // no pdf to analyse loaded
      Message.warning(mainGUI.getMainshell(), LocalSigner.i18n("notarySign.loadFileFirst"));
      return;
    }

    PdfAnalyzer analyser = mainGUI.getPdfViewerPane().getCurrentAnalyzer();
    if (analyser == null)
    { // no file open yet
      Message.warning(mainGUI.getMainshell(), LocalSigner.i18n("notarySign.loadFileFirst"));
      return;
    }

    // get the display
    final Display display = mainGUI.getMainshell().getDisplay();

    // create new shell
    final Shell dialog = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    dialog.setText(TITLE);
    dialog.setImage(GuiHelper.loadAppIcon(display));

    final Font font = mainGUI.getFont();

    GridLayout dialogGridLayout = new GridLayout(1, false);
    dialogGridLayout.marginWidth = 0;
    dialogGridLayout.marginHeight = 0;
    dialogGridLayout.marginTop = 0;
    dialogGridLayout.verticalSpacing = 0;
    dialogGridLayout.horizontalSpacing = 0;
    dialogGridLayout.marginBottom = 0;
    dialog.setLayout(dialogGridLayout);
    dialog.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    addValidationReport(dialog, font, analyser);

    addCloseButton(dialog, dialog, font);

    // pack
    dialog.pack();

    // size
    final Rectangle parentsize = mainGUI.getMainshell().getBounds();

    final Point boxSize = new Point(600, 400);
    dialog.setBounds(parentsize.x + parentsize.width / 2 - boxSize.x / 2, //
        parentsize.y + parentsize.height / 2 - boxSize.y / 2, //
        boxSize.x, //
        boxSize.y); //

    dialog.open();

    while (!dialog.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  private void addValidationReport(final Composite parent, final Font font, PdfAnalyzer analyser)
  {
    // short report
    final Text analysisShortText = new Text(parent, SWT.MULTI | SWT.WRAP);
    analysisShortText.setEditable(false);
    analysisShortText.setText(getValidationReportShort(analyser));
    analysisShortText.setFont(font);
    analysisShortText.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
    analysisShortText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 0, 0));

    // horizontal line
    final Label spacer = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
    GridData spacerGridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
    spacer.setLayoutData(spacerGridData);

    // full report per analyser
    final Text analysisFullReportText = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
    analysisFullReportText.setEditable(false);
    analysisFullReportText.setText(getValidationReportFullReport(analyser));
    analysisFullReportText.setFont(font);
    analysisFullReportText.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
    analysisFullReportText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 0, 0));

    // set Window-Layout
    GridLayout scrolledCompLayout = new GridLayout(1, true);
    scrolledCompLayout.marginWidth = 21;
    scrolledCompLayout.marginTop = 21;
    scrolledCompLayout.verticalSpacing = 10;
    scrolledCompLayout.marginBottom = 8;
    parent.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
    parent.setLayout(scrolledCompLayout);
  }

  private String getValidationReportShort(PdfAnalyzer analyser)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(LocalSigner.i18n("format.claimedFormat"));
    sb.append(" ");
    if (analyser.getValidationResults().isSupportedPdfA())
    {
      sb.append(analyser.getValidationResults().getParsedPdfAFlavourAsString());
    }
    else if (analyser.isNotAPdfA())
    {
      sb.append(LocalSigner.i18n("pdfAValidationMsgBox.noPdfAFile"));
    }
    else
    {
      sb.append(analyser.getValidationResults().getParsedPdfAFlavourAsString());
      sb.append(" ");
      sb.append(LocalSigner.i18n("pdfAValidationMsgBox.unsupportedPdfA"));
    }

    sb.append(System.lineSeparator());
    sb.append(LocalSigner.i18n("format.validatedFormat"));
    sb.append(" ");

    if (!analyser.getValidationResults().isError())
    {
      if (analyser.getValidationResults().isSupportedPdfA())
      {
        sb.append(analyser.getValidationResults().getValidatedFlavourAsString());
      }
      else
      {
        sb.append(LocalSigner.i18n("pdfAValidationMsgBox.noCheck"));
      }
    }
    else
    {
      sb.append(LocalSigner.i18n("pdfAValidationMsgBox.errorMsg"));
    }
    return sb.toString();
  }

  private String getValidationReportFullReport(PdfAnalyzer analyser)
  {
    StringBuilder sb = new StringBuilder();
    PdfAValidationResults results = analyser.getValidationResults();

    if (results.isError())
    {
      return LocalSigner.i18n("pdfAValidationMsgBox.errorMsg");
    }

    if (!results.isSupportedPdfA())
    {
      return LocalSigner.i18n("pdfAValidationMsgBox.unsupportedFileCheckResult");
    }

    String i18nValid = LocalSigner.i18n("format.validFile");
    String i18nInvalid = LocalSigner.i18n("format.invalidFile");

    String flavourAsString = results.getValidatedFlavourAsString();
    String state = results.isCompliant() ? String.format(i18nValid, flavourAsString)
        : String.format(i18nInvalid, flavourAsString);

    sb.append(results.getProfileDescription());
    sb.append(": ");
    sb.append(state);
    sb.append(System.lineSeparator());

    sb.append(results.getTestedAssertions());
    sb.append(System.lineSeparator());
    sb.append(System.lineSeparator());

    return sb.toString();
  }

  private void addCloseButton(final Composite parent, final Shell dialog, final Font font)
  {
    Composite buttonArea = new Composite(parent, SWT.NONE);

    GridLayout buttonAreaGridLayout = new GridLayout(1, false);
    buttonAreaGridLayout.marginRight = 3;
    buttonAreaGridLayout.marginBottom = 8;
    buttonAreaGridLayout.marginTop = 7;
    buttonArea.setLayout(buttonAreaGridLayout);
    buttonArea.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    buttonArea.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 0, 0));

    // close button
    final Button close = new Button(buttonArea, SWT.PUSH);
    close.setFont(font);
    close.setText(LocalSigner.i18n("close"));
    close.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 5, 5));
    close.addListener(SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent(final Event arg0)
      {
        dialog.close();
      }

    });
    dialog.setDefaultButton(close);
  }
}
