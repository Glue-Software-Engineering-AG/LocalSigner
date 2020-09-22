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

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.faceless.pdf2.OutputProfile;
import org.faceless.pdf2.PDF;
import org.faceless.pdf2.PDFImage;
import org.faceless.pdf2.PDFPage;
import org.faceless.pdf2.PDFParser;
import org.faceless.pdf2.PDFReader;
import org.faceless.pdf2.PagePainter;
import ch.admin.localsigner.utils.ColorToConvert;

/**
 * This class determines the rough algorithm for converting a PDF to PDF/A
 * according to the template pattern. It is used by GUI and CLI.
 *
 */
public abstract class ConvertToPDFATemplate
{

  protected PDF oldpdf;
  protected PDFParser parser;
  protected PDF newpdf;
  ColorModel colorModel = ColorToConvert.COLOR.getColorModel();

  String dpi = "200";

  //template method
  protected void buildPDFA(byte[] inputFile, String outputName) throws IOException, InterruptedException
  {
    init(inputFile);
    createProfile();
    createPDFDocument();
    renderPDF(outputName);
  }

  protected abstract void createPDFDocument() throws InterruptedException;

  protected void createPage(int i) throws InterruptedException
  {
    PDFPage oldpage = oldpdf.getPage(i);
    PagePainter painter = parser.getPagePainter(oldpage);
    if (dpi == null)
      dpi = "200";
    BufferedImage image = painter.getImage(Float.parseFloat(dpi),
        colorModel);
    PDFImage pdfimage = new PDFImage(image);
    PDFPage newpage = newpdf.newPage(oldpage.getWidth(), oldpage.getHeight());
    newpage.drawImage(pdfimage, 0, 0, oldpage.getWidth(), oldpage.getHeight());
  }

  protected void init(byte[] inputFile) throws IOException
  {
    oldpdf = new PDF(new PDFReader(new ByteArrayInputStream(inputFile)));
    parser = new PDFParser(oldpdf);
  }

  protected void createProfile()
  {
    ICC_Profile icc = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
    OutputProfile profile = new OutputProfile(OutputProfile.PDFA1b_2005, "sRGB",
        null, "http://www.color.org", null, icc);
    newpdf = new PDF(profile);
  }

  protected void renderPDF(String outputName) throws FileNotFoundException, IOException
  {
    FileOutputStream fileOutputStream = new FileOutputStream(outputName);
    newpdf.render(fileOutputStream);
    newpdf.close();
    fileOutputStream.close();
  }

  public void setColorModel(ColorToConvert colorModel)
  {
    this.colorModel = colorModel.getColorModel();
  }

  public void setDpi(String dpi)
  {
    if (dpi != null)
    {
      this.dpi = dpi;
    }
    else
    {
      dpi = "200";
    }
  }
}
