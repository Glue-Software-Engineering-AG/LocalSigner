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
package ch.admin.localsigner.gui.profile;

import java.io.File;
import java.text.MessageFormat;
import java.util.NoSuchElementException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.config.resources.FileResources;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.profile.PropertiesGUI.ImageMode;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Loads the values of the specified profile to the PropertiesGUI.
 *
 * @author greiler (only outsourced from LoadProfileListener)
 * @author $Author$
 * @version $Revision$
 */
public class ProfileLoader
{


  private static final Logger LOGGER = Logger.getLogger(ProfileLoader.class);

  private static final String CERTIFICATION_SIGNATURE_TYPE = "certification";

  /**
   * Loads the specified profile.
   *
   * @param profile Name of the profile to load to the PropertiesGUI
   * @param maingui The MainGUI instance
   */
  public static void loadProfile(final MainGUI maingui, final Profile profile)
  {
    PropertiesGUI propertiesGui = maingui.getPropertiesGui();

    LOGGER.debug("Loading profile " + profile.getName());
    if (profile.getPath() == null)
    {
      return;
    }

    try
    {
      // load profile
      PropertiesConfiguration config = new PropertiesConfiguration();
      config.setDelimiterParsingDisabled(true);
      config.load(profile.getPath());

      profile.setConfig(config);
      LOGGER.debug("successfully loaded profile: " + profile.getName());

      // set value to PropertiesGUI
      propertiesGui.setProfilePath(profile.getPath());

      propertiesGui.setOutputDir(config.getString(Profile.OUTPUT_DIR));

      propertiesGui.setPdfAttachment(getPdfAttachementPath(config, propertiesGui));

      if (maingui.getInputFile() != null)
      {
        // from properties GUI to omit NullPointer
        maingui.getDocument().getInputFile().updateAttachment(propertiesGui.getPdfAttachment());
      }

      String outputdir = propertiesGui.getOutputDir();
      if (StringUtils.isEmpty(outputdir))
      {
        // there was no output directory specified in the profile - just make input path the output path.
        if (maingui.getInputFile()==null)
        {
          maingui.setOutputFile(maingui.getDocument().proposeOutputNameFinal(), false);
        }
      } else
      {
        // if there was an output directory specified in the profile - use it
        if (!outputdir.endsWith(File.separator))
        {
          // append file separator if necessary
          outputdir = outputdir + File.separator;
        }
        final int index = maingui.getOutputFile().lastIndexOf(File.separator);
        maingui.setOutputFile(outputdir + maingui.getOutputFile().substring(index + 1), false);
      }

      propertiesGui.setDisplaySignedDocument(config.getBoolean(Profile.SHOW_SIGNED_DOCUMENT));

      final String sigtype = config.getString(Profile.TYPE_OF_SIGNATURE);
      propertiesGui.setCertificationType(CERTIFICATION_SIGNATURE_TYPE.equals(sigtype));

      propertiesGui.setVisibleSignature(config.getBoolean(Profile.VISIBLE_SIGNATURE));

      String backgroundImgAsString = getBackgroundImageAsString(config);

      if (propertiesGui.isVisibleSignature())
      {
        propertiesGui.setLeftPos(config.getInt(Profile.LEFT_POS));
        propertiesGui.setTopPos(config.getInt(Profile.TOP_POS));
        propertiesGui.setBoxWidth(config.getInt(Profile.BOXWIDTH));
        propertiesGui.setBoxHeight(config.getInt(Profile.BOXHEIGHT));
        propertiesGui.setSignaturePageProfile(config.getInt(Profile.SIGN_ON));

        propertiesGui.setTextShownInSignature(config.getBoolean(
            Profile.SHOW_TEXT_IN_SIGNATURE, true));

        propertiesGui.setImageShownInSignature(config.getBoolean(
            Profile.SHOW_IMAGE_IN_SIGNATURE,
            StringUtils.isNotEmpty(backgroundImgAsString)));

        String imageMode = config
            .getString(Profile.IMAGE_MODE, ImageMode.NONE.toString());

        propertiesGui.setImageMode(imageMode);
        
        String customHeight = config.getString(Profile.IMAGE_SIZE_FIXED_HEIGHT);
        String customWidth = config.getString(Profile.IMAGE_SIZE_FIXED_WIDTH);
        if (bothSetAndNumeric(customHeight, customWidth))
        {
          propertiesGui.setFixedImageHeight(Integer.valueOf(customHeight));
          propertiesGui.setFixedImageWidth(Integer.valueOf(customWidth));
        }

        propertiesGui.setSecondSignatureYAxisFixed(config.getBoolean(
            Profile.ALIGN_SECOND_VISIBLE_SIGNATURE, false));
      }

      propertiesGui.setLocation(config.getString(Profile.LOCATION));
      propertiesGui.setContact(config.getString(Profile.CONTACT));
      propertiesGui.setContactLabelVisible(config.getBoolean(Profile.CONTACT_SHOW_LABEL, true));
      propertiesGui.setReason(config.getString(Profile.REASON));
      propertiesGui.setReasonLabelVisible(config.getBoolean(Profile.REASON_SHOW_LABEL, true));

      propertiesGui.setBackgroundImage(backgroundImgAsString);

      if (config.getBoolean(Profile.ENABLE_TIMESTAMPING))
      {
        String url = config.getString(Profile.TSAURL);
        String user = config.getString(Profile.TSAUSER);
        String pw = config.getString(Profile.TSAPASSWORD);
        if (StringUtils.isNotBlank(url))
        {
          propertiesGui.addCustomTsa(new TsaConfiguration(url, url, user, pw, url));
        } else {
          // LOCALSIG-279: Signatur muss immer mit Zeitstempel erfolgen
          propertiesGui.addCustomTsa(ApplicationConfiguration.getSwissGovernmentTSA());
        }
      } else
      {
        // LOCALSIG-279: Signatur muss immer mit Zeitstempel erfolgen
        propertiesGui.addCustomTsa(ApplicationConfiguration.getSwissGovernmentTSA());
      }

      propertiesGui.setProfilePath(profile.getPath());
      propertiesGui.enableSaveButton(profile.canSave());
      propertiesGui.doUpdateGuiAfterProfileLoaded();

      replaceImageIfNotExists(maingui, profile, propertiesGui, config);
    } catch (ConfigurationException ce)
    {
      Message.error(maingui.getMainshell(), LocalSigner.i18n("loadProfileError"));
      LOGGER.debug("could not load signature profile", ce);
    } catch (NoSuchElementException nsee)
    {
      Message.error(maingui.getMainshell(), LocalSigner.i18n("loadProfileError"));
      LOGGER.debug("could not load signature profile", nsee);
    }
  }

