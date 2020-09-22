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
import java.io.IOException;
import java.security.Security;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
 * Offline validator with the new vera greenfield validator.
 *
 * @author Marc-André Jungo
 * @author Roland Keller
 */
public class OfflinePdfValidator
{
  private static final Logger LOGGER = Logger.getLogger(OfflinePdfValidator.class);

  private PdfAValidationResults results = new PdfAValidationResults();

  private final byte[] fileBytes;

  public OfflinePdfValidator(final byte[] fileBytes) throws IOException
  {
    Security.addProvider(new BouncyCastleProvider());

    VeraGreenfieldFoundryProvider.initialise();

    this.fileBytes = fileBytes;
  }

  /**
   * Start the VeraPdf-Validation and set the validationResult, the
   * PdfA-Flavour, the error boolean and the supportedPdfA boolean in the
   * ValidationResults (only validate the file, if the PdfA-Flavour is
   * supported, otherwise its only set the booleans for error and supportedPdfa
   * to false). If the PdfA-Flavour is supported the supportedPdfA boolean is
   * true. If a error is happen during validation the error boolean is true.
   */
  protected synchronized void validatePdfFile()
  {

    try (PDFAParser parser = Foundries.defaultInstance().createParser(new ByteArrayInputStream(fileBytes)))
    {
      results.setParsedPdfAFlavour(parser.getFlavour());
      LOGGER.debug("Parser parsed PdfAFlavour: " + parser.getFlavour());
      if (!results.claimsToBeASupportedPdfA())
      {
        return;
      }

      PDFAValidator validator = Foundries.defaultInstance().createFailFastValidator(parser.getFlavour(), 20);
      ValidationResult vr = validator.validate(parser);
      if (!vr.isCompliant())
      {
        if (vr.getPDFAFlavour() == PDFAFlavour.PDFA_2_A)
        {
          LOGGER.debug("We found a PDF/A-2a which does not validate, so we try to validate with PDF/A-2u");
          validator = Foundries.defaultInstance().createFailFastValidator(PDFAFlavour.PDFA_2_U, 20);
          vr = validator.validate(parser);
        }
        if (vr.getPDFAFlavour() == PDFAFlavour.PDFA_1_A)
        {
          LOGGER.debug("We found a PDF/A-1a which does not validate, so we try to validate with PDF/A-1b");
          validator = Foundries.defaultInstance().createFailFastValidator(PDFAFlavour.PDFA_1_B, 20);
          vr = validator.validate(parser);
        }
      }
      results.setVeraPdfValidationResult(vr);

    } catch (ValidationException | ModelParsingException | EncryptedPdfException | IOException ex)
    {
      LOGGER.warn("not able to determine if the file is a PDF/A of any kind.", ex);
      results.markAsInvalid();
    }
  }

  /**
   * Check if the VeraPdf-validationResult is Null, if the
   * VeraPdf-validationResult is Null its start the file validation with
   * VeraPDF.
   *
   * @return the complete Validationresults (List VeraPdf-Validationresult,
   *         boolean isSupportedPdfA, boolean isError, PDFFlavour)
   */
  protected synchronized PdfAValidationResults getValidationResults()
  {
    if (results.validationNeeded())
    {
      validatePdfFile();
    }
    return results;
  }
}
