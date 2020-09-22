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
package ch.admin.localsigner.config;

import static org.junit.Assert.*;
import java.io.File;
import org.junit.Test;

/**
 * Test class for ZulabConfiguration
 *
 * @author greiler
 * @author $Author$
 * @version $Revision$
 */
public class ZulabConfigurationTest
{

  public ZulabConfigurationTest()
  {
  }

  @Test
  public void testWellFunction() throws Exception
  {
    File testConfiguration = new File("test" + File.separator + "zulabtest.xml");

    ZulabConfiguration zbconf = new ZulabConfiguration(testConfiguration);

    // test version
    assertEquals("2016-10-10T18:23:51.0", zbconf.getVersion());

    // test cantons
    assertEquals(26, zbconf.getCantons().size());
    assertEquals("ZH", zbconf.getCantons().get("ZH").getValue());
    assertEquals("Zürich", zbconf.getCantons().get("ZH").getTranslationDe());
    assertEquals("Zurich", zbconf.getCantons().get("ZH").getTranslationFr());
    assertEquals("Zurigo", zbconf.getCantons().get("ZH").getTranslationIt());

    assertEquals("BS", zbconf.getCantons().get("BS").getValue());
    assertEquals("Basel-Stadt", zbconf.getCantons().get("BS").getTranslationDe());
    assertEquals("Bâle-Ville", zbconf.getCantons().get("BS").getTranslationFr());
    assertEquals("Basilea Città", zbconf.getCantons().get("BS").getTranslationIt());

    assertEquals("JU", zbconf.getCantons().get("JU").getValue());
    assertEquals("Jura", zbconf.getCantons().get("JU").getTranslationDe());
    assertEquals("Jura", zbconf.getCantons().get("JU").getTranslationFr());
    assertEquals("Giura", zbconf.getCantons().get("JU").getTranslationIt());

    // test domains
    assertEquals(2, zbconf.getDomains().size());

    assertEquals("upreg", zbconf.getDomains().get("upreg").getValue());
    assertEquals("Register der Urkundspersonen", zbconf.getDomains().get("upreg").getTranslationDe());
    assertEquals("Registre des officiers publics", zbconf.getDomains().get("upreg").getTranslationFr());
    assertEquals("Registro dei pubblici ufficiali rogatori", zbconf.getDomains().get("upreg").getTranslationIt());

    assertEquals("hreg", zbconf.getDomains().get("hreg").getValue());
    assertEquals("HReg.ch/de", zbconf.getDomains().get("hreg").getTranslationDe());
    assertEquals("HReg.ch/fr", zbconf.getDomains().get("hreg").getTranslationFr());
    assertEquals("HReg.ch/it", zbconf.getDomains().get("hreg").getTranslationIt());
  }
}
