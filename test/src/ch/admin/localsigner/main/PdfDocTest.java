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
package ch.admin.localsigner.main;

import ch.admin.localsigner.config.LanguageConfiguration;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.PdfDoc;
import ch.admin.localsigner.main.exception.FileOpenException;
import com.lowagie.text.exceptions.BadPasswordException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Ignore
public class PdfDocTest
{
  private PdfDoc document;

  private MainGUI gui;

  private final String FILE = "test/test.pdf";

  private final String FILE_PDF = "test/testpdf.pdf";

  private final String FILE_MOD = "test/test-mod.pdf";

  private final String FILE_MOD_SIG = "test/test-mod-sig.pdf";

  private final String FILE_CASE = "test/test-mod.PDF";

  @Before
  public void init() throws ConfigurationException, IOException, FileOpenException {
    LocalSigner.setLangConf(new LanguageConfiguration("de"));
    gui = new MainGUI();
    document = gui.getDocument();
    document.setInputFile(new InputFile(gui));
    document.getInputFile().setOriginalFile(FILE, true);
    document.getInputFile().updateAttachment(FILE);

    // copy test files
    String[] files = new String[]
    {
      FILE_PDF, FILE_MOD, FILE_MOD_SIG, FILE_CASE
    };
    for (String file : files)
    {
      FileInputStream fis = new FileInputStream(FILE);
      FileOutputStream fos = new FileOutputStream(file);
      IOUtils.copy(fis, fos);
      fis.close();
      fos.close();
    }
  }

  @After
  public void clean()
  {
    // delete test files
    String[] files = new String[] { FILE_PDF, FILE_MOD, FILE_MOD_SIG, FILE_CASE };
    for (String file : files)
    {
      boolean ok = new File(file).delete();
    }
  }

  @Test
  public void proposeName() throws BadPasswordException, FileOpenException {
    assertEquals("test/test-sig.pdf", document.proposeOutputNameFinal());
    assertEquals("test/test-mod.pdf", document.proposeOutputNameIntermediate());
   // assertTrue(document.getInputFile().getFileToDisplay(true).contains("merge"));
    assertEquals(document.getInputFile().getFileToDisplay(true), document.getInputFile().getFileToSign());

    // test if file name contains pdf
    document.getInputFile().setOriginalFile(FILE_PDF, true);
    assertEquals("test/testpdf-sig.pdf", document.proposeOutputNameFinal());
    assertEquals("test/testpdf-mod.pdf", document.proposeOutputNameIntermediate());
  }

  @Test
  public void proposeNameChain() throws FileOpenException {
    document.getInputFile().setOriginalFile(FILE_MOD, true);
    assertEquals("test/test-mod-sig.pdf", document.proposeOutputNameFinal());
    assertEquals("test/test-mod.pdf", document.proposeOutputNameIntermediate());

    document.getInputFile().setOriginalFile(FILE_MOD_SIG, true);
    assertEquals("test/test-mod-sig-sig.pdf", document.proposeOutputNameFinal());
    assertEquals("test/test-mod-sig-mod.pdf", document.proposeOutputNameIntermediate());
  }

  @Test
  public void proposeNameCase() throws FileOpenException {
    document.getInputFile().setOriginalFile(FILE_CASE, true);
    assertEquals("test/test-mod-sig.pdf", document.proposeOutputNameFinal());
    assertEquals("test/test-mod.PDF", document.proposeOutputNameIntermediate());
  }

  @Test
  public void checkSum()
  {
    String origSum = document.getInputFile().getChecksumOfOriginalFile();
    String mergeSum = document.getInputFile().getChecksumOfMergedFile();
    assertEquals("b5f70372b379b4917be53d7d3fe5620afe72c8eee68a7d5d77a662cd7815d340", origSum);
    assertFalse(origSum.equals(mergeSum));
    assertEquals(64, origSum.length());
    assertEquals(64, mergeSum.length());
  }
}
