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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.util.ConfigurationInitializer;

/**
 * Detects PKCS#11 libs respecting configuration.
 *
 * @author greiler (only outsourced from ApplicationConfiguration)
 * @author $Author$
 * @version $Revision$
 */
public class Pkcs11Configuration
{
  private static final Logger LOGGER = Logger.getLogger(Pkcs11Configuration.class);

  private final ConfigurationInitializer configInit;

  public Pkcs11Configuration(ConfigurationInitializer configInit)
  {
    this.configInit = configInit;
  }

  // check PKCS11 configuration
  public void checkPkcs11Configuration(PropertiesConfiguration userConfig)
    throws ConfigurationException
  {
    String pkcs11Path = userConfig.getString(Config.PKCS11_LIB, "");
    if (StringUtils.isBlank(pkcs11Path))
    {
      pkcs11Path = "notfound";
    }

    File checkFile = new File(pkcs11Path);
    if (checkFile.exists())
    {
      LOGGER.debug("Existing PKCS11 lib: " + pkcs11Path);
    } else
    {
      LOGGER.debug("PKCS11 lib not found: " + checkFile.getAbsolutePath());
      Set<String> detect = detectPkcs11Lib().keySet();
      LOGGER.debug("Detected PKCS11 lib: " + detect);
      if (detect.size() > 0)
      {
        // use first for config
        String[] libs = detect.toArray(new String[0]);
        setValue(userConfig, Config.PKCS11_LIB, libs[0]);
      }
    }
  }

  private void setValue(PropertiesConfiguration config, final String key, final Object value)
      throws ConfigurationException
  {
    LOGGER.debug("writing " + key + "/" + value);
    config.setProperty(key, value);
    config.save();
  }

  /**
   * Return a list of PKCS11 path found on this system.
   *
   * @return list of library path
   */
  public Map<String, String> detectPkcs11Lib()
  {
    Map<String, String> foundLibs = new HashMap<String, String>();

    try
    {
      File configFile = configInit.getCachedDriversConfig();
      if (!configFile.exists())
      {
        // not found, take default file
        configFile = configInit.getDefaultDriversConfig();
      }

      LOGGER.debug("load pkcs11 driver from " + configFile.getAbsolutePath());
      PropertiesConfiguration config = new PropertiesConfiguration(configFile);

      Map<String, String> descriptions = new HashMap<String, String>();
      Map<String, String> libs = new HashMap<String, String>();
      Iterator<String> keys = config.getKeys();
      while (keys.hasNext())
      {
        String key = keys.next();

        if (key.startsWith("text"))
        {
          String[] vals = config.getStringArray(key);
          // rejoin separated values
          descriptions.put(key, StringUtils.join(vals, ", "));
        }

        if (key.startsWith("lib"))
        {
          String[] vals = config.getStringArray(key);
          for (String val : vals)
          {
            // replace Windows Variable
            if (val.startsWith("%PROGRAMFILES%"))
            {
              val = System.getenv("PROGRAMFILES") + val.substring(14);
            }
            libs.put(val, key);
          }
        }
      }
      LOGGER.debug("Known PKCS11 libs: " + libs.size());

      // check files
      for (String path : libs.keySet())
      {
        File lib = new File(path);
        if (lib.exists())
        {
          String descrKey = libs.get(path).replaceAll("lib", "text");
          String descr = descriptions.get(descrKey);
          foundLibs.put(path, descr);
        }
      }
    } catch (ConfigurationException e)
    {
      LOGGER.error("Cannot detect PKCS11 driver library", e);
    }

    return foundLibs;
  }

}