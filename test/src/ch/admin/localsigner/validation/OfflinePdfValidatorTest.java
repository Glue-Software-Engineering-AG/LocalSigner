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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class OfflinePdfValidatorTest
{

  /**
   * Testing LOCALSIG-410. We modded a PDF/A-3a as PDF/A-2a.
   *
   * @throws IOException
   */
  @Test
  public void testPdfA2a() throws IOException
  {
    String file = "test/files/PDF-A/valid/testdokument_2a_localsig410.pdf";
    OfflinePdfValidator validator = validateFile(file);
    Assert.assertEquals("PDF/A-2a", validator.getValidationResults().getValidatedFlavourAsString());
    Assert.assertEquals("PDF/A-2a", validator.getValidationResults().getParsedPdfAFlavourAsString());
    Assert.assertTrue(validator.getValidationResults().isCompliant());
  }

  /**
   * Testing LOCALSIG-410. Valid file with a wrong PDF/A Flavour.
   *
   * @throws IOException
   */
  @Test
  public void testFallback() throws IOException
  {
    String file = "test/files/PDF-A/valid/wrongPDFAFlavour/pdfa1a_invalid_but_valid_pdfa1b_localsig410.pdf";
    OfflinePdfValidator validator = validateFile(file);
    Assert.assertEquals("PDF/A-1b", validator.getValidationResults().getValidatedFlavourAsString());
    Assert.assertEquals("PDF/A-1a", validator.getValidationResults().getParsedPdfAFlavourAsString());
    Assert.assertTrue(validator.getValidationResults().isCompliant());
  }

  /**
   * Testing not supported files (PDF/A-3A).
   *
   * @throws IOException
   */
  @Test
  public void testNotSupported() throws IOException
  {
    String folder = "test/files/PDF-A/not_supported";
    ArrayList<File> files = new ArrayList<>();
    files = listFilesFromSubfolders(folder, files);
    for (File file : files)
    {
      OfflinePdfValidator validator = validateFile(file);
      Assert.assertFalse(file.getName() + " was incorrectly reconized as supported file",
          validator.getValidationResults().isSupportedPdfA());
    }
  }

  /**
   * List all files from Folder and Subfolder.
   * @param path Path of the parent folder
   * @param files ArrayList to store the files.
   *
   * @return List of found files within the folder and subfolders
   */
  public ArrayList<File> listFilesFromSubfolders(String path, ArrayList files)
  {
    File folder = new File(path);
    for (File file : folder.listFiles())
    {
      if (file.isDirectory())
      {
        listFilesFromSubfolders(file.getPath(), files);
      } else
      {
        files.add(file);
      }
    }
    return files;
  }

  /**
   * Testing supported but invalid files.
   *
   * @throws IOException
   */
  @Test
  public void testInvalidFiles() throws IOException
  {
    File folder = new File("test/files/PDF-A/invalid/ok");
    for (File file : folder.listFiles())
    {
      OfflinePdfValidator validator = validateFile(file);
      Assert.assertTrue(file.getName() + " was incorrectly recognized as not supported.",
          validator.getValidationResults().isSupportedPdfA());
      Assert.assertFalse(file.getName() + " was incorrectly reconized as valid",
          validator.getValidationResults().isCompliant());
    }
  }

  /**
   * Testing supported and valid files.
   *
   * @throws IOException
   */
  @Test
  public void testValidFiles() throws IOException
  {
    File folder = new File("test/files/PDF-A/valid");
    for (File file : folder.listFiles())
    {
      if (!file.isDirectory())
      {
        OfflinePdfValidator validator = validateFile(file);

        Assert.assertTrue(file.getName() + " was incorrectly reconized as not supported file",
            validator.getValidationResults().isSupportedPdfA());
        Assert.assertTrue(file.getName() + " was incorrectly recognized as invalid",
            validator.getValidationResults().isCompliant());

        switch (validator.getValidationResults().getParsedPdfAFlavourAsString())
        {
          case "PDFA/A-1a":
            Assert.assertEquals(file.getName() + "is not a PDF/A-1a file", "PDF/A-1a",
                validator.getValidationResults().getValidatedFlavourAsString());
            break;
          case "PDF/A-1b":
            Assert.assertEquals(file.getName() + "is not a PDF/A-1b file", "PDF/A-1b",
                validator.getValidationResults().getValidatedFlavourAsString());
            break;
          case "PDF/A-2a":
            Assert.assertEquals(file.getName() + "is not a PDF/A-2a file", "PDF/A-2a",
                validator.getValidationResults().getValidatedFlavourAsString());
            break;
          case "PDF/A-2b":
            Assert.assertEquals(file.getName() + "is not a PDF/A-2b file", "PDF/A-2b",
                validator.getValidationResults().getValidatedFlavourAsString());
            break;
          case "PDF/A-2u":
            Assert.assertEquals(file.getName() + "is not a PDF/A-2u file", "PDF/A-2u",
                validator.getValidationResults().getValidatedFlavourAsString());
            break;
          default:
          // do nothing
        }
      }
    }
  }

  public OfflinePdfValidator validateFile(String filePath) throws IOException
  {
    File file = new File(filePath);
    return validateFile(file);
  }

  public OfflinePdfValidator validateFile(File testfile) throws IOException
  {
    OfflinePdfValidator validator = null;
    try (FileInputStream fis = new FileInputStream(testfile))
    {
      byte[] fileArr = IOUtils.toByteArray(fis);
      validator = new OfflinePdfValidator(fileArr);
      validator.validatePdfFile();
    }
    return validator;
  }
}
