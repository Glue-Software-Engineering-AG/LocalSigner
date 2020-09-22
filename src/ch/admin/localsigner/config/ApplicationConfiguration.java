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
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.util.ConfigurationInitializer;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.notary.cantonal.seal.info.client.CantonalSealInfoWsClient;

/**
 * This class is a wrapper for the application configuration file
 * 'init.properties' in the configuration folder.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ApplicationConfiguration
{
  private static final String EMPTY_STRING = "";

  private static final String ALL_STRING = "all";

  private static final String FALSE_STRING = "false";

  private static final String TRUE_STRING = "true";

  private static final Logger LOGGER = Logger.getLogger(ApplicationConfiguration.class);

  // Helper to initialzie the configuration
  ConfigurationInitializer initializer = null;

  // the system configuration object
  private PropertiesConfiguration systemConfiguration;

  // the zulab configuration object
  private ZulabConfiguration zulabConfiguration;

  // the user configuration object
  private PropertiesConfiguration userConfiguration = null;

  /**
   * constructor read in the property file and make it usable to the application
   * via commons configuration.
   *
   * @throws ConfigurationException
   */
  public ApplicationConfiguration() throws ConfigurationException, IOException
  {
    initializer = new ConfigurationInitializer();

    systemConfiguration = initializer.getSystemConfiguration();
    zulabConfiguration = initializer.getZulabConfiguration();
    userConfiguration = initializer.getUserConfiguration();
  }

  /**
   * needed to reload the ZulaB Configuration file after the new version was
   * installed
   */
  public void reloadUpdatedZulabConfiguration()
  {
    LOGGER.debug("reloading configuration after updating zulab configuration");

    try
    {
      initUserConfiguration();
      zulabConfiguration = initializer.getZulabConfiguration();

      LOGGER.info("successfully reloading configuration after updating zulab configuration");
    } catch (Exception e)
    {
      LOGGER.error("Cannot init user configuration", e);
    }
  }

  public void debugVariables()
  {
    initializer.debugVariables();
  }

  /**
   * Get the property from user configuration if allowed from edit property,
   * else from system configuration. Returns the default value if missing.
   */
  private String getUserValue(final String property, final String propertyEdit, final String defaultValue)
  {
    String value = systemConfiguration.getString(property, defaultValue);
    if (isEditable(propertyEdit))
    {
      return userConfiguration.getString(property, value);
    }
    return value;
  }

  /**
   * Checks whether the given <b>propertyToCheck</b> can be configured by the
   * user or if always the system configuration value is used. This information
   * is used to build up the configuration screen (which elements should be
   * displayed to the user) and to decide if the system or user configuration
   * value is returned by the getter methods of this class.
   *
   * @param propertyToCheck
   */
  public boolean isEditable(final String propertyToCheck)
  {
    return systemConfiguration.getBoolean(propertyToCheck, false);
  }

  /**
   * Returns the language configured in the init.properties file
   *
   * @return configured language
   */
  public String getLanguage()
  {
    return getUserValue(Config.LANGUAGE, Config.LANGUAGE_EDIT, LocalSigner.AUTOMATIC_LANGUAGE_PROPERTY);
  }

  /**
   * Does evaluate the system property if configured value is "auto".
   */
  public String getLanguageEvaluatingAuto()
  {
    String langFromConfig = getLanguage();
    if (langFromConfig.equals(LocalSigner.AUTOMATIC_LANGUAGE_PROPERTY))
    {
      // get language from operating system
      langFromConfig = System.getProperties().getProperty(LocalSigner.USER_LANGUAGE_PROPERTY);
    }
    return langFromConfig;
  }

  /**
   * Return the configured proxy http host
   *
   * @return
   */
  public String getProxyHttpHost()
  {
    return getUserValue(Config.PROXY_HTTP_HOST, Config.PROXY_EDIT, null);
  }

  /**
   * Return the configured proxy http port
   *
   * @return
   */
  public String getProxyHttpPort()
  {
    return getUserValue(Config.PROXY_HTTP_PORT, Config.PROXY_EDIT, null);
  }

  /**
   * Return the configured proxy http host
   *
   * @return
   */
  public String getProxyHttpsHost()
  {
    return getUserValue(Config.PROXY_HTTPS_HOST, Config.PROXY_EDIT, null);
  }

  /**
   * Return the configured proxy http port
   *
   * @return
   */
  public String getProxyHttpsPort()
  {
    return getUserValue(Config.PROXY_HTTPS_PORT, Config.PROXY_EDIT, null);
  }

  public String getProxyExclusions()
  {
    return getUserValue(Config.PROXY_EXCLUSIONS, Config.PROXY_EDIT, null);
  }

  public String getPacURI()
  {
    return getUserValue(Config.PAC_URI, Config.PROXY_EDIT, null);
  }

  public String getProxyConfigurationKindStr()
  {
    return getUserValue(Config.PROXY_CONFIGURATION_KIND, Config.PROXY_EDIT, "");
  }



  public enum PdfViewer
  {
    INTERNAL, ADOBE, DUAL

  }

  /**
   * Return the viewer that should be used. DUAL means automatic switch between
   * Adobe and internal. The value of the property is true, false or dual
   * (backward compatibility to true, false)
   */
  public PdfViewer getViewer()
  {
    String value = getUserValue(Config.INTERNALVIEWER, Config.INTERNALVIEWER_EDIT, TRUE_STRING);

    if (TRUE_STRING.equalsIgnoreCase(value))
    {
      return PdfViewer.INTERNAL;
    }

    if (FALSE_STRING.equalsIgnoreCase(value))
    {
      return PdfViewer.ADOBE;
    }

    // value should be dual
    return PdfViewer.DUAL;
  }

  /**
   * Return the configured font size
   *
   * @return Font size to be used for the GUI
   */
  public int getFontSize()
  {
    String value = getUserValue(Config.FONTSIZE, Config.FONTSIZE_EDIT, "8");
    return Integer.parseInt(value);
  }

  /**
   * Return the configured PKCS11 library (platform dependent)
   *
   * @return path to pkcs11 library
   */
  public String getPkcs11Lib()
  {
    return getUserValue(Config.PKCS11_LIB, Config.PKCS11_LIB_EDIT, EMPTY_STRING);
  }

  /**
   * Return the configured PKCS12 file
   *
   * @return path to pkcs12 file
   */
  public String getPkcs12File()
  {
    return getUserValue(Config.PKCS12_FILE, Config.PKCS12_FILE_EDIT, EMPTY_STRING);
  }

  /**
   * Return timestamp configuration. This is ALWAYS taken from the system wide
   * configuration
   *
   * @param emptyText
   *          text to show for no timestamp
   * @return tsa configuration
   */
  public List<TsaConfiguration> getTSAConfig()
  {
    final String[] servers = systemConfiguration.getStringArray(Config.TIMESTAMPSERVER);
    final String[] usernames = systemConfiguration.getStringArray(Config.TIMESTAMPUSER);
    final String[] passwords = systemConfiguration.getStringArray(Config.TIMESTAMPPASSWORD);
    final String[] descriptions = systemConfiguration.getStringArray(Config.TIMESTAMPDESCRIPTION);
    final List<TsaConfiguration> tsaconfig = new ArrayList<TsaConfiguration>();

    List<String> internalTsa = getInternalTsa();

    // internal QuoVadis TSA
    TsaConfiguration qv = new TsaConfiguration("http://tsa.quovadisglobal.com/TSS/HttpTspServer", "TSA QuoVadis",
        "TSA QuoVadis");
    if (internalTsa.contains(ALL_STRING) || internalTsa.contains(qv.getLookupKey()))
    {
      tsaconfig.add(qv);
    }

    // internal SwissSign TSA
    TsaConfiguration sw = new TsaConfiguration("https://tsa.swisssign.net", "TSA SwissSign", "TSA SwissSign");
    if (internalTsa.contains(ALL_STRING) || internalTsa.contains(sw.getLookupKey()))
    {
      tsaconfig.add(sw);
    }

    TsaConfiguration pk = getSwissGovernmentTSA();
    if (internalTsa.contains(ALL_STRING) || internalTsa.contains(pk.getLookupKey()))
    {
      tsaconfig.add(pk);
    }

    // add config from init.properties
    for (int i = 0; i < servers.length; i++)
    {
      if (StringUtils.isNotEmpty(servers[i]))
      {
        tsaconfig.add(new TsaConfiguration(servers[i], descriptions[i], usernames[i], passwords[i], descriptions[i]));
      }
    }

    return tsaconfig;
  }

  public static TsaConfiguration getSwissGovernmentTSA()
  {
    // internal Admin PKI TSA
    return new TsaConfiguration("http://tsa.pki.admin.ch/tsa", "TSA Swiss AdminPKI", "Swiss Government TSA ");
  }

  /**
   * Store the given value in the user configuration.
   *
   * @param key
   * @param value
   * @throws ConfigurationException
   */
  public final void setValue(final String key, final Object value) throws ConfigurationException
  {
    LOGGER.debug("writing " + key + "/" + value);
    userConfiguration.setProperty(key, value);
    userConfiguration.save();
  }

  /**
   * Returns <code>true</code> if the current user is allowed to change at least
   * one of the configuration properties. <code>false</code> otherwise.
   *
   * @return true or false.
   */
  public boolean userConfigurationAllowed()
  {
    List<String> editables = Config.getEditableNames();

    boolean allowed = false;

    for (String e : editables)
    {
      // default if missing: false
      allowed = allowed | systemConfiguration.getBoolean(e, false);
    }

    return allowed;
  }

  /**
   * Return the configured property of integrity check
   *
   * @return
   */
  public boolean isShowIntegrityCheck()
  {
    String value = getUserValue(Config.INTEGRITY_CHECK, Config.INTEGRITY_CHECK_EDIT, TRUE_STRING);
    return Boolean.parseBoolean(value);
  }

  /**
   * Return the configured property if it is possible to sign a document that is
   * not PDF/A conformant
   *
   * @return
   */
  public boolean isSignNonPdfA()
  {
    String value = getUserValue(Config.SIGN_NON_PDF_A, Config.SIGN_NON_PDF_A_EDIT, TRUE_STRING);
    return Boolean.parseBoolean(value);
  }

  public boolean isSidePanelActive()
  {
    String value = getUserValue(Config.SIDE_PANEL_ACTIVE, Config.SIDE_PANEL_ACTIVE_EDIT, TRUE_STRING);
    return Boolean.parseBoolean(value);
  }

  public boolean isLtvActive()
  {
    String value = getUserValue(Config.LTV_ACTIVE, Config.LTV_ACTIVE_EDIT, TRUE_STRING);
    return Boolean.parseBoolean(value);
  }

  public boolean isOcspActive()
  {
    String value = getUserValue(Config.LTV_OCSP_ACTIVE, Config.LTV_OCSP_ACTIVE_EDIT, TRUE_STRING);
    return Boolean.parseBoolean(value);
  }

  /**
   * Returns the input path configured in the init.properties file
   *
   * @return configured path
   */
  public String getInputpath()
  {
    return getUserValue(Config.INPUT_PATH, Config.INPUT_PATH_EDIT, null);
  }

  /**
   * Return the configured property of the user profiles
   *
   * @return
   */
  public boolean isSystemProfilesOnly()
  {
    boolean systemConfig = systemConfiguration.getBoolean(Config.SYSTEM_PROFILES_ONLY, false);

    return systemConfig;
  }

  /**
   * Return the configured property of the simple mode
   *
   * @return true for simple mode
   */
  private boolean isSimpleMode()
  {
    return getSystemOrUserConfiguredBoolean(Config.SIMPLE_MODE);
  }

  public GuiViewMode getGuiViewMode()
  {
    String guiViewModeConfigValue = getUserValue(Config.GUI_VIEW_MODE, Config.GUI_VIEW_MODE_EDITABLE,
        GuiViewMode.UNKNOWN.getConfigurationValue());

    GuiViewMode gvm = GuiViewMode.fromConfigurationValue(guiViewModeConfigValue);

    if (gvm.isUnknown())
    {
      LOGGER.debug(Config.GUI_VIEW_MODE + " is not configured. This is the first start after upgrade. Considering "
          + Config.SIMPLE_MODE + "-value.");

      if (isSimpleMode())
      {
        return GuiViewMode.SIMPLE_MODE;
      }
      else
      {
        return GuiViewMode.PROFESSIONAL_MODE;
      }
    }

    return gvm;
  }

  private boolean getSystemOrUserConfiguredBoolean(String propName)
  {
    boolean systemConfig = systemConfiguration.getBoolean(propName, false);
    return userConfiguration.getBoolean(propName, systemConfig);
  }

  /**
   * Remove the settings menu from user interface.
   *
   * @return true if settings menu is hidden
   */
  public boolean isHideSettings()
  {
    return getSystemOrUserConfiguredBoolean(Config.SETTINGS_HIDDEN);
  }

  /**
   * Return the current version
   *
   * @return LocalSigner version
   */
  public String getVersion()
  {
    return userConfiguration.getString(Config.VERSION, EMPTY_STRING);
  }

  /**
   * Return screen position of last use
   *
   * @return saved screen position
   */
  public String getWindowPosition()
  {
    return userConfiguration.getString(Config.WINDOW_POSITION, EMPTY_STRING);
  }

  /**
   * Return signature language of last use
   *
   * @return signature language
   */
  public String getSignatureLanguage()
  {
    return userConfiguration.getString(Config.SIGNATURE_LANG, EMPTY_STRING);
  }

  /**
   * URL for the update check (return XML document)
   */
  public String getUpdateCheckUrl()
  {
    return systemConfiguration.getString(Config.UPDATECHECK_URL);
  }

  /**
   * The Card Reader name must match this regular expression
   */
  public String getReaderMatchRegexp()
  {
    return systemConfiguration.getString(Config.READER_MATCH_REGEXPPATTERN);
  }

  /**
   * Show all certificates in list
   */
  public boolean isShowAllCertificates()
  {
    return userConfiguration.getBoolean(Config.SHOW_ALL_CERTIFICATES, false);
  }

  /**
   * Enable experimental feature
   */
  public boolean isExperimental()
  {
    return userConfiguration.getBoolean(Config.EXPERIMENTAL, false);
  }

  /**
   * Enable debugging to file
   */
  public boolean isDebug()
  {
    return getSystemOrUserConfiguredBoolean(Config.DEBUG);
  }

  /**
   * Get PKCS#12 password from user properties
   */
  public String getPkcs12Password()
  {
    return userConfiguration.getString(Config.PKCS12_PASSWORD, null);
  }

  /**
   * Select internal proxies to show in list
   */
  public List<String> getInternalTsa()
  {
    // value is missing, default is all
    List<String> missing = new ArrayList<String>();
    missing.add(ALL_STRING);

    List<Object> systemConfig = systemConfiguration.getList(Config.INTERNAL_TSA, missing);
    List<Object> configs = new ArrayList<Object>();
    if (isEditable(Config.INTERNAL_TSA_EDIT))
    {
      configs = userConfiguration.getList(Config.INTERNAL_TSA, systemConfig);
    }
    List<String> tsaConfs = new ArrayList<String>();
    for (Object object : configs)
    {
      tsaConfs.add((String) object);
    }
    return tsaConfs;
  }

  /**
   * Get the file extension for a signed document.
   * If the value does not exist in the user configuration or is empty,
   * the value from the system configuration ("-sig") is returned.
   *
   * @return the defined file extension.
   */
  public String getSignedDocExtension()
  {
    String signedDocExtension = getSystemOrUserConfiguredValue(Config.SIGNED_DOC_EXTENSION).trim();
    if (signedDocExtension.isEmpty())
    {
      signedDocExtension = systemConfiguration.getString(Config.SIGNED_DOC_EXTENSION).trim();
    }
    return signedDocExtension;
  }

  public Map<String, String> detectPkcs11Lib()
  {
    return initializer.detectPkcs11Lib();
  }

  public final String getUserProfileFolder()
  {
    return initializer.getUserProfileFolder();
  }

  public final String getUserProfileCacheFolder()
  {
    return initializer.getUserProfileCacheFolder();
  }

  public final String getUserInternalConfig()
  {
    return initializer.getUserInternalConfig();
  }

  public final String getDebugFile()
  {
    return initializer.getDebugFile();
  }

  public final String getSystemProfileFolder(boolean useCache)
  {
    return initializer.getSystemProfileFolder(useCache);
  }

  public final String getUserConfig()
  {
    return initializer.getUserConfig();
  }

  public final String getUserConfigCacheFolder()
  {
    return initializer.getUserConfigCacheFolder();
  }

  public final void initUserConfiguration() throws ConfigurationException, IOException
  {
    initializer.initUserConfiguration();
    userConfiguration = initializer.getUserConfiguration();
  }

  public final String getUserConfigFolder()
  {
    return initializer.getUserConfigFolder();
  }

  public String getValidatorUrl()
  {
    return getSystemOrUserConfiguredValue(Config.VALIDATOR_URL);
  }

  private String getSystemOrUserConfiguredValue(String key)
  {
    String systemConfig = systemConfiguration.getString(key);
    return userConfiguration.getString(key, systemConfig);
  }

  public String getValidatorUser()
  {
    return getSystemOrUserConfiguredValue(Config.VALIDATOR_USER);
  }

  public String getValidatorPassword()
  {
    return getSystemOrUserConfiguredValue(Config.VALIDATOR_PASSWORD);
  }

  public String getFunktionsnachweisUrl()
  {
    return getSystemOrUserConfiguredValue(Config.FUNKTIONSNACHWEIS);
  }

  public boolean isFunknachweisAktiv()
  {
    return getSystemOrUserConfiguredBoolean(Config.FUNKTIONSNACHWEIS_AKTIV);
  }

  public boolean isSignaturepagesBundAktiv()
  {
    return getSystemOrUserConfiguredBoolean(Config.SIGNATUREPAGES_BUND);
  }

  public String getFunktionsnachweisCanton()
  {
    return userConfiguration.getString(Config.FUNKTIONSNACHWEIS_ZULAB_CANTON, null);
  }

  public String getFunktionsnachweisDomain()
  {
    return userConfiguration.getString(Config.FUNKTIONSNACHWEIS_ZULAB_DOMAIN, null);
  }

  public boolean isFunktionsnachweisShowDialog()
  {
    return userConfiguration.getBoolean(Config.FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG, true);
  }

  public String getUpdateCantonAndDomainListUrl()
  {
    String systemConfig = systemConfiguration.getString(Config.FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL);
    return userConfiguration.getString(Config.FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL, systemConfig);
  }

  public Map<String, ZulabConfiguration.Entry> getZulabCantons()
  {
    return zulabConfiguration.getCantons();
  }

  public Map<String, ZulabConfiguration.Entry> getZulabDomains()
  {
    return zulabConfiguration.getDomains();
  }

  public String getZulabListsVersion()
  {
    return zulabConfiguration.getVersion();
  }

  public File getZulabListsFile()
  {
    return zulabConfiguration.getFile();
  }

  public String getCantonalSealUpdateUrl()
  {
    return getSystemOrUserConfiguredValue(Config.CANTONAL_SEAL_UPDATE_URL);
  }

  public void setCantonalSealLastUpdateTimestamp(Date lastUpdate) throws ConfigurationException
  {
    SimpleDateFormat format = new SimpleDateFormat(CantonalSealInfoWsClient.LAST_MODIFIED_FORMAT, Locale.US);
    setValue(Config.CANTONAL_SEAL_UPDATE_TIMESTAMP, escapeComma(format.format(lastUpdate)));
  }

  private String escapeComma(String noList)
  {
    return noList.replaceAll(",", "\\\\,");
  }

  public Date getCantonalSealLastUpdateTimestamp()
  {
    try
    {
      SimpleDateFormat format = new SimpleDateFormat(CantonalSealInfoWsClient.LAST_MODIFIED_FORMAT, Locale.US);
      String lastModifiedValue = userConfiguration.getString(Config.CANTONAL_SEAL_UPDATE_TIMESTAMP,
          format.format(new Date(0L)));

      return format.parse(lastModifiedValue);
    } catch (ParseException e)
    {
      LOGGER.warn("Time stamp of last update of the cantonal seal xml is not parseable. "
          + "Returning 1.1.1970 to enforce update", e);

      return new Date(0L);
    }
  }

  public File getCantonalSealFile()
  {
    String config = systemConfiguration.getString(Config.CANTONAL_SEAL_FILE);
    if (systemConfiguration.getBoolean(Config.CANTONAL_SEAL_FILE_EDIT, false))
    {
      config = userConfiguration.getString(Config.CANTONAL_SEAL_FILE, config);
    }

    return Paths.get(getUserConfigFolder(), config).toFile();
  }

  public String getDefaultTenant()
  {
    return getSystemOrUserConfiguredValue(Config.VALIDATOR_DEFAULT_MANDANT);
  }
}
