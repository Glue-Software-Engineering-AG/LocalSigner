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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * Bulk testing the pdf validator. Used to detect if future library updates
 * change the validation result.
 */
public class TestSuiteBulkTest
{
  private final static Logger LOGGER = Logger.getLogger(TestSuiteBulkTest.class);

  @Test
  public void testInvalidFiles() throws IOException
  {
    String pathToFolder = "test/files/PDF-A/invalid/";

    List<String> fileNames = getFileNames(pathToFolder);
    List<ResultContainer> results = validateFiles(pathToFolder, fileNames);

    assertEquals(fileNames.size(), results.size());

    for (ResultContainer r : results)
    {
      assertFalse(r.valid);
    }
  }

  @Test
  public void testValidFiles() throws IOException
  {
    String pathToFolder = "test/files/PDF-A/valid/";

    List<String> fileNames = getFileNames(pathToFolder);
    List<ResultContainer> results = validateFiles(pathToFolder, fileNames);

    assertEquals(fileNames.size(), results.size());

    for (ResultContainer r : results)
    {
      StringBuilder sb = new StringBuilder();
      for (ValidationResult l : r.results)
      {
        sb.append(l.getPDFAFlavour().getPart().getName());
        sb.append(l.getPDFAFlavour().getLevel().getCode());

        for (TestAssertion assertion : l.getTestAssertions())
        {
          sb.append("  - ");
          sb.append(assertion.getMessage());
          sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        if (!r.valid)
        {
          LOGGER.info(r.filename + " -> " + sb.toString() + "\n\n");
        }
        assertTrue(r.filename + " -> " + sb.toString(), r.valid);
      }
    }

  }

  private List<ResultContainer> validateFiles(String filePath, List<String> files) throws IOException
  {

    List<ResultContainer> testResults = new ArrayList<ResultContainer>();

    for (String filename : files)
    {

      PdfAnalyzer analyzer = new PdfAnalyzer(filePath + filename);
      ValidationResult results = analyzer.getValidationResults().getVeraPdfValidationResult();
      ResultContainer c = new ResultContainer();
      c.results = new ArrayList<ValidationResult>();
      c.filename = filename;
      if (results.isCompliant())
      {
        c.valid = true;
      }
      c.results.add(results);
      testResults.add(c);
    }
    return testResults;
  }

  private List<String> getFileNames(String strFolder)
  {
    List<String> result = new ArrayList<String>();
    File folder = new File(strFolder);
    File[] listOfFiles = folder.listFiles();

    for (File f : listOfFiles)
    {
      if (f.isFile() && f.getName().endsWith("pdf"))
      {
        result.add(f.getName());
      }
    }
    return result;
  }

  private class ResultContainer
  {

    public String filename;

    public boolean valid = false;

    List<ValidationResult> results;

  }

}
