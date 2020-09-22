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
 * gives access to the parameter "-Dbase=...". The value of this parameter should point to the installation directory
 * of LocalSigner. If this parameter is not set, the current working directory will be used as base path.
 *
 * @author greiler
 * @author $Author$
 * @version $Revision$
 */
public class BasePath
{
  private static final Logger LOGGER = Logger.getLogger(BasePath.class);

  private static final String BASE_PATH_PARAMETER = "base";

  private static final String EGOV_RESOURCE_PATH = "resources/egov/";

  private static final String BUND_RESOURCE_PATH = "resources/signaturepages_bund/";

  private static final String EXAMPLES_PATH = "resources/examples/";

  public static String getEGovResourcesPath()
  {
    return getBasePath() + EGOV_RESOURCE_PATH;
  }

  public static String getBundResourcesPath()
  {
    return getBasePath() + BUND_RESOURCE_PATH;
  }

  public static String getExamplesPath()
  {
    return getBasePath() + EXAMPLES_PATH;
  }

  public static String getBasePath()
  {
    return getBasePathParameterOrCurrentLocation() + File.separator;
  }

  private static String getBasePathParameterOrCurrentLocation()
  {
    String base = System.getProperty(BASE_PATH_PARAMETER);

    if (StringUtils.isEmpty(base))
    {
      LOGGER.warn("LocalSigner not started with -Dbase option. Using current directory as base path.");
      base = System.getProperty("user.dir");
    }

    return base;
  }

  private BasePath()
  {
    // hide constructor of util class
  }
}
