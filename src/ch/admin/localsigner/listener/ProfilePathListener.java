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
package ch.admin.localsigner.listener;

import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class opens the profile path dialog.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class ProfilePathListener implements SelectionListener
{
  private final Shell shell;

  private final Text profilePathText;

  public ProfilePathListener(Shell shell, Text profilePathText)
  {
    this.shell = shell;
    this.profilePathText = profilePathText;
  }

  @Override
  public void widgetDefaultSelected(final SelectionEvent e)
  {
    widgetSelected(e);
  }

  @Override
  public void widgetSelected(final SelectionEvent e)
  {
    DirectoryDialog dlg = new DirectoryDialog(shell);

    // Change the title bar text
    dlg.setText(LocalSigner.i18n("profilepathtitle"));

    // Customizable message displayed in the dialog
    dlg.setMessage(LocalSigner.i18n("profilepathtext"));

    // Calling open() will open and run the dialog.
    // It will return the selected directory, or
    // null if user cancels
    String dir = dlg.open();
    if (dir != null)
    {
      // Set the text box to the new selection
      profilePathText.setText(dir);
    }
  }

}