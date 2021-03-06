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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Listener for the checkbox signature type: Certification.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class SigTypeCertificationListener implements Listener {

  private final PropertiesGUI propertiesGui;

  /**
   * Constructor
   * @param propertiesGui
   *          The properties GUI
   */
  public SigTypeCertificationListener(final PropertiesGUI propertiesGui) {
    this.propertiesGui = propertiesGui;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    propertiesGui.setCertificationType(true); // true means signature
  }
}
