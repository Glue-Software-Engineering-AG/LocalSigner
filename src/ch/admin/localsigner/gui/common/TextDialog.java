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
package ch.admin.localsigner.gui.common;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.main.LocalSigner;

/**
 * This class displays a message dialog with a text field and a button "ok".
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class TextDialog
{
  private static final Logger LOGGER = Logger.getLogger(TextDialog.class);

  private String input;

  private int columns;

  private int rows;

  public TextDialog(final MainGUI maingui, final String title,
          final String message, final int width, final int height)
  {
    // get the display
    final Display display = maingui.getMainshell().getDisplay();

    // create new shell
    final Shell box = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    box.setLayout(new GridLayout(1, false));
    box.setText(title);

    final Font font = maingui.getFont();
    final Font fixFont = new Font(maingui.getMainshell().getDisplay(),
            new FontData[]
            {
              new FontData("Courier", 10, SWT.NORMAL)
            });

    // message
    final Label label = new Label(box, SWT.NONE);
    label.setText(message);
    label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
    label.setFont(font);

    // input field
    final Text text = new Text(box, SWT.BORDER | SWT.MULTI | SWT.WRAP);
    GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
    GC gc = new GC(text);
    FontMetrics fm = gc.getFontMetrics();

    LOGGER.debug("Mouse box: " + width + "x" + height);

    // calculate max columns and rows
    columns = (int) (width / 2.6);
    rows = (int) (height / 4.6);
    LOGGER.debug("Text box: " + columns + " columns, " + rows + " rows");

    // calculater size for editor
    int guiWidth = (int) (columns * fm.getAverageCharWidth() * 1.15);
    int guiHeight = (int) (rows * fm.getHeight() * 0.85);
    LOGGER.debug("GUI box: " + guiWidth + "x" + guiHeight);

    gd.heightHint = guiHeight;
    gd.minimumHeight = guiHeight;
    gd.widthHint = guiWidth;
    gd.minimumWidth = guiWidth;
    text.setLayoutData(gd);
    text.setFont(fixFont);

    // verify content
    text.addVerifyListener(new VerifyListener()
    {
      @Override
      public void verifyText(VerifyEvent e)
      {
        if (e.character == '\b')
        {
          // always allow delete (back space)
          e.doit = true;
          return;
        }

        if (e.character == '\n' || e.character == '\r' || e.character == '\f')
        {
          if (countLines(text.getText()) >= rows)
          {
            // too many rows
            e.doit = false;
            return;
          }
          e.doit = true;
          return;
        }

        String newText = text.getText() + e.character;
        for (String line : newText.split("\n"))
        {
          if (line.length() > columns)
          {
            char lastChar = line.charAt(line.length() - 1);
            if (line.length() == columns + 1
                    && (lastChar == '\n' || lastChar == '\r' || lastChar == '\f'))
            {
              // ok, new line is allowed
              e.doit = true;
            }
            else
            {
              // line too long
              e.doit = false;
              return;
            }
          }
        }
      }

    });

    // ok button
    final Button ok = new Button(box, SWT.PUSH);
    ok.setFont(font);
    ok.setText(LocalSigner.i18n("ok"));
    ok.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    ok.addListener(SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent(final Event arg0)
      {
        input = text.getText();
        box.close();
      }

    });
    box.setDefaultButton(ok);

    // pack
    box.pack();

    // size
    final Rectangle parentsize = maingui.getMainshell().getBounds();
    // place in the middle
    final Point boxSize = box.getSize();
    box.setBounds(parentsize.x + parentsize.width / 2 - boxSize.x / 2,
            parentsize.y
            + parentsize.height / 2 - boxSize.y / 2, boxSize.x, boxSize.y);

    box.open();

    while (!box.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  public String getInput()
  {
    return input;
  }


  private int countLines(final String text)
  {
    return StringUtils.countMatches(text, "\n") + 1;
  }

}
