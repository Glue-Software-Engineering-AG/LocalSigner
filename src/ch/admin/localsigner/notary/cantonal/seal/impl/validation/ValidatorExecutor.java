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
package ch.admin.localsigner.notary.cantonal.seal.impl.validation;

import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.YesNoDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.localsigner.cantonal.seal.Validator;
import ch.glue.localsigner.cantonal.seal.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Executes the validators defined by CantonalSealPlugin.getValidators().
 *
 * @author ag
 */
public class ValidatorExecutor
{
  private final static Logger LOGGER = Logger.getLogger(ValidatorExecutor.class);

  private List<Validator> validators = new ArrayList<Validator>();

  public ValidatorExecutor(List<Validator> validators)
  {
    this.validators = validators;
  }

  public boolean executeValidators(final byte[] pdfData)
      throws ValidationException
  {

    for (Validator validator : validators)
    {
      Validator.Result result = validator.validate(pdfData);

      if (result.isQuestion() )
      {
        if (askUserToCancel(result))
        {
          LOGGER.info("Validator " + validator.getClass().getSimpleName()
              + " failed with result 'question'. User decided to cancel.");
          return false;
        }

        LOGGER.info("Validator " + validator.getClass().getSimpleName()
            + " failed with result 'question'. User decided to proceed.");

      } else if (result.isError())
      {
        informUserAboutError(result);
        LOGGER.info("Validator "+validator.getClass().getSimpleName()+" failed with result 'error'. Cancel process");

        return false;
      }
    }

    return true;
  }

  /* to be able to override in headless unit test */
  protected boolean askUserToCancel(Validator.Result result)
  {
    YesNoDialog qDialog = new YesNoDialog(LocalSigner.mainGui.getMainshell(),
        LocalSigner.i18n("question.ValidatorExecutor.title"), LocalSigner.i18n(result.getI18nMessage()));

    return !qDialog.isUserDecision();
  }

  /* to be able to override in headless unit test */
  protected void informUserAboutError(Validator.Result result)
  {
    Message.error(LocalSigner.mainGui.getMainshell(), LocalSigner.i18n(result.getI18nMessage()));
  }
}
