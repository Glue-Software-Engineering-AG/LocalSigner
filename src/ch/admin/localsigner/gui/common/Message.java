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

import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * This class simply displays a message box with a given title and a given
 * message. The user has the only possibility to click "ok".
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class Message
{
  public Message(final Shell shell, final int style, final String title,
          final String message, final Listener okListener)
  {
    if (shell.isDisposed())
    {
      // shell already closed
      return;
    }

    shell.getDisplay().asyncExec(new Runnable()
    {
      @Override
      public void run()
      {
        final MessageBox mb = new MessageBox(shell, style);
        mb.setText(title);
        mb.setMessage(message);
        int button = mb.open();
        if (okListener != null && button == SWT.OK)
        {
          okListener.handleEvent(null);
        }

        LocalSigner.mainGui.refreshViewer();
      }

    });
  }

  public static void warning(final Shell shell, final String message)
  {
    new Message(shell, SWT.ICON_INFORMATION | SWT.ON_TOP, LocalSigner.i18n(
            "warning"), message, null);
  }

  public static void warning(final Shell shell, final String title,
          final String message)
  {
    new Message(shell, SWT.ICON_INFORMATION | SWT.ON_TOP, title, message, null);
  }

  public static void error(final Shell shell, final String message)
  {
    new Message(shell, SWT.ICON_ERROR | SWT.ON_TOP, LocalSigner.i18n(
            "error"), message, null);
  }

}
