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
package ch.admin.localsigner.main;

import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

/**
 * Under some circumstances the JVM does not delete temporary files created by LocalSigner at shutdown. For this
 * reason we go through all files in the temp-directory prefixed by the value of TEMP_FILE_PREFIX and delete the files
 * created by a previous instance. To avoid deleting files of another instance running alongside, we just delete files
 * older than the value in MIN_AGE.
 * Every created temporary file must therefor begin with the prefix stored in TEMP_FILE_PREFIX!
 *
 * Fixes Mantis 5992.
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public class TempFilesCleanerUtil
{

  private static final Logger LOGGER = Logger.getLogger(TempFilesCleanerUtil.class);

  public static final String TEMP_FILE_PREFIX = "LocalSigner_";

  private static final long MIN_AGE = 24L * 60L * 60L * 1000L; // 24 h = 1 day

  static int cleanOldTemporaryFiles()
  {
    int counter = 0;
    try
    {
      File tempDir = getTemporaryFilesDirectory();

      FilenameFilter localSignerTemporaryFiles = new PrefixFileFilter(TEMP_FILE_PREFIX);

      File[] oldTempFiles = tempDir.listFiles(localSignerTemporaryFiles);

      long lastModifiedMaxDate = new Date().getTime() - MIN_AGE;
      if (oldTempFiles != null)
      {
        for (File oldTempFile : oldTempFiles)
        {

          if (oldTempFile.lastModified() < lastModifiedMaxDate)
          {
            if (oldTempFile.delete())
            {
              counter++;
            }
            else
            {
              LOGGER.info("not able to delete old temporary file: "
                  + oldTempFile.getAbsolutePath());
            }
          }
          else
          {
            LOGGER.debug(oldTempFile.getAbsolutePath()
                + " is not old enough, probably of a LS running alongside.");
          }
        }
      }
    } catch (IOException ioe)
    {
      LOGGER.warn("not able to delete old temporary files", ioe);
    }

    return counter;
  }

  private static File getTemporaryFilesDirectory() throws IOException
  {
    String tempDirName = System.getProperty("java.io.tmpdir");

    if (tempDirName == null)
    {
      //http://www.mkyong.com/java/how-to-get-the-temporary-file-path-in-java/

      //create a temp file
      File temp = File.createTempFile(TEMP_FILE_PREFIX + "tempfile-delete", ".tmp");

      //Get tempropary file path
      String absolutePath = temp.getAbsolutePath();

      tempDirName = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
    }

    File tempDir = new File(tempDirName);

    if (!tempDir.isDirectory())
    {
      throw new IOException("Temporary directory " + tempDirName + " is not a directory");
    }
    if (!tempDir.canRead())
    {
      throw new IOException("Temporary directory " + tempDirName + " is not readable");
    }
    if (!tempDir.canWrite())
    {
      throw new IOException("Temporary directory " + tempDirName + " is not writable");
    }

    return tempDir;
  }

  private TempFilesCleanerUtil()
  {
    // one does never instantiate a util class
  }
}
