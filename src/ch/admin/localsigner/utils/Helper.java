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
package ch.admin.localsigner.utils;

import org.apache.log4j.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper class for various functions.
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class Helper
{
  private static final Logger LOGGER = Logger.getLogger(Helper.class);

  private static final String DATEFORMAT = "yyyyMMddHHmm";

  private Helper()
  {
    // hide constructor for utility class
  }

  /**
   * Backup a file by moving it and append the date as suffix.
   * @param file File to backup
   */
  public static void backup(File file) {
    String date = new SimpleDateFormat(DATEFORMAT).format(new Date());
    File backup = new File(file.getAbsolutePath() + "_" + date);
    boolean success = file.renameTo(backup);
    String message = "Backup file to " + backup.getAbsolutePath() + "was successful: " + success;
    if (!success){
      LOGGER.error(message);
    } else{
      LOGGER.debug(message);
    }
  }

}
