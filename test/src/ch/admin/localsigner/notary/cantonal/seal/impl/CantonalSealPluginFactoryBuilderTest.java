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
package ch.admin.localsigner.notary.cantonal.seal.impl;

import ch.glue.localsigner.cantonal.seal.factory.PluginInstantiationException;
import ch.glue.localsigner.cantonal.seal.factory.AbstractCantonalSealPluginFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.junit.Test;
import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;
import ch.glue.localsigner.cantonal.seal.impl.sdms.SdmsSealPluginFactory;
import junit.framework.TestCase;

/**
 *
 * @author greiler
 */
public class CantonalSealPluginFactoryBuilderTest extends TestCase
{
  private static final String DOMAIN_TEST = "test";
  private static final String DOMAIN_HREG = "hreg";
  private static final String DOMAIN_NOTARY = "upreg";
  private static final String CANTON_TI = "ti";
  private static final String CANTON_BE = "be";
  private static final String CANTON_VD = "vd";

  @Test
  public void testSuccess() throws Exception
  {
    assertEquals(1,
        CantonalSealPluginFactoryBuilder.build(CANTON_VD, DOMAIN_NOTARY).getOrderedPluginVersions().size());
  }

  @Test
  public void testCaseInsensivitySuccess() throws Exception
  {
    assertEquals(1, CantonalSealPluginFactoryBuilder
        .build(CANTON_VD.toUpperCase(), DOMAIN_NOTARY).getOrderedPluginVersions().size());
  }

  @Test
  public void testUnsupportedDomain() throws Exception
  {
    try
    {
      CantonalSealPluginFactoryBuilder.build(CANTON_VD, "ehra");
      fail("there is a plugin for canton VD in the wrong domain ehra");
    } catch(PluginInstantiationException ex)
    {
      assertEquals("error.PluginInstantiationException.ERR_NO_MATCHING_FACTORY_FOR_DOMAIN_AND_CANTON.601",
          ex.getI18nKey());
    }
  }

  @Test
  public void testAnyCanton() throws Exception
  {
    // there are no unsupported cantons since LOCALSIG-364
    CantonalSealPluginFactoryBuilder.build("any", DOMAIN_NOTARY);
  }

  @Test
  public void testNothingConfigured() throws Exception
  {
    EndPoints eps = new EndPoints();
    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new Date());
    XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    eps.setRelease(xmlCalendar);

    assertFalse(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_VD, DOMAIN_NOTARY, eps));
  }

  @Test
  public void testNotConfigured() throws Exception
  {
    assertFalse(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_TI, DOMAIN_NOTARY, buildGoodConfig()));
    assertFalse(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_VD, DOMAIN_TEST, buildGoodConfig()));
    assertFalse(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_TI, DOMAIN_TEST, buildGoodConfig()));
  }

  @Test
  public void testCorrectlyConfiguration() throws Exception
  {
    assertTrue(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_VD, DOMAIN_NOTARY, buildGoodConfig()));
    assertTrue(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_BE, DOMAIN_NOTARY, buildGoodConfig()));
    assertTrue(CantonalSealPluginFactoryBuilder.isSealConfigured(CANTON_VD, DOMAIN_HREG, buildGoodConfig()));
  }

  @Test
  public void testPluginFactoryInstantiation() throws Exception
  {
    AbstractCantonalSealPluginFactory acspf = CantonalSealPluginFactoryBuilder.build(CANTON_VD, DOMAIN_NOTARY);
    assertTrue(acspf instanceof SdmsSealPluginFactory);
  }


  private EndPoints buildGoodConfig() throws Exception
  {
    EndPoints eps = new EndPoints();

    EndPoints.EndPoint ep_VD_UPREG_v1 = new EndPoints.EndPoint();
    ep_VD_UPREG_v1.setCanton(CANTON_VD);
    ep_VD_UPREG_v1.setDomain(DOMAIN_NOTARY);
    ep_VD_UPREG_v1.setEndPointUrl("fail://wrong.version");
    ep_VD_UPREG_v1.setVersion(new BigDecimal("1"));
    eps.getEndPoint().add(ep_VD_UPREG_v1);

    EndPoints.EndPoint ep_VD_UPREG_v1_1 = new EndPoints.EndPoint();
    ep_VD_UPREG_v1_1.setCanton(CANTON_VD);
    ep_VD_UPREG_v1_1.setDomain(DOMAIN_NOTARY);
    ep_VD_UPREG_v1_1.setEndPointUrl("success://all.ok");
    ep_VD_UPREG_v1_1.setVersion(new BigDecimal("1.1"));
    eps.getEndPoint().add(ep_VD_UPREG_v1_1);

    EndPoints.EndPoint ep_BE_UPREG_v1_1 = new EndPoints.EndPoint();
    ep_BE_UPREG_v1_1.setCanton(CANTON_BE);
    ep_BE_UPREG_v1_1.setDomain(DOMAIN_NOTARY);
    ep_BE_UPREG_v1_1.setEndPointUrl("fail://wrong.canton");
    ep_BE_UPREG_v1_1.setVersion(new BigDecimal("1"));
    eps.getEndPoint().add(ep_BE_UPREG_v1_1);

    EndPoints.EndPoint ep_VD_HREG_v1_1 = new EndPoints.EndPoint();
    ep_VD_HREG_v1_1.setCanton(CANTON_VD);
    ep_VD_HREG_v1_1.setDomain(DOMAIN_HREG);
    ep_VD_HREG_v1_1.setEndPointUrl("fail://wrong.domain");
    ep_VD_HREG_v1_1.setVersion(new BigDecimal("1"));
    eps.getEndPoint().add(ep_VD_HREG_v1_1);

    GregorianCalendar c = new GregorianCalendar();
    c.setTime(new Date());
    XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    eps.setRelease(xmlCalendar);

    return eps;
  }
}
