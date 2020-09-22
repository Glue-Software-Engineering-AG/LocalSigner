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

import ch.admin.localsigner.config.util.BasePath;
import ch.admin.localsigner.validation.SHAChecksum;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class initiates the security & integrity checks for LocalSigner
 *
 */
class SecurityCheck
{
  private static final Logger LOGGER = Logger.getLogger(SecurityCheck.class);

  /*
   * See project 'suis-security-tools' method PKCS11TokenTest.java>>testEncode
   * to generate this form of the certificate
   */
  private static final byte[] HARDWARE_CERT =
  {
    48,-126,5,80,48,-126,4,56,-96,3,2,1,2,2,17,0,-101,121,
61,-1,123,40,34,89,83,-72,98,103,-49,-111,-61,-20,48,13,6,9,
42,-122,72,-122,-9,13,1,1,11,5,0,48,124,49,11,48,9,6,
3,85,4,6,19,2,71,66,49,27,48,25,6,3,85,4,8,19,
18,71,114,101,97,116,101,114,32,77,97,110,99,104,101,115,116,101,
114,49,16,48,14,6,3,85,4,7,19,7,83,97,108,102,111,114,
100,49,24,48,22,6,3,85,4,10,19,15,83,101,99,116,105,103,
111,32,76,105,109,105,116,101,100,49,36,48,34,6,3,85,4,3,
19,27,83,101,99,116,105,103,111,32,82,83,65,32,67,111,100,101,
32,83,105,103,110,105,110,103,32,67,65,48,30,23,13,50,48,48,
50,49,49,48,48,48,48,48,48,90,23,13,50,51,48,50,49,48,
50,51,53,57,53,57,90,48,-127,-89,49,11,48,9,6,3,85,4,
6,19,2,67,72,49,13,48,11,6,3,85,4,17,12,4,51,48,
48,55,49,13,48,11,6,3,85,4,8,12,4,66,101,114,110,49,
13,48,11,6,3,85,4,7,12,4,66,101,114,110,49,29,48,27,
6,3,85,4,9,12,20,83,99,104,119,97,114,122,116,111,114,115,
116,114,97,115,115,101,32,51,49,49,37,48,35,6,3,85,4,10,
12,28,71,108,117,101,32,83,111,102,116,119,97,114,101,32,69,110,
103,105,110,101,101,114,105,110,103,32,65,71,49,37,48,35,6,3,
85,4,3,12,28,71,108,117,101,32,83,111,102,116,119,97,114,101,
32,69,110,103,105,110,101,101,114,105,110,103,32,65,71,48,-126,1,
34,48,13,6,9,42,-122,72,-122,-9,13,1,1,1,5,0,3,-126,
1,15,0,48,-126,1,10,2,-126,1,1,0,-82,-14,75,71,79,53,
25,-76,123,-95,-95,-93,39,-24,-27,-27,-96,17,-97,-106,-34,-30,1,43,
-89,2,106,52,84,-5,-16,109,-45,-21,-97,2,84,-63,-31,-56,112,-94,
-45,-100,53,-37,85,89,65,-108,119,-43,-120,-123,12,-87,101,115,-128,-123,
14,100,123,126,-81,-90,126,104,40,-48,60,1,-53,-127,21,-128,82,-22,
58,94,61,127,-9,-33,-76,19,-100,-119,98,64,83,-102,-121,-71,-47,-86,
107,-122,32,53,-39,120,-38,122,5,-2,60,-10,-116,-10,29,-43,-57,-55,
85,54,97,-86,-122,110,6,19,11,73,-92,56,-114,-128,-112,3,-106,-114,
34,-7,38,112,112,16,-105,113,-42,-52,-54,98,44,91,-29,73,39,-99,
-10,76,5,-77,-90,42,16,-57,13,70,9,4,6,-120,40,-123,-128,27,
-37,-72,-40,24,-71,-92,-90,-122,20,61,-63,-84,-73,39,31,-14,-46,118,
-44,-57,92,-49,98,95,-73,-57,98,112,-66,-83,-58,-58,1,-9,-62,-5,
-97,-128,-3,106,41,-111,-72,-25,-53,-5,-8,10,-61,46,75,54,-123,54,
-105,88,-27,45,-98,92,103,-89,-75,-101,103,-25,102,29,126,76,-27,-14,
-127,47,18,-33,-62,39,44,11,90,76,7,50,75,96,18,97,2,3,
1,0,1,-93,-126,1,-97,48,-126,1,-101,48,31,6,3,85,29,35,
4,24,48,22,-128,20,14,-31,58,-88,83,58,49,-43,-118,-66,-63,-69,
-83,103,26,3,-123,-83,52,14,48,29,6,3,85,29,14,4,22,4,
20,50,-18,95,125,86,-42,-59,104,22,107,102,-106,23,44,23,-53,14,
-88,25,110,48,14,6,3,85,29,15,1,1,-1,4,4,3,2,7,
-128,48,12,6,3,85,29,19,1,1,-1,4,2,48,0,48,19,6,
3,85,29,37,4,12,48,10,6,8,43,6,1,5,5,7,3,3,
48,17,6,9,96,-122,72,1,-122,-8,66,1,1,4,4,3,2,4,
16,48,64,6,3,85,29,32,4,57,48,55,48,53,6,12,43,6,
1,4,1,-78,49,1,2,1,3,2,48,37,48,35,6,8,43,6,
1,5,5,7,2,1,22,23,104,116,116,112,115,58,47,47,115,101,
99,116,105,103,111,46,99,111,109,47,67,80,83,48,67,6,3,85,
29,31,4,60,48,58,48,56,-96,54,-96,52,-122,50,104,116,116,112,
58,47,47,99,114,108,46,115,101,99,116,105,103,111,46,99,111,109,
47,83,101,99,116,105,103,111,82,83,65,67,111,100,101,83,105,103,
110,105,110,103,67,65,46,99,114,108,48,115,6,8,43,6,1,5,
5,7,1,1,4,103,48,101,48,62,6,8,43,6,1,5,5,7,
48,2,-122,50,104,116,116,112,58,47,47,99,114,116,46,115,101,99,
116,105,103,111,46,99,111,109,47,83,101,99,116,105,103,111,82,83,
65,67,111,100,101,83,105,103,110,105,110,103,67,65,46,99,114,116,
48,35,6,8,43,6,1,5,5,7,48,1,-122,23,104,116,116,112,
58,47,47,111,99,115,112,46,115,101,99,116,105,103,111,46,99,111,
109,48,23,6,3,85,29,17,4,16,48,14,-127,12,105,110,102,111,
64,103,108,117,101,46,99,104,48,13,6,9,42,-122,72,-122,-9,13,
1,1,11,5,0,3,-126,1,1,0,66,-64,-108,45,71,-88,16,36,
-120,104,-25,20,-72,-47,-71,106,-10,-70,-109,-95,-106,54,-102,49,-56,-52,
-107,-121,-110,-47,6,-53,38,55,-53,32,104,25,66,125,-81,97,93,-94,
-45,61,-71,100,-103,-59,-59,-22,14,39,-125,56,-103,104,28,-57,-8,61,
-52,37,-92,5,-62,-93,71,1,96,-70,-56,48,107,79,-13,24,-21,47,
62,-101,-95,-97,86,-50,8,-20,-48,66,10,-51,84,78,31,88,-20,107,
-109,-18,78,-119,56,71,34,114,-27,75,-58,76,121,46,53,-13,3,-93,
-107,-14,2,-5,38,38,-30,-79,119,74,-109,104,-87,-63,-111,100,-76,-121,
-79,-28,78,-6,-83,-98,7,80,-115,-87,100,36,-108,-98,125,35,-75,28,
17,-58,-93,46,9,-57,-122,-41,-50,-80,-20,-53,28,59,9,-80,74,-99,
-101,61,-116,-77,110,-11,73,-3,-108,42,123,24,-3,6,-58,-123,-36,61,
20,59,-14,80,-116,-48,-38,-52,62,106,-95,19,93,16,69,88,97,-16,
91,72,67,37,15,62,17,107,-48,-18,52,58,53,47,-101,57,46,-49,
113,65,-122,30,-45,-45,54,-26,71,60,44,126,85,-18,-112,91,118,28,
93,-13,-16,93,-4,-41,48,110,-37,-5,104,34,7,-70,
  };

