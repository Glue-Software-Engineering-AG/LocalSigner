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
package ch.admin.localsigner.notary.cantonal.seal.info.client;

import ch.glue.localsigner.cantonal.seal.exception.CantonalSealException;
import java.io.Serializable;

/**
 *
 * @author greiler
 */
public class CantonalSealInfoException extends CantonalSealException
{
  public enum CantonalSealInfoErrorCode implements Serializable {
    ERR_URL_TO_CONFIGURATION_NOT_VALID(701,
        "error.CantonalSealInfoException.ERR_URL_TO_CONFIGURATION_NOT_VALID.701"),

    ERR_XML_CONFIGURATION_NOT_VALID(702,
        "error.CantonalSealInfoException.ERR_XML_CONFIGURATION_NOT_VALID.702"),

    ERR_XML_CONFIGURATION_NOT_LOADABLE(703,
        "error.CantonalSealInfoException.ERR_XML_CONFIGURATION_NOT_LOADABLE.703"),

    ERR_XML_CONFIGURATION_GET_HTTP_ERROR(704,
        "error.CantonalSealInfoException.ERR_XML_CONFIGURATION_GET_HTTP_ERROR.704"),

    ERR_XML_CONFIGURATION_HEAD_HTTP_ERROR(705,
        "error.CantonalSealInfoException.ERR_XML_CONFIGURATION_HEAD_HTTP_ERROR.705"),

    ERR_XML_CONFIGURATION_NOT_WRITABLE(706,
        "error.CantonalSealInfoException.ERR_XML_CONFIGURATION_NOT_WRITABLE.706");

    private final int code;
    private final String i18nKey;

    private CantonalSealInfoErrorCode(int code, String i18nKey)
    {
      this.code = code;
      this.i18nKey = i18nKey;
    }

    public int getCode()
    {
      return code;
    }

    public String getI18nKey()
    {
      return i18nKey;
    }
  }

  public CantonalSealInfoException(CantonalSealInfoErrorCode code, String message)
  {
    super(code.getI18nKey(), message);
  }

  public CantonalSealInfoException(CantonalSealInfoErrorCode code, String message, Throwable cause)
  {
    super(code.getI18nKey(), message, cause);
  }
}
