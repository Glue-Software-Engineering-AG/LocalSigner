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

/**
 * Representation of a validaton result
 *
 * @author greiler
 */

public class ValidationStatus
{
  public enum STATE {
    SUCCESS,
    FAIL_ERROR,
    FAIL_QUESTION;
  }

  private STATE state;
  private String i18nKey;

  public ValidationStatus(STATE state, String i18nKey)
  {
    this.state = state;
    this.i18nKey = i18nKey;
  }

  public STATE getState()
  {
    return state;
  }

  public String getI18nKey()
  {
    return i18nKey;
  }
}
