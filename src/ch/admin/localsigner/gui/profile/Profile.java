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

import org.apache.commons.configuration.PropertiesConfiguration;
import ch.admin.localsigner.gui.profile.PropertiesGUI.ImageMode;

/**
 * This class holds the data of a profile
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class Profile {

  // localsigner version identifier
  public static final String VERSION = "version";

  // app specific properties
  public static final String SHOW_SIGNED_DOCUMENT = "showSignedDocument";
  public static final String PDF_ATTACHMENT = "pdfAttachment";
  public static final String OUTPUT_DIR = "outputDir";

  // signature specific properties
  public static final String TSAPASSWORD = "tsapassword";
  public static final String TSAUSER = "tsauser";
  public static final String TSAURL = "tsaurl";
  public static final String ENABLE_TIMESTAMPING = "enableTimestamping";
  public static final String BACKGROUND_IMAGE = "backgroundImage";
  public static final String REASON = "reason";
  public static final String REASON_SHOW_LABEL = "reasonShowLabel";
  public static final String CONTACT = "contact";
  public static final String CONTACT_SHOW_LABEL = "contactShowLabel";
  public static final String LOCATION = "location";
  public static final String SIGN_ON = "signOn";
  public static final String BOXHEIGHT = "boxheight";
  public static final String BOXWIDTH = "boxwidth";
  public static final String TOP_POS = "topPos";
  public static final String LEFT_POS = "leftPos";
  public static final String VISIBLE_SIGNATURE = "visibleSignature";

  public static final String TYPE_OF_SIGNATURE = "typeOfSignature";

  /**
   * Boolean flag to signal if text should be shown in visible signature.
   */
  public static final String SHOW_TEXT_IN_SIGNATURE = "showTextInSignature";

  /**
   * Boolean flag to signal if image should be shwon in visible signature.
   */
  public static final String SHOW_IMAGE_IN_SIGNATURE = "showImageInSignature";

  /**
   * For deciding how the image is to be drawn (size). One of {@link ImageMode}.
   */
  public static final String IMAGE_MODE = "imageMode";

  /**
   * Fixed image size in MM, custom set by user.
   */
  public static final String IMAGE_SIZE_FIXED_WIDTH = "imageWidthDefined";

  /**
   * Fixed image size in MM, custom set by user.
   */
  public static final String IMAGE_SIZE_FIXED_HEIGHT = "imageHeightDefined";

  /**
   * If this flag is set to true, a second visible signature has a fixed Y-Axis
   * based on the first signature.
   */
  public static final String ALIGN_SECOND_VISIBLE_SIGNATURE = "alignSecondVisibleSignatureYAxis";


  private final ProfileType type;
  private final String path;
  private final String name;
  private PropertiesConfiguration config;

  public Profile(final ProfileType type, final String name, final String path) {
    this.type = type;
    this.name = name;
    this.path = path;
  }

  public boolean canSave() {
    return type != ProfileType.SYSTEM_PROFILE;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public String getDescription() {
    final StringBuilder description = new StringBuilder(name);

    if (config == null) {
      return description.toString();
    }

    description.append("   <Merge: ");
    if (config.getString(PDF_ATTACHMENT).length() > 0) {
      description.append("enabled");
    } else {
      description.append("disabled");
    }

    description.append(", Timestamping: ");
    if (config.getBoolean(ENABLE_TIMESTAMPING)) {
      description.append("enabled");
    } else {
      description.append("disabled");
    }

    description.append(", Type of signature: ");
    final String sigtype = config.getString(TYPE_OF_SIGNATURE);
    if ("certification".equals(sigtype)) {
      description.append("Certification>");
    } else {
      description.append("Signature>");
    }
    return description.toString();
  }

  public void setConfig(final PropertiesConfiguration config) {
    this.config = config;
  }

  public boolean isDefaultType()
  {
    return this.type == ProfileType.DEFAULT_PROFILE;
  }

  public boolean isSystemType()
  {
    return this.type == ProfileType.SYSTEM_PROFILE;
  }
}
