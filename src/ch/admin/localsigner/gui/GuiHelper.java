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
package ch.admin.localsigner.gui;

import ch.admin.localsigner.config.resources.ImageResources;
import ch.admin.suis.client.core.service.to.ValidStatus;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class provides some simple GUI utilities used throughout the project.
 * 
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public final class GuiHelper
{
  private static final Logger LOGGER = Logger.getLogger(GuiHelper.class);

  /**
   * This class cannot be instantiated, the utility methods are provided as
   * static methods.
   */
  private GuiHelper()
  {
    // not meant to be instantiated
  }

  /**
   * Creates dummy labels as placeholders.
   * 
   * @param composite
   *          the Composite
   * @param numberOfDummies
   *          Number of dummy elements to be created
   */
  public static void makeDummy(final Composite composite,
          final int numberOfDummies)
  {
    for (int i = 0; i < numberOfDummies; i++)
    {
      new Label(composite, SWT.NONE); // dummy
    }
  }

  /**
   * Creates a SWT text label.
   * 
   * @param parent
   *          Parent object
   * @param style
   *          Label style
   * @param text
   *          Label text
   * @param font
   *          Label font
   * @return text label
   */
  public static Label label(final Composite parent, final int style,
          final String text, final Font font)
  {
    final Label label = new Label(parent, style);
    label.setFont(font);
    label.setText(text);
    return label;
  }

  /**
   * Creates a SWT text.
   * 
   * @param parent
   *          Parent object
   * @param style
   *          Text style
   * @param font
   *          Text font
   * @return text
   */
  public static Text text(final Composite parent, final int style,
          final Font font)
  {
    final Text text = new Text(parent, style);
    text.setFont(font);
    return text;
  }

  public static TreeItem treeItem(final Widget parent, final String text, final Font font)
  {
    TreeItem itm = null;
    if (parent instanceof Tree)
    {
      itm = new TreeItem((Tree) parent, 0);
    }
    if (parent instanceof TreeItem)
    {
      itm = new TreeItem((TreeItem) parent, 0);
    }

    if (itm == null)
    {
      return null;
    }

    itm.setText(text);
    itm.setFont(font);    
    return itm;
  }

  /**
   * Read the current application version from the manifest file.
   * 
   * @return Application version
   */
  public static String getVersion()
  {
    String version = "n/a";
    String date = "n/a";
    // read manifest from all 3 jars
    try
    {
      final Enumeration<URL> files = GuiHelper.class.getClassLoader().
              getResources(
              "META-INF/MANIFEST.MF");
      while (files.hasMoreElements())
      {
        final URL u = files.nextElement();
        final Properties props = new Properties();
        props.load(u.openStream());
        String builtVersion = props.getProperty("Built-Version");
        if (builtVersion != null)
        {
          version = builtVersion;
        }
        String builtDate = props.getProperty("Built-Date");
        if (builtDate != null)
        {
          date = builtDate;
        }
      }
    } catch (Exception e)
    {
      LOGGER.debug("cannot read manifest", e);
    }
    LOGGER.info("Version: " + version + " from " + date);
    return version;
  }

  /**
   * Get centered screen position of a window.
   * 
   * @param shell
   *          Window to center on screen
   * @param width
   *          Preferred width of window
   * @param height
   *          Preferred height of window
   * @return new position of window
   */
  public static Rectangle getScreenPosition(final Shell shell, final int width,
          final int height)
  {
    // place shell in the middle of the screen
    Rectangle bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
    Rectangle rect = new Rectangle((bounds.width - width) / 2,
            (bounds.height - height) / 2, width, height);
    return rect;
  }

  /**
   * This does center the dialog based on current position and size of the
   * MainGUI.
   * 
   * @param mainGui
   *          the MainGUI (parent comp)
   * @param dialog
   *          the new dialog to display
   */
  public static void centerDialogBasedOnMainGUI(final MainGUI mainGui, final Shell dialog)
  {
    final Rectangle parentsize = mainGui.getMainshell().getBounds();
    // place in the middle
    final Point calculatedSize = dialog.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    dialog.setBounds(parentsize.x + parentsize.width / 2 - calculatedSize.x / 2,
        parentsize.y + parentsize.height / 2 - calculatedSize.y / 2, calculatedSize.x,
        calculatedSize.y);
  }

  /**
   * Check if windows needs full screen.
   * @param shell
   * @return true if full screen
   */
  public static boolean needsMaximize(final Shell shell)
  {
    Rectangle bounds = shell.getDisplay().getBounds();
    if (bounds.height < shell.getBounds().height)
    {
      // fullscreen window
      LOGGER.debug("fullscreen window");
      return true;
    }
    if (bounds.width < shell.getBounds().width)
    {
      // fullscreen window
      LOGGER.debug("fullscreen window");
      return true;
    }
    return false;
  }

  /**
   * Get small validator images (green tick, red cross)
   * @param status Validator status
   * @return image
   */
  public static Image validatorImage(final ValidStatus status)
  {
    String name;

    switch (status)
    {
    case VALID:
      name = ImageResources.IMG_ACCEPT;
      break;
    case UNSURE:
      name = ImageResources.IMG_UNSURE;
      break;
    default:
      name = ImageResources.IMG_ERROR;
      break;
    }

    return loadImage(name);
  }
  
  public static Image loadImage(final String name) {
    return new Image(Display.getCurrent(), GuiHelper.class.getResourceAsStream(name));
  }

  /**
   * Enable all subelements of the composite recursively.
   */
  public static void enableAll(Composite parent)
  {
    setEnabledRecursive(parent, true, true);
  }

  /**
   * Disable all sublemements of the composite recusively.
   */
  public static void disableAll(Composite parent)
  {
    setEnabledRecursive(parent, false, true);
  }

  /**
   * Enable all direct children of the given composite.
   */
  public static void enableDirectChildren(Composite parent)
  {
    setEnabledRecursive(parent, true, false);
  }

  /**
   * Disable all direct children of the given composite.
   */
  public static void disableDirectChildren(Composite parent)
  {
    setEnabledRecursive(parent, false, false);
  }

  private static void setEnabledRecursive(final Composite composite,
      final boolean enabled, final boolean recursive)
  {
    Validate.notNull(composite, "composite");

    Control[] children = composite.getChildren();

    for (Control aChildren : children) {
      if (aChildren instanceof Composite) {
        if (recursive) {
          setEnabledRecursive((Composite) aChildren, enabled, recursive);
        }
      } else {
        aChildren.setEnabled(enabled);
      }
    }

    composite.setEnabled(enabled);
  }

  /**
   * This method loads the LocalSigner icon
   *
   * @see ImageResources#IMG_APP
   *
   * @param display to create the image
   * @return LocalSigner icon
   */
  public static Image loadAppIcon(final Display display)
  {
    final ImageData imgData = new ImageData(GuiHelper.class.getResourceAsStream(ImageResources.IMG_APP));
    final Image image = new Image(display, imgData);
    return image;
  }

}
