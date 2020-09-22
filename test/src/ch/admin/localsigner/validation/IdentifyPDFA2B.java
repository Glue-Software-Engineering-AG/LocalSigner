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
package ch.admin.localsigner.validation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * This unit test is for identifying PDFA2b files and move them in another
 * directory.
 *
 * Readme.txt: Sollten PDF/A-2b doch einmal unterstützt werden, so kann die
 * Triage mittels dem UnitTest ch.admin.localsigner.validation.IdentifyPDFA2B
 * neu vorgenommen werden. Ggf. die Pfade anpassen.
 *
 */
public class IdentifyPDFA2B
{

  private static final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(IdentifyPDFA2B.class);

  @Before
  public void setup()
  {
    VeraGreenfieldFoundryProvider.initialise();
  }

  /**
   * Adapt paths for identifying and moving files.
   */
  @Test
  public void testPositivFalse() throws IOException
  {
    String path = "test/files/PDF-A/positivFalse/";
    String destPath = "test/files/PDF-A/pdf2B/";

    List<String> fileNames = getFileNames(path);
    for (String fn : fileNames)
    {
      validateAndMoveFilePDF2B(path + fn, destPath + fn);
    }
  }

  private void validateAndMoveFilePDF2B(String fileName, String destFileName)
  {

    try
    {
      byte[] fileByteArray = FileUtils.readFileToByteArray(new File(fileName));
      PDFAParser parser = Foundries.defaultInstance().createParser(new ByteArrayInputStream(fileByteArray));
      PDFAValidator validator2b = Foundries.defaultInstance().createValidator(PDFAFlavour.PDFA_2_B, false);
      ValidationResult result = validator2b.validate(parser);

      LOGGER.info(fileName + " -> " + result.isCompliant());

      if (result.isCompliant())
      {
        Path srcPath = new File(fileName).toPath();
        Path dstPath = new File(destFileName).toPath();

        Files.move(srcPath, dstPath);
      }

    } catch (ModelParsingException | IOException | EncryptedPdfException | ValidationException ex)
    {
      LOGGER.warn(ex);
    }

  }

  private List<String> getFileNames(String strFolder)
  {
    List<String> result = new ArrayList<String>();
    File folder = new File(strFolder);
    File[] listOfFiles = folder.listFiles();

    if (listOfFiles == null)
    {
      return result;
    }

    for (File f : listOfFiles)
    {
      if (f.isFile() && f.getName().endsWith("pdf"))
      {
        result.add(f.getName());
      }
    }
    return result;
  }

}
