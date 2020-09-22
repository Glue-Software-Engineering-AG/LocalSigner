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

import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.config.ConfigurationGUI;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This listener is called when the user clicks the configuration menu.
 * @author Rafael Wampfler
 * @author $Author: wampfler $
 * @version $Revision: 885 $
 */
public class ConfigurationListener implements Listener {

  private final MainGUI maingui;

  /**
   * Constructor
   * @param maingui
   *          Parent shell
   */
  public ConfigurationListener(final MainGUI maingui) {
    this.maingui = maingui;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    new ConfigurationGUI(maingui);
  }

}
