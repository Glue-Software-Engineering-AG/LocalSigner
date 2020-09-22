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

import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.log4j.Logger;

/**
 * Parses the endpoints-xml file
 *
 * @author greiler
 */
public class CantonalSealInfoParser
{
  private static final Logger LOGGER = Logger.getLogger(CantonalSealInfoParser.class);
  /**
   * There is no need to initialize this class!
   */
  private CantonalSealInfoParser()
  {
    // no need to initialize
  }

  /**
   * Parses the EndPoints-XML (loaded from the address configured at cantonalSealUpdateUrl).
   * @return The EndPoints represented in the XML.
   * @param xmlData The bytes representing the XML content.
   * @throws CantonalSealInfoException if something went wrong.
   */
  public static EndPoints parseEndpointsXml(InputStream xml) throws CantonalSealInfoException
  {
    try
    {
      return parseXml(xml);
    } catch(JAXBException e)
    {
      String msg = "XML with cantonal seal end points information is not parseable to an EndPoints object";

      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_VALID, msg, e);
    } catch(Exception e)
    {
      String msg = "There is a problem with the XML containing the cantonal seal end points information";

      LOGGER.error(msg, e);
      throw new CantonalSealInfoException(
          CantonalSealInfoException.CantonalSealInfoErrorCode.ERR_XML_CONFIGURATION_NOT_LOADABLE, msg, e);
    }
  }

  /** for unittesting only */
  protected static EndPoints parseEndpointsXml(byte [] xmlData) throws CantonalSealInfoException
  {
    return parseEndpointsXml(new ByteArrayInputStream(xmlData));
  }

  protected static EndPoints parseXml(InputStream is) throws JAXBException, XMLStreamException
  {
    JAXBContext jc = JAXBContext.newInstance(EndPoints.class);

    Unmarshaller unmarshaller = jc.createUnmarshaller();
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader xmlReader = factory.createXMLEventReader(is);
    JAXBElement<EndPoints> jaxbElement = unmarshaller.unmarshal(xmlReader, EndPoints.class);
    EndPoints endpoints = jaxbElement.getValue();

    return endpoints;
  }
}
