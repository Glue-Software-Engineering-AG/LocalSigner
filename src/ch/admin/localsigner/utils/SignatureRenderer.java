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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * This class renders a signature as nice image. The image is inserted in the signature.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public final class SignatureRenderer
{
  public byte[] render(final String title, final String text, final int width,
          final int height, final int stroke, final Color backgroundColor,
          final Color borderColor, final Color textColor) throws IOException
  {
    int scaleFactor = 5;
    int w = width * scaleFactor;
    int h = height * scaleFactor;
    int arc = w / 10;
    int offset = h / 20;

    // render image to temp file
    BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = (Graphics2D) bimg.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int borderOff = (int) Math.ceil(stroke / 2.0);

    // background corners
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, w, h);

    // background
    g.setColor(backgroundColor);
    g.fillRoundRect(borderOff, borderOff, w - 2 * borderOff, h - 2 * borderOff, arc, arc);

    // border
    g.setColor(borderColor);
    g.setStroke(new BasicStroke(stroke));
    g.drawRoundRect(borderOff, borderOff, w - 2 * borderOff, h - 2 * borderOff, arc, arc);

    // render title
    g.setColor(textColor);
    renderString(g, new Font("Arial", Font.BOLD, 10), title, new Rectangle(offset, offset, w - 2 * offset, h / 2 - 2 * offset));

    // render text
    renderString(g, new Font("Arial", Font.PLAIN, 10), text, new Rectangle(offset, h / 2 + offset, w - 2 * offset, h / 2 - 2 * offset));

    // set jpeg quality to max
    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
    ImageWriter writer = iter.next();
    ImageWriteParam iwp = writer.getDefaultWriteParam();
    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    iwp.setCompressionQuality(1);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writer.setOutput(new MemoryCacheImageOutputStream(bos));
    IIOImage image = new IIOImage(bimg, null, null);
    writer.write(null, image, iwp);
    writer.dispose();

    return bos.toByteArray();
  }

  private void renderString(Graphics2D g, Font font, String text, Rectangle rectangle)
  {
    int fontSize = 5;
    int fontY = 0;

    // debug box
    //g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

    while (fontSize < 150)
    {
      Font tmpFont = font.deriveFont((float) (fontSize + 1));
      FontMetrics m = g.getFontMetrics(tmpFont);

      if (m.stringWidth(text) < rectangle.width && m.getHeight() < rectangle.height)
      {
        fontSize++;

        int space = (rectangle.height - m.getHeight()) / 2;
        fontY = rectangle.y + rectangle.height - space - m.getDescent();
      }
      else
      {
        // too big for box
        break;
      }
    }

    g.setFont(font.deriveFont((float) fontSize));
    g.drawString(text, rectangle.x, fontY);
  }

}
