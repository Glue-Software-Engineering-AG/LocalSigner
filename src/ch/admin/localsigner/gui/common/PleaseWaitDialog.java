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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.gui.MainGUI;

/**
 * This class shows a simple please wait dialog.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class PleaseWaitDialog
{

  private static final int ADD_Y_DEFAULT_PX = 50;

  private final Shell box;

  private Label label;

  public PleaseWaitDialog(final MainGUI maingui, final String title, final String message)
  {
    this(maingui, title, message, ADD_Y_DEFAULT_PX, SWT.CENTER);
  }

  public PleaseWaitDialog(final MainGUI maingui, final String title,
      final String message, int additionalHeight, int verticalAlignment)
  {
    // get the display
    final Display display = maingui.getMainshell().getDisplay();

    // create new shell
    box = new Shell(display, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    box.setLayout(new GridLayout());
    box.setText(title);
    box.setImage(GuiHelper.loadAppIcon(display));

    // message
    label = new Label(box, SWT.NONE);
    label.setText(message);
    label.setLayoutData(new GridData(SWT.CENTER, verticalAlignment, true, true, 1, 1));
    label.setFont(maingui.getFont());

    // pack
    box.pack();
    // size
    final Rectangle parentsize = maingui.getMainshell().getBounds();
    // place in the middle
    final Point calculatedSize = box.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    box.setBounds(parentsize.x + parentsize.width / 2 - calculatedSize.x / 2,
        parentsize.y + parentsize.height / 2 - calculatedSize.y / 2, calculatedSize.x,
        calculatedSize.y + additionalHeight);
    box.open();
  }

  /**
   * Method to close this dialog. It just forwards the close to the internal
   * shell.
   */
  public void close()
  {
    if (!box.isDisposed())
    {
      box.close();
    }
  }

  public void updateLabel(final String text)
  {
    box.getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        String newlabel = label.getText() + "\n" + text;
        label.setText(newlabel);
        label.pack();
      }
    });

  }

  public void replaceLabel(final String text)
  {
    box.getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        label.setText(text);
        label.pack();
        box.forceFocus();
      }
    });

  }
}
