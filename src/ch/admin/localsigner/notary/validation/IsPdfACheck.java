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
import org.apache.log4j.Logger;
import ch.admin.bj.upreg.fn.client.core.validation.Validatable;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.validation.PdfAnalyzer;

public class IsPdfACheck implements Validatable
{
  private static final Logger LOGGER = Logger.getLogger(IsPdfACheck.class);

  @Override
  public boolean stopOnNegative()
  {
    return false;
  }

  @Override
  public boolean validate(byte[] bytes)
  {
    try
    {
      PdfAnalyzer analyzer = new PdfAnalyzer(bytes);
      analyzer.validatePdfA();
      return analyzer.getValidationResults().isSupportedPdfA() && !analyzer.getValidationResults().isError();
    } catch (IOException ioe)
    {
      LOGGER.warn("not able to validate file for PDF/A-Conformance.", ioe);

      return false;
    }
  }

  @Override
  public String getUserMessage()
  {
    return LocalSigner.i18n("notarySign.noPdfA");
  }


}
