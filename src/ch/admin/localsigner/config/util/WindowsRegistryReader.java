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

import org.apache.log4j.Logger;

import java.io.*;

/**
 * Reads a value from the windows registry. Outsourced from ApplicationConfiguration.
 *
 * @author greiler
 * @author $Author$
 * @version $Revision$
 */
public class WindowsRegistryReader
{
  private static final Logger LOGGER = Logger.getLogger(WindowsRegistryReader.class);

  /*
   * note: a win32 process on win64 is redirected to a sepcial registry key! If
   * needed read http://en.wikipedia.org/wiki/WoW64
   */
  public static String readRegistry(String location, String key)
  {
    BufferedReader reader = null;
    InputStream is = null;
    try
    {
      String query = "reg query " + '"' + location + "\" /v " + key;
      Process process = Runtime.getRuntime().exec(query);

      is = process.getInputStream();
      StringWriter writer = new StringWriter();
      char[] buffer = new char[1024];
      reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      int n;
      while ((n = reader.read(buffer)) != -1)
      {
        writer.write(buffer, 0, n);
      }
      // Parse out the value
      // Output has the following format:
      // \n<Version information>\n\n<key>\t<registry type>\t<value>
      String[] parsed = writer.toString().split("\\s+");
      return parsed[parsed.length - 1];
    } catch (Exception e)
    {
      LOGGER.error("Cannot read registry value for " + location + " " + key, e);
      return null;
    } finally {
      try {
        if (reader!=null) {
          reader.close();
        } else if (is!=null) {
          is.close();
        }
      } catch (IOException e) {
        LOGGER.error("Cannot close buffer",e);
      }
    }
  }
}
