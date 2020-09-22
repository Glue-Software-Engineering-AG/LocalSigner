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

/**
 * This class holds PDF constants.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public final class Constants
{
  /**
   * Measurement for PDF positioning. PDF A4 page has 842 x 595
   * pixels and 297mm x 210mm. This means: 1 mm = 842/297 = 2.84 pixels
   */
  public static final float ONE_MILLIMETER_IN_PDF = (float) 842 / 297;

  /**
   * Extension of a PDF document
   */
  public static final String PDF_FILE_SUFFIX = ".pdf";

  /**
   * Extension for a signature field PDF document
   */
  public static final String MODIFIED_PDF_SUFFIX = "-mod.pdf";

  /**
   * Extension for a PDF document with Funktionsnachweis
   */
  public static final String FUNKTIONSNACHWEIS_SUFFIX = "-fn.pdf";

  /**
   * Extension for a PDF converted to a PDF/A standard PDF
   */
  public static final String PDF_A_CONVERTED_SUFFIX = "-conv.pdf";

  public static final BoxPosition DEFAULT_SIGNATURE_POSITION = BoxPosition.fromMillimeters(1, 120, 10, 80, 25);

  private Constants()
  {
    // hide constructor for utility class
  }

}
