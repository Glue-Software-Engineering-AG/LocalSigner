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
package ch.admin.localsigner.config.util;

import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.Pkcs11Configuration;
import ch.admin.localsigner.config.ZulabConfiguration;
import ch.admin.localsigner.utils.Helper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Initializes the configuration in ApplicationConfiguration.
 *
 * @author boris, greiler
 * @author $Author$
 * @version $Revision$
 */
public class ConfigurationInitializer
{
  private static final Logger LOGGER = Logger.getLogger(ConfigurationInitializer.class);

  // default profiles path
  private static final String DEFAULT_PROFILES_PATH = "resources/profiles";
  private static final String DEFAULT_CONFIGURATION_PATH = "configuration";

  // the system configuration
  private PropertiesConfiguration systemConfiguration;

  // the zulab configuration
  private ZulabConfiguration zulabConfiguration;

  // the user configuration
  private PropertiesConfiguration userConfiguration = null;


  public ConfigurationInitializer()
    throws ConfigurationException, IOException
  {
    initializeConfiguration();
  }

  /**
   * Initilizatoin of the System, Tenants and User configuration.
   *
   * @throws ConfigurationException
   * @throws IOException
   */
  private void initializeConfiguration()
    throws ConfigurationException, IOException
  {
    initSystemConfiguration();
    initUserConfiguration();

    if (!getDefaultProfilesPath().equals(getSystemProfileFolder(true)))
    { // only update cache if not default configuration
      updateProfileCache();
    }
  }

  public PropertiesConfiguration getSystemConfiguration()
  {
    return systemConfiguration;
  }

  public ZulabConfiguration getZulabConfiguration()
  {
    return zulabConfiguration;
  }

  public PropertiesConfiguration getUserConfiguration()
  {
    return userConfiguration;
  }


  /**
   * Initializes the system configuration.
   *
   * @throws ConfigurationException
   * @throws IOException
   */
  private void initSystemConfiguration() throws ConfigurationException, IOException
  {
    File config = getDefaultInitConfigurationFile();
    systemConfiguration = new PropertiesConfiguration(config.getAbsoluteFile());
    LOGGER.info("System configuration successfully loaded from " + config.getAbsolutePath());

    updateCache();
    loadCachedValues();
  }

  /**
   * Initializes the user configuration. If the user configuration file exists
   * in the users home directory it is used, otherwise the file is created with
   * default values.
   *
   * @throws ConfigurationException
   * @throws IOException
   */
  public final void initUserConfiguration() throws ConfigurationException, IOException
  {
    // check if user configuration exists
    final File userConfigFolder = new File(getUserConfigFolder());
    final File userProfilesFolder = new File(getUserProfileFolder());
    final File userConfigFile = new File(getUserConfig());
    final File zulabConfigFile = getZulabConfigurationFile();

    // check if folder exists - otherwise create it
    if (userConfigFolder.mkdirs())
    {
      LOGGER.debug("user configuration folder has been created successfully: " + userConfigFolder.getAbsolutePath());
      // true - the folder did not exist - and has been created
    }
    else
    {
      LOGGER.debug("user configuration folder already exists");
    }

    // check if directory is writable
    if (!userConfigFolder.canWrite())
    {
      LOGGER.error("Cannot write user config folder: " + userConfigFolder.getAbsolutePath());
      throw new IOException( "Cannot write directory " + userConfigFolder.getAbsolutePath()
              + ". \nThe path for the directory can be changed in the init.properties. \nRefer to the documentation.");
    }

    // the default profile folder - check for it - otherwise create it
    if (userProfilesFolder.mkdir())
    {
      LOGGER.debug("user profile folder has been created successfully: " + userProfilesFolder.getAbsolutePath());
      // true - the folder did not exist and has been created
    }
    else
    {
      LOGGER.debug("users profile folder already exists");
    }

    if (userConfigFile.createNewFile())
    {
      // true - the file did not exist and has been created
      LOGGER.info("user configuration file has been created successfully: " + userConfigFile.getAbsolutePath());

      // create default config
      userConfiguration = new PropertiesConfiguration(userConfigFile);

      // from system config values
      final List<String> props = Config.getUserPropertyNames();

      for (String name : props)
      {
        userConfiguration.setProperty(name, systemConfiguration.getProperty(name));
      }

      // and set default value for user signature profiles as they SHOULD be different
      // from the system config, because the user can probably not write into the
      // system config path.
      userConfiguration.setProperty(Config.PROFILE_PATH, userProfilesFolder.getAbsolutePath());

      // and save it
      userConfiguration.save();
    }
    else
    {
      // false - user configuration already exists
      LOGGER.debug("user configuration file already exists - use it");

      // read it
      userConfiguration = new PropertiesConfiguration(userConfigFile);
    }

    new Pkcs11Configuration(this).checkPkcs11Configuration(userConfiguration);

    // ZulaB (Zulassungsbestätigung) Configuration
    zulabConfiguration = new ZulabConfiguration(zulabConfigFile.getAbsoluteFile());
    LOGGER.info("ZulaB configuration successfully loaded from " + zulabConfigFile.getAbsolutePath());

    copyDefaultProfile();
  }

