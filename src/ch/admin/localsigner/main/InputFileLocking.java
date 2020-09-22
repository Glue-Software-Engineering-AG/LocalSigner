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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.apache.log4j.Logger;
import ch.admin.localsigner.main.exception.FileOpenException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.*;

/**
 * Encapsulates the handling of file locking needed for the appmode = subprocess.
 */
class InputFileLocking
{

  private static final Logger LOGGER = Logger.getLogger(InputFileLocking.class);

  private FileChannel inputFileChannel = null;

  private FileLock inputFileLock = null;

  /**
   * @return true falls kein FileChannel offen ist und der Lock valide ist. Sonst false.
   */
  boolean isLocked()
  {
    return inputFileChannel != null && inputFileLock != null && inputFileLock.isValid();
  }

  void lockFile(final String inputFileName) throws IOException, FileOpenException
  {
    if (inputFileLock != null)
    {
      String problem = "lockFile: inputFileLock not null";
      LOGGER.error(problem);
      throw new FileOpenException(problem, FileOpenException.Reason.UNSPECIFIED);
    }
    if (inputFileChannel != null)
    {
      String problem = "lockFile: inputFileChannel not null";
      LOGGER.error(problem);
      throw new FileOpenException(problem, FileOpenException.Reason.UNSPECIFIED);
    }
    Path inputFilePath = Paths.get(inputFileName);
    inputFileChannel = FileChannel.open(inputFilePath, READ, WRITE);
    inputFileLock = inputFileChannel.tryLock();
    if (inputFileLock == null)
    {
      String problem = "lockFile: inputFileLock == null => no lock acquired "
          + "because another program holds an overlapping lock";
      LOGGER.error(problem);
      throw new FileOpenException(problem, FileOpenException.Reason.FILE_IS_LOCKED);
    } else if (!inputFileLock.isValid())
    {
      String problem = "lockFile: inputFileLock not valid";
      LOGGER.error(problem);
      throw new FileOpenException(problem, FileOpenException.Reason.UNSPECIFIED);
    }
    LOGGER.debug("lockFile: locked file " + inputFileName);
  }

  void unlockFile(final String inputFileName)
  {
    try
    {
      // Release the lock - if it is not null!
      if (inputFileLock != null)
      {
        if (inputFileLock.isValid())
        {
          inputFileLock.release();
          inputFileLock = null;
          LOGGER.debug("unlockFile: Unlocked "
              + (inputFileName != null ? inputFileName : "noFile"));
        }
        else
        {
          LOGGER.debug("unlockFile: Input lock was not valid, filename "
              + (inputFileName != null ? inputFileName : "noFile"));
        }
      }
      else
      {
        LOGGER.debug("unlockFile: Input lock was null, filename "
            + (inputFileName != null ? inputFileName : "noFile"));
      }
    } catch (Exception exc)
    {
      LOGGER.error("unlockFile: error unlocking", exc);
    } finally
    {
      if (inputFileChannel != null)
      {
        try
        {
          inputFileChannel.close();
          inputFileChannel = null;
        } catch (IOException e)
        {
          LOGGER.error("unlockFile: inputFileChannel close error in finally", e);
        }
      }
    }
  }

  byte[] getDataFromLockedFile() throws IOException
  {
    inputFileChannel.position(0);
    ByteArrayOutputStream ous = new ByteArrayOutputStream();

    final ByteBuffer buffer = ByteBuffer.allocate(4096);
    try
    {
      int read;
      while ((read = inputFileChannel.read(buffer)) != -1)
      {
        ous.write(buffer.array(), 0, read);
        buffer.rewind();
      }
    } catch (IOException e)
    {
      LOGGER.error("getFileContents: ", e);
      throw e;
    }
    return ous.toByteArray();
  }

  /**
   * @return true falls ein FileChannel offen ist und der Lock valide ist. Sonst
   *         false.
   */
  boolean isWriteable()
  {
    return inputFileChannel == null || inputFileLock == null || inputFileLock.isValid();
  }

  void overWriteInputFile(byte[] fileData) throws IOException
  {
    inputFileChannel.position(0);
    ByteBuffer buf = ByteBuffer.allocate(fileData.length);
    buf.clear();
    buf.put(fileData);
    buf.flip();
    while (buf.hasRemaining())
    {
      inputFileChannel.write(buf);
    }
    inputFileChannel.truncate(fileData.length);
  }

}
