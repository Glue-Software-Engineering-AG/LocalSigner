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
package ch.admin.localsigner.gui.common;

import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import java.io.File;
import java.nio.file.Paths;

/**
 * Used to get the output file name
 */
public class SaveFileDialog
{

  /**
   * Returns the name of the output file the user wants to write the result of an operation to.
   * @param originalFilename The name of the original file to generate a proposal.
   * @param suffix the default suffix of the resulting file of that operation.
   * @return The name of the output file the user wants to write the result to. NULL if cancelled!
   */
  public static String getOutputFile(String originalFilename, String suffix)
  {
    String outputName = getOutputNameProposal(LocalSigner.mainGui.getInputFileName(), suffix);

    File asFile = new File(outputName);

    // open a dialog for pdf file storage
    final FileDialog dialog = new FileDialog(LocalSigner.mainGui.getMainshell(), SWT.SAVE);

    dialog.setFilterPath(asFile.getParent());
    dialog.setFileName(asFile.getName());
    dialog.setFilterNames(new String[] { "PDF" });
    dialog.setFilterExtensions(new String[] { "*.pdf" });
    outputName = dialog.open();

    // if the user did not select a file just return null
    if (StringUtils.isEmpty(outputName)) {
      return null;
    }

    // concatenate file name
    String filename = dialog.getFilterPath() + File.separator + dialog.getFileName();

    // ask to override if file exists already
    final File outputFile = new File(outputName);
    if (LocalSigner.mainGui.isInteractiveMode() && outputFile.exists())
    {
      int response = createDialogFileExists();

      if (response == SWT.NO)
      {
        return getOutputFile(filename, suffix);
      }
    }

    if (Paths.get(outputName).equals(Paths.get(originalFilename)))
    {
      int response = createDialogOutputEqualToInput();

      if (response == SWT.OK)
      {
        return getOutputFile(filename, suffix);
      }
    }
     return filename;
  }

  private static int createDialogFileExists()
  {
    MessageBox messageBox = new MessageBox(LocalSigner.mainGui.getMainshell(),
        SWT.ICON_QUESTION | SWT.YES | SWT.NO);
    messageBox.setMessage(LocalSigner.i18n("fileExistsDialog.text"));
    messageBox.setText(LocalSigner.i18n("fileExistsDialog.title"));
    int response = messageBox.open();
    return response;
  }

  private static int createDialogOutputEqualToInput()
  {
    MessageBox messageBox = new MessageBox(LocalSigner.mainGui.getMainshell(),
        SWT.ICON_INFORMATION | SWT.OK );
    messageBox.setMessage(LocalSigner.i18n("outputEqualsInputDialog.text"));//LocalSigner.i18n("input == output not allowed!"));
    messageBox.setText(LocalSigner.i18n("outputEqualsInputDialog.title"));
    int response = messageBox.open();
    return response;
  }

  public static String getOutputNameProposal(String originalFilename, String suffix)
  {
    String file = toLowerCaseSuffix(originalFilename);
    if (file.endsWith(suffix))
    {
      return originalFilename;
    }
    file = file.substring(0, file.lastIndexOf(Constants.PDF_FILE_SUFFIX)) + suffix;
    return file;
  }

  public static String toLowerCaseSuffix(final String file)
  {
    if (file == null)
    {
      return "";
    }
    // find file suffix
    int index = file.lastIndexOf('.');

    // lower case file suffix
    if (index > 0)
    {
      return file.substring(0, index) + file.substring(index).toLowerCase();
    }
    return file;
  }

  private SaveFileDialog()
  {
    // hide constructor
  }
}
