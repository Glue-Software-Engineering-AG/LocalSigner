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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.config.util.BasePath;
import ch.admin.localsigner.config.util.ConfigurationVariablesReplacer;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Takes care of getting the profile / attachment pdf file.
 * 
 * 
 * @author weisskopf
 *
 */
public class ProfileFileLoader
{

  private static final Logger LOGGER = Logger.getLogger(ProfileFileLoader.class);

  public static File loadFileFromPath(String file, String profilePath)
  {
    if (StringUtils.isBlank(file))
    {
      return null;
    }

    File theFile = tryToLoadTheFile(file, profilePath);
    return theFile;
  }

  public static String findFile(String file, String profilePath)
  {
    if (StringUtils.isBlank(file))
    {
      return "";
    }

    File theFile = tryToLoadTheFile(file, profilePath);
    return theFile.getAbsolutePath();
  }

  private static File tryToLoadTheFile(String file, String profilePath)
  {
    if (SystemUtils.IS_OS_WINDOWS)
    {
      file = expandWindowsVariables(file);
    }

    File theFile = new File(file);

    if (!theFile.exists())
    {
      LOGGER.info("Cannot find file: " + theFile.getAbsolutePath());
      theFile = tryRelativeToProfileDirectory(file, profilePath);
    }

    if (!theFile.exists())
    {
      LOGGER.info("Cannot find file: " + theFile.getAbsolutePath());
      theFile = tryRelativeToSystemProfileDir(file);
    }

    if (!theFile.exists())
    {
      LOGGER.info("Cannot find file: " + theFile.getAbsolutePath());
      theFile = tryRelativeToBaseDir(file);
    }

    return theFile;
  }

  private static String expandWindowsVariables(String file)
  {
    String expanded = ConfigurationVariablesReplacer.replaceWindowsVariables(file);
    if (expanded != null)
    {
      file = expanded;
    }
    return file;
  }

  private static File tryRelativeToBaseDir(String file)
  {
    File basePath = new File(BasePath.getBasePath());
    return new File(basePath, file);
  }

  private static File tryRelativeToSystemProfileDir(String file)
  {
    File profileDir = new File(LocalSigner.appConfig.getSystemProfileFolder(true));
    return new File(profileDir, file);
  }

  private static File tryRelativeToProfileDirectory(String file, String profilePath)
  {
    File profileDir = new File(profilePath).getParentFile();
    return new File(profileDir, file);
  }

}
