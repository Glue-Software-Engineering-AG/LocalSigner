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
package ch.admin.localsigner.notary.update;

import ch.admin.localsigner.config.ZulabConfiguration;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import junit.framework.TestCase;
import org.apache.commons.configuration.ConfigurationException;

/**
 *
 * @author greiler
 */
public class CantonAndDomainListUpdaterTest extends TestCase
{

  public CantonAndDomainListUpdaterTest(String testName)
  {
    super(testName);
  }

  public void testIsUpdateAvailable() throws ConfigurationException, UnsupportedEncodingException
  {
    // by date
    assertTrue(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2016-05-02T09:47:00.000+01:00"), "2016-04-05T18:23:51.000+01:00"));

    assertFalse(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2014-10-12T10:18:00.000+01:00"), "2016-09-11T18:23:51.000+01:00"));

    // by time
    assertTrue(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2016-09-11T18:23:52.000+01:00"), "2016-09-11T18:23:51.000+01:00"));

    assertFalse(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2016-09-11T18:23:50.000+01:00"), "2016-09-11T18:23:51.000+01:00"));

    // exact same
    assertFalse(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2016-09-11T18:23:51.000+01:00"), "2016-09-11T18:23:51.000+01:00"));

    // no list installed yet
    assertTrue(new CantonAndDomainListUpdater(null, null)
        .isUpdateAvailable(getZulabConfigXml("2016-09-11T18:23:51.000+01:00"),
            ZulabConfiguration.VERSION_NOT_AVAILABLE));
  }

  private ZulabConfiguration getZulabConfigXml(String date) throws ConfigurationException, UnsupportedEncodingException
  {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<config version=\""+ date +"\" xmlns=\"http://www.glue.ch/localsigner/zulabconfiguration\">\n" +
        "    <comment>Demokommentar</comment>\n" +
        "    <cantons>\n" +
        "        <canton>\n" +
        "            <value>CH</value>\n" +
        "            <translations>\n" +
        "                <german>Confoederatio Helvetica</german>\n" +
        "                <french>Confoederatio Helvetica</french>\n" +
        "                <italian>Confoederatio Helvetica</italian>\n" +
        "            </translations>\n" +
        "        </canton>\n" +
        "    </cantons>\n" +
        "    <domains>\n" +
        "        <domain>\n" +
        "            <value>Test</value>\n" +
        "            <translations>\n" +
        "                <german>Tester/in</german>\n" +
        "                <french>Testeur/euse</french>\n" +
        "                <italian>Testinese/O_o</italian>\n" +
        "            </translations>\n" +
        "        </domain>\n" +
        "</domains>\n" +
        "</config>";

    return new ZulabConfiguration(new ByteArrayInputStream(xml.getBytes("UTF-8")));
  }

  public void testHumanReadableDate_Date()
  {
    String actualDe = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate("2016-09-11T18:23:51.0", Locale.GERMAN);
    String actualFr = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate("2016-09-11T18:23:51.0", Locale.FRENCH);
    String actualIt = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate("2016-09-11T18:23:51.0", Locale.ITALIAN);

    assertEquals("Date not correctly parsed or formatted in DE", "11.09.2016", actualDe);
    assertEquals("Date not correctly parsed or formatted in FR", "11 sept. 2016", actualFr);
    assertEquals("Date not correctly parsed or formatted in IT", "11-set-2016", actualIt);

    String actualEn = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate("2016-09-11T18:23:51.0", Locale.ENGLISH);
    assertEquals("Date not correctly parsed or formatted in IT", "Sep 11, 2016", actualEn);
  }

  public void testHumanReadableDate_NotAvailable()
  {
    String actualDe = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate(ZulabConfiguration.VERSION_NOT_AVAILABLE, Locale.GERMAN);
    String actualFr = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate(ZulabConfiguration.VERSION_NOT_AVAILABLE, Locale.FRENCH);
    String actualIt = new CantonAndDomainListUpdater(null, null)
        .humanReadableDateFromVersionDate(ZulabConfiguration.VERSION_NOT_AVAILABLE, Locale.ITALIAN);

    assertEquals("Date not correctly treated if not installed yet in DE", "n\\a", actualDe);
    assertEquals("Date not correctly treated if not installed yet in FR", actualDe, actualFr);
    assertEquals("Date not correctly treated if not installed yet in IT", actualFr, actualIt);
  }
}
