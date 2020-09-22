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

import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.LocalSigner;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.*;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class creates the PDF file with the signature preview written on it. The
 * signature preview is showed after the user selected the signature position
 * with the mouse.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public final class SignaturePreview
{
  private static final Logger LOGGER = Logger.getLogger(SignaturePreview.class);

  private static final int INTENSITY = 180;

  private final Color colorSig = new Color(INTENSITY, 255, INTENSITY); // green

  private final Color colorCert = new Color(INTENSITY, INTENSITY, 255); // blue

  private final Color colorSigField = new Color(250, INTENSITY, 250); // violet

  private final Color colorTextField = new Color(255, 255, INTENSITY); // yellow

  private static final int POSITION_PAGE = 0;
  private static final int POSITION_LOWER_LEFT_X = 1;
  private static final int POSITION_LOWER_LEFT_Y = 2;
  private static final int POSITION_UPPER_RIGHT_X = 3;
  private static final int POSITION_UPPER_RIGHT_Y = 4;

  private MainGUI maingui;

  private static SignaturePreview instance;

  // private constructor
  private SignaturePreview()
  {
    // not meant to be instantiated
  }

  public static SignaturePreview getInstance()
  {
    if (instance == null)
    {
      instance = new SignaturePreview();
    }
    return instance;
  }

  /**
   * Adds the signature preview onto the given input file and returns the new
   * file path. If the preview can not be applied the original file is returned
   * instead.
   *
   * @param originalFile
   *          The input file
   * @param gui
   *          The application main GUI
   * @return The path to the file with the preview
   * @throws BadPasswordException
   */
  @SuppressWarnings("unchecked")
  public byte[] createPreview(final byte[] originalFile, final MainGUI gui)
          throws BadPasswordException
  {
    byte[] preview = originalFile;

    try
    {
      this.maingui = gui;
      final PropertiesGUI propertiesGui = gui.getPropertiesGui();
      final PdfReader reader = new PdfReader(originalFile);

      // calculate where to sign the pdf
      int signpage = propertiesGui.getSignaturePageSignature(reader.getNumberOfPages());

      int pageHeight = (int) reader.getPageSizeWithRotation(signpage).getHeight();

      BoxPosition guiPos ;
      if (propertiesGui.isImageOnlyAndFixedSize())
      {
        guiPos = new BoxPosition(signpage, propertiesGui.getLeftPosInPdfUnits(),
            propertiesGui.getTopPosPdfUnit(), propertiesGui.getBoxWidthImage(),
            propertiesGui.getBoxHeightImage());
      }
      else
      {
        guiPos = new BoxPosition(signpage, propertiesGui.getLeftPosInPdfUnits(),
            propertiesGui.getTopPosPdfUnit(), propertiesGui.getBoxWidthInPdfUnits(),
            propertiesGui.getBoxHeightInPdfUnits());
      }
      BoxPosition pdfPos = guiPos.convertGuiToPdf(pageHeight);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      final PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);

      // add signature box
      if (maingui.canDraw())
      {
        this.addSignatureBox(stamp, pdfPos);
      }

      // add signature fields
      ArrayList<String> fields = reader.getAcroFields().getBlankSignatureNames();
      for (String name : fields)
      {
        LOGGER.debug("draw signature field " + name);
        float[] pos = reader.getAcroFields().getFieldPositions(name);
        pdfPos = new BoxPosition((int) pos[POSITION_PAGE],
          pos[POSITION_LOWER_LEFT_X],
          pos[POSITION_LOWER_LEFT_Y],
          pos[POSITION_UPPER_RIGHT_X] - pos[POSITION_LOWER_LEFT_X],
          pos[POSITION_UPPER_RIGHT_Y] - pos[POSITION_LOWER_LEFT_Y]);
        pdfPos.setName(name);
        this.addSignatureField(stamp, pdfPos);
      }

      if (maingui.getGuiMode() == GuiMode.addSignatureField)
      {
        // also draw temporary signature fields
        for (BoxPosition box : maingui.getDocument().getSigFields())
        {
          LOGGER.debug("draw temporary signature field " + box.getName() + " on page "
              + box.getPage());

          pageHeight = (int) reader.getPageSizeWithRotation(box.getPage()).
                  getHeight();
          pdfPos = box.convertGuiToPdf(pageHeight);

          this.addSignatureField(stamp, pdfPos);
        }
      }

      stamp.close();
      baos.close();

      return baos.toByteArray();

    } catch (BadPasswordException bpe)
    {
      throw bpe;
    } catch (Exception e)
    {
      LOGGER.error("Cannot preview", e);
      // if something goes wrong during stamping return the original unstamped
      // file
    }

    return preview;
  }

  private void setTransparency(final PdfContentByte cb, final float alpha)
  {
    PdfGState gstate = new PdfGState();
    gstate.setFillOpacity(alpha);
    cb.saveState();
    cb.setGState(gstate);
  }

  private void addSignatureBox(final PdfStamper stamp,
          final BoxPosition position)
          throws DocumentException, IOException
  {
    PdfContentByte over = stamp.getOverContent(position.getPage());
    float x = position.getxInPdfUnits();
    float y = position.getyInPdfUnits();
    float w = position.getwInPdfUnits();
    float h = position.gethInPdfUnits();

    // find out if the preview is for signing or certifying
    Color color;
    String text;
    String previewText;
    String image = maingui.getPropertiesGui().getBackgroundImage();

    boolean isImageUsed = maingui.getPropertiesGui().shouldShowImageInSignature();
    boolean isTextUsed = maingui.getPropertiesGui().shouldShowTextInSignature();

    if (maingui.getPropertiesGui().isCertificationType())
    {
      color = colorCert;
      text = LocalSigner.i18n("certification");
      previewText = LocalSigner.i18n("sigText");
    }
    else
    {
      color = colorSig;
      text = LocalSigner.i18n("signature");
      previewText = LocalSigner.i18n("sigText");
    }

    if (maingui.getGuiMode() == GuiMode.addTextField)
    {
      // add text box
      color = colorTextField;
      text = LocalSigner.i18n("textField");
      previewText = "";
      image = "";
    }

    if (maingui.getGuiMode() == GuiMode.addSignatureField)
    {
      color = colorSigField;
      text = LocalSigner.i18n("sigField");
      previewText = "";
      image = "";
    }

    // add some text
    over.beginText();
    over.setColorFill(Color.BLACK);
    over.setColorStroke(Color.BLACK);
    over.setFontAndSize(
            BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
            BaseFont.EMBEDDED), 10);
    // box title
    over.showTextAligned(Element.ALIGN_LEFT, text, x + 5, y + h + 5, 0);
    // box resize hint
    if (maingui.isInternalViewer())
    {
      String hintText = evaluateHintToShow(isImageUsed, isTextUsed);

      String[] split = hintText.split("\n");
      for (int i = 0; i < split.length; i++)
      {
        over.showTextAligned(Element.ALIGN_TOP, split[i], x, y - 12 - i * 12, 0);
      }
    }
    over.endText();

    // black dashed border
    over.setColorFill(Color.BLACK);
    over.rectangle(x, y, w, h);
    LOGGER.debug("Signature box: " + position);
    over.setLineDash(4, 0);
    over.setLineWidth(1);

    // preview box of image
    if ("".equals(image))
    {
      image = null;
    }
    if (image != null && isImageUsed && isTextUsed)
    {
      // draw line between image and text
      over.moveTo(x + w / 2, y);
      over.lineTo(x + w / 2, y + h);
    }

    // crosshair
    over.moveTo(x - 15, y + h);
    over.lineTo(x, y + h);
    over.lineTo(x, y + h + 15);

    // stroke all lines
    over.stroke();

    // crosshair dot
    over.circle(x, y + h, 3);
    over.fill();

    // fill with transparent color
    this.setTransparency(over, 0.5f);
    over.setColorFill(color);
    over.rectangle(x, y, w, h);
    over.fill();

    // preview text
    over.setColorFill(Color.BLACK);
    if (image != null && isImageUsed && isTextUsed)
    {
      over.beginText();
      over.showTextAligned(Element.ALIGN_CENTER, LocalSigner.i18n("sigImage"), x
              + (int) (0.25 * w), y - 5 + h / 2, 0);
      over.showTextAligned(Element.ALIGN_CENTER, previewText, x + (int) (0.75
              * w), y - 5
              + h / 2, 0);
      over.endText();
    }
    else if (image != null && isImageUsed && !isTextUsed) // image only
    {
      over.beginText();
      over.showTextAligned(Element.ALIGN_CENTER, LocalSigner.i18n("sigImage"), x
          + (int) (0.5 * w), y - 5 + h / 2, 0);
      over.endText();
    }
    else
    // text only
    {
      over.beginText();
      over.showTextAligned(Element.ALIGN_CENTER, previewText, x
              + (int) (0.5 * w), y - 5
              + h / 2, 0);
      over.endText();
    }
  }

  private String evaluateHintToShow(boolean isImageUsed, boolean isTextUsed)
  {
    if (isImageUsed && !isTextUsed)
    {
      return LocalSigner.i18n("chooseRectangleFixed");
    }
    else
    {
      return LocalSigner.i18n("chooseRectangle");
    }
  }

  private void addSignatureField(final PdfStamper stamp, final BoxPosition position)
      throws DocumentException, IOException
  {

    float x = position.getxInPdfUnits();
    float y = position.getyInPdfUnits();
    float w = position.getwInPdfUnits();
    float h = position.gethInPdfUnits();

    PdfContentByte over = stamp.getOverContent(position.getPage());
    LOGGER.debug("drawing signature field " + position.getName() + " on page "
        + position.getPage());
    LOGGER.debug("position: " + position);

    // signature name
    over.setColorFill(Color.BLACK);
    over.setColorStroke(Color.BLACK);
    over.beginText();
    over.setFontAndSize(
        BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED), 10);
    over.showTextAligned(Element.ALIGN_CENTER, position.getName(), (int) (x + 0.5 * w),
        (int) (y + 0.5 * h), 0);
    over.endText();

    // draw dashed border
    over.rectangle(x, y, w, h);
    over.setLineDash(4, 0);
    over.setLineWidth(1);
    over.stroke();

    // fill with transparent color
    this.setTransparency(over, 0.5f);

    over.setColorFill(colorSigField);
    over.rectangle(x, y, w, h);
    over.fill();
  }

}
