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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.SwingWorker;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.PleaseWaitDialog;
import ch.admin.localsigner.gui.common.SaveFileDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileOpenException;
import ch.admin.localsigner.utils.Constants;

/**
 * This class converts a PDF to PDF/A. Why is it so complicated? This is a
 * Swing-Listener, so we are on the Swing-Render-Thread (EDT). We must schedule
 * the SWT calls on the SWT Thread (via asyncExec). The Converter-Thread should
 * not block EDT / SWT, so we use a SwingWorker to do it in background. Again we
 * need asyncExec to schedule SWT updates to the MessageBox inside the
 * Converter-Thread.
 *
 */
public class ConvertToPdfAListener implements ActionListener
{
  private final static Logger LOGGER = Logger.getLogger(ConvertToPdfAListener.class);

  private String outputName;

  private PleaseWaitDialog dlg;

  private byte[] fileToConvert;

  @Override
  public void actionPerformed(ActionEvent e)
  {

    // schedule SWT updates in SWT-Thread
    LocalSigner.mainGui.getMainshell().getDisplay().syncExec(new Runnable()
    {

      @Override
      public void run()
      {
        // choose file to store converted PDF
        outputName = SaveFileDialog.getOutputFile(LocalSigner.mainGui.getInputFileName(),
            Constants.PDF_A_CONVERTED_SUFFIX);
        if (StringUtils.isEmpty(outputName))
        {
          LOGGER.info("user canceled conversion in file dialog");
          return;
        }

        // show "please wait"-dialog
        dlg = new PleaseWaitDialog(LocalSigner.mainGui, LocalSigner
            .i18n("pdfAConvertMsgBox.title"), LocalSigner.i18n("pdfAConvertMsgBox.text"));

        fileToConvert = LocalSigner.mainGui.getInputFile();
      }
    });

    // schedule background work outside the EDT
    SwingWorker<String, Void> worker = new SwingWorker<String, Void>()
    {

      @Override
      protected String doInBackground() throws Exception
      {
        PdfConverter converter = new PdfConverter(dlg, fileToConvert, outputName);
        converter.run();
        return "";
      }

    };
    worker.execute();

  }

  class PdfConverter extends ConvertToPDFATemplate implements Runnable
  {
    byte[] inputFile;

    String outputName;

    PleaseWaitDialog dlg;

    PdfConverter(PleaseWaitDialog dlg, byte[] inputFile, String outputName)
    {
      this.inputFile = inputFile;
      this.outputName = outputName;
      this.dlg = dlg;
    }

    @Override
    public void run()
    {
      try
      {
        buildPDFA(inputFile, outputName);
        LocalSigner.mainGui.getMainshell().getDisplay().asyncExec(new Runnable()
        {

          @Override
          public void run()
          {
            try {
              LocalSigner.mainGui.setInputFileAndCheck(outputName, true);
            } catch (FileOpenException e) {
              FileExceptionHandler.showAppropriateErrorMessage(e, outputName);
            }
          }
        });

      } catch (InterruptedException ex)
      {
        LOGGER.warn("Could not convert to pdfa!", ex);
        Message.warning(LocalSigner.mainGui.getMainshell(),
            LocalSigner.i18n("pdfAConvertProblem"));
      } catch (IOException ex)
      {
        LOGGER.warn("Could not convert to pdfa!", ex);
        Message.warning(LocalSigner.mainGui.getMainshell(),
            LocalSigner.i18n("pdfAConvertProblem"));
      } finally
      {
        LocalSigner.mainGui.getMainshell().getDisplay().asyncExec(new Runnable()
        {

          @Override
          public void run()
          {
            if(dlg != null) dlg.close();
          }
        });
      }
    }

    @Override
    protected
    void createPDFDocument() throws InterruptedException {
      int numberOfPages = oldpdf.getNumberOfPages();
      for (int i = 0; i < numberOfPages; i++)
      {
        dlg.replaceLabel(LocalSigner.i18n("pdfAConvertMsgBox.text") + "\n"
            + LocalSigner.i18n("page") + " " + (i + 1) + " / " + numberOfPages);
        createPage(i);
      }
    }
  }
}