  /**
   * Update cache of system profiles. The profiles might be on a network share.
   * They can be used from a cache even if the network share is unreachable at
   * the moment.
   */
  private void updateProfileCache()
  {
    File cache = new File(getUserProfileCacheFolder());
    String profilePath = getSystemProfileFolder(false);
    File profiles = new File(profilePath);

    if (profiles.exists())
    {
      try
      {
        FileUtils.deleteDirectory(cache);
        LOGGER.info("Copy system profiles from " + profiles.getAbsolutePath() + " to " + cache.getAbsolutePath());
        FileUtils.copyDirectory(profiles, cache);
      } catch (IOException e)
      {
        LOGGER.error("Cannot update system profiles", e);
      }
    }
    else
    {
      LOGGER.info(profiles.getAbsolutePath() + " not found to copy to cache");
    }
  }

  /**
   * copy default profile if missing
   */
  private void copyDefaultProfile() throws ConfigurationException
  {
    final File userDefaultProfile = new File(getUserDefaultProfile());

    if (userDefaultProfile.exists())
    {
      LOGGER.debug("user default profile already exists");
    } else
    {
      PropertiesConfiguration defaultProfile = new PropertiesConfiguration(userDefaultProfile);
      defaultProfile.setDelimiterParsingDisabled(true);

      File defaultFile = getCachedMasterProfile();
      if (!defaultFile.exists())
      {
        // not found, take default file
        defaultFile = getDefaultMasterProfile();
      }

      defaultProfile.load(defaultFile);
      defaultProfile.save(userDefaultProfile);
      LOGGER
          .info("user default profile created: " + userDefaultProfile.getAbsolutePath());
    }

    LOGGER.debug("User configuration successfully initialized");
  }

