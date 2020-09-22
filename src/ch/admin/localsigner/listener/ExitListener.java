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

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * This listener is called when the user clicks exit in the main dialog.
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ExitListener implements Listener
{

  private static final Logger LOGGER = Logger.getLogger(ExitListener.class);


  private final Shell shell;
  /**
   * Constructor
   * @param shell
   *          Parent shell
   */
  public ExitListener(final Shell shell)
  {
    this.shell = shell;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event) {
    this.shell.dispose();
    // ugly, but recommended by the SWT people to terminate gracefully on Mac
    System.exit(0);
  }
}