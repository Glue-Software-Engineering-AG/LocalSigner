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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import com.lowagie.text.exceptions.BadPasswordException;
import ch.admin.localsigner.main.InputFile;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.BoxPosition;
import ch.admin.localsigner.utils.Constants;
import org.apache.commons.io.FilenameUtils;

/**
 * This class holds the current loaded PDF document and all its states
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class PdfDoc
{
  private InputFile inputFile;

  private List<BoxPosition> sigFields;

  private String outputFile = "";

  private boolean interactiveMode = true;

  public void setInteractiveMode(boolean interactiveMode)
  {
    this.interactiveMode = interactiveMode;
  }

  public InputFile getInputFile()
  {
    return inputFile;
  }

  public void setInputFile(InputFile inputFile)
  {
    this.inputFile = inputFile;
  }

  public List<BoxPosition> getSigFields()
  {
    if (sigFields == null)
    {
      sigFields = new ArrayList<BoxPosition>();
    }
    return sigFields;
  }

  public void setSigFields(List<BoxPosition> sigFields)
  {
    this.sigFields = sigFields;
  }

  void setOutputFile(String filename)
  {
    this.outputFile = filename;
  }

  public String getOutputFile()
  {
    return this.outputFile;
  }

  public byte[] getOutputFileData() throws IOException
  {
    if (outputFile == null)
    {
      return null;
    }
    if (inputFile == null)
    {
      return null;
    }
    if (inputFile.isInputFile(outputFile))
    {
      // input is locked, get byte array
      try
      {
        return getInputFile().getFileToDisplay(true);
      } catch (BadPasswordException e)
      {
        // TODO
        return null;
      }
    }
    return FileUtils.readFileToByteArray(new File(this.outputFile));
  }

  /**
   * Proposed name for final file
   *
   * @return absolute file name
   */
  public String proposeOutputNameFinal()
  {
    if (interactiveMode)
    {
      try
      {
        final String originalFilename = getInputFile().getInputFileName();
        final String baseFilename = FilenameUtils.getBaseName(originalFilename);
        final String baseFilePath = FilenameUtils.getFullPath(originalFilename);
        final String signedFilename = baseFilePath + baseFilename + LocalSigner.appConfig.getSignedDocExtension()
            + Constants.PDF_FILE_SUFFIX;
        return signedFilename;
      } catch (Exception e)
      {
        return "";
      }
    }
    else
    {
      return getInputFile() != null ? getInputFile().getInputFileName() : "";
    }
  }

  /**
   * Proposed name for intermediate file
   *
   * @return absolute file name
   */
  public String proposeOutputNameIntermediate()
  {
    if (interactiveMode)
    {
      String file = toLowerCaseSuffix(getInputFile().getInputFileName());
      if (file.endsWith(Constants.MODIFIED_PDF_SUFFIX))
      {
        return getInputFile().getInputFileName();
      }
      file = file.substring(0, file.indexOf(Constants.PDF_FILE_SUFFIX)) + Constants.MODIFIED_PDF_SUFFIX;
      return file;
    }
    else
    {
      return getInputFile() != null ? getInputFile().getInputFileName() : "";
    }
  }

  private static String toLowerCaseSuffix(final String file)
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
}
