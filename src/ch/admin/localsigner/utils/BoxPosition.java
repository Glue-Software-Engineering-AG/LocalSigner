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
 * Data class for a signature position box. All values are given as PdfUnits,
 * but note that we really just use the int value of it. <br>
 * For backwards compatibility all values are stored as well as MM and can be
 * accessed (from / to profile).
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class BoxPosition
{

  private final int page;

  private final float xInPdfUnits;

  private final float yInPdfUnits;

  private final float wInPdfUnits;

  private final float hInPdfUnits;

  private String name;

  public static BoxPosition fromMillimeters(final int page, final int x, final int y, final int width, final int height)
  {
    return new BoxPosition(page,
      x * Constants.ONE_MILLIMETER_IN_PDF,
      y * Constants.ONE_MILLIMETER_IN_PDF,
      width * Constants.ONE_MILLIMETER_IN_PDF,
      height * Constants.ONE_MILLIMETER_IN_PDF);
  }

  /**
   * This is an alternative constructor, which uses PdfUnits.
   */
  public BoxPosition(final int page, final float x, final float y, final float width, final float height)
  {
    this.page = page;
    this.xInPdfUnits = x;
    this.yInPdfUnits = y;
    this.hInPdfUnits = height;
    this.wInPdfUnits = width;
  }

  public int getPage()
  {
    return page;
  }

  /** X in Millimeter */
  public int getX()
  {
    return Math.round(xInPdfUnits / Constants.ONE_MILLIMETER_IN_PDF);
  }

  /** Y in Millimeter */
  public int getY()
  {
    return Math.round(yInPdfUnits / Constants.ONE_MILLIMETER_IN_PDF);
  }

  /** with in Millimeter */
  public int getWidth()
  {
    return Math.round(wInPdfUnits / Constants.ONE_MILLIMETER_IN_PDF);
  }

  /** height in Millimeter */
  public int getHeight()
  {
    return Math.round(hInPdfUnits / Constants.ONE_MILLIMETER_IN_PDF);
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  protected BoxPosition convertGuiToPdf(final int pdfPageHeight)
  {
    final float leftPixel = xInPdfUnits;

    final float topPixel = yInPdfUnits + hInPdfUnits;
    final float bottomPixel = pdfPageHeight - topPixel;

    BoxPosition pdfBox = new BoxPosition(page, leftPixel, bottomPixel, getwInPdfUnits(), gethInPdfUnits());
    pdfBox.setName(name);
    return pdfBox;
  }

  @Override
  public String toString()
  {
    return xInPdfUnits + "@" + yInPdfUnits + " " + wInPdfUnits + "x" + hInPdfUnits;
  }

  public float getxInPdfUnits()
  {
    return xInPdfUnits;
  }

  public float getyInPdfUnits()
  {
    return yInPdfUnits;
  }

  public float getwInPdfUnits()
  {
    return wInPdfUnits;
  }

  public float gethInPdfUnits()
  {
    return hInPdfUnits;
  }

}
