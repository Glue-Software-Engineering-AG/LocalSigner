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
package ch.admin.localsigner.notary.validation;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import ch.admin.bj.upreg.fn.client.core.validation.Validatable;
import ch.admin.localsigner.main.LocalSigner;

public class HasOneOrMoreSignaturesCheck implements Validatable
{

  private static final Logger LOGGER = Logger.getLogger(HasOneOrMoreSignaturesCheck.class);

  @Override
  public boolean validate(byte[] pdfData)
  {
    try (PdfReader reader = new PdfReader(pdfData))
    {
      AcroFields af = reader.getAcroFields();
      @SuppressWarnings("unchecked")
      ArrayList<String> sigNames = af.getSignatureNames();
      return sigNames.size() >= 1;
    } catch (IOException e)
    {
      LOGGER.warn("Could not read file", e);
      throw new UnhandledException(e);
    }
  }

  @Override
  public boolean stopOnNegative()
  {
    return true;
  }

  @Override
  public String getUserMessage()
  {
    return LocalSigner.i18n("notarySign.notOneSignature");
  }

}
