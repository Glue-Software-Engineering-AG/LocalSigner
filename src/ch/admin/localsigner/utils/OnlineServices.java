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
package ch.admin.localsigner.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.config.resources.SecurityResources;
import java.net.MalformedURLException;

/**
 * Tests if online services are available.
 *
 * @author wampfler
 * @author $Author$
 * @version $Revision$
 */
public class OnlineServices
{
  private final static int DEFAULT_READ_TIMEOUT = 10000;
  private final static int DEFAULT_CONNECTION_TIMEOUT = 10000;
  private final static boolean FOLLOW_REDIRECT = true;

  private final static Logger LOGGER = Logger.getLogger(OnlineServices.class);

  private final ApplicationConfiguration config;

  public OnlineServices(ApplicationConfiguration config)
  {
    this.config = config;

  }

  /**
   * tests if the URL is reachable.
   */
  private boolean exists(String urlToCheck)
  {
    try
    {
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection con = getConfiguredSslConnection(urlToCheck);
      con.setRequestMethod("HEAD");
      con.connect();
      LOGGER.debug("The connect was successful for the url: " + urlToCheck);
      return true;
    } catch (Exception e)
    {
      LOGGER.info("This URL is not accessible: " + urlToCheck, e);
      return false;
    }
  }

  /**
   * Returns a connection to the given URL using the internal truststore. Uses a DEFAULT_READ_TIMEOUT and
   * DEFAULT_CONNECTION_TIMEOUT as defined as class variables.
   * @param url URL to connect to
   * @return a configured HttpURLConnection
   * @throws MalformedURLException
   * @throws IOException
   */
  public HttpURLConnection getConfiguredSslConnection(String url)
      throws MalformedURLException, IOException
  {
      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
      con.setInstanceFollowRedirects(FOLLOW_REDIRECT);

      SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
      if (sslSocketFactory != null && con instanceof HttpsURLConnection)
      {
        ((HttpsURLConnection) con).setSSLSocketFactory(sslSocketFactory);
      }
      con.setReadTimeout(DEFAULT_READ_TIMEOUT);
      con.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);

      return con;
  }

  public boolean isUpdateCheckUp()
  {
    return exists(config.getUpdateCheckUrl());
  }

  public boolean isOnlineValidatorUp()
  {
    return exists(config.getValidatorUrl());
  }

  public boolean isNotaryServiceUp()
  {
    return exists(config.getFunktionsnachweisUrl());
  }

  /** may return null */
  private SSLSocketFactory getSSLSocketFactory()
  {
    SSLSocketFactory sslSocketFactory = null;
    try
    {
      sslSocketFactory = SecurityResources.createSSLFactory();
    } catch (Exception ex)
    {
      LOGGER.warn("unable to load truststore", ex);
    }
    return sslSocketFactory;
  }

  /**
   * BIT TSA does not respond to http GET or HEAD request with a good http code,
   * so we need to do a simple POST with dummy data and mime type set.
   *
   * @return true if the TSA at the given URL is responding.
   */
  public static boolean isTSAReady(String tsaUrl)
  {
    try
    {
      String dummyData = "abc";
      URL url = new URL(tsaUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/timestamp-query");
      connection.setRequestProperty("Content-Length", "" + Integer.toString(dummyData.getBytes().length));
      connection.setUseCaches(false);
      connection.setReadTimeout(DEFAULT_READ_TIMEOUT);
      connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
      connection.setInstanceFollowRedirects(FOLLOW_REDIRECT);

      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(dummyData);
      wr.flush();
      wr.close();
      connection.disconnect();

      int respCode = connection.getResponseCode();

      if (respCode != HttpURLConnection.HTTP_OK && respCode != HttpURLConnection.HTTP_BAD_REQUEST)
      { // a BAD REQUEST (400) is also good since we send rubbish and cannot expect the server to respond with OK.
        LOGGER.info("TSA at " + tsaUrl + " is not ready. HTTP-Response code: " + respCode);
        return false;
      } else {
        return true;
      }
    } catch (IOException ioex)
    {
      LOGGER.warn("This TSA is not ready: " + tsaUrl, ioex);
      return false;
    }
  }
}
