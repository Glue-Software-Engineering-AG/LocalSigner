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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

/**
 * This class gives access to the lists used for ZulaB Funktionsnachweis as configured in file zulab.xml.
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public final class ZulabConfiguration
{
  private static final Logger LOGGER = Logger.getLogger(ZulabConfiguration.class);

  public static final String VERSION_NOT_AVAILABLE = "n\\a";

  protected HashMap<String, Entry> cantonsConfiguration;
  protected HashMap<String, Entry> domainsConfiguration;
  protected String versionConfiguration;
  protected File file;

  public ZulabConfiguration(File xmlFile) throws ConfigurationException
  {
    this.file = xmlFile;
    loadConfiguration();
  }

  public File getFile()
  {
    return file;
  }

  public void loadConfiguration() throws ConfigurationException
  {
    // the following two tests should be done by XMLConfiguration. But tests have shown that this is not the case...
    if (!file.exists())
    {
      writeDummyZulabConfig(file);
    }

    if (!file.canRead())
    {
      throw new ConfigurationException("The ZulaB configuration file "+file.getAbsolutePath()+" may not be read.");
    }

    try
    {
      init(new FileInputStream(file));
    } catch(FileNotFoundException ex)
    {
      throw new ConfigurationException(
          "the ZulaB configuration file "+file.getAbsolutePath()+" may not be found.", ex);
    }
  }
  private void writeDummyZulabConfig(File file) throws ConfigurationException
  {
    LOGGER.info("ZulaB configuration is not yet available. Creating dummy configuration to initiate update");

    try
    {
      file.createNewFile();
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
          + "<config version=\""+VERSION_NOT_AVAILABLE+"\"\n"
          + "        xmlns=\"http://www.glue.ch/localsigner/zulabconfiguration\">\n"
          + "  <cantons/>\n"
          + "  <domains/>\n"
          + "</config>").getBytes("UTF-8"));
      fos.close();
    } catch (Exception e)
    {
      // auch wenn debug=true ist, wird dieser Teil noch nicht in das debug.log geschrieben, da diese initialisierung
      // erst danach geschiet. Dieses Logstatement wird also nur in die Konsole geschrieben!
      LOGGER.warn("Not able to create dummy zulab configuration", e);
      throw new ConfigurationException("The ZulaB configuration file "+file.getAbsolutePath()+" may not be created.");
    }
  }

  public ZulabConfiguration(InputStream xmlProperties) throws ConfigurationException
  {
    init(xmlProperties);
  }

  private void init(InputStream xmlProperties) throws ConfigurationException
  {
    // http://commons.apache.org/proper/commons-configuration/userguide/howto_xml.html#Escaping_special_characters
    XMLConfiguration configurationFile = new XMLConfiguration();
    configurationFile.setDelimiterParsingDisabled(true);
    configurationFile.setAttributeSplittingDisabled(true);
    configurationFile.load(xmlProperties);

    loadVersionConfiguration(configurationFile);

    loadCantonsConfiguration(configurationFile);

    loadDomainsConfiguration(configurationFile);
  }

  protected void loadVersionConfiguration(XMLConfiguration xmlConfiguration)
  {
    versionConfiguration = (String)xmlConfiguration.getRootNode().getAttributes("version").get(0).getValue();
  }

  protected void loadCantonsConfiguration(XMLConfiguration xmlConfiguration)
  {
    cantonsConfiguration = new HashMap<String, Entry>();
    int numberOfCantons = xmlConfiguration.getList("cantons.canton.value").size();

    String currentCantonKey = "";
    for (int cantonCount = 0; cantonCount < numberOfCantons; cantonCount++)
    {
      currentCantonKey = "cantons.canton("+cantonCount+")";

      Entry entry = new Entry();

      entry.setValue(xmlConfiguration.getString(currentCantonKey+".value"));
      entry.setTranslationDe(xmlConfiguration.getString(currentCantonKey+".translations.german"));
      entry.setTranslationFr(xmlConfiguration.getString(currentCantonKey+".translations.french"));
      entry.setTranslationIt(xmlConfiguration.getString(currentCantonKey+".translations.italian"));

      cantonsConfiguration.put(entry.getValue(), entry);
    }
  }

  protected void loadDomainsConfiguration(XMLConfiguration xmlConfiguration)
  {
    domainsConfiguration = new HashMap<String, Entry>();
    int numberOfDomains = xmlConfiguration.getList("domains.domain.value").size();

    String currentDomainKey = "";
    for (int domainCount = 0; domainCount < numberOfDomains; domainCount++)
    {
      currentDomainKey = "domains.domain("+domainCount+")";

      Entry entry = new Entry();

      entry.setValue(xmlConfiguration.getString(currentDomainKey+".value"));
      entry.setTranslationDe(xmlConfiguration.getString(currentDomainKey+".translations.german"));
      entry.setTranslationFr(xmlConfiguration.getString(currentDomainKey+".translations.french"));
      entry.setTranslationIt(xmlConfiguration.getString(currentDomainKey+".translations.italian"));

      domainsConfiguration.put(entry.getValue(), entry);
    }
  }

  public String getVersion()
  {
    return versionConfiguration;
  }

  public HashMap<String, Entry> getCantons()
  {
    return cantonsConfiguration;
  }

  public HashMap<String, Entry> getDomains()
  {
    return domainsConfiguration;
  }

  public class Entry {
    private String valueString = "";
    private String translationDe = "";
    private String translationFr = "";
    private String translationIt = "";

    public Entry()
    {
    }

    public String getValue()
    {
      return valueString;
    }

    public void setValue(String value)
    {
      this.valueString = value;
    }

    public String getTranslation(Locale locale)
    {
      if (locale.getLanguage().equals(new Locale("fr").getLanguage()))
      {
        return getTranslationFr();
      } else if (locale.getLanguage().equals(new Locale("it").getLanguage()))
      {
        return getTranslationIt();
      } else // default
      {
        return getTranslationDe();
      }
    }

    public String getTranslationDe()
    {
      return translationDe;
    }

    public void setTranslationDe(String translationDe)
    {
      this.translationDe = translationDe;
    }

    public String getTranslationFr()
    {
      return translationFr;
    }

    public void setTranslationFr(String translationFr)
    {
      this.translationFr = translationFr;
    }

    public String getTranslationIt()
    {
      return translationIt;
    }

    public void setTranslationIt(String translationIt)
    {
      this.translationIt = translationIt;
    }
  }

  /**
   * Marker class
   */
  @SuppressWarnings("serial")
  public class NoSuchTenantException extends Exception
  {
    public NoSuchTenantException(String message)
    {
      super(message);
    }
  }
}