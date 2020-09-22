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

import ch.admin.localsigner.gui.PdfAValidationDialog;
import ch.admin.localsigner.main.LocalSigner;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This class is the listener which is called when the user chooses "PDF/A Prüfung" from
 * the extras menu.
 *
 * @author greiler
 * @author keller
 * @author $Author$
 * @version $Revision$
 */
public class PdfAValidationListener implements Listener
{

  /**
   * Handle listener event.
   *
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {
    PdfAValidationDialog vd = new PdfAValidationDialog(LocalSigner.mainGui);
  }
}