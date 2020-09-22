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
import ch.admin.localsigner.utils.OnlineServices;

public class HasNetworkConnectionCheck implements Validatable
{

  @Override
  public boolean validate(byte[] pdfData)
  {
    return new OnlineServices(LocalSigner.appConfig).isNotaryServiceUp();
  }

  @Override
  public boolean stopOnNegative()
  {
    return true;
  }

  @Override
  public String getUserMessage()
  {
    return LocalSigner.i18n("notarySign.noNetworkConnection");
  }

}
