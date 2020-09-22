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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.main.LocalSigner;

public class ProfileCollector
{

  private static final Logger LOGGER = Logger.getLogger(ProfileCollector.class);

  private static final String PROPERTY_SUFFIX = ".properties";

  public static List<Profile> loadProfileCombo(String cliSignatureProfile)
  {
    List<Profile> selectableProfiles = new ArrayList<Profile>();
    File userProfileDir = new File(LocalSigner.appConfig.getUserProfileFolder());
    LOGGER.debug("Loading user profiles from " + userProfileDir.getAbsolutePath());

    File systemProfileDir = new File(LocalSigner.appConfig.getSystemProfileFolder(true));
    LOGGER.debug("Loading system profiles from " + systemProfileDir.getAbsolutePath());

    FilenameFilter filter = new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(PROPERTY_SUFFIX);
      }
    };

    // get all profile files
    String[] userProfiles;
    if (LocalSigner.appConfig.isSystemProfilesOnly())
    {
      // system profiles only, don't load user profiles to list
      userProfiles = null;
    }
    else
    {
      userProfiles = userProfileDir.list(filter);
    }
    String[] systemProfiles = systemProfileDir.list(filter);

    // if user has not configured a correct path
    if (userProfiles == null)
    {
      userProfiles = new String[0];
      LOGGER.debug("Setting user profiles to empty");
    }
    if (systemProfiles == null)
    {
      systemProfiles = new String[0];
      LOGGER.debug("Setting system profiles to empty");
    }

    // add profiles to combo box
    if (!LocalSigner.appConfig.isSystemProfilesOnly())
    {
      Profile custom = addProfileCustom(cliSignatureProfile);
      if (custom != null)
      {
        selectableProfiles.add(custom);
      }
      selectableProfiles.addAll(addProfileUser(userProfileDir, userProfiles));
    }

    if (LocalSigner.appConfig.getGuiViewMode().isProfessionalMode()
        || LocalSigner.appConfig.getGuiViewMode().isMinimalMode())
    {
      // add system profiles in pro and minimal mode
      selectableProfiles.addAll(addProfileSystem(systemProfileDir, systemProfiles));
    }

    LOGGER.debug("loaded "+selectableProfiles.size()+" profiles");
    return selectableProfiles;

  }

  private static Profile addProfileCustom(String cliSignatureProfile)
  {
    if (cliSignatureProfile != null)
    {
      File custom = new File(cliSignatureProfile);
      if (custom.exists())
      {
        String name = custom.getAbsolutePath()
            .replace(PROPERTY_SUFFIX, StringUtils.EMPTY);
        Profile customProfile = new Profile(ProfileType.CUSTOM_PROFILE, name,
            custom.getAbsolutePath());
        LOGGER.debug("add custom profile " + name + " from " + custom.getAbsolutePath());
        return customProfile;
      }
      else
      {
        LOGGER.debug("custom profile missing: " + custom.getAbsolutePath());
      }
    }
    return null;
  }

  private static List<Profile> addProfileUser(File userProfileDir, String[] userProfiles)
  {
    List<Profile> foundProfiles = new ArrayList<Profile>();
    for (String profile : userProfiles)
    {
      String name = profile.replace(PROPERTY_SUFFIX, StringUtils.EMPTY);
      String path = userProfileDir.getAbsolutePath() + File.separator + profile;
      ProfileType type = ProfileType.USER_PROFILE;
      if (Config.DEFAULT_PROFILE.equals(name))
      {
        type = ProfileType.DEFAULT_PROFILE;
      }
      else if (LocalSigner.appConfig.getGuiViewMode().isSimpleMode()
          || LocalSigner.appConfig.getGuiViewMode().isMinimalMode())
      {
        // skipping other profiles in simple and minimal mode
        continue;
      }

      Profile userProfile = new Profile(type, name, path);
      LOGGER.debug("add user profile " + name + " from " + path);
      foundProfiles.add(userProfile);
    }
    return foundProfiles;
  }

  private static List<Profile> addProfileSystem(File systemProfileDir,
      String[] systemProfiles)
  {
    List<Profile> foundProfiles = new ArrayList<Profile>();
    for (String profile : systemProfiles)
    {
      String name = profile.replace(PROPERTY_SUFFIX, Config.SYSTEM_PROFILE_MARK);
      String path = systemProfileDir.getAbsolutePath() + File.separator + profile;
      LOGGER.debug("add system profile " + name + " from " + path);
      Profile systemProfile = new Profile(ProfileType.SYSTEM_PROFILE, name, path);
      foundProfiles.add(systemProfile);
    }
    return foundProfiles;
  }

}
