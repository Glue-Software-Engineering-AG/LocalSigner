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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.validation.PdfAnalyzer;

@SuppressWarnings("serial")
public class ViewerPanel extends JApplet
{
  private boolean isInfoPanelVisible = false;

  private PdfViewerPanelBFO bfoViewer;

  private PdfInfoPanel infoPanel;

  private PdfAnalyzer currentAnalyzer = null;

  ViewerPanel(final MainGUI mainGui)
  {
    setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 0.01;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridx = 0;
    constraints.gridy = 0;

    infoPanel = new PdfInfoPanel(mainGui);
    infoPanel.setVisible(isInfoPanelVisible);
    add(infoPanel, constraints);

    bfoViewer = new PdfViewerPanelBFO(mainGui);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.gridy = 1;
    constraints.weighty = 0.95;

    add(bfoViewer, constraints);
  }

  private PdfInfoPanel getInfoPanel()
  {
    return infoPanel;
  }

  public void updateStatus(final PdfAnalyzer analyzer)
  {
    currentAnalyzer = analyzer;

    SwingUtilities.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        getInfoPanel().clear();
        analyzer.validatePdfA();

        if (analyzer.getValidationResults().isSupportedPdfA() && !analyzer.getValidationResults().isError()
            && analyzer.getValidationResults().isCompliant())
        {
          getInfoPanel().showBlueInfo(analyzer);
        }
        else
        {
          getInfoPanel().showYellowInfo(analyzer);
        }
      }
    });
  }

  public PdfAnalyzer getCurrentAnalyzer()
  {
    return currentAnalyzer;
  }

  public void closePdf()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        bfoViewer.close();
      }
    });

  }

  public int getScrollPosition()
  {
    return bfoViewer.getScrollPosition();
  }
  public void setDisableDrawing(boolean b)
  {
    bfoViewer.setDisableDrawing(b);
  }

  public void openFile(final byte[] fileData)
  {

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        Thread.currentThread().setName("open-file-in-bfo");

        bfoViewer.openFile(fileData);
      }
    });
  }

  public void setScrollPosition(final int pos)
  {
    SwingUtilities.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        bfoViewer.setScrollPosition(pos);
        bfoViewer.validate();
      }
    });
  }

  public void showInfoPanel()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        isInfoPanelVisible = true;
        infoPanel.setVisible(true);
      }
    });

  }

  public void hideInfoPanel()
  {
    isInfoPanelVisible = false;
  }

}
