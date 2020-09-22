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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import ch.admin.localsigner.config.ApplicationConfiguration;

/**
 *
 * @author greiler
 */
public class CantonalSealInfoWsClientTest
{

  private ApplicationConfiguration getConfig() throws Exception
  {
    Calendar cal = Calendar.getInstance();
    cal.set(2017, 0, 13);
    return getConfig(cal.getTime(), "http://www.openegov.ch/ls25/update");
  }

  private ApplicationConfiguration getConfig(final Date date, final String url) throws Exception
  {
    ApplicationConfiguration ac = new ApplicationConfiguration()
    {

      @Override
      public Date getCantonalSealLastUpdateTimestamp()
      {
        return date;
      }

      @Override
      public String getCantonalSealUpdateUrl()
      {
        // this is the update url for the localsigner itself - but it will ever
        // be there!
        return url;
      }

      @Override
      public String getProxyHttpHost()
      {
        return System.getProperty("http.proxyHost");
      }

      @Override
      public String getProxyHttpPort()
      {
        return System.getProperty("http.proxyPort");
      }
    };

    return ac;
  }

  @Test
  public void testIsUpdateRequired() throws Exception
  {
    CantonalSealInfoWsClient instance = new CantonalSealInfoWsClient(getConfig());
    assertTrue(instance.isUpdateRequired());
  }

  @Test
  public void testGetHttpDateHeader() throws Exception
  {
    CantonalSealInfoWsClient instance = new CantonalSealInfoWsClient(getConfig());
    assertNotNull(instance.getHttpLastModifiedHeader());
  }
}
