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

import java.awt.image.ColorModel;
import org.faceless.pdf2.PDFParser;

public enum ColorToConvert
{
  BW(PDFParser.BLACKANDWHITE), GREY(PDFParser.GRAYSCALE), COLOR(PDFParser.RGB);

  private ColorModel colorModel;

  // is this the default value or was this option chosen by setting the command
  // line parame -colormodel
  private boolean isDefaultValue = true;

  private ColorToConvert(ColorModel colorModel)
  {
    this.colorModel = colorModel;
  }

  public ColorModel getColorModel()
  {
    return colorModel;
  }

  public boolean isDefaultValue()
  {
    return isDefaultValue;
  }

  public void setDefaultValue(boolean isDefaultValue)
  {
    this.isDefaultValue = isDefaultValue;
  }
}