  private static final byte[] SOFTWARE_CERT =
  {
    48, -126, 4, -23, 48, -126, 3, -47, -96, 3, 2, 1, 2, 2, 16, 106, -91, 75, 44, -90,
    -1, -13, 100, -12, -81, 127, 36, -99, -107, -55, 59, 48, 13, 6, 9, 42, -122, 72,
    -122, -9, 13, 1, 1, 5, 5, 0, 48, -127, -76, 49, 11, 48, 9, 6, 3, 85, 4, 6, 19, 2,
    85, 83, 49, 23, 48, 21, 6, 3, 85, 4, 10, 19, 14, 86, 101, 114, 105, 83, 105, 103,
    110, 44, 32, 73, 110, 99, 46, 49, 31, 48, 29, 6, 3, 85, 4, 11, 19, 22, 86, 101,
    114, 105, 83, 105, 103, 110, 32, 84, 114, 117, 115, 116, 32, 78, 101, 116, 119,
    111, 114, 107, 49, 59, 48, 57, 6, 3, 85, 4, 11, 19, 50, 84, 101, 114, 109, 115, 32,
    111, 102, 32, 117, 115, 101, 32, 97, 116, 32, 104, 116, 116, 112, 115, 58, 47, 47,
    119, 119, 119, 46, 118, 101, 114, 105, 115, 105, 103, 110, 46, 99, 111, 109, 47,
    114, 112, 97, 32, 40, 99, 41, 48, 52, 49, 46, 48, 44, 6, 3, 85, 4, 3, 19, 37, 86,
    101, 114, 105, 83, 105, 103, 110, 32, 67, 108, 97, 115, 115, 32, 51, 32, 67, 111,
    100, 101, 32, 83, 105, 103, 110, 105, 110, 103, 32, 50, 48, 48, 52, 32, 67, 65, 48,
    30, 23, 13, 48, 57, 48, 50, 49, 56, 48, 48, 48, 48, 48, 48, 90, 23, 13, 49, 50, 48,
    50, 49, 57, 50, 51, 53, 57, 53, 57, 90, 48, -127, -84, 49, 11, 48, 9, 6, 3, 85, 4,
    6, 19, 2, 67, 72, 49, 13, 48, 11, 6, 3, 85, 4, 8, 19, 4, 66, 101, 114, 110, 49, 13,
    48, 11, 6, 3, 85, 4, 7, 19, 4, 66, 101, 114, 110, 49, 37, 48, 35, 6, 3, 85, 4, 10,
    20, 28, 71, 108, 117, 101, 32, 83, 111, 102, 116, 119, 97, 114, 101, 32, 69, 110,
    103, 105, 110, 101, 101, 114, 105, 110, 103, 32, 65, 71, 49, 49, 48, 47, 6, 3, 85,
    4, 11, 19, 40, 68, 105, 103, 105, 116, 97, 108, 32, 73, 68, 32, 67, 108, 97, 115,
    115, 32, 51, 32, 45, 32, 74, 97, 118, 97, 32, 79, 98, 106, 101, 99, 116, 32, 83,
    105, 103, 110, 105, 110, 103, 49, 37, 48, 35, 6, 3, 85, 4, 3, 20, 28, 71, 108, 117,
    101, 32, 83, 111, 102, 116, 119, 97, 114, 101, 32, 69, 110, 103, 105, 110, 101,
    101, 114, 105, 110, 103, 32, 65, 71, 48, -127, -97, 48, 13, 6, 9, 42, -122, 72,
    -122, -9, 13, 1, 1, 1, 5, 0, 3, -127, -115, 0, 48, -127, -119, 2, -127, -127, 0,
    -113, 42, -71, 75, -14, -76, -29, -65, 94, -112, -70, -69, 21, -50, -7, -52, 1, 18,
    28, 20, -88, -5, -56, 59, -36, 63, 78, 54, -41, -45, 102, -114, 28, 101, -82, 95,
    100, 72, -59, 16, 19, -5, -17, -49, 70, 79, -2, 76, 112, 52, -22, -67, -90, -127,
    -100, 6, 15, 75, 7, -24, 20, -27, -9, -34, 25, 120, -12, -107, 92, 82, -122, -59,
    56, 37, 13, -56, -128, 113, 2, -124, -63, 92, -30, -121, -59, -23, -81, -107, -80,
    56, -62, 11, 53, 71, -23, 97, -100, 96, 96, 121, -14, 38, -98, 11, -33, -55, -25,
    -25, -20, -64, -43, -61, 32, -115, 117, 115, 99, 112, -47, 29, -39, 69, 59, 100,
    -118, 122, -115, -83, 2, 3, 1, 0, 1, -93, -126, 1, 127, 48, -126, 1, 123, 48, 9, 6,
    3, 85, 29, 19, 4, 2, 48, 0, 48, 14, 6, 3, 85, 29, 15, 1, 1, -1, 4, 4, 3, 2, 7,
    -128, 48, 64, 6, 3, 85, 29, 31, 4, 57, 48, 55, 48, 53, -96, 51, -96, 49, -122, 47,
    104, 116, 116, 112, 58, 47, 47, 67, 83, 67, 51, 45, 50, 48, 48, 52, 45, 99, 114,
    108, 46, 118, 101, 114, 105, 115, 105, 103, 110, 46, 99, 111, 109, 47, 67, 83, 67,
    51, 45, 50, 48, 48, 52, 46, 99, 114, 108, 48, 68, 6, 3, 85, 29, 32, 4, 61, 48, 59,
    48, 57, 6, 11, 96, -122, 72, 1, -122, -8, 69, 1, 7, 23, 3, 48, 42, 48, 40, 6, 8,
    43, 6, 1, 5, 5, 7, 2, 1, 22, 28, 104, 116, 116, 112, 115, 58, 47, 47, 119, 119,
    119, 46, 118, 101, 114, 105, 115, 105, 103, 110, 46, 99, 111, 109, 47, 114, 112,
    97, 48, 19, 6, 3, 85, 29, 37, 4, 12, 48, 10, 6, 8, 43, 6, 1, 5, 5, 7, 3, 3, 48,
    117, 6, 8, 43, 6, 1, 5, 5, 7, 1, 1, 4, 105, 48, 103, 48, 36, 6, 8, 43, 6, 1, 5, 5,
    7, 48, 1, -122, 24, 104, 116, 116, 112, 58, 47, 47, 111, 99, 115, 112, 46, 118,
    101, 114, 105, 115, 105, 103, 110, 46, 99, 111, 109, 48, 63, 6, 8, 43, 6, 1, 5, 5,
    7, 48, 2, -122, 51, 104, 116, 116, 112, 58, 47, 47, 67, 83, 67, 51, 45, 50, 48, 48,
    52, 45, 97, 105, 97, 46, 118, 101, 114, 105, 115, 105, 103, 110, 46, 99, 111, 109,
    47, 67, 83, 67, 51, 45, 50, 48, 48, 52, 45, 97, 105, 97, 46, 99, 101, 114, 48, 31,
    6, 3, 85, 29, 35, 4, 24, 48, 22, -128, 20, 8, -11, 81, -24, -5, -2, 61, 61, 100,
    54, 124, 104, -49, 91, 120, -88, -33, -71, -59, 55, 48, 17, 6, 9, 96, -122, 72, 1,
    -122, -8, 66, 1, 1, 4, 4, 3, 2, 4, 16, 48, 22, 6, 10, 43, 6, 1, 4, 1, -126, 55, 2,
    1, 27, 4, 8, 48, 6, 1, 1, 0, 1, 1, -1, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1,
    1, 5, 5, 0, 3, -126, 1, 1, 0, 122, -128, 76, -23, -36, -40, -35, -116, 70, -54,
    127, 40, 66, -72, -1, -116, -58, 11, -42, -6, -2, -82, -84, 67, -88, 46, 91, 113,
    -69, -54, -90, 79, 6, 24, -69, -62, -17, 27, -109, -106, 127, -127, 15, 14, -96,
    24, -47, 72, -58, 118, 86, -122, -31, 30, 7, -64, 52, -52, 55, -16, -39, 46, 28,
    35, -31, 80, 34, 102, 104, 39, 33, 1, 21, -32, 58, 52, 53, -10, -42, 101, 42, 88,
    40, 4, 60, 68, -84, -55, -73, 84, -117, 124, 97, 71, -121, 74, 44, 81, 31, -44, 82,
    -37, 112, -4, 111, 101, 60, -12, 82, -80, 9, -112, 107, -29, 9, -85, -114, 100, 55,
    -63, 125, -9, 33, -1, 38, 101, 48, 99, 45, 98, -13, -23, -11, -37, -39, -16, -55,
    9, 57, -11, 0, -109, 122, -28, 91, -36, 5, -87, 46, 1, -96, 118, -19, 94, -24, 0,
    121, 105, 112, -92, 8, -98, 114, 109, 113, -101, 36, 12, -119, 61, -63, 80, 120,
    75, -116, 52, 74, -53, -62, 85, 37, 36, 57, 67, 77, -116, 89, 127, 26, 103, -41,
    -13, 29, 81, -13, 44, -85, 90, 112, -80, 8, -85, 22, -26, -51, 11, 5, 113, 123, 39,
    -35, -111, -16, 86, 28, 71, 91, -32, 52, 3, 126, 113, -74, -41, -65, 33, 19, 30,
    -102, -90, -82, -107, -94, 108, -19, -76, 107, -17, -17, -92, -75, -54, -90, -89,
    -127, 52, 24, -126, -23, 120, -97, -40, 49, 71, 97, 83
  };

