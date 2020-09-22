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
package ch.admin.localsigner.gui.viewer;

/**
 * This class holds information about the last visible signature in the current
 * pdf displayed by PdfViewerPanelBFO.
 * 
 * @author weisskopf
 *
 */
class LastSignature
{

  private int lastSignatureOnPage;
  
  private float lastSignatureYValue;

  private float lastSignatureXValue;

  private float lastSignatureWidth;

  public int getLastSignatureOnPage()
  {
    return lastSignatureOnPage;
  }

  public void setLastSignatureOnPage(int lastSignatureOnPage)
  {
    this.lastSignatureOnPage = lastSignatureOnPage;
  }

  public float getLastSignatureYValue()
  {
    return lastSignatureYValue;
  }

  public void setLastSignatureYValue(float lastSignatureYValue)
  {
    this.lastSignatureYValue = lastSignatureYValue;
  }

  public float getLastSignatureXValue()
  {
    return lastSignatureXValue;
  }

  public void setLastSignatureXValue(float lastSignatureXValue)
  {
    this.lastSignatureXValue = lastSignatureXValue;
  }

  public float getLastSignatureWidth()
  {
    return lastSignatureWidth;
  }

  public void setLastSignatureWidth(float lastSignatureWidth)
  {
    this.lastSignatureWidth = lastSignatureWidth;
  }
  
  
  
}
