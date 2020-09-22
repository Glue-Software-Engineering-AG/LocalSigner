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

import ch.admin.localsigner.config.util.BasePath;
import java.io.File;
import java.util.Locale;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * This class is a wrapper for the language configuration. Depending on the
 * chosen language this class loads the corresponding language file and makes
 * the content available to the application using commons.Configuration.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class LanguageConfiguration
{

  private static final Logger LOGGER = Logger.getLogger(LanguageConfiguration.class);

  // path to language configuration file
  private static final String LANGUAGE_FOLDER = "language" + File.separator;

  // the configuration object
  private PropertiesConfiguration config = null;

  private String language;

  /**
   * Constructor for language configuration with given abbreviation.
   *
   * @param language
   *          Abbreviation of language
   * @throws ConfigurationException
   */
  public LanguageConfiguration(final String language) throws ConfigurationException
  {
    this.language = language;
    // create new language configuration based on the given language
    try
    {
      config = new PropertiesConfiguration();
      config.setDelimiterParsingDisabled(true);
      config.load(new File(getLanguageFolder() + language + ".properties"));
    } catch (ConfigurationException ce)
    {
      LOGGER.error("could not load desired language: " + language + ". try to load default langage");
      // try to load the default language: de
      config = new PropertiesConfiguration();
      config.setDelimiterParsingDisabled(true);
      config.load(new File(getLanguageFolder() + "de.properties"));
    }

    LOGGER.info("Language Configuration " + language + " successfully loaded");
  }

  /**
   * This method is to append third party properties to the language bundles of LocalSigner. This is used e.g. for
   * cantonal seal plugins.
   *
   * @param additionalProperties Additional translations. If a key in the additionalProperties is the same as in the
   * original LocalSigner translations file the value will be overridden!
   */
  public void addConfiguration(Configuration additionalProperties)
  {
    config.copy(additionalProperties);
  }

  public static final String getLanguageFolder()
  {
    return BasePath.getBasePath() + LANGUAGE_FOLDER;
  }

  /**
   * Get a string from this language file.
   *
   * @param key
   *          Key to search for
   * @return Language value for this key
   */
  public String get(final String key)
  {
    final String val = config.getString(key);
    if (val == null)
    {
      LOGGER.debug("*** Missing language key: " + key);
      return "";
    }
    return val;
  }

  public Locale getLocale()
  {
    return new Locale(language);
  }

}
