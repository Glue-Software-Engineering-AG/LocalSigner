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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import org.junit.Test;
import ch.admin.localsigner.update.UpdateChecker;

/**
 * @author $Author$
 * @version $Revision$
 */
public class UpdateCheckerTest
{

  @Test
  public void testWindows() throws FileNotFoundException
  {
    String os = "Windows XP";
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.ENGLISH, os);
    assertEquals("2.5.0", checker.getVersion());
    assertEquals("19. November 2010", checker.getDate());
    assertEquals("This is the new LocalSigner", checker.getDescription());
    assertEquals("text for auto update", checker.getDownloadLink());
    assertEquals(
        "http://www.e-service.admin.ch/wiki/display/suispublic/Open+eGov+LocalSigner+Installation",
        checker.getDownloadPage());
  }

  @Test
  public void testWindowsWithNewline() throws FileNotFoundException
  {
    String os = "Windows XP";
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.GERMAN, os);
    assertEquals("2.5.0", checker.getVersion());
    assertEquals("19. November 2010", checker.getDate());
    assertEquals("Dies ist der neue LocalSigner\r" + "zweite Linie",
        checker.getDescription());
    assertEquals("text for auto update", checker.getDownloadLink());
    assertEquals(
        "http://www.e-service.admin.ch/wiki/display/suispublic/Open+eGov+LocalSigner+Installation",
        checker.getDownloadPage());
  }

  @Test
  public void testMac() throws FileNotFoundException
  {
    String os = "Mac OS X";
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.GERMAN, os);
    assertEquals("2.4.1", checker.getVersion());
    assertEquals("4. August 2010", checker.getDate());
    assertEquals("Dies ist der neue LocalSigner", checker.getDescription());
    assertEquals("text for auto update", checker.getDownloadLink());
    assertEquals(
        "http://www.e-service.admin.ch/wiki/display/suispublic/Open+eGov+LocalSigner+Installation",
        checker.getDownloadPage());
  }

  @Test
  public void testLinux() throws FileNotFoundException
  {
    String os = "Linux";
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.FRENCH, os);
    assertEquals("2.3.0", checker.getVersion());
    assertEquals("4. Juni 2010", checker.getDate());
    assertEquals("C'est le nouveaux LocalSigner", checker.getDescription());
    assertEquals("text for auto update", checker.getDownloadLink());
    assertEquals(
        "http://www.e-service.admin.ch/wiki/display/suispublic/Open+eGov+LocalSigner+Installation",
        checker.getDownloadPage());
  }

  @Test
  public void testSolaris() throws FileNotFoundException
  {
    String os = "SunOS";
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.ITALIAN, os);
    assertEquals("2.3.0", checker.getVersion());
    assertEquals("4. Juni 2010", checker.getDate());
    assertEquals("Questo è il nuovo LocalSigner", checker.getDescription());
    assertEquals("text for auto update", checker.getDownloadLink());
    assertEquals(
        "http://www.e-service.admin.ch/wiki/display/suispublic/Open+eGov+LocalSigner+Installation",
        checker.getDownloadPage());
  }

  @Test
  public void testParseError()
  {
    UpdateChecker checker = new UpdateChecker();
    InputStream data = new ByteArrayInputStream("<release>error</release>".getBytes());
    checker.parseXml(data, Locale.ENGLISH, "Linux");

    assertNull(checker.getVersion());
    assertNull(checker.getDate());
    assertNull(checker.getDescription());
    assertNull(checker.getDownloadLink());
    assertNull(checker.getDownloadPage());
  }

  @Test
  public void testUpdate() throws FileNotFoundException
  {
    UpdateChecker checker = new UpdateChecker();
    checker.parseXml(new FileInputStream("test/updateCheck.xml"), Locale.ENGLISH,
        "Windows");
    assertFalse(checker.hasUpdate(null));
    assertFalse(checker.hasUpdate(""));
    assertTrue(checker.hasUpdate("0.9.5"));
    assertTrue(checker.hasUpdate("1.0"));
    assertTrue(checker.hasUpdate("2"));
    assertTrue(checker.hasUpdate("2.4.10"));
    assertFalse(checker.hasUpdate("2.5.0"));
    assertFalse(checker.hasUpdate("2.5.1"));
    assertFalse(checker.hasUpdate("2.6"));
    assertFalse(checker.hasUpdate("2.7.2"));
    assertFalse(checker.hasUpdate("3.0.0"));
    assertFalse(checker.hasUpdate("3.1"));
    assertFalse(checker.hasUpdate("4"));
  }
}
