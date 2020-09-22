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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for the application configuration files.
 *
 */
public final class Config
{
  // the name of the default profile
  public static final String DEFAULT_PROFILE = "default";

  /**
   * Stores the name of the last used signature profile.
   */
  public static final String LAST_USED_PROFILE = "lastusedprofile";

  // marker for the system profiles in the profile combo
  public static final String SYSTEM_PROFILE_MARK = " (System)";

  // Main system configuration file
  public static final String INIT_CONFIG = "init.properties";

  public static final String DRIVERS_CONFIG = "drivers.properties";

  // Mandants configuration file
  public static final String TENANTS_CONFIG = "tenants.xml";

  // Cantons and Domains for Funktionsnachweis
  public static final String ZULAB_CONFIG = "zulab.xml";

  // Internal master default profile
  public static final String MASTER_DEFAULT_PROFILE = "internaldefaultprofile.properties";

  /**
   * Values in init.properties configuration file
   */
  // path to signature profiles
  public static final String PROFILE_PATH = "profilepath";

  public static final String PROFILE_PATH_EDIT = "profilepathEditable";

  // language of interface
  public static final String LANGUAGE = "language";

  public static final String LANGUAGE_EDIT = "languageEditable";

  // external config directory (to use on share)
  public static final String CONFIG_PATH = "configPath";

  // proxy configuration
  public static final String PROXY_HTTP_HOST = "proxyhost";

  public static final String PROXY_HTTP_PORT = "proxyport";

  public static final String PROXY_HTTPS_HOST = "proxyhttpshost";

  public static final String PROXY_HTTPS_PORT = "proxyhttpsport";

  public static final String PROXY_EXCLUSIONS = "proxyExclusions";

  public static final String PAC_URI = "pacURI";

  public static final String PROXY_CONFIGURATION_KIND = "proxyConfigurationKind";

  public static final String PROXY_EDIT = "proxyEditable";

  // viewer configuration
  public static final String INTERNALVIEWER = "internalviewer";

  public static final String INTERNALVIEWER_EDIT = "internalviewerEditable";

  // font size of interface
  public static final String FONTSIZE = "fontsize";

  public static final String FONTSIZE_EDIT = "fontsizeEditable";

  // cert store
  public static final String PKCS11_LIB = "pkcs11Lib";

  public static final String PKCS11_LIB_EDIT = "pkcs11LibEditable";

  public static final String PKCS12_FILE = "pkcs12File";

  public static final String PKCS12_FILE_EDIT = "pkcs12FileEditable";

  // intergity check
  public static final String INTEGRITY_CHECK = "showIntegrityCheck";

  public static final String INTEGRITY_CHECK_EDIT = "showIntegrityCheckEditable";

  // Sign non-PDF/A conformant documents
  public static final String SIGN_NON_PDF_A = "signNonPdfA";

  public static final String SIGN_NON_PDF_A_EDIT = "signNonPdfAEditable";

  // File extension for signed documents
  public static final String SIGNED_DOC_EXTENSION = "signedDocExtension";

  // Acticate side panel
  public static final String SIDE_PANEL_ACTIVE = "sidePanelActive";

  public static final String SIDE_PANEL_ACTIVE_EDIT = "sidePanelActiveEditable";

  // ltv
  public static final String LTV_ACTIVE = "ltvActive";

  public static final String LTV_ACTIVE_EDIT = "ltvActiveEditable";

  public static final String LTV_OCSP_ACTIVE = "ocspActive";

  public static final String LTV_OCSP_ACTIVE_EDIT = "ocspActiveEditable";

  // input path
  public static final String INPUT_PATH = "inputpath";

  public static final String INPUT_PATH_EDIT = "inputpathEditable";

  // limit to system profiles only
  public static final String SYSTEM_PROFILES_ONLY = "systemProfilesOnly";

  // simple user interface
  // depricated: now the GUI_VIEW_MODE parameter should be used. This value remains for backwards compatibility reasons.
  public static final String SIMPLE_MODE = "simpleMode";

  // user interface mode (extension of the SIMPLE_MODE boolean value).
  public static final String GUI_VIEW_MODE = "guiViewMode";

  public static final String GUI_VIEW_MODE_EDITABLE = "guiViewModeEditable";

  // hide settings menu from user interface
  public static final String SETTINGS_HIDDEN = "hideSettings";

  // optional user folder
  public static final String USER_FOLDER = "userFolder";

  // url for update check
  public static final String UPDATECHECK_URL = "updateCheckUrl";

  // show all certificates in certificate dialog
  public static final String SHOW_ALL_CERTIFICATES = "showAllCertificates";

  // if set, the reader name of a token must match the reg exp pattern to be displayed
  public static final String READER_MATCH_REGEXPPATTERN = "readerMatchPattern";

  // time stamp (TSA)
  public static final String TIMESTAMPDESCRIPTION = "timestampdescription";

  public static final String TIMESTAMPPASSWORD = "timestamppassword";

  public static final String TIMESTAMPUSER = "timestampuser";

  public static final String TIMESTAMPSERVER = "timestampserver";

  public static final String INTERNAL_TSA = "internalTsa";

  public static final String INTERNAL_TSA_EDIT = "internalTsaEditable";

  // last window position
  public static final String WINDOW_POSITION = "windowPosition";

  // language of visible signature
  public static final String SIGNATURE_LANG = "signatureLang";

  // configuration file properties
  public static final String VERSION = "version";

  public static final String DATE = "date";

  public static final String EXPERIMENTAL = "experimental";