  private static void replaceImageIfNotExists(final MainGUI maingui,
      final Profile profile, PropertiesGUI propertiesGui, PropertiesConfiguration config)
  {
    File backgroundImage = getBackgroundImagePath(config, propertiesGui);
    if (backgroundImage != null && !backgroundImage.exists()
        && propertiesGui.isImageOnlyAndFixedSize())
    {
      // show warning if signature is visible
      String text = MessageFormat.format(
          LocalSigner.i18n("propertiesGUI.imageNotFoundSetToDefault"),
          getBackgroundImageAsString(config));
      Message.error(maingui.getMainshell(), text);
      String fallbackImg = ProfileFileLoader.findFile(FileResources.FALLBACK_IMAGE, profile.getPath());
      propertiesGui.setBackgroundImage(fallbackImg);
    }
  }

  private static boolean bothSetAndNumeric(String customHeight, String customWidth)
  {
    return StringUtils.isNotBlank(customHeight) && StringUtils.isNotBlank(customWidth)
        && StringUtils.isNumeric(customHeight) && StringUtils.isNumeric(customWidth);
  }

  private static File getBackgroundImagePath(PropertiesConfiguration profileConfig,
      PropertiesGUI propertiesGui)
  {
    String imageName = getBackgroundImageAsString(profileConfig);
    return ProfileFileLoader.loadFileFromPath(imageName, propertiesGui.getProfilePath());
  }

  private static String getBackgroundImageAsString(PropertiesConfiguration profileConfig)
  {
    return profileConfig.getString(Profile.BACKGROUND_IMAGE);
  }

  private static String getPdfAttachementPath(PropertiesConfiguration profileConfig, PropertiesGUI propertiesGui)
  {
    String attachmentName = profileConfig.getString(Profile.PDF_ATTACHMENT);

    return ProfileFileLoader.findFile(attachmentName, propertiesGui.getProfilePath());
  }



  private ProfileLoader()
  {
    // hide constructor of utility class
  }
}
