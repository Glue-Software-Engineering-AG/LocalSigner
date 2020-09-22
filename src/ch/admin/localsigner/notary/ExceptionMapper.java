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
package ch.admin.localsigner.notary;

import ch.admin.bj.upreg.fn.client.core.exception.ClientErrorCode;
import ch.admin.localsigner.main.LocalSigner;
import ch.zulab.proof.transfer.to.ErrorCode;

public class ExceptionMapper
{
    private static final String WS_PREFIX = "fn.ws.error.";
    private static final String CLIENT_PREFIX = "fn.client.error.";


  public static String convertCodeToMsg(ErrorCode code)
  {

    switch (code)
    {
      case ERR_FN_PERMISSION_DENIED:
        return LocalSigner.i18n(WS_PREFIX + "permission_denied");
      case ERR_FN_MANY_PERSON_FOR_CERTIFICATE:
        return LocalSigner.i18n(WS_PREFIX + "many_person_for_certificate");
      case ERR_FN_NO_PERSON_WITH_THIS_CERTIFICATE:
        return LocalSigner.i18n(WS_PREFIX + "no_person_with_this_certificate");
      case ERR_FN_NO_RELEVANT_RELATIONSHIPS_ACTIVE:
        return LocalSigner.i18n(WS_PREFIX + "no_relevant_relationships_active");
      case ERR_FN_PDF_SIGN_CERT_NOT_KNOWN_FOR_USER:
        return LocalSigner.i18n(WS_PREFIX + "pdf_sign_cert_not_known_for_user");
      case ERR_INT_UNKOWN:
        return LocalSigner.i18n(WS_PREFIX + "internal_error");
      case ERR_INVALID_DATA:
        return LocalSigner.i18n(WS_PREFIX + "invalid_data");
      case ERR_INVALID_HASH:
        return LocalSigner.i18n(WS_PREFIX + "invalid_hash");
      case ERR_SIGN_CERT_NOT_FOUND:
        return LocalSigner.i18n(WS_PREFIX + "sign_cert_not_found");
      case ERR_TIMEOUT_BETWEEN_RT1_RT2:
        return LocalSigner.i18n(WS_PREFIX + "timeout_between_r1_r2");
      case ERR_UNKOWN_UUID_AT_RT2:
        return LocalSigner.i18n(WS_PREFIX + "unknown_uuid_at_rt2");
      case ERR_INVALID_REVISION_NUMBER:
        return LocalSigner.i18n(WS_PREFIX + "invalid_revision_number");
      case ERR_MISSING_REQUIRED_PARAM:
        return LocalSigner.i18n(WS_PREFIX + "missing_required_param");
      case ERR_MISSING_RT1_CALL_BEFORE_RT2:
        return LocalSigner.i18n(WS_PREFIX + "missing_rt1_call_before_rt2");
      case ERR_SIGCERT_AUTHCERT_MISMATCH:
        return LocalSigner.i18n(WS_PREFIX + "sigcert_authcert_mismatch");
      case ERR_FN_DISCRETE_VALIDATOR_NOT_VALID:
        return LocalSigner.i18n(WS_PREFIX + "err_fn_discrete_validator_not_valid");
      case ERR_SIGDATE_BEFORE_REGISTER_ACTIVATION:
        return LocalSigner.i18n(WS_PREFIX + "err_sigdate_before_register_activation");
      case ERR_INT_SERVICE_CALL_FAILED:
        return LocalSigner.i18n(WS_PREFIX + "err_int_service_call_failed");
      case ERR_INT_SERVER_DOWN:
        return LocalSigner.i18n(WS_PREFIX + "err_int_server_down");

      default:
        return LocalSigner.i18n(WS_PREFIX + "unexpected");
    }
  }

  public static String convertCodeToMsg(ClientErrorCode code)
  {
    switch (code)
    {
      case ERR_INT_SERVICE_CALL_FAILED:
        return LocalSigner.i18n(WS_PREFIX + "err_int_service_call_failed"); // Achtung: Übersetzung von WS-ErrorCodes!
      case ERR_SIGNATURE_POSITION_TOO_FAR_RIGHT:
        return LocalSigner.i18n(CLIENT_PREFIX + "signature_position_too_far_right");
      case ERR_SIGNATURE_POSITION_TOO_HIGH:
        return LocalSigner.i18n(CLIENT_PREFIX + "signature_position_too_high");
      default:
        return LocalSigner.i18n(CLIENT_PREFIX + "unexpected");
    }
  }
}
