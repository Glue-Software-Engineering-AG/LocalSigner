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

import ch.admin.bj.upreg.fn.client.core.validation.Validatable;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.validation.OnlineValidation;
import ch.admin.suis.client.core.service.to.ValidStatus;
import ch.admin.suis.client.core.service.to.ValidationResponseV2;
import org.apache.log4j.Logger;

/**
 * Prüft mit validator.ch ob alle Signaturen voll qualifiziert sind.
 *
 * @author beat.weisskopf@glue.ch
 * @author markus.bloesch@glue.ch
 */
public class AreAllSignaturesFullQualifiedCheck implements Validatable
{
  private final static Logger LOGGER = Logger.getLogger(AreAllSignaturesFullQualifiedCheck.class);

  @Override
  public boolean validate(byte[] pdfData)
  {
    return allSignaturesFullQuallified(pdfData);
  }

  private boolean allSignaturesFullQuallified(byte[] pdfData)
  {
    try
    {
      final ValidationResponseV2 results =
          OnlineValidation.validateSignatures(pdfData, LocalSigner.appConfig.getDefaultTenant());

      return results.isValid() == ValidStatus.VALID;
    } catch (Exception ex)
    {
      LOGGER.warn("Could not read file or connect to discrete validator", ex);
      return false;
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
    return LocalSigner.i18n("notarySign.notFullQualifiedSignature");
  }

}