  public static final String DEBUG = "debug";

  public static final String PKCS12_PASSWORD = "pkcs12password";

  // validator
  public static final String VALIDATOR_URL = "validatorUrl";
  public static final String VALIDATOR_DEFAULT_MANDANT = "validatorMandant";
  public static final String VALIDATOR_USER = "validatorUser";
  public static final String VALIDATOR_PASSWORD = "validatorPassword";

  // Funktionsnachweis
  public static final String FUNKTIONSNACHWEIS = "funknachweisUrl";

  public static final String FUNKTIONSNACHWEIS_AKTIV = "funknachweisAktiv";

  public static final String FUNKTIONSNACHWEIS_ZULAB_CANTON = "zulabCanton";

  public static final String FUNKTIONSNACHWEIS_ZULAB_DOMAIN = "zulabDomain";

  public static final String FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG = "zulabShowDialog";

  public static final String FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL = "zulabListUpdateUrl";

  public static final String FUNKTIONSNACHWEIS_ZULAB_CANTON_EDIT = "zulabCantonEditable";
  public static final String FUNKTIONSNACHWEIS_ZULAB_DOMAIN_EDIT = "zulabDomainEditable";
  public static final String FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG_EDIT = "zulabShowDialogEditable";
  public static final String FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL_EDIT= "zulabListUpdateUrlEditable";

  public static final String CANTONAL_SEAL_UPDATE_URL = "cantonalSealUpdateUrl";
  public static final String CANTONAL_SEAL_UPDATE_URL_EDIT = "cantonalSealUpdateUrlEditable";
  public static final String CANTONAL_SEAL_UPDATE_TIMESTAMP = "cantonalSealUpdateTimeStamp";
  public static final String CANTONAL_SEAL_UPDATE_TIMESTAMP_EDIT = "cantonalSealUpdateTimeStampEditable";
  public static final String CANTONAL_SEAL_FILE = "cantonalSealFile";
  public static final String CANTONAL_SEAL_FILE_EDIT = "cantonalSealFileEditable";

  public static final String SIGNATUREPAGES_BUND = "signaturepages_bund";

  private Config()
  {
    // hide constructor for utility class
  }

  /**
   * Getter for all property names as a list to be able to iterate over them
   *
   * @return List of properties the user can change
   */
  public static List<String> getUserPropertyNames()
  {
    final ArrayList<String> properties = new ArrayList<String>();

    properties.add(INTEGRITY_CHECK);

    // make sure to overwrite this for the default user configuration!
    properties.add(PROFILE_PATH);

    properties.add(LANGUAGE);
    properties.add(PROXY_HTTP_HOST);
    properties.add(PROXY_HTTP_PORT);
    properties.add(PROXY_HTTPS_HOST);
    properties.add(PROXY_HTTPS_PORT);
    properties.add(PROXY_EXCLUSIONS);
    properties.add(PROXY_CONFIGURATION_KIND);
    properties.add(PAC_URI);
    properties.add(INTERNALVIEWER);
    properties.add(FONTSIZE);
    properties.add(INPUT_PATH);
    /*
     * Timestamping default values are configured ONLY in the system config
     * TIMESTAMPDESCRIPTION TIMESTAMPPASSWORD
     * TIMESTAMPUSER TIMESTAMPSERVER
     */
    properties.add(PKCS11_LIB);
    properties.add(PKCS12_FILE);
    properties.add(INTERNAL_TSA);

    properties.add(FUNKTIONSNACHWEIS_ZULAB_CANTON);
    properties.add(FUNKTIONSNACHWEIS_ZULAB_DOMAIN);
    properties.add(FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG);
    properties.add(FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL);

    properties.add(CANTONAL_SEAL_UPDATE_TIMESTAMP);
    properties.add(CANTONAL_SEAL_UPDATE_URL);
    properties.add(CANTONAL_SEAL_FILE);

    properties.add(SIGNED_DOC_EXTENSION);

    return properties;
  }

  /**
   * Getter for all property edit name
   *
   * @return List of properties the user is allowed to change
   */
  protected static List<String> getEditableNames()
  {
    final ArrayList<String> editables = new ArrayList<String>();

    editables.add(PROFILE_PATH_EDIT);
    editables.add(LANGUAGE_EDIT);
    editables.add(PROXY_EDIT);
    editables.add(INTERNALVIEWER_EDIT);
    editables.add(FONTSIZE_EDIT);
    editables.add(PKCS11_LIB_EDIT);
    editables.add(PKCS12_FILE_EDIT);
    editables.add(INTEGRITY_CHECK_EDIT);
    editables.add(SIGN_NON_PDF_A_EDIT);
    editables.add(SIDE_PANEL_ACTIVE_EDIT);
    editables.add(INPUT_PATH_EDIT);
    editables.add(INTERNAL_TSA_EDIT);

    editables.add(FUNKTIONSNACHWEIS_ZULAB_CANTON_EDIT);
    editables.add(FUNKTIONSNACHWEIS_ZULAB_DOMAIN_EDIT);
    editables.add(FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG_EDIT);
    editables.add(FUNKTIONSNACHWEIS_ZULAB_DOMAIN_CANTON_LIST_UPDATE_URL_EDIT);

    editables.add(CANTONAL_SEAL_UPDATE_URL_EDIT);
    editables.add(CANTONAL_SEAL_UPDATE_TIMESTAMP_EDIT);
    editables.add(CANTONAL_SEAL_FILE_EDIT);

    return editables;
  }

}