  private void updateCache()
  {
    String additionalSharedConfigPath = getAdditionalSharedConfigPath();
    if (StringUtils.isEmpty(additionalSharedConfigPath))
    {
      return;
    }

    // calculate checksum to detect modifications
    File cachedAdditionalSharedConfig = getAdditionalSharedConfigCache();
    long additionalSharedConfigCacheSum = checkSum(cachedAdditionalSharedConfig);

    File cachedMasterProfile = getCachedMasterProfile();
    long defaultProfileSum = checkSum(cachedMasterProfile);

    // load configuration (userFolder may be used for cache directory!)
    try
    {
      File additionalSharedInitConfigFile = new File(additionalSharedConfigPath + File.separator + Config.INIT_CONFIG);
      PropertiesConfiguration extra = new PropertiesConfiguration(additionalSharedInitConfigFile);

      // load does not remove old value!
      systemConfiguration.copy(extra);
    } catch (ConfigurationException e)
    {
      LOGGER.info("Cannot load additional system configuration from " + additionalSharedConfigPath);
    }

    File cache = new File(getUserConfigCacheFolder());
    File config = new File(additionalSharedConfigPath);

    if (config.exists())
    {
      try
      {
        FileUtils.deleteDirectory(cache);
        LOGGER.info("Copy system configuration from " + config.getAbsolutePath() + " to " + cache.getAbsolutePath());
        FileUtils.copyDirectory(config, cache);
      } catch (IOException e)
      {
        LOGGER.error("Cannot update system cache", e);
      }
    }
    else
    {
      LOGGER.info(config.getAbsolutePath() + " not found to copy to cache");
      return;
    }

    // calculate checksum again and handle modifications
    boolean configChanged = additionalSharedConfigCacheSum != checkSum(cachedAdditionalSharedConfig);
    boolean defaultProfileChange = defaultProfileSum != checkSum(cachedMasterProfile);

    LOGGER.debug(cachedAdditionalSharedConfig.getName() + " changed: " + configChanged);
    LOGGER.debug(cachedMasterProfile.getName() + " changed: " + defaultProfileChange);

    if (configChanged)
    {
      Helper.backup(new File(getUserConfig()));
    }

    if (defaultProfileChange)
    {
      Helper.backup(new File(getUserDefaultProfile()));
    }
  }

  // Pkcs11 Config
  public Map<String, String> detectPkcs11Lib()
  {
    return new Pkcs11Configuration(this).detectPkcs11Lib();
  }

  // Files and Paths
  private static String getDefaultConfigurationPath()
  {
    return BasePath.getBasePath() + DEFAULT_CONFIGURATION_PATH + File.separator;
  }

  public String getDefaultProfilesPath()
  {
    return BasePath.getBasePath() + DEFAULT_PROFILES_PATH + File.separator;
  }

  public File getCachedDriversConfig()
  {
    return new File(getUserConfigCacheFolder() + Config.DRIVERS_CONFIG).getAbsoluteFile();
  }

  public File getDefaultDriversConfig()
  {
    return new File(getDefaultConfigurationPath() + Config.DRIVERS_CONFIG).getAbsoluteFile();
  }

  private File getCachedMasterProfile()
  {
    return new File(getUserConfigCacheFolder() + Config.MASTER_DEFAULT_PROFILE);
  }

  private File getDefaultMasterProfile()
  {
    return new File(getDefaultConfigurationPath() + Config.MASTER_DEFAULT_PROFILE);
  }

  /**
   * Additional configuration directory for share
   */
  private String getAdditionalSharedConfigPath()
  {
    return ConfigurationVariablesReplacer.replaceVariables(systemConfiguration.getString(Config.CONFIG_PATH));
  }

  private long checkSum(File file)
  {
    try
    {
      if (!file.exists())
      {
        // file does not exists
        return 0;
      }
      return FileUtils.checksumCRC32(file);
    } catch (IOException e)
    {
      LOGGER.debug("Cannot checksum " + file.getAbsolutePath(), e);
      return 0;
    }
  }

  /**
   * Returns the profile path of the system defined profile folder.
   *
   * @see <code>getProfilepath()</code>
   * @return system wide defined profile directory path
   */
  public final String getSystemProfileFolder(boolean useCache)
  {
    File cache = new File(getUserProfileCacheFolder());
    if (cache.exists() && useCache)
    {
      return cache.getAbsolutePath();
    }

    // fallback without cache
    String path = ConfigurationVariablesReplacer.replaceVariables(systemConfiguration.getString(Config.PROFILE_PATH));
    if (StringUtils.isBlank(path))
    {
      return getDefaultProfilesPath();
    }
    return path;
  }

  /**
   * System profiles cache in user folder
   */
  public final String getUserProfileCacheFolder()
  {
    return getUserConfigFolder() + File.separator + "cache-systemprofile";
  }

  private File getDefaultInitConfigurationFile()
  {
    return new File(getDefaultConfigurationPath() + Config.INIT_CONFIG);
  }

