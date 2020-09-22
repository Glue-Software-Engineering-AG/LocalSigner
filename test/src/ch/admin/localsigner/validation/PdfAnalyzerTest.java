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

import java.io.IOException;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author schaefer
 */
public class PdfAnalyzerTest extends TestCase
{

  public PdfAnalyzerTest(String testName)
  {
    super(testName);
  }

  public void test1GetAcroFieldsNotSignature() throws IOException
  {
    PdfAnalyzer analyzer = new PdfAnalyzer("test/forms/alltypes_nosignature_enabled_acrofields.pdf");
    List<String> acroFilds = analyzer.getAcroFieldsNotSignature();

    assertEquals(
        "Es wurden Felder zurückgegeben, obwohl das Formular LS-enabled ist und kein Signaturfeld enthalten ist.", 0,
        acroFilds.size());
  }

  public void test2GetAcroFieldsNotSignature() throws IOException
  {
    PdfAnalyzer analyzer = new PdfAnalyzer("test/forms/alltypes_nosignature_notenabled_acrofields.pdf");
    List<String> acroFilds = analyzer.getAcroFieldsNotSignature();

    assertEquals(
        "Es wurden nicht 7 Felder zurückgegeben, obwohl das Formular nicht LS-enabled ist und kein Signaturfeld enthalten ist.",
        7, acroFilds.size());
  }

  public void test3GetAcroFieldsNotSignature() throws IOException
  {
    PdfAnalyzer analyzer = new PdfAnalyzer("test/forms/alltypes_signature_enabled_acrofields.pdf");
    List<String> acroFilds = analyzer.getAcroFieldsNotSignature();

    assertEquals(
        "Es wurden Felder zurückgegeben, obwohl das Formular LS-enabled ist und ein Signaturfeld enthalten ist.", 0,
        acroFilds.size());
  }

  public void test4GetAcroFieldsNotSignature() throws IOException
  {
    PdfAnalyzer analyzer = new PdfAnalyzer("test/forms/alltypes_signature_notenabled_acrofields.pdf");
    List<String> acroFields = analyzer.getAcroFieldsNotSignature();

    assertEquals(
        "Es wurden nicht 7 Felder zurückgegeben, obwohl das Formular nicht LS-enabled ist und ein Signaturfeld enthalten ist.",
        7, acroFields.size());
  }
}
