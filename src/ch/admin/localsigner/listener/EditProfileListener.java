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

import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * This class opens the edit profile window.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class EditProfileListener implements SelectionListener
{
  private final PropertiesGUI propertiesGui;

  private final Shell mainshell;

  public EditProfileListener(PropertiesGUI propertiesGui, Shell mainshell)
  {
    this.propertiesGui = propertiesGui;
    this.mainshell = mainshell;
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    widgetSelected(e);
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    // place it correctly
    Rectangle propertybounds = propertiesGui.getShell().getBounds();
    propertybounds.x = (mainshell.getBounds().width - propertybounds.width)
            / 2
            + mainshell.getBounds().x;
    propertybounds.y = mainshell.getBounds().height - propertybounds.height
            + mainshell.getBounds().y - 10;
    propertiesGui.getShell().setBounds(propertybounds);

    // and set the profile name into the title
    propertiesGui.getShell().setText(LocalSigner.i18n("changeProfile"));

    // finally open the property shell
    propertiesGui.getShell().open();
  }

}
