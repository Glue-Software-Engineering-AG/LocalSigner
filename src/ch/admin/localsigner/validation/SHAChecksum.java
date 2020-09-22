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
package ch.admin.localsigner.validation;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class calculates the check sum of a file. The checksum uses SHA 256 as
 * hashing algorithm.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public final class SHAChecksum
{
  private SHAChecksum()
  {
    // hide constructor for utility class
  }

  /**
   * Calculate the hash of a file
   * @param filaData byteArray of File to hash
   * @return hash as byte[]
   * @throws NoSuchAlgorithmException
   * @throws Exception
   */
  private static byte[] createChecksum(final byte[] filaData)
          throws IOException, NoSuchAlgorithmException
  {
    final MessageDigest complete = MessageDigest.getInstance("SHA-256");
    complete.update(filaData);
    return complete.digest();
  }

  /**
   * Calculate the hash of a file
   * @param filaData
   *          byteArray of File to hash
   * @return hash as hex string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String getChecksum(final byte[] filaData)
          throws NoSuchAlgorithmException, IOException
  {
    byte[] b = createChecksum(filaData);

    final StringBuilder result = new StringBuilder();
    for (byte aB : b) {
      result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString();
  }


  /**
   * Calculate the hash of a file
   * @param filename
   *          File to hash
   * @return hash as hex string
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static String getChecksum(final String filename)
          throws NoSuchAlgorithmException, IOException
  {
    byte[] b = createChecksum(filename);

    final StringBuilder result = new StringBuilder();
    for (byte aB : b) {
      result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
    }
    return result.toString();
  }

  /**
   * Calculate the hash of a file
   * @param filename
   *          File to hash
   * @return hash as byte[]
   * @throws NoSuchAlgorithmException
   * @throws Exception
   */
  private static byte[] createChecksum(final String filename)
          throws IOException, NoSuchAlgorithmException
  {
    final MessageDigest complete = MessageDigest.getInstance("SHA-256");
    byte[] data = FileUtils.readFileToByteArray(new File(filename));
    complete.update(data);
    return complete.digest();
  }
}
