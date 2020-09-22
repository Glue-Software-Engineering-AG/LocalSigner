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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.resources.SecurityResources;
import ch.admin.localsigner.main.LocalSigner;

/**
 *
 * @author greiler
 */
public class CantonalSealInfoWsClient
{
  private static final Logger LOGGER = Logger.getLogger(CantonalSealInfoWsClient.class);

  public static final String LAST_MODIFIED_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

  private static final String LAST_MODIFIED_HEADER = "Last-Modified";

  private static final int HTTP_OK = 200;

  ApplicationConfiguration config = null;

  public CantonalSealInfoWsClient(ApplicationConfiguration config)
  {
    this.config = config;
  }

  public boolean isUpdateRequired() throws CantonalSealInfoException
  {
    Date currentVersionOnServer = getHttpLastModifiedHeader();

    boolean updateRequired = currentVersionOnServer.after(getLastXmlLoadDate());

    if (LocalSigner.isInDebugMode())
    {
      LOGGER.debug("Update of cantonal seal configuration is " + (updateRequired ? "" : "not ") + "required.\n"
          + "    Installed version from " + getLastXmlLoadDate() + "\n" + "    Available version is "
          + currentVersionOnServer);
    }

    return updateRequired;
  }

  /**
   * Loads the EndPoints-XML from the addresse configured at
   * cantonalSealUpdateUrl. The HEAD-Request has to be done using
   * #isUpdateRequired.
   * 
   * @return The EndPoints represented in the XML.
   * @throws CantonalSealInfoException
   *           if something went wrong
   */
  public byte[] loadEndpoints() throws CantonalSealInfoException
  {
    try
    {
      URL url = new URL(config.getCantonalSealUpdateUrl());
      return loadXml(url);
    } catch (MalformedURLException e)
    {
      String msg = "Configured URL to get cantonal seal plugin information is malformed! See configured value at "
          + Config.CANTONAL_SEAL_UPDATE_URL + ", current value is " + config.getCantonalSealUpdateUrl();
      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_URL_TO_CONFIGURATION_NOT_VALID, msg, e);
    } catch (CantonalSealInfoException e)
    {
      throw e; // simply rethrow
    } catch (Exception e)
    {
      String msg = "There is a problem with the XML from " + config.getCantonalSealUpdateUrl();

      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_VALID, msg, e);
    }
  }

  protected byte[] loadXml(URL url) throws CantonalSealInfoException
  {
    try
    {
      CloseableHttpClient httpclient = createHttpClient();
      HttpGet httpGet = new HttpGet(url.toURI());
      CloseableHttpResponse response = httpclient.execute(httpGet);

      try
      {
        validateStatusCode200(response, url);

        HttpEntity entity = response.getEntity();
        return IOUtils.toByteArray(entity.getContent());
      } finally
      {
        safeClose(response);
      }
    } catch (CantonalSealInfoException e)
    {
      throw e;
    } catch (Exception e)
    {
      String msg = "Problem while requesting ressource " + url.toString() + " over GET";
      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_LOADABLE, msg, e);
    }
  }

  /**
   * Convenience method to get the time stamp of the last xml update from the
   * users config.
   * 
   * @return the date of the last update of the cantonal seal endpoints xml
   */
  protected Date getLastXmlLoadDate()
  {
    return config.getCantonalSealLastUpdateTimestamp();
  }

  protected URL getXmlLoadURL() throws CantonalSealInfoException
  {
    try
    {
      return new URL(config.getCantonalSealUpdateUrl());
    } catch (MalformedURLException ex)
    {
      String msg = "URL to update the cantonal seal XML is malformed. See value of configuration (system and user "
          + "config) for key " + Config.CANTONAL_SEAL_UPDATE_URL + ". Current value is: "
          + config.getCantonalSealUpdateUrl();

      LOGGER.error(msg);

      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_URL_TO_CONFIGURATION_NOT_VALID, msg, ex);
    }
  }

  /**
   * Throws an excpetion if the HTTP status is not 200
   * 
   * @param response
   *          the response to check the HTTP status of
   * @param url
   *          for logging purposes only
   * @throws CantonalSealInfoException
   */
  protected void validateStatusCode200(CloseableHttpResponse response, URL url) throws CantonalSealInfoException
  {
    StatusLine statusLine = response.getStatusLine();
    int status = statusLine.getStatusCode();
    if (status != HTTP_OK)
    {
      String msg = "Status " + status + " returned for GET by " + url.toString();
      LOGGER.warn(msg);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_GET_HTTP_ERROR, msg);
    }
  }

  /**
   * Gets the last-modified date (header information) of the resource at the
   * given URL.
   *
   * @return the date represented by the last-modified attribute of the header.
   *         If the header is not present, 'null' gets returned.
   * @throws CantonalSealInfoException
   *           if something went wrong
   */
  public Date getHttpLastModifiedHeader() throws CantonalSealInfoException
  {
    try
    {
      URL url = getXmlLoadURL();
      HttpHead httphead = new HttpHead(url.toURI());

      CloseableHttpClient httpclient = createHttpClient();

      CloseableHttpResponse response = httpclient.execute(httphead);

      try
      {
        validateStatusCode200(response, url);

        return extractLastModified(response, url);
      } finally
      {
        safeClose(response);
      }
    } catch (CantonalSealInfoException statusNot200)
    {
      throw statusNot200;
    } catch (Exception e)
    {
      String msg = "Problem while requesting HTTP HEAD of cantonal seal XML";
      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_HEAD_HTTP_ERROR, msg, e);
    }
  }

  /**
   * Creates a closeable http client with the httpTrustStore.jks (in
   * LocalSigner.jar) as its trust store.
   */
  protected CloseableHttpClient createHttpClient()
      throws CertificateException, IOException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException
  {
    SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(SecurityResources.createSSLFactory(),
        new HostnameVerifier()
        { // no special hostname verification
          @Override
          public boolean verify(String hostname, SSLSession ssls)
          {
            return true;
          };
        });

    HttpClientBuilder builder = HttpClients.custom();

    CloseableHttpClient httpclient = builder.setSSLSocketFactory(factory).useSystemProperties().build();

    return httpclient;
  }

  /**
   * Returns the Date representetd by the Last-Modified header attribute. Or
   * null if not present.
   * 
   * @param response
   *          The response to extract the header attribute from
   * @param url
   *          For loggin purposes only
   * @return The date parsed from the header attribute or null if not present
   * @throws java.text.ParseException
   *           if the date could not be parsed
   */
  protected Date extractLastModified(CloseableHttpResponse response, URL url) throws ParseException
  {
    Header[] headers = response.getAllHeaders();
    String lastModifiedValue;
    for (Header header : headers)
    {
      if (LAST_MODIFIED_HEADER.equals(header.getName()))
      {
        lastModifiedValue = header.getValue();

        if (lastModifiedValue != null)
        {
          SimpleDateFormat format = new SimpleDateFormat(LAST_MODIFIED_FORMAT, Locale.US);
          Date d = format.parse(lastModifiedValue);

          LOGGER.info("last-modified date of " + url.toString() + " is " + lastModifiedValue);

          return d;
        }
      }
    }

    LOGGER.warn("Header 'Last-Modified' is not present for resource at " + url.toString());
    return null;
  }

  protected void safeClose(CloseableHttpResponse response)
  {
    if (response != null)
    {
      try
      {
        response.close();
      } catch (Exception ex)
      {
        // What a pity!
      }
    }
  }

}
