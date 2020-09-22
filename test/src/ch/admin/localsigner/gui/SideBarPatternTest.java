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
package ch.admin.localsigner.gui;

import static ch.admin.localsigner.validation.OnlineValidator.UPREG_FN_MANDANT;
import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.LinkedList;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Several tests to check correct behavior of "upreg-fn" tenant indication from subject CN.
 *
 * @author bloesch
 */
@Ignore // weil das auf Windows nicht funktioniert!
public class SideBarPatternTest
{

  @BeforeClass
  public static void setupConfiguration() throws ConfigurationException, IOException
  {
    LocalSigner.appConfig = new ApplicationConfiguration();
  }

  @Test
  public void checkMissingSpaceNoUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "SwissRegister of Notaries", new LinkedList<String>());

    assertEquals(LocalSigner.appConfig.getDefaultTenant(), actualTenant);
  }

  @Test
  public void checkTypoNoUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "Swiss Regster of Notaries", new LinkedList<String>());

    assertEquals(LocalSigner.appConfig.getDefaultTenant(), actualTenant);
  }

  @Test
  public void checkMinimialUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "Swiss Register of Notaries", new LinkedList<String>());

    assertEquals(UPREG_FN_MANDANT, actualTenant);
  }

  @Test
  public void checkMultiSpaceUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "  Swiss   Register   of   Notaries  ", new LinkedList<String>());

    assertEquals(UPREG_FN_MANDANT, actualTenant);
  }

  @Test
  public void checkLawBaseEqualUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "Swiss Confederation - Swiss Register of Notaries", new LinkedList<String>());

    assertEquals(UPREG_FN_MANDANT, actualTenant);
  }

  @Test
  public void checkAdditionalCharactersUpregFNTenant()
  {
    String actualTenant =
        SideBar.getActualTenantForIssuerDNOrSubjectCN("", "Swiss - Swiss Register of Notaries - Confederation", new LinkedList<String>());

    assertEquals(UPREG_FN_MANDANT, actualTenant);
  }

}
