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

import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

/**
 * ValidationResults from the OfflinePdfValidator.validatePdfFile
 *
 * @author keller
 */
public class PdfAValidationResults
{

  private ValidationResult veraPdfValidationResult = null;

  private boolean error;

  private PDFAFlavour parsedPdfAFlavour;

  /**
   * Get the VeraPdf validation result.
   *
   * @return VeraPdf validation result
   */
  ValidationResult getVeraPdfValidationResult()
  {
    return veraPdfValidationResult;
  }

  boolean validationNeeded()
  {
    return veraPdfValidationResult == null;
  }

  public boolean isCompliant()
  {
    return getVeraPdfValidationResult().isCompliant();
  }

  public String getValidatedFlavourAsString()
  {
    return getVeraPdfValidationResult().getPDFAFlavour().getPart().getName()
        + getVeraPdfValidationResult().getPDFAFlavour().getLevel().getCode();
  }

  /**
   * Set the ValidationResult from the VeraPdf-Validation.
   *
   * @param veraPdfValidationResult
   *          from the VeraPdf-Validation
   */
  void setVeraPdfValidationResult(ValidationResult veraPdfValidationResult)
  {
    this.veraPdfValidationResult = veraPdfValidationResult;
  }

  /**
   * Get the boolean if the file PdfA-Flavour is supported.
   *
   * @return boolean if the PdfA-Flavour from the file is supported
   */
  public boolean isSupportedPdfA()
  {
    return claimsToBeASupportedPdfA();
  }

  /**
   * Get the Error boolean (true if a error is happen during validation).
   *
   * @return boolean error
   */
  public boolean isError()
  {
    return error;
  }

  /**
   * If we got an exception, we mark the file.
   */
  public void markAsInvalid()
  {
    this.error = true;
  }

  /**
   * Returns PdfAFlavour represented as a string like 'PDF/A-1b'
   *
   * @return String to display to user
   */
  public String getParsedPdfAFlavourAsString()
  {
    return "PDF/A-" + parsedPdfAFlavour.toString();
  }

  /**
   * Set pdfAFlavour from the checked file.
   *
   * @param pdfAFlavour
   *          from the checked file.
   */
  public void setParsedPdfAFlavour(PDFAFlavour pdfAFlavour)
  {
    this.parsedPdfAFlavour = pdfAFlavour;
  }

  /**
   * Checks if the current validated result is either PDFAFlavour.PDFA_1_B or
   * PDFAFlavour.PDFA_2_U and thus _not_ accessible ("barrierefrei").
   */
  boolean isNotAccessible()
  {
    return getVeraPdfValidationResult().getPDFAFlavour() == PDFAFlavour.PDFA_1_B
        || getVeraPdfValidationResult().getPDFAFlavour() == PDFAFlavour.PDFA_2_U;
  }

  boolean isNotAPdfA()
  {
    return parsedPdfAFlavour == PDFAFlavour.NO_FLAVOUR;
  }

  /**
   * Details of the validated VeraPDF profile.
   */
  public String getProfileDescription()
  {
    return getVeraPdfValidationResult().getProfileDetails().getDescription();
  }

  /**
   * Details of the validated VeraPDF assertions.
   */
  public String getTestedAssertions()
  {
    StringBuilder sb = new StringBuilder();
    for (TestAssertion assertion : getVeraPdfValidationResult().getTestAssertions())
    {
      sb.append("  - ");
      sb.append(assertion.getMessage());
      sb.append(System.lineSeparator());
    }

    return sb.toString();
  }

  /**
   * Tests for Flavours PDFA_1_A, PDFA_1_B, PDFA_2_A and PDFA_2_U
   *
   * @return true if one of the above mentioned flavours is declared (no
   *         validation done!)
   */
  boolean claimsToBeASupportedPdfA()
  {
    return PDFAFlavour.PDFA_1_A == parsedPdfAFlavour || PDFAFlavour.PDFA_1_B == parsedPdfAFlavour
        || PDFAFlavour.PDFA_2_A == parsedPdfAFlavour || PDFAFlavour.PDFA_2_B == parsedPdfAFlavour
        ||PDFAFlavour.PDFA_2_U == parsedPdfAFlavour;
  }
}
