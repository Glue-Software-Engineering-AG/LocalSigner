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

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;

/**
 *
 * @author greiler
 */
public class CantonalSealInfoParserTest
{
  @Test
  public void testParseXml() throws Exception
  {
    EndPoints ep = CantonalSealInfoParser.parseXml(loadXmlConfig());

    assertEquals(3, ep.getEndPoint().size());
    assertEquals(2018, ep.getRelease().getYear());
    assertEquals(2, ep.getRelease().getMonth());
    assertEquals(12, ep.getRelease().getDay());
  }

  private InputStream loadXmlConfig()
  {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<endPoints xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "    <release>2018-02-12</release>\n" +
        "    <endPoint>\n" +
        "        <canton>VD</canton>\n" +
        "        <domain>upreg</domain>\n" +
        "        <version>1.0</version>\n" +
        "        <endPointUrl>https://i-reg.sdms.ch/seal/</endPointUrl>\n" +
        "    </endPoint>\n" +
        "    <endPoint>\n" +
        "        <canton>BE</canton>\n" +
        "        <domain>upreg</domain>\n" +
        "        <version>1.0</version>\n" +
        "        <endPointUrl>http://www.glue.com/nothing/to/see</endPointUrl>\n" +
        "    </endPoint>\n" +
        "    <endPoint>\n" +
        "        <canton>BE</canton>\n" +
        "        <domain>ehra</domain>\n" +
        "        <version>1.0</version>\n" +
        "        <factoryParameters>\n" +
        "            <parameter>\n" +
        "                <name>name2</name>\n" +
        "                <value>value2</value>\n" +
        "            </parameter>\n" +
        "            <parameter>\n" +
        "                <name>name3</name>\n" +
        "                <value>value3</value>\n" +
        "            </parameter>\n" +
        "        </factoryParameters>\n" +
        "        <endPointUrl>http://www.glue.com/nothing/to/see</endPointUrl>\n" +
        "    </endPoint>\n" +
        "</endPoints>\n";

    return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
  }
}