  private SWTSplash splash;

  private boolean problem;

  /**
   * Constructor
   *
   * @param splashScreen
   *          Splash screen to draw check progress
   */
  protected void check(final SWTSplash splashScreen)
  {
    this.splash = splashScreen;
    final String[] classpathentries = getClasspath();

    if (classpathentries.length > 30)  {
      // running in IDE
       return;
    }

    if (classpathentries.length != 5)
    {
      generalErrorDuringSecurityChecking("Classpath length is modified");
      return;
    }

    // check all expected classpath entries
    checkLocalsignerJar(classpathentries[0]);
    checkLibraryJar(classpathentries[1], BasePath.getBasePath()+"/lib/bcprov-jdk15on-1.59.jar",
      "1c31e44e331d25e46d293b3e8ee2d07028a67db011e74cb2443285aed1d59c85");
    checkLibraryJar(classpathentries[2], BasePath.getBasePath()+"/lib/bcmail-jdk15on-1.59.jar",
      "404cd478ba2bf456db7b8a29cc4a47b00b6cbbd024b83845c8092d3fea2460cb");
    checkLibraryJar(classpathentries[3], BasePath.getBasePath()+"/lib/bcpkix-jdk15on-1.59.jar",
      "601d85cfbcef76a1cb77cbf755a6234a4ba1d4c02a98d9a81028d471f388694f");
    // cannot check SWT lib, different on every platform
  }

