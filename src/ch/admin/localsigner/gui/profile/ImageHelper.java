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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Handling of profile images. Reads the Dimension and the DPI value of the
 * image from the Metadata and scales the size to fit to Pdf-Points.
 * 
 * @author weisskopf
 *
 */
public class ImageHelper
{

  private static final float ONE_INCH = 25.4f;

  private static final Logger LOGGER = Logger.getLogger(ImageHelper.class);

  private static final int DPI_PDF_UNIT = 72;

  public Dimension getBackgroundImageDimension(String image, String profilePath)
  {
    if (StringUtils.isBlank(image))
    {
      return new Dimension();
    }
    File file = ProfileFileLoader.loadFileFromPath(image, profilePath);

    if (file == null ||!file.exists())
    {
      LOGGER.warn("The background image does not exist at path: "
          + (file!=null?file.getAbsolutePath():"null"));
      return new Dimension();
    }

    ImageInputStream in = null;
    try
    {
      in = ImageIO.createImageInputStream(file);
      if (in == null)
      {
        return new Dimension();
      }

      final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
      if (readers.hasNext())
      {
        ImageReader reader = readers.next();
        try
        {
          reader.setInput(in);
          // lookup dpi value in image, convert dpi to pdf-units (72dpi)
          Point dpi = getDpi(reader);
          return getScaledImageDimension(reader, dpi);
        } finally
        {
          reader.dispose();
        }
      }
    } catch (IOException ioex)
    {
      LOGGER.error("Could not load the image from: " + file.getAbsolutePath(), ioex);
      throw new UnhandledException(ioex);
    } finally
    {
      IOUtils.closeQuietly(in);
    }
    return new Dimension();
  }

  private Dimension getScaledImageDimension(ImageReader reader, Point dpi)
      throws IOException
  {
    double dpiScaleX = dpi.getX() / DPI_PDF_UNIT;
    double dpiScaleY = dpi.getY() / DPI_PDF_UNIT;
    int imgWidthInPixels = reader.getWidth(0);
    int scaledWidth = (int) (imgWidthInPixels / dpiScaleX);
    int imgHeightInPixels = reader.getHeight(0);
    int scaledHeight = (int) (imgHeightInPixels / dpiScaleY);
    return new Dimension(scaledWidth, scaledHeight);
  }

  private Point getDpi(ImageReader reader)
  {
    // default values
    int xDPI = 72;
    int yDPI = 72;
    IIOMetadata meta;
    try
    {
      meta = reader.getImageMetadata(0);
    } catch (IOException e)
    {
      return new Point(xDPI, yDPI);
    }

    IIOMetadataNode root = (IIOMetadataNode) meta.getAsTree("javax_imageio_1.0");
    NodeList nodes = root.getElementsByTagName("HorizontalPixelSize");
    if (nodes.getLength() > 0)
    {
      IIOMetadataNode dpcWidth = (IIOMetadataNode) nodes.item(0);
      NamedNodeMap nnm = dpcWidth.getAttributes();
      Node item = nnm.item(0);
      xDPI = Math.round(ONE_INCH / Float.parseFloat(item.getNodeValue()));

      nodes = root.getElementsByTagName("VerticalPixelSize");
      IIOMetadataNode dpcHeight = (IIOMetadataNode) nodes.item(0);
      nnm = dpcHeight.getAttributes();
      item = nnm.item(0);
      yDPI = Math.round(ONE_INCH / Float.parseFloat(item.getNodeValue()));
    }
    return new Point(xDPI, yDPI);
  }

}
