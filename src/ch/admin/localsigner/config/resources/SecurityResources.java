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
package ch.admin.localsigner.config.resources;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.apache.commons.lang.UnhandledException;

/**
 * Util class to load trust stores.
 *
 * @author greiler
 * @author $Author$
 * @version $Revision$
 * @since 07.01.2014
 */
public class SecurityResources
{

  public static final String TSA_TRUST_STORE = "tsaStore.jks";

  public static final char[] TSA_TRUST_STORE_PW = new char[]
  {
      '1', '2', '3', '4', '5', '6'
  };

  public static final String HTTPS_TRUST_STORE = "/httpsTrustStore.jks";

  public static final char[] HTTPS_TRUST_STORE_PW = new char[]
  {
      '1', '2', '3', '4', '5', '6'
  };

  private static SSLSocketFactory sslSocketFactory = null;

  public static SSLSocketFactory createSSLFactory()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, KeyManagementException
  {
    if (sslSocketFactory == null)
    { // loading it once is enough ;)
      KeyStore httpsTrustStore = KeyStore.getInstance("JKS");
      httpsTrustStore.load(SecurityResources.class.getResourceAsStream(SecurityResources.HTTPS_TRUST_STORE),
          SecurityResources.HTTPS_TRUST_STORE_PW);

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(httpsTrustStore);
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(null, tmf.getTrustManagers(), null);
      SSLSocketFactory sslFactory = ctx.getSocketFactory();

      sslSocketFactory = sslFactory;
    }

    return sslSocketFactory;
  }

  public static SSLContext createHttpsSSLContext(X509KeyManager x509KeyManager)
  {
    try
    {
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
      KeyStore httpsTrustStore = KeyStore.getInstance("JKS");
      httpsTrustStore.load(SecurityResources.class
          .getResourceAsStream(SecurityResources.HTTPS_TRUST_STORE),
          SecurityResources.HTTPS_TRUST_STORE_PW);
      tmf.init(httpsTrustStore);
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(x509KeyManager != null ? new X509KeyManager[] { x509KeyManager } : null, tmf.getTrustManagers(), null);
      return ctx;
    } catch (Exception ex)
    {
      // this setup stuff is all static, if it fails we cant do anything
      throw new UnhandledException(ex);
    }
  }
}
