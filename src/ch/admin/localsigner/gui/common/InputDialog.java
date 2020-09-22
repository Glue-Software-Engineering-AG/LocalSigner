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

import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * This class simply displays an info message box with a given title and a given
 * message. The user has the only possibility to click "ok".
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class InputDialog
{
  private final Shell box;

  private String input;

  public InputDialog(final MainGUI maingui, final String title,
          final String message, final boolean password)
  {
    // get the display
    final Display display = maingui.getMainshell().getDisplay();

    // create new shell
    box = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE
            | SWT.APPLICATION_MODAL);
    box.setLayout(new GridLayout());
    box.setText(title);
    box.setImage(GuiHelper.loadAppIcon(display));

    final Font font = maingui.getFont();

    // message
    final Label label = new Label(box, SWT.NONE);
    label.setText(message);
    label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
    label.setFont(font);

    // input field
    final Text text;
    if (password)
    {
      text = new Text(box, SWT.BORDER | SWT.PASSWORD);
    }
    else
    {
      text = new Text(box, SWT.BORDER);
    }

    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    text.setFont(font);

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

    box.setBounds(GuiHelper.getScreenPosition(box, box.getBounds().width, box.
            getBounds().height));

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

}
