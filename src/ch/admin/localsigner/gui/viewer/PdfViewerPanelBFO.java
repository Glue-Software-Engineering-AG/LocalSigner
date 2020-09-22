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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;
import org.faceless.pdf2.PDF;
import org.faceless.pdf2.PDFPage;
import org.faceless.pdf2.PDFReader;
import org.faceless.pdf2.viewer2.DocumentPanelEvent;
import org.faceless.pdf2.viewer2.DocumentPanelListener;
import org.faceless.pdf2.viewer2.DocumentViewport;
import org.faceless.pdf2.viewer2.PDFViewer;
import org.faceless.pdf2.viewer2.PagePanelInteractionEvent;
import org.faceless.pdf2.viewer2.PagePanelInteractionListener;
import org.faceless.pdf2.viewer2.ViewerFeature;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.profile.Profile;
import ch.admin.localsigner.gui.profile.ProfileLoader;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.BoxPosition;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import java.awt.BasicStroke;
import java.awt.MouseInfo;
import java.awt.Stroke;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.UnhandledException;

/**
 * This class displays a PDF document using the BFO reader and draw / place signature boxes
 * (http://big.faceless.org/products/pdf/viewer.jsp).
 *
 * @author Rafael Wampfler
 */
public class PdfViewerPanelBFO extends JPanel implements DocumentPanelListener, PagePanelInteractionListener,
    KeyListener
{

  private static final float ZERO_FLOAT = 0.0f;

  private static final Color LIGHT_GRAY = new Color(100, 100, 100);
  private static final Color TRANSPARENT_RED = new Color(255, 0, 0, 10);

  private static final int MINIMAL_BOX_SIZE = 10; // pt; applies only on drawn boxes; size in profile may be smaller

  private static final int POSITION_PAGE = 0;

  private static final int POSITION_LOWER_LEFT_X = 1;

  private static final int POSITION_LOWER_LEFT_Y = 2;

  private static final int POSITION_UPPER_RIGHT_X = 3;

  private static final int POSITION_UPPER_RIGHT_Y = 4;

  private static final int SCROLL_SPEED = 15;

  private static final String EVENT_TYPE_VIEWPORT_CHANGED = "viewportChanged";

  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(PdfViewerPanelBFO.class);

  private final PDFViewer viewer;

  private boolean drawing = false;

  private boolean disableDrawing = false;

  private int x;

  private int y;

  private int w;

  private int h;

  private int defaultSizeWidth;

  private int defaultSizeHeight;

  private final MainGUI mainGui;

  /**
   * This point is on the PagePanel.
   */
  private Point lastMouse;

  /**
   * This point is on the DocumentPanel.
   */
  private Point lastMouseScreen;

  private int lastPageNr = 0;

  private PDF pdf;

  private boolean isFixedDrawMode;

  private float fixedWidthPixel;

  private float fixedHeightPixel;

  private boolean isAlignSecond;

  private LastSignature lastSignature;

  /**
   * Constructor
   *
   * @param mainGui
   * Main GUI
   */
  public PdfViewerPanelBFO(final MainGUI mainGui)
  {
    this.mainGui = mainGui;
    final PdfViewerPanelBFO instance = this;

    viewer = new PDFViewer(getViewerFeatures());

    this.setLayout(new BorderLayout());

    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        instance.add(viewer);
        viewer.addDocumentPanelListener(instance);
      }

    });
    setVisible(true);

  }

  private Point getLastMouseOnPagePanel(PagePanelInteractionEvent event)
  {
    return event.getMouseEvent().getPoint();
  }

  private Point getMousePositionOnDocumentPanel(final PagePanelInteractionEvent event)
  {
    Point mousePosOnScreen = MouseInfo.getPointerInfo().getLocation();
    Point viewerPosOnScreen = viewer.getLocationOnScreen();
    Point mousePosOnViewer = new Point(
        (int) (mousePosOnScreen.getX() - viewerPosOnScreen.getX()),
        (int) (mousePosOnScreen.getY() - viewerPosOnScreen.getY()));
    return mousePosOnViewer;

    // Das folgende funktioniert auf MacOS nicht mehr, darum rechnen wir das halt jetzt von Hand aus. Das funktioniert
    // überall tadellos. Siehe LOCALSIG-358.
    // event.getEventOnNewPanel().getPagePanel().getDocumentPanel().getMousePosition();
  }

  private List<ViewerFeature> getViewerFeatures()
  {
    ArrayList<ViewerFeature> list = new ArrayList<ViewerFeature>(0);

    // no features at the moment
    return list;
  }

  /**
   * Handle mouse events on PDF viewer.
   *
   * @param event The events on this page panel
   */
  @Override
  public void pageAction(final PagePanelInteractionEvent event)
  {
    if (disableDrawing)
    {
      return;
    }

    Point mouse = getLastMouseOnPagePanel(event);
    if (mouse != null)
    {
      lastMouse = mouse;
    }

    final String type = event.getType();

    if ("mousePressed".equals(type))
    {
      mousePressed(event);
    }
    if ("mouseReleased".equals(type))
    {
      mouseReleased(event);
    }
    if ("mouseDragged".equals(type))
    {
      mouseDragged(event);
    }
  }

  /**
   * Close PDF viewer panel.
   */
  public void close()
  {
    LOGGER.debug("closing pdf panel");
    if (getCurrentShownPDF() != null)
    {
      getCurrentShownPDF().close();
      viewer.getActiveDocumentPanel().setPDF(null);
    }
  }

  /**
   * Open a file in the PDF viewer.
   *
   * @param fileData File to show
   */
  public final void openFile(final byte[] fileData)
  {

    try
    {
      pdf = loadPDF(fileData);
      /*
      * Since bfo version 2.15 it seems to respect options in pdfs, but we do
      * not want this. We override this option to be able to see all pages.
       */
      pdf.setOption("pagelayout", "OneColumn");
      viewer.getActiveDocumentPanel().setPDF(null);
      resetLastSignedPageIfOutOfRange();
      viewer.getActiveDocumentPanel().setPDF(pdf, pdf.getPage(lastPageNr));
      resetLastDrawnPage();

      // we want to control the viewer only by ourselves
      viewer.getActiveDocumentPanel().getViewport().getActionMap().clear();
      viewer.getActiveDocumentPanel().getViewport().addKeyListener(this);

      viewer.repaint();
      fullWidth();
    } catch (NullPointerException npe)
    {
      // may happen on slow machines during displaying startup file (on Adrian's netbook)
      LOGGER.debug("Error opening PDF file ", npe);
    } catch (Exception e)
    {
      LOGGER.error("Error opening PDF file ", e);
      Message.warning(mainGui.getMainshell(),
          LocalSigner.i18n("jpanelOpenFileError"),
          LocalSigner.i18n("jpanelOpenFileErrorExtended"));
    }
  }

  private void resetLastSignedPageIfOutOfRange()
  {
    int totalPages = pdf.getNumberOfPages();
    // lastPage is zero-indexed, totalPages counts from one
    if (totalPages < (lastPageNr + 1))
    {
      resetLastDrawnPage();
    }
  }

  private void resetLastDrawnPage()
  {
    lastPageNr = 0;
  }

  private void closeOldFile()
  {
    // close old file (temp file may remain)
    if (getCurrentShownPDF() != null)
    {
      getCurrentShownPDF().close();
    }
  }

  private PDF getCurrentShownPDF()
  {
    return viewer.getActiveDocumentPanel().getPDF();
  }

  private PDF loadPDF(final byte[] fileData) throws IOException, FileNotFoundException
  {

    closeOldFile();
    // future version: open pdf files with password encryption
    // StandardEncryptionHandler enc = new StandardEncryptionHandler();
    // enc.setUserPassword("secret");
    // enc.setOwnerPassword("secret");
    final PDF pdf = new PDF(new PDFReader(new ByteArrayInputStream(fileData)));
    return pdf;
  }

  /**
   * Set viewer to full window width.
   */
  private void fullWidth()
  {
    if (viewer.getActiveDocumentPanel().getViewport() != null)
    {
      viewer.getActiveDocumentPanel().getViewport().setZoomMode(DocumentViewport.ZOOM_FITWIDTH);
      viewer.invalidate();
      viewer.repaint();
    }
  }

  private void mousePressed(final PagePanelInteractionEvent event)
  {
    drawing = true;

    evalFixedDrawingMode();
    evalIsAlignYAxis();
    if (isAlignYAxisEnabledAndDoesItApply()
        && event.getPage().getPageNumber() != lastSignature.getLastSignatureOnPage())
    {
      PDFPage thePageToShow = getCurrentShownPDF().getPage(lastSignature.getLastSignatureOnPage() - 1);
      viewer.getActiveDocumentPanel().setPage(thePageToShow, 0,
          lastSignature.getLastSignatureYValue() - 100,
          viewer.getActiveDocumentPanel().getZoom());
    }

    x = lastMouse.x;
    y = lastMouse.y;
    LOGGER.debug("Mouse pressed: " + x + ", " + y);

    Point mouse = getMousePositionOnDocumentPanel(event);
    if (mouse != null)
    {
      lastMouseScreen = mouse;
    }

    if (isFixedDrawingMode())
    {
      repaint();
    }
  }

  private void mouseReleased(final PagePanelInteractionEvent event)
  {
    LOGGER.debug("Mouse released: " + lastMouse.x + ", " + lastMouse.y);
    drawing = false;

    final int signpage = evaluateCurrentSigningPage(event);

    BoxPosition boxPos;
    // calculate the position (x,y,width,heigth)
    if (isScaledDrawingMode())
    {
      boxPos = calculateBoxPositionScaled(event, signpage);
    } else
    {
      boxPos = calculateBoxPositionFixed(event, signpage);
    }

    final BoxPosition boxPos2 = boxPos;

    // store the values in the signature property dialog
    mainGui.getMainshell().getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        PropertiesGUI props = mainGui.getPropertiesGui();
        props.setLefPosPdfUnits(boxPos2.getxInPdfUnits());
        props.setTopPosPdfUnits(boxPos2.getyInPdfUnits());
        props.setBoxWidthInPdfUnits(boxPos2.getwInPdfUnits());
        props.setBoxHeightInPdfUnits(boxPos2.gethInPdfUnits());
        props.setSignaturePageDraw(boxPos2.getPage());
        mainGui.reloadInputFile(true);
      }
    });
  }

  private BoxPosition calculateBoxPositionFixed(final PagePanelInteractionEvent event, int signpage)
  {
    final float xfactor = getScalingXAxisFromPage(event);
    x = lastMouse.x;
    float xcm = x * xfactor;

    float yfactor = getScalingYAxisFromPage(event);
    float ycm;
    if (isAlignYAxisEnabledAndDoesItApply())
    {
      if (isInWidthRangeOfLastSignature(xfactor))
      {
        y = lastMouse.y;
        ycm = y * yfactor;
        xcm = lastSignature.getLastSignatureXValue();
      } else
      {
        y = Math.round(lastSignature.getLastSignatureYValue() / yfactor);
        ycm = lastSignature.getLastSignatureYValue();
      }
      signpage = lastSignature.getLastSignatureOnPage();
    } else
    {
      y = lastMouse.y;
      ycm = y * yfactor;
    }

    // scaling to pdf pixel
    return new BoxPosition(signpage, xcm, ycm, fixedWidthPixel, fixedHeightPixel);
  }

  private int evaluateCurrentSigningPage(final PagePanelInteractionEvent event)
  {
    final int signpage = event.getPage().getPageNumber();
    setLastDrawnPage(signpage);
    return signpage;
  }

  private void setLastDrawnPage(final int signpage)
  {
    lastPageNr = signpage - 1;
  }

  private float getScalingYAxisFromPage(final PagePanelInteractionEvent event)
  {
    return (float) event.getPage().getHeight()
        / (float) (event.getPagePanel().getHeight());
  }

  private float getScalingXAxisFromPage(final PagePanelInteractionEvent event)
  {
    return (float) event.getPage().getWidth() / (float) event.getPagePanel().getWidth();
  }

  private BoxPosition calculateBoxPositionScaled(final PagePanelInteractionEvent event,
      int currentSigningPage)
  {
    // TODO bei starker Verkleinerung der page kann ein ganz kleines viereck
    // gezeichnet werden, dann geht diese prüfung schief und die Box wird
    // riesig. der offset müsste sich an die skalierung anpassen


    // scaling to pdf pixel
    final float xfactor = getScalingXAxisFromPage(event);
    final float yfactor = getScalingYAxisFromPage(event);
    if (isBoxTooSmall(lastMouse.x - x, lastMouse.y - y))
    { // just a mouseclick
      LOGGER.debug("user just clicked and did not drag a rectangle - use default values");
      reloadProfileValues();
      reloadProfileBoxSize();
      w = (int)(defaultSizeWidth / xfactor);
      h = (int)(defaultSizeHeight / yfactor);
    }

    x = (isNegative(w) ? x + w : x);
    y = (isNegative(h) ? y + h : y);
    w = Math.abs(w);
    h = Math.abs(h);

    // override for Y-Value on Y-Axis-Fixation
    if (isAlignYAxisEnabledAndDoesItApply())
    {
      y = Math.round(lastSignature.getLastSignatureYValue() / yfactor);
      currentSigningPage = lastSignature.getLastSignatureOnPage();
    }

    LOGGER.debug("Box: " + Math.abs(w) + "x" + Math.abs(h) + " @ " + x + "," + y
        + " page " + currentSigningPage);

    final float xPdfUnits = x * xfactor;
    final float yPdfUnits = y * yfactor;
    final float wPdfUnits = w * xfactor;
    final float hPdfUnits = h * yfactor;

    LOGGER.debug("Box in pdfpoints: " + wPdfUnits + "x" + hPdfUnits + " @ " + xPdfUnits
        + "," + yPdfUnits);

    return new BoxPosition(currentSigningPage, xPdfUnits, yPdfUnits, wPdfUnits, hPdfUnits);
  }
  
  private void reloadProfileValues()
  {
    mainGui.getMainshell().getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        Profile profile = mainGui.getSelectedProfile();
        ProfileLoader.loadProfile(mainGui, profile);
      }
    });
  }

  private void reloadProfileBoxSize()
  {
    mainGui.getMainshell().getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        PropertiesGUI props = mainGui.getPropertiesGui();
        
        if (props.isImageOnlyAndFixedSize())
        {
          LOGGER.debug("Mode to compute default size is: Image only with fixed size");
          PdfViewerPanelBFO.this.defaultSizeWidth = (int)props.getBoxWidthImage();
          PdfViewerPanelBFO.this.defaultSizeHeight = (int)props.getBoxHeightImage();
        }
        else
        {
          LOGGER.debug("Mode to compute default size is: Not only image with fixed size");
          PdfViewerPanelBFO.this.defaultSizeWidth = (int)props.getBoxWidthInPdfUnits();
          PdfViewerPanelBFO.this.defaultSizeHeight = (int)props.getBoxHeightInPdfUnits();
        }
        LOGGER.debug("Default image size is: w=" + PdfViewerPanelBFO.this.defaultSizeWidth + ", h="
            + PdfViewerPanelBFO.this.defaultSizeHeight);
      }
    });
  }

  private void mouseDragged(final PagePanelInteractionEvent event)
  {
    if (isFixedDrawingMode())
    {
      Point mouse = getMousePositionOnDocumentPanel(event);
      if (mouse != null)
      {
        lastMouseScreen = mouse;
      }
    }

    h = lastMouse.y - y;
    w = lastMouse.x - x;
    repaint();
  }

  /**
   * Draw the mouse selection (signature box) onto the PDF with a grey
   * rectangle.
   *
   * @param graphics
   * Graphics object to draw on
   */
  @Override
  public void paint(final Graphics graphics)
  {
    super.paint(graphics);
    Graphics2D g2d = (Graphics2D) graphics.create();

    if (!drawing)
    {
      return;
    }

    try
    {
      g2d.setColor(LIGHT_GRAY);

      if (isScaledDrawingMode())
      {
        drawScaledRectangle(g2d);
      } else
      {
        drawCrossHair(g2d);
      }
    } catch (Exception e)
    {
      // not ready yet
      LOGGER.debug("cannot paint rectangle", e);
    }
  }

  private void drawScaledRectangle(final Graphics graphics)
  {
    int screenX = lastMouseScreen.x;
    if (isNegative(w))
    {
      screenX += w;
    }
    int screenY = lastMouseScreen.y;
    if (isNegative(h))
    {
      screenY += h;
    }

    // override Y-Axis
    if (isAlignYAxisEnabledAndDoesItApply())
    {
      final float yfactor = getScalingYAxisFromViewer();

      screenY = calcYAxisRelativeToCurrentPage(yfactor);
    }

    graphics.drawRect(screenX, screenY, Math.abs(w), Math.abs(h));

    if (isBoxTooSmall(w, h))
    {
      graphics.setColor(TRANSPARENT_RED);
      graphics.fillRect(screenX+1, screenY+1, Math.abs(w)-1, Math.abs(h)-1);
    }
  }

  private boolean isBoxTooSmall(int w, int h)
  {
    final float xfactor = getScalingXAxisFromViewer();
    final float yfactor = getScalingYAxisFromViewer();
    return Math.abs(w) * xfactor < MINIMAL_BOX_SIZE || Math.abs(h) * yfactor < MINIMAL_BOX_SIZE;
  }

  private boolean isNegative(int x)
  {
    return x < 0;
  }

  private boolean isScaledDrawingMode()
  {
    return !isFixedDrawingMode();
  }

  private void drawCrossHair(final Graphics graphics)
  {
    // convert mm to pixel and scale it to display
    // scaling to pdf pixel
    final float xfactor = getScalingXAxisFromViewer();
    final float yfactor = getScalingYAxisFromViewer();

    int theYValue;
    int theXValue = lastMouseScreen.x;
    // evaluate the Y-value to use!
    if (isAlignYAxisEnabledAndDoesItApply())
    {
      if (isInWidthRangeOfLastSignature(xfactor))
      {
        theXValue = calcXAxisRelativeToCurrentPage(xfactor);
        theYValue = lastMouseScreen.y;
      } else
      {
        theXValue = lastMouseScreen.x;
        theYValue = calcYAxisRelativeToCurrentPage(yfactor);
      }
    } else
    {
      theYValue = lastMouseScreen.y;
    }

    Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
    ((Graphics2D) graphics).setStroke(dashed);
    // line from top to bottom
    graphics.drawLine(theXValue, 0, theXValue, getHeight());
    // line form right to left
    graphics.drawLine(0, theYValue, getWidth(), theYValue);

    int heightFixedScaledPixel = Math.round((fixedHeightPixel / xfactor));
    int widthFixedScaledPixel = Math.round((fixedWidthPixel / xfactor));

    graphics
        .drawRect(theXValue, theYValue, widthFixedScaledPixel, heightFixedScaledPixel);
  }

  private boolean isInWidthRangeOfLastSignature(final float xfactor)
  {
    return lastMouse.x > lastSignature.getLastSignatureXValue() / xfactor
        && (lastMouse.x < (lastSignature.getLastSignatureXValue() + lastSignature
        .getLastSignatureWidth()) / xfactor);
  }

  private int calcYAxisRelativeToCurrentPage(final float yfactor)
  {
    int scrollPosY = getScrollPosition();
    scrollPosY = scrollPosY - viewer.getActiveDocumentPanel().getViewport().getPagePanel().getY();
    // pixel from pdf scaled to display pixels
    int theYValue = Math.round(lastSignature.getLastSignatureYValue() / yfactor);
    return theYValue - scrollPosY;
  }

  private int calcXAxisRelativeToCurrentPage(final float xfactor)
  {
    int scrollPositionX = getHorizontalScrollbar().getValue();
    int xPosition = viewer.getActiveDocumentPanel().getViewport().getPagePanel().getX();
    // pixel from pdf scaled to display pixels
    int theYValue = Math.round(lastSignature.getLastSignatureXValue() / xfactor);
    return theYValue + xPosition - scrollPositionX;
  }

  private float getScalingYAxisFromViewer()
  {
    return (float) viewer.getActiveDocumentPanel().getViewport().getPage().getHeight()
        / (float) viewer.getActiveDocumentPanel().getViewport().getPagePanel().getHeight();
  }

  private float getScalingXAxisFromViewer()
  {
    return (float) viewer.getActiveDocumentPanel().getViewport().getPage().getWidth()
        / (float) viewer.getActiveDocumentPanel().getViewport().getPagePanel().getWidth();
  }

  /**
   * Called when document is updated.
   *
   * @param event document panel event
   */
  @Override
  public void documentUpdated(DocumentPanelEvent event)
  {
    if (EVENT_TYPE_VIEWPORT_CHANGED.equals(event.getType()))
    {
      event.getDocumentPanel().getViewport().addPagePanelInteractionListener(this);
    }
  }

  /**
   * Enable or disable drawing the signature box on the PDF.
   *
   * @param disableDrawing true to disable
   */
  public void setDisableDrawing(boolean disableDrawing)
  {
    this.disableDrawing = disableDrawing;
  }

  /**
   * Check if drawing is not allowed at the moment.
   *
   * @return true if drawing is disabled
   */
  public boolean isDisableDrawing()
  {
    return disableDrawing;
  }

  public int getScrollPosition()
  {
    if (viewer.getActiveDocumentPanel() == null)
    {
      return 0;
    }

    return getVerticalScrollbar().getValue();
  }

  public void setScrollPosition(int pos)
  {
    if (hasActiveDocumentPanel())
    {
      getVerticalScrollbar().setValue(pos);
    }
  }

  @Override
  public void keyTyped(KeyEvent e)
  {
    // nop
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
    // nop
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_DOWN)
    {
      moveViewPortDown();
    } else if (keyCode == KeyEvent.VK_UP)
    {
      moveViewPortUp();
    } else if (keyCode == KeyEvent.VK_PAGE_DOWN)
    {
      moveViewPortPageDown();
    } else if (keyCode == KeyEvent.VK_PAGE_UP)
    {
      moveViewPortPageUp();
    } else if (keyCode == KeyEvent.VK_END)
    {
      moveViewPortToEnd();
    } else if (keyCode == KeyEvent.VK_HOME)
    {
      moveViewPortToStart();
    }
  }

  private void moveViewPortDown()
  {
    if (hasActiveDocumentPanel())
    {
      int currVal = getVerticalScrollbar().getValue();
      int maxVal = getVerticalScrollbar().getMaximum();
      int newVal = currVal + SCROLL_SPEED;
      if (newVal > maxVal)
      {
        getVerticalScrollbar().setValue(maxVal);
      } else
      {
        getVerticalScrollbar().setValue(newVal);
      }
    }
  }

  private void moveViewPortUp()
  {
    if (hasActiveDocumentPanel())
    {
      int currVal = getVerticalScrollbar().getValue();
      int minVal = getVerticalScrollbar().getMinimum();
      int newVal = currVal - SCROLL_SPEED;
      if (newVal < minVal)
      {
        getVerticalScrollbar().setValue(minVal);
      } else
      {
        getVerticalScrollbar().setValue(newVal);
      }
    }
  }

  private void moveViewPortPageDown()
  {
    if (hasActiveDocumentPanel())
    {
      int currVal = getVerticalScrollbar().getValue();
      int maxVal = getVerticalScrollbar().getMaximum();
      int step = getVerticalScrollbar().getVisibleAmount();
      int newVal = currVal + step;
      if (newVal > maxVal)
      {
        getVerticalScrollbar().setValue(maxVal);
      } else
      {
        getVerticalScrollbar().setValue(newVal);
      }
    }
  }

  private void moveViewPortPageUp()
  {
    if (hasActiveDocumentPanel())
    {
      int currVal = getVerticalScrollbar().getValue();
      int minVal = getVerticalScrollbar().getMinimum();
      int step = getVerticalScrollbar().getVisibleAmount();
      int newVal = currVal - step;
      if (newVal < minVal)
      {
        getVerticalScrollbar().setValue(minVal);
      } else
      {
        getVerticalScrollbar().setValue(newVal);
      }
    }
  }

  private void moveViewPortToEnd()
  {
    if (hasActiveDocumentPanel())
    {
      int maxVal = getVerticalScrollbar().getMaximum();
      getVerticalScrollbar().setValue(maxVal);
    }
  }

  private boolean hasActiveDocumentPanel()
  {
    return viewer.getActiveDocumentPanel() != null;
  }

  private void moveViewPortToStart()
  {
    if (hasActiveDocumentPanel())
    {
      int minVal = getVerticalScrollbar().getMinimum();
      getVerticalScrollbar().setValue(minVal);
    }
  }

  private Adjustable getVerticalScrollbar()
  {
    return viewer.getActiveDocumentPanel().getViewport().getAdjustable(Adjustable.VERTICAL);
  }

  private Adjustable getHorizontalScrollbar()
  {
    return viewer.getActiveDocumentPanel().getViewport()
        .getAdjustable(Adjustable.HORIZONTAL);
  }

  private boolean isFixedDrawingMode()
  {
    return isFixedDrawMode;
  }

  private void evalFixedDrawingMode()
  {
    mainGui.getMainshell().getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        PropertiesGUI props = mainGui.getPropertiesGui();
        boolean isFixedMode = props.isImageOnlyAndFixedSize();
        PdfViewerPanelBFO.this.isFixedDrawMode = isFixedMode;
        if (isFixedMode)
        {
          PdfViewerPanelBFO.this.fixedHeightPixel = props.getBoxHeightImage();
          PdfViewerPanelBFO.this.fixedWidthPixel = props.getBoxWidthImage();
        }
      }
    });
  }

  private void evalIsAlignYAxis()
  {
    mainGui.getMainshell().getDisplay().syncExec(new Runnable()
    {

      @Override
      public void run()
      {
        PropertiesGUI props = mainGui.getPropertiesGui();
        boolean isFeatureEnabledInProfile = props.shouldSecondSignatureYAxisBeFixed();
        if (!isFeatureEnabledInProfile)
        {
          PdfViewerPanelBFO.this.isAlignSecond = false;
          return;
        }

        try (PdfReader reader = new PdfReader(mainGui.getDocument().getInputFile().getOriginalFile());)
        {
          AcroFields acroFields = reader.getAcroFields();
          @SuppressWarnings ("unchecked")
          ArrayList<String> sigNames = acroFields.getSignatureNames();
          if (sigNames.isEmpty())
          {
            PdfViewerPanelBFO.this.isAlignSecond = false;
            return;
          }

          // Ordering of signature fields
          Map<Integer, String> orderedSet = sortSignatures(acroFields, sigNames);

          String lastVisibleSignature = getLastVisibleSignature(orderedSet, acroFields);

          if (lastVisibleSignature == null)
          {
            PdfViewerPanelBFO.this.isAlignSecond = false;
            return;
          }

          float[] pos = acroFields.getFieldPositions(lastVisibleSignature);

          int signedPage = (int) pos[POSITION_PAGE];
          float lowerLeftX = pos[POSITION_LOWER_LEFT_X];
          float upperRightX = pos[POSITION_UPPER_RIGHT_X];
          float upperRightY = pos[POSITION_UPPER_RIGHT_Y];

          Rectangle pageSize = reader.getPageSizeWithRotation(signedPage);

          LastSignature signature = new LastSignature();
          signature.setLastSignatureOnPage(signedPage);
          signature.setLastSignatureYValue(pageSize.getHeight() - upperRightY);
          signature.setLastSignatureXValue(lowerLeftX);
          signature.setLastSignatureWidth(upperRightX - lowerLeftX);

          PdfViewerPanelBFO.this.lastSignature = signature;
          PdfViewerPanelBFO.this.isAlignSecond = true;

        } catch (IOException e)
        {
          LOGGER.error("Error detecting first signature of file", e);
          throw new UnhandledException(e);
        }
      }

      private Map<Integer, String> sortSignatures(AcroFields acroFields,
          ArrayList<String> sigNames)
      {
        Map<Integer, String> orderedSet = new TreeMap<Integer, String>();
        for (String signatureName : sigNames)
        {
          orderedSet.put(acroFields.getRevision(signatureName), signatureName);
        }
        return orderedSet;
      }

      /**
       * Move from last to first signature and return the first to be visible.
       */
      private String getLastVisibleSignature(Map<Integer, String> orderedSet,
          AcroFields acroFields)
      {
        for (int i = orderedSet.size(); i > 0; i--)
        {
          String signatureName = orderedSet.get(i);
          float[] pos = acroFields.getFieldPositions(signatureName);
          if (!isInvisibleSignature(pos))
          {
            return signatureName;
          }
        }
        return null;
      }

      private boolean isInvisibleSignature(float[] pos)
      {
        float lowerLeftX = pos[POSITION_LOWER_LEFT_X];
        float lowerLeftY = pos[POSITION_LOWER_LEFT_Y];
        float upperRightX = pos[POSITION_UPPER_RIGHT_X];
        float upperRightY = pos[POSITION_UPPER_RIGHT_Y];
        return lowerLeftX == ZERO_FLOAT && lowerLeftY == ZERO_FLOAT
            && upperRightX == ZERO_FLOAT && upperRightY == ZERO_FLOAT;
      }
    });
  }

  private boolean isAlignYAxisEnabledAndDoesItApply()
  {
    return isAlignSecond;
  }
}
