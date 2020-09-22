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
import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.List;

/**
 * This class simply displays an info message box with given buttons. The user
 * can click on each button.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class ButtonsDialog
{
  private int selectedButton = -1;

  public ButtonsDialog(final Shell shell, final String title,
          final List<String> buttonTexts, final String message)
  {
    final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM
            | SWT.APPLICATION_MODAL);
    dialog.setText(title);
    final GridLayout layout = new GridLayout(1, false);
    dialog.setLayout(layout);

    final Label label = new Label(dialog, SWT.NONE);
    label.setText(message);

    // click listener for all buttons
    final Listener listener = new Listener()
    {
      @Override
      public void handleEvent(final Event event)
      {
        Integer button = (Integer) event.widget.getData();
        selectedButton = button;
        dialog.close();
      }

    };

    // spacing label
    new Label(dialog, SWT.NONE);

    // add buttons (value 0 to n)
    for (int i = 0; i < buttonTexts.size(); i++)
    {
      final Button button = new Button(dialog, SWT.PUSH);
      button.setText(buttonTexts.get(i));
      button.addListener(SWT.Selection, listener);
      button.setData(i);
      button.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
              false, 1, 1));
    }

    // spacing label
    new Label(dialog, SWT.NONE);

    // add cancel button (value -1)
    final Button cancel = new Button(dialog, SWT.PUSH);
    cancel.setText(LocalSigner.i18n("cancel"));
    cancel.addListener(SWT.Selection, listener);
    cancel.setData(-1);
    cancel.setLayoutData(new GridData(GridData.END, GridData.FILL, false, false,
            1, 1));
    cancel.setFocus();
    dialog.setDefaultButton(cancel);

    dialog.pack();
    dialog.setBounds(GuiHelper.getScreenPosition(dialog,
            dialog.getBounds().width,
            dialog.getBounds().height));
    dialog.open();

    final Display display = shell.getDisplay();
    while (!dialog.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  public int getUserDecision()
  {
    return selectedButton;
  }

}
