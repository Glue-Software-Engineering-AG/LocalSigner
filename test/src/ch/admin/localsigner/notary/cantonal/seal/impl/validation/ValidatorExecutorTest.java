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

import ch.glue.localsigner.cantonal.seal.Validator;
import ch.glue.localsigner.cantonal.seal.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author greiler
 */
public class ValidatorExecutorTest extends TestCase
{

  public enum RESULT{
    ERROR_SHOWN, QUESTION_ASKED, NOT_SET;
  }
  public static RESULT result;

  public ValidatorExecutorTest(String testName)
  {
    super(testName);
  }

  @Test
  public void testExecuteValidators_ONE_ERROR() throws Exception
  {
    result = RESULT.NOT_SET;

    ValidatorExecutor instance = loadMocketValidatorExecutor(loadErrorValidators());

    instance.executeValidators(new byte[]{1,2,3,4});

    assertEquals(RESULT.ERROR_SHOWN, result);
  }

  @Test
  public void testExecuteValidators_ONE_QUESTION() throws Exception
  {
    result = RESULT.NOT_SET;

    ValidatorExecutor instance = loadMocketValidatorExecutor(loadQuestionValidators());

    instance.executeValidators(new byte[]{1,2,3,4});

    assertEquals(RESULT.QUESTION_ASKED, result);
  }

  @Test
  public void testExecuteValidators_ALL_OK() throws Exception
  {
    result = RESULT.NOT_SET;

    ValidatorExecutor instance = loadMocketValidatorExecutor(loadSuccessValidators());

    instance.executeValidators(new byte[]{1,2,3,4});

    assertEquals(RESULT.NOT_SET, result);
  }

  private ValidatorExecutor loadMocketValidatorExecutor(List<Validator> validators)
  {
    return new ValidatorExecutor(validators) {
      protected boolean askUserToCancel(Validator.Result result)
      {
        ValidatorExecutorTest.result = RESULT.QUESTION_ASKED;
        return false;
      }

      protected void informUserAboutError(Validator.Result result)
      {
        ValidatorExecutorTest.result = RESULT.ERROR_SHOWN;
      }
    };
  }

  private List<Validator> loadErrorValidators()
  {
    List<Validator> validators = new ArrayList<Validator>(2);

    validators.add(loadOkValidator());
    validators.add(loadErrorValidator());
    validators.add(loadOkValidator());

    return validators;
  }

  private List<Validator> loadQuestionValidators()
  {
    List<Validator> validators = new ArrayList<Validator>(2);

    validators.add(loadOkValidator());
    validators.add(loadNoncriticalValidator());
    validators.add(loadOkValidator());

    return validators;
  }

  private List<Validator> loadSuccessValidators()
  {
    List<Validator> validators = new ArrayList<Validator>(2);

    validators.add(loadOkValidator());
    validators.add(loadOkValidator());
    validators.add(loadOkValidator());

    return validators;
  }

  private Validator loadOkValidator()
  {
    return new Validator()
    {
      @Override
      public Validator.Result validate(byte[] bytes) throws ValidationException
      {
        return new Result("all.OK", Validator.Result.TYPE.SUCCESS);
      }
    };
  }
  private Validator loadErrorValidator()
  {
    return new Validator()
    {
      @Override
      public Validator.Result validate(byte[] bytes) throws ValidationException
      {
        return new Result("all.Failed", Validator.Result.TYPE.ERROR);
      }
    };
  }
  private Validator loadNoncriticalValidator()
  {
    return new Validator()
    {
      @Override
      public Validator.Result validate(byte[] bytes) throws ValidationException
      {
        return new Result("all.NotThatBadButNotOk", Validator.Result.TYPE.QUESTION);
      }
    };
  }
}
