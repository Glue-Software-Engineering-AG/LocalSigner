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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * This class shows a yes/cancel message box with a warning icon by
 * encapsulating the SWT MessageBox. The chosen value is returned with a
 * separate function.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class YesNoDialog {

  private final int result;

  /**
   * Constructor
   * @param shell
   *          Parent shell
   * @param title
   *          Title text
   * @param message
   *          Message text
   */
  public YesNoDialog(final Shell shell, final String title, final String message) {
    final MessageBox mb = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK
        | SWT.CANCEL);
    mb.setText(title);
    mb.setMessage(message);
    result = mb.open();
  }

  /**
   * Get the user decision of the dialog.
   * @return true if the user clicked 'ok' in the dialog, false otherwise.
   */
  public boolean isUserDecision() {
    return result == SWT.OK;
  }
}
