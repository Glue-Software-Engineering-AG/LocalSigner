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
package ch.admin.localsigner.main;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.config.resources.ImageResources;
import ch.admin.localsigner.gui.GuiHelper;

/**
 * Splash screen using SWT.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$v
 */
class SWTSplash
{
  private static final Logger LOGGER = Logger.getLogger(SWTSplash.class);

  private boolean skip;

  private Shell shell;

  private ProgressBar bar;

  private final Display display;

  /**
   * Constructor
   * 
   * @param display
   *          SWT display variable
   */
  protected SWTSplash(final Display display)
  {
    this.display = display;
  }

  /**
   * Check if the user skipped the splash screen
   * @return true if skip
   */
  public boolean skipSplash()
  {
    return skip;
  }

  /**
   * Set the current progress (0-100)
   * @param value
   *          current progress from 0 to 100
   */
  public void setProgress(final int value)
  {
    if (shell != null && !shell.isDisposed() && !shell.getDisplay().isDisposed())
    {
      shell.getDisplay().asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          bar.setSelection(value);
        }

      });
    }
  }

  /**
   * Close the splash screen
   */
  public void close()
  {
    LOGGER.debug("close splash");

    if (!display.isDisposed())
    {
      display.asyncExec(new Runnable()
      {
        @Override
        public void run()
        {
          if (!shell.isDisposed())
          {
            shell.close();
          }
        }

      });
    }
  }

  /**
   * Open the splash screen
   */
  public void open()
  {
    final Image image = new Image(display,
        SWTSplash.class.getResourceAsStream(ImageResources.IMG_SPLASH));
    shell = new Shell(display, SWT.ON_TOP);
    shell.setLayout(new FormLayout());
    shell.setSize(image.getBounds().width + 2, image.getBounds().height + 2);
    shell.setBackgroundImage(image);

    bar = new ProgressBar(shell, SWT.SMOOTH);
    bar.setMaximum(100);
    final FormData progressData = new FormData();
    progressData.left = new FormAttachment(20, 0);
    progressData.right = new FormAttachment(60, 0);
    progressData.bottom = new FormAttachment(100, -15);
    bar.setLayoutData(progressData);

    Button button = new Button(shell, SWT.PUSH);
    button.setText("Skip check...");
    button.setSize(120, 30);
    final FormData buttonData = new FormData();
    buttonData.left = new FormAttachment(60, 10);
    buttonData.bottom = new FormAttachment(100, -11);
    button.setLayoutData(buttonData);
    button.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        LOGGER.debug("skip clicked");
        skip = true;
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e)
      {
        this.widgetSelected(e);
      }

    });

    button.setFocus();
    shell.setDefaultButton(button);

    this.addLabels();

    shell.setBounds(GuiHelper.getScreenPosition(shell, shell.getBounds().width,
            shell.getBounds().height));
    shell.open();

    shell.forceActive();
    shell.forceFocus();

    // endless loop
    while (!shell.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
        if (skipSplash())
        {
          this.close();
        }
      }
    }
  }

  private void addLabels()
  {
    final Color white = new Color(display, 255, 255, 255);
    Label versionLabel = new Label(shell, SWT.NONE);
    versionLabel.setText("Version " + GuiHelper.getVersion());
    versionLabel.setBackground(white);
    final FormData versionData = new FormData();
    versionData.left = new FormAttachment(80, 0);
    versionData.bottom = new FormAttachment(18, 0);
    versionLabel.setLayoutData(versionData);

    Label javaLabel = new Label(shell, SWT.NONE);
    javaLabel.setText("Java " + System.getProperty("java.version"));
    javaLabel.setBackground(white);
    final FormData javaData = new FormData();
    javaData.left = new FormAttachment(80, 0);
    javaData.bottom = new FormAttachment(18, 25);
    javaLabel.setLayoutData(javaData);
  }
}
