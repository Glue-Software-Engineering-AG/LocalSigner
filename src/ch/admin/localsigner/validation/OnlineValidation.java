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

import static ch.admin.localsigner.validation.OnlineValidator.UPREG_FORMULAR_MANDANT;

import ch.admin.localsigner.config.resources.SecurityResources;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.SignatureInfo;
import ch.admin.suis.client.core.service.RestServiceClient;
import ch.admin.suis.client.core.service.ServiceConfig;
import ch.admin.suis.client.core.service.to.StreamRequest;
import ch.admin.suis.client.core.service.to.ValidationResponseV2;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * Checks a signature on the signature validator using the REST client.
 */
public class OnlineValidation
{

  public static final String EMPTY_TENANT_MESSAGE = "Cannot validate signature with empty tenant.";

  public static final String EMPTY_URL_MESSAGE = "Cannot validate with empty validator url.";

  private final static Logger LOGGER = Logger.getLogger(OnlineValidation.class);

  /**
   * Perform a discrete "document" validation with all signatures if possible.
   * There is no validator tenant accepting upreg formular signatures followed
   * by qualified signatures, the former is handled by single signature validation.
   *
   * Currently supported tenants (mandants) are "upreg-fn", "upreg-formular" or
   * "Qualified" / "FullQualified" defined by the default configuration tenant.
   *
   * @param pdfAnalyzer
   * @param mainMandant
   * @return ValidationResponseV2 validation result
   * @throws NoSuchAlgorithmException
   * @throws IOException
   * @throws UniformInterfaceException
   */
  public static ValidationResponseV2 validateSignatures(PdfAnalyzer pdfAnalyzer,
      String mainMandant) throws NoSuchAlgorithmException, IOException, UniformInterfaceException
  {

    LOGGER.info("Online validate signatures");

    ValidationResponseV2 allResults;

    if (UPREG_FORMULAR_MANDANT.equals(mainMandant))
    {
      allResults = validateSingleRequestSignatures(pdfAnalyzer.getFileContent(), pdfAnalyzer.getSignatures());

    } else
    {
      allResults = validateSignatures(pdfAnalyzer.getFileContent(), mainMandant);
    }

    return allResults;
  }

  /**
   * Perform a discrete document validation against the given mainMandant.
   *
   * @param revisionPdfBytes
   * @param mainMandant
   * @return ValidationResponseV2 validation result
   * @throws NoSuchAlgorithmException
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static ValidationResponseV2 validateSignatures(byte[] revisionPdfBytes,
      String mainMandant) throws NoSuchAlgorithmException, FileNotFoundException, IOException
  {
    StreamRequest streamRequest = new StreamRequest(new ByteArrayInputStream(revisionPdfBytes), mainMandant, "fileName");

    checkNonEmptyMandant(mainMandant);
    RestServiceClient client = createValidationServiceClient();

    return client.validateOneRequestS(
      Collections.singletonList(streamRequest), false, "doc", null, "de", null);

  }

  protected static ValidationResponseV2 validateSingleRequestSignatures(
      byte[] revisionPdfBytes, Map<Integer, SignatureInfo> signatureEntries)
      throws NoSuchAlgorithmException, IOException
  {
    Iterator<Integer> orderedSignatureKeys = new TreeSet<Integer>(signatureEntries.keySet()).iterator();

    RestServiceClient client = createValidationServiceClient();

    SignatureInfo firstFormularInfoValue = signatureEntries.get(orderedSignatureKeys.next());
    ValidationResponseV2 allResults = client.validateOneSignature(
        revisionPdfBytes, UPREG_FORMULAR_MANDANT, false, firstFormularInfoValue.getName(),
        "doc", null, null, "de", null);

    while (orderedSignatureKeys.hasNext())
    {
      SignatureInfo currentSignatureInfoValue = signatureEntries.get(orderedSignatureKeys.next());

      String qualifiedMandant = LocalSigner.appConfig.getDefaultTenant();
      checkNonEmptyMandant(qualifiedMandant);
      ValidationResponseV2 tempResult = client.validateOneSignature(
          revisionPdfBytes, qualifiedMandant, false, currentSignatureInfoValue.getName(),
          "doc", null, null, "de", null);

      allResults.getFileReports().get(0).getSignatureReports().add(
          tempResult.getFileReports().get(0).getSignatureReports().get(0)
        );
    }

    return allResults;
  }

  protected static RestServiceClient createValidationServiceClient()
  {
    SSLContext httpsSSLContext = SecurityResources.createHttpsSSLContext(null);

    String url = LocalSigner.appConfig.getValidatorUrl();
    ServiceConfig serviceConfig = new ServiceConfig(url);
    serviceConfig.setBasicAuthUser(LocalSigner.appConfig.getValidatorUser());
    serviceConfig.setBasicAuthPassword(LocalSigner.appConfig.getValidatorPassword());
    serviceConfig.setSslContext(httpsSSLContext);

    return new RestServiceClient(serviceConfig);
  }

  protected static void checkNonEmptyMandant(String mandant)
  {
    if (StringUtils.isEmpty(mandant))
    {
      LOGGER.warn(EMPTY_TENANT_MESSAGE);
      throw new IllegalStateException(EMPTY_TENANT_MESSAGE);
    }
  }

  protected static void checkNonEmptyUrl(String url)
  {
    if (StringUtils.isEmpty(url))
    {
      LOGGER.warn(EMPTY_URL_MESSAGE);
      throw new IllegalStateException(EMPTY_URL_MESSAGE);
    }
  }
}
