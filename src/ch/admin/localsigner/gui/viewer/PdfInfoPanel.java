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
package ch.admin.localsigner.gui.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.validation.PdfAnalyzer;

@SuppressWarnings("serial")
public class PdfInfoPanel extends JPanel
{

  private JLabel statusLabel;

  private JButton convertToPdfABtn;

  private final static Color LIGHT_BLUE = new Color(135, 206, 250);
  private MainGUI mainGui;

  public PdfInfoPanel(MainGUI mainGui)
  {
    this.mainGui = mainGui;
    createGUI();
  }

  private void createGUI()
  {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(new Insets(3, 3, 3, 3)));

    statusLabel = new JLabel();
    add(statusLabel, BorderLayout.CENTER);

    createAndAddInvisibleConvertButton();
  }

  private void createAndAddInvisibleConvertButton()
  {
    convertToPdfABtn = new JButton(LocalSigner.i18n("pdfInfoPanel.convert"));
    convertToPdfABtn.setVisible(false);
    convertToPdfABtn.addActionListener(new ConvertToPdfAListener());

    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
    buttonPane.add(convertToPdfABtn);
    buttonPane.setBackground(Color.YELLOW);
    add(buttonPane, BorderLayout.EAST);
  }

  private boolean isConvertPossibleAndNeeded(PdfAnalyzer analyzer)
  {
    return !analyzer.isSigned();
  }



  void showYellowInfo(PdfAnalyzer analyzer)
  {
    String statusText = LocalSigner.i18n("pdfInfoPanel.isNotPdfA");
    setBackground(Color.YELLOW);
    if (mainGui.isInteractiveMode())
    {
      if (isConvertPossibleAndNeeded(analyzer)) {
        statusText = statusText + " " + LocalSigner.i18n("pdfInfoPanel.isNotPdfAButConversionPossible");
        convertToPdfABtn.setVisible(true);
      } else {
        statusText = statusText + " " + LocalSigner.i18n("pdfInfoPanel.isNotPdfAConversionImpossible");
        convertToPdfABtn.setVisible(false);
      }
    }
    statusLabel.setText("<html>" + statusText + "</html>");
  }

  void showBlueInfo(PdfAnalyzer analyzer)
  {
    String compliantStandard = analyzer.getValidationResults().getValidatedFlavourAsString();
    String additionalText;
    if (analyzer.isNotAccessible())
    {
      additionalText = LocalSigner.i18n("pdfInfoPanel.isNotAccessible");
    }
    else
    {
      additionalText = LocalSigner.i18n("pdfInfoPanel.isAccessible");
    }
    String i18n = LocalSigner.i18n("pdfInfoPanel.isPdfA");

    statusLabel.setText("<html>" + String.format(i18n, compliantStandard, additionalText) + "</html>");
    setBackground(LIGHT_BLUE);
  }

  void clear()
  {
    statusLabel.setText("");
    convertToPdfABtn.setVisible(false);
  }
}
