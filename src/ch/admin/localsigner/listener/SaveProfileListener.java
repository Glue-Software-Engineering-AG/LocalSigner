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
package ch.admin.localsigner.listener;

import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.profile.Profile;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.gui.profile.PropertiesGUI.ImageMode;
import ch.admin.localsigner.main.LocalSigner;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * This class saves the profiles.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class SaveProfileListener implements Listener
{
  private static final String NEWLINE = "\n";

  private static final String PROPERTIES_FILE_SUFFIX = ".properties";

  private static final String TRUE = "true";

  private static final String FALSE = "false";

  private static final Logger LOGGER = Logger.getLogger(SaveProfileListener.class);

  private final MainGUI maingui;

  private final PropertiesGUI propertiesGui;

  private final Shell shell;

  private final boolean askName;

  private final boolean showConfirmation;

  private TsaConfiguration profileTsa;

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI
   * @param askName
   *          Ask for a file name (save as) or overwrite current file (save)
   * @param showConfirmation
   *          show message box with text after profile is successfully saved
   */
  public SaveProfileListener(final MainGUI maingui, final PropertiesGUI propertiesGui, final boolean askName,
    final boolean showConfirmation)
  {
    this.maingui = maingui;
    this.propertiesGui = propertiesGui;
    this.shell = maingui.getMainshell();
    this.askName = askName;
    this.showConfirmation = showConfirmation;
  }

  /**
   * When the save profile button gets pressed, we let the user choose an output
   * file name and and store the current configuration into this file.
   */
  @Override
  public void handleEvent(final Event event)
  {
    if (validateAndShowErrors())
    {
      return;
    }

    String fullPath = maingui.getSelectedProfile().getPath();

    if (askName)
    {
      fullPath = askForFileName();
    }

    if (StringUtils.isEmpty(fullPath))
    {
      // no file selected so just return
      LOGGER.debug("no file for storing profile selected - return");
      return;
    }
    LOGGER.debug("save profile: " + fullPath);

    // add ".properties" to the file name if it does not end with ".properties"
    if (!fullPath.endsWith(PROPERTIES_FILE_SUFFIX))
    {
      fullPath = fullPath + PROPERTIES_FILE_SUFFIX;
    }

    PropertiesConfiguration config = getConfig();

    try
    {
      config.save(fullPath);
      if (showConfirmation)
      {
        final MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
        mb.setText(LocalSigner.i18n("saveOk"));
        mb.setMessage(LocalSigner.i18n("saveOkExtended"));
        mb.open();
      }
    } catch (ConfigurationException e)
    {
      Message.error(shell, LocalSigner.i18n("saveError"));
      LOGGER.debug("could not save signature profile", e);
    }

    final String profileName = new File(fullPath).getName().replace(PROPERTIES_FILE_SUFFIX, StringUtils.EMPTY);

    // store the new profile as the last used profile to make it visible
    // once the user closes the property window
    storeLastUsedProfile(profileName);

    // set new items in main GUI
    maingui.loadProfileCombo();
    maingui.setSelectedProfile(profileName);

    // close properties GUI
    propertiesGui.close();
  }

  private boolean validateAndShowErrors()
  {
    StringBuilder errorMsg = new StringBuilder();
    if (propertiesGui.shouldShowImageInSignature()
        && StringUtils.isBlank(propertiesGui.getBackgroundImage()))
    {
      errorMsg.append(LocalSigner.i18n("propertiesGUI.specifyImage")).append(NEWLINE);
    }

    if (propertiesGui.isVisibleSignature() && !propertiesGui.shouldShowImageInSignature()
        && !propertiesGui.shouldShowTextInSignature())
    {
      errorMsg.append(LocalSigner.i18n("propertiesGUI.enableTextOrImage")).append(NEWLINE);
    }

    if (propertiesGui.useCustomImageSize()
        && (propertiesGui.getBoxHeightImage() == 0 || propertiesGui.getBoxWidthImage() == 0))
    {
      errorMsg.append(LocalSigner.i18n("propertiesGUI.specifyWidthHeight")).append(NEWLINE);
    }

    if (errorMsg.length() > 0)
    {
      final MessageBox mb = new MessageBox(propertiesGui.getShell(), SWT.ICON_ERROR);
      mb.setText(LocalSigner.i18n("propertiesGUI.cannotSaveProfile"));
      mb.setMessage(errorMsg.toString());
      mb.open();
    }

    return errorMsg.length() > 0;
  }

  /**
   * Stores the last used profile in the internal config property file
   *
   * @param profileName
   */
  private void storeLastUsedProfile(final String profileName)
  {
    final Properties properties = new Properties();

    // Write properties file.
    try
    {
      properties.setProperty(Config.LAST_USED_PROFILE, profileName);
      properties.store( new FileOutputStream(LocalSigner.appConfig.getUserInternalConfig()), null);
      LOGGER.debug("updated lastUsedProfile to: " + profileName);
    } catch (IOException e)
    {
      LOGGER.warn("could not store last used profile - simply procceed", e);
    }
  }

  public void setProfileTsa(TsaConfiguration profileTsa)
  {
    this.profileTsa = profileTsa;
  }

  private String askForFileName()
  {
    // file name proposal
    String proposedFilename = maingui.getSelectedProfile().getName();
    int start = proposedFilename.indexOf(Config.SYSTEM_PROFILE_MARK);
    if (start > 0)
    {
      // remove "(System)" from profile name, add copy before
      // (replaceAll not working with "(" in name)
      proposedFilename = proposedFilename.substring(0, start)
        + proposedFilename.substring(start + Config.SYSTEM_PROFILE_MARK.length());
      proposedFilename = "copy-" + proposedFilename;
    }

    final FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
    dialog.setFilterPath(LocalSigner.appConfig.getUserProfileFolder());
    dialog.setFilterNames(new String[]
    {
      "Signature Profiles"
    });
    dialog.setFilterExtensions(new String[]
    {
      "*.properties"
    });
    dialog.setFileName(proposedFilename);
    return dialog.open();
  }

  private PropertiesConfiguration getConfig()
  {

    PropertiesConfiguration config = new PropertiesConfiguration();
    config.setDelimiterParsingDisabled(true);

    config.addProperty(Profile.VERSION, GuiHelper.getVersion());

    config.addProperty(Profile.OUTPUT_DIR, propertiesGui.getOutputDir());
    config.addProperty(Profile.PDF_ATTACHMENT, propertiesGui.getPdfAttachment());

    config.addProperty(Profile.SHOW_SIGNED_DOCUMENT,
        propertiesGui.isDisplaySignedDocument());

    String sigtype = "signature";
    if (propertiesGui.isCertificationType())
    {
      sigtype = "certification";
    }
    config.addProperty(Profile.TYPE_OF_SIGNATURE, sigtype);

    config.addProperty(Profile.VISIBLE_SIGNATURE, propertiesGui.isVisibleSignature());

    config.addProperty(Profile.LOCATION, propertiesGui.getLocation());
    config.addProperty(Profile.REASON, propertiesGui.getReason());
    config.addProperty(Profile.REASON_SHOW_LABEL, propertiesGui.getReasonLabelVisible());
    config.addProperty(Profile.CONTACT, propertiesGui.getContact());
    config
        .addProperty(Profile.CONTACT_SHOW_LABEL, propertiesGui.getContactLabelVisible());

    config.addProperty(Profile.BACKGROUND_IMAGE, propertiesGui.getBackgroundImage());

    if (propertiesGui.isVisibleSignature())
    {
      config.addProperty(Profile.LEFT_POS, propertiesGui.getLeftPos());
      config.addProperty(Profile.TOP_POS, propertiesGui.getTopPos());
      config.addProperty(Profile.BOXWIDTH, propertiesGui.getBoxWidth());
      config.addProperty(Profile.BOXHEIGHT, propertiesGui.getBoxHeight());
      config.addProperty(Profile.SIGN_ON, propertiesGui.getSignaturePageProfile());

      config.addProperty(Profile.SHOW_TEXT_IN_SIGNATURE,
          propertiesGui.shouldShowTextInSignature());
      config.addProperty(Profile.SHOW_IMAGE_IN_SIGNATURE,
          propertiesGui.shouldShowImageInSignature());
      config.addProperty(Profile.IMAGE_MODE, propertiesGui.getImageMode());

      if (propertiesGui.getImageMode() == ImageMode.FROM_USER)
      {
        String height = propertiesGui.getFixedImageHeight();
        String width = propertiesGui.getFixedImageWidth();

        config.addProperty(Profile.IMAGE_SIZE_FIXED_HEIGHT, height);
        config.addProperty(Profile.IMAGE_SIZE_FIXED_WIDTH, width);
      }

      if (propertiesGui.shouldSecondSignatureYAxisBeFixed())
      {
        config.addProperty(Profile.ALIGN_SECOND_VISIBLE_SIGNATURE, TRUE);
      }

    }

    // save TSA configuration
    TsaConfiguration configuredTsa = propertiesGui.getTsaSelection();

    if (StringUtils.isEmpty(configuredTsa.getUrl())
        && maingui.getSelectedProfile().isDefaultType() && profileTsa != null)
    {
      // don't save no TSA in default profile; use the old TSA
      configuredTsa = profileTsa;
    }

    if (StringUtils.isEmpty(configuredTsa.getUrl()))
    {
      config.addProperty(Profile.ENABLE_TIMESTAMPING, FALSE);
    }
    else
    {
      // check if it is an internal TSA
      String systemTsaName = null;
      List<TsaConfiguration> systemTsa = LocalSigner.appConfig.getTSAConfig();
      for (TsaConfiguration sysTsa : systemTsa)
      {
        if (sysTsa.getUrl().equals(configuredTsa.getUrl()))
        {
          systemTsaName = sysTsa.getLookupKey();
          break;
        }
      }

      if (systemTsaName != null)
      {
        // save system TSA, only the description (URL is secret)
        config.addProperty(Profile.ENABLE_TIMESTAMPING, TRUE);
        config.addProperty(Profile.TSAURL, systemTsaName);
      }
      else
      {
        // save full TSA info
        config.addProperty(Profile.ENABLE_TIMESTAMPING, TRUE);
        config.addProperty(Profile.TSAURL, configuredTsa.getUrl());
        config.addProperty(Profile.TSAUSER, configuredTsa.getUsername());
        config.addProperty(Profile.TSAPASSWORD, configuredTsa.getPassword());
      }
    }

    return config;
  }
}