  /**
   * Returns the classpath of the application as a String[]. The particular
   * entries (jars, classes, etc.) are ordered ascendingly in the string array.
   *
   * @return classpath
   */
  private String[] getClasspath()
  {
    String classpathDelimiter = ";";
    // this should be windows anyway
    if (!(System.getProperty("os.name")).toUpperCase().contains("WINDOWS"))
    {
      classpathDelimiter = ":";
    }
    final String classpath = System.getProperty("java.class.path");

    // get all classpath entries
    return classpath.split(classpathDelimiter);
  }

  /**
   * Check the localsigner.jar file
   * <ul>
   * <li>Check that the localsigner.jar is the second argument in the classpath
   * and check that the provided lib is the one inside the applications lib
   * folder.</li>
   * <li>Check, that the jar is valid, all signatures of its entries are valid
   * and that it is signed with the specified code signing certificate (static
   * member).</li>
   * </ul>
   *
   * @param pathEntry
   *          second entry in classpath
   */
  private void checkLocalsignerJar(final String pathEntry)
  {
    if (splash.skipSplash())
    {
      return;
    }

    LOGGER.debug("checking LocalSigner " + pathEntry);

    // check, that the localsigner.jar is the second argument of the
    // classpath
    if (pathEntry == null)
    {
      generalErrorDuringSecurityChecking("Classpath missing");
      return;
    }
    if (!pathEntry.contains("localsigner.jar"))
    {
      generalErrorDuringSecurityChecking("Classpath wrong");
      return;
    }

    // and check that the provided argument is the jar file from the lib
    // directory
    // check that the provided jar file is the one which is provided with
    // the application
    final String localsignerProvidedPath = new File(BasePath.getBasePath()+"lib/localsigner.jar").getAbsolutePath();
    final String localsignerGivenPath = new File(pathEntry).getAbsolutePath();
    if (!localsignerProvidedPath.equals(localsignerGivenPath))
    {
      generalErrorDuringSecurityChecking("localsigner.jar path wrong");
      return;
    }

    // and check, that all signatures are correct and all files (w/o
    // directories)
    // have the code signing certificate attached.

    JarFile jarfile = null;
    try
    {
      jarfile = openJarFile(localsignerGivenPath);
      if (jarfile != null)
      {
        processJarFileEntries(localsignerGivenPath, jarfile);
      }
    } finally
    {
      if (jarfile != null)
      {
        try
        {
          jarfile.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
  }

  private JarFile openJarFile(final String localsignerGivenPath)
  {
    try
    {
      // open jar file with signature verification set to true
      return new JarFile(localsignerGivenPath, true);
    } catch (Exception e)
    {
      generalErrorDuringSecurityChecking("Cannot read jar");
      return null;
    }
  }

  private void processJarFileEntries(final String localsignerGivenPath, JarFile jarfile)
  {

    int jarCount = jarfile.size();
    int count = 0;
    // iterate over all entries in the jar file
    for (Enumeration<JarEntry> entries = jarfile.entries(); entries.hasMoreElements();)
    {
      if (splash.skipSplash())
      {
        return;
      }
      count++;
      splash.setProgress(100 * count / jarCount);

      final JarEntry entry = entries.nextElement();

      // load the whole jar file entry
      InputStream is = null;
      try
      {
        is = jarfile.getInputStream(entry);
        // it is important to load the whole jar entry to check for
        // correct signatures
        // and to be sure the certificates used for signed are also
        // loaded.
        int val = 0;
        while (val != -1)
        {
          val = is.read();
        }
      } catch (Exception ioe)
      {
        LOGGER.error("Cannot verify " + entry, ioe);
        generalErrorDuringSecurityChecking("Error while checking jar entry " + entry.getName());
        return;
      } finally {
        if (is!=null) {
          try
          {
            is.close();
          } catch (IOException e)
          {
            LOGGER.error("cannot close Inputstream",e);
          }
        }
      }

    }
    LOGGER.debug("Checked files: " + count);
  }

  /**
   * Checks a library jar.
   * <ol>
   * <li>Checks that the jar is inside the application (lib folder). Otherwise an error is shown.</li>
   * <li>Checks that the provided jar is actually the one bundled with the application (hash)</li>
   * </ol>
   */
  private void checkLibraryJar(final String pathEntry, final String filename, final String expectedHash)
  {
    if (splash.skipSplash())
    {
      return;
    }

    LOGGER.debug("Checking jar " + pathEntry);

    if (pathEntry == null)
    {
      generalErrorDuringSecurityChecking("Classpath missing");
      return;
    }

    // check that the provided jar file is the one which is provided with
    // the app
    final String providedPath = new File(filename).getAbsolutePath();
    final String givenPath = new File(pathEntry).getAbsolutePath();
    LOGGER.debug("provided: " + providedPath);
    LOGGER.debug("cp-entry: " + givenPath);
    if (!providedPath.equals(givenPath))
    {
      generalErrorDuringSecurityChecking("Path wrong");
      return;
    }

    // check that the jar lib is the correct one
    try
    {
      String sum = SHAChecksum.getChecksum(givenPath);
      LOGGER.debug("Hash: " + sum);
      if (!expectedHash.equals(sum))
      {
        generalErrorDuringSecurityChecking(filename + " tampered");
      }
    } catch (Exception e)
    {
      LOGGER.error("Cannot verify hash", e);
      generalErrorDuringSecurityChecking(filename + " tampered");
    }
  }

  /**
   * This method simply pops up an error message box and displays a general
   * error. This error may happen during security checking and initializing of
   * the application.
   */
  private void generalErrorDuringSecurityChecking(final String problem)
  {
    LOGGER.error("This LocalSigner installation has been altered. Please download the application again.");
    LOGGER.error("Cause: " + problem);
    if (splash != null)
    {
      splash.close();
    }

    this.problem = true;
  }

  public boolean getProblem()
  {
    return problem;
  }

}
