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

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Replaces variables with values in the configuration.
 *
 * @author greiler (only outsourced from ApplicationConfiguration)
 * @author $Author$
 * @version $Revision$
 */
public class ConfigurationVariablesReplacer
{
  private static final Logger LOGGER = Logger.getLogger(ConfigurationVariablesReplacer.class);

 public static String replaceVariables(String input)
  {
    String res = input;
    if (res == null)
    {
      return null;
    }

    String os = System.getProperty("os.name");

    String username = System.getProperty("user.name");
    if (StringUtils.isNotEmpty(username))
    {
      // replace $username with system user name
      res = res.replace("$username", username);
    }

    String userhome = System.getProperty("user.home");
    if (StringUtils.isNotEmpty(userhome))
    {
      // replace $userhome with system user home
      res = res.replace("$userhome", userhome);
    }

    String homeshare = System.getenv("HOMESHARE");
    if (StringUtils.isNotEmpty(homeshare))
    {

      // replace $homeshare with value
      res = res.replace("$homeshare", homeshare);

      // replace $bundhome with value
      if (os.contains("Windows XP"))
      {
        // synchronized in Windows XP
        res = res.replace("$bundhome", homeshare + File.separator + "windata");
      }
      else
      {
        // synchronized in Windows 7, 8, 10,...
        res = res.replace("$bundhome", homeshare + File.separator + "config");
      }
    }
    else if (res.contains("$homeshare") || res.contains("$bundhome"))
    {
      LOGGER.debug(input + ": $homeshare or $bundhome variable used but not found");
      return null;
    }

    // Windows specific
    if (os.contains("Windows") && res.contains("$depagency"))
    {
      String reg = WindowsRegistryReader.readRegistry("HKLM\\Software\\APS", "DepAgency");
      if (StringUtils.isNotEmpty(reg))
      {
        // replace $depagency with value
        res = res.replace("$depagency", reg);
      }
      else
      {
        if (input.contains("$depagency"))
        {
          LOGGER.error(input + ": $depagency variable used but not set in registry");
        }
        // else: $depagency not used

        return null;
      }
    }

    if (os.contains("Windows"))
    {
      res = replaceWindowsVariables(res);
      if (res == null)
      {
        return null;
      }
    }

    // try to find custom configuration for current user
    File allDir = new File(res, "all");
    if (allDir.exists() && allDir.isDirectory())
    {
      File customDir = new File(res, username);
      if (customDir.exists() && customDir.isDirectory())
      {
        return customDir.getAbsolutePath();
      }
      else
      {
        return allDir.getAbsolutePath();
      }
    }

    return res;
  }

  /**
   * Replace any Windows environment variable, all of them. Returns NULL if any
   * var can not be expanded!
   */
  public static String replaceWindowsVariables(String string)
  {
    int start = string.indexOf('%');
    int stop = string.indexOf('%', start + 1);
    while (start >= 0 && stop > 1)
    {
      // querry value for variable
      String var = string.substring(start + 1, stop);
      String val = System.getenv(var);

      LOGGER.debug("Variable: %" + var + "% = " + val);

      // break up if value cannot be found
      if (StringUtils.isEmpty(val))
      {
        LOGGER.error(string + ": " + var + " variable used but not found");
        return null;
      }

      // replace variable with value
      string = string.replace("%" + var + "%", val);

      // prepare next loop
      start = string.indexOf('%');
      stop = string.indexOf('%', start + 1);
    }

    return string;
  }
}