  /**
   * Get the folder for the user configuration
   *
   * @return folder path
   */
  public final String getUserConfigFolder()
  {
    String defaultHome = System.getProperty("user.home") + File.separator + ".localsigner";

    String folder = systemConfiguration.getString(Config.USER_FOLDER, defaultHome);

    if (StringUtils.isEmpty(folder))
    {
      // empty in configuration
      folder = defaultHome;
    }

    folder = ConfigurationVariablesReplacer.replaceVariables(folder);
    if (StringUtils.isEmpty(folder))
    {
      return defaultHome;
    }

    return folder;
  }

  /**
   * System configuration cache in user folder
   */
  public final String getUserConfigCacheFolder()
  {
    return getUserConfigFolder() + File.separator + "cache-systemconfig" + File.separator;
  }

  public String getDebugFile()
  {
    return getUserConfigFolder() + File.separator + "debug.log";
  }

  /**
   * path to user default profile
   */
  protected final String getUserDefaultProfile()
  {
    return getUserProfileFolder() + File.separator + Config.DEFAULT_PROFILE + ".properties";
  }

  /**
   * Returns the path to the folder containing the signature profiles. This is
   * ALWAYS the path from the user configuration. Use getSystemProfilepath() to
   * get the profile path configured by the system wide configuration.
   *
   * @see <code>getSystemProfilepath()</code>
   * @return profile directory path
   */
  public final String getUserProfileFolder()
  {
    if (userConfiguration == null)
    {
      return getUserConfigFolder() + File.separator + "profiles";
    }

    String path = ConfigurationVariablesReplacer.replaceVariables(userConfiguration.getString(Config.PROFILE_PATH));
    if (StringUtils.isBlank(path))
    {
      return getUserConfigFolder() + File.separator + "profiles";
    }
    return path;
  }

  /**
   * path to internal config file
   */
  public final String getUserInternalConfig()
  {
    return getUserConfigFolder() + File.separator + "internalconfig.properties";
  }


  public File getZulabConfigurationFile()
  {
    return new File(getUserConfigFolder() + File.separator + Config.ZULAB_CONFIG);
  }

  /**
   * path to the user config file
   */
  public final String getUserConfig()
  {
    return getUserConfigFolder() + File.separator + "userconfiguration.properties";
  }

  private void loadCachedValues()
  {
    File configFile = getAdditionalSharedConfigCache();
    if (configFile.exists())
    {
      try
      {
        PropertiesConfiguration extra = new PropertiesConfiguration(configFile);

        // load does not remove old value!
        systemConfiguration.copy(extra);
        LOGGER.info("System configuration successfully loaded from " + configFile.getAbsolutePath());
      } catch (ConfigurationException e)
      {
        LOGGER.error( "Cannot load additional system configuration from " + configFile.getAbsolutePath(), e);
      }
    }
  }

  private File getAdditionalSharedConfigCache()
  {
    return new File(getUserConfigCacheFolder() + Config.INIT_CONFIG);
  }

  public void debugVariables()
  {
    // test system variables
    LOGGER.debug("$username  -> " + ConfigurationVariablesReplacer.replaceVariables("$username"));
    LOGGER.debug("$userhome  -> " + ConfigurationVariablesReplacer.replaceVariables("$userhome"));
    LOGGER.debug("$homeshare -> " + ConfigurationVariablesReplacer.replaceVariables("$homeshare"));
    LOGGER.debug("$bundhome  -> " + ConfigurationVariablesReplacer.replaceVariables("$bundhome"));
    LOGGER.debug("$depagency -> " + ConfigurationVariablesReplacer.replaceVariables("$depagency"));

    LOGGER.debug("User configuration:  " + getUserConfigFolder());
    LOGGER.debug("User profiles:       " + getUserProfileFolder());
    LOGGER.debug("System profiles:     " + getSystemProfileFolder(true) + " (with Cache)");
    LOGGER.debug("System profiles:     " + getSystemProfileFolder(false) + " (without Cache)");
    LOGGER.debug("Extra configuration: " + getAdditionalSharedConfigPath());
  }
}
