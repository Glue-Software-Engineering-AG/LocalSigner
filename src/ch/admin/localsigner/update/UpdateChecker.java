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
package ch.admin.localsigner.update;

import java.io.InputStream;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class checks online for updates of LocalSigner.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class UpdateChecker
{
  private String version;

  private String date;

  private String description;

  private String downloadLink;

  private String downloadPage;

  private static final Logger LOGGER = Logger.getLogger(UpdateChecker.class);

  /**
   * Parse the given XML response
   * 
   * @param data
   *          XML data
   * @param locale
   *          language for description
   * @param osName
   *          operating system name <code>System.getProperty("os.name"))</code>
   */
  public void parseXml(final InputStream data, final Locale locale, final String osName)
  {
    LOGGER.debug("parse update for " + osName);
    try
    {
      String os;
      if (osName.contains("Windows"))
      {
        os = "windows";
      }
      else if (osName.contains("Mac"))
      {
        os = "mac";
      }
      else if (osName.contains("Linux"))
      {
        os = "linux";
      }
      else if (osName.contains("SunOS"))
      {
        os = "solaris";
      }
      else
      {
        LOGGER.error("Unsupported operating system for updates: " + osName);
        return;
      }

      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
      NodeList nl = doc.getDocumentElement().getElementsByTagName(os);
      for (int i = 0; i < nl.getLength(); i++)
      {
        Element el = (Element) nl.item(i);
        version = getTextValue(el, "version");
        date = getTextValue(el, "date");
        description = getTextValue(getChildElement(el, "description"),
                locale.getLanguage());
        downloadLink = getTextValue(el, "downloadLink");
        downloadPage = getTextValue(getChildElement(el, "downloadPage"),
                locale.getLanguage());
      }
    } catch (Exception e)
    {
      LOGGER.debug("Cannot parse update data", e);
    }
  }

  private String getTextValue(Element element, String tag)
  {
    String textVal = null;
    NodeList nl = element.getElementsByTagName(tag);
    if (nl != null && nl.getLength() > 0)
    {
      Element el = (Element) nl.item(0);
      textVal = el.getFirstChild().getNodeValue();
    }
    if (textVal != null)
    {
      textVal = textVal.trim();
    }

    return textVal;
  }

  private Element getChildElement(Element element, String tag)
  {
    NodeList nl = element.getElementsByTagName(tag);
    if (nl != null && nl.getLength() > 0)
    {
      return (Element) nl.item(0);
    }
    return null;
  }

  public String getVersion()
  {
    return version;
  }

  public String getDate()
  {
    return date;
  }

  public String getDescription()
  {
    return description;
  }

  public String getDownloadLink()
  {
    return downloadLink;
  }

  public String getDownloadPage()
  {
    return downloadPage;
  }

  public boolean hasUpdate(String oldVersion)
  {
    LOGGER.debug("old: " + oldVersion + ", new: " + version);
    if (oldVersion == null || version == null)
    {
      return false;
    }
    if ("n/a".equals(oldVersion))
    {
      return true;
    }

    if (oldVersion.equals(version))
    {
      return false;
    }

    String[] splitNew = version.split("\\.");
    String[] splitOld = oldVersion.split("\\.");

    int rounds = Math.min(splitNew.length, splitOld.length);
    for (int i = 0; i < rounds; i++)
    {
      try
      {
        int newVal = Integer.parseInt(splitNew[i]);
        int oldVal = Integer.parseInt(splitOld[i]);
        if (newVal > oldVal)
        {
          return true;
        }
        if (newVal < oldVal)
        {
          return false;
        }
        // else next round
      } catch (NumberFormatException e)
      {
        // not a number
        return false;
      }
    }
    return true;
  }

}
