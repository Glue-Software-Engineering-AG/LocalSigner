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

import java.io.IOException;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Run the external PAT tool for PIN changes.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class LaunchListener implements Listener
{

  private static final Logger LOGGER = Logger.getLogger(LaunchListener.class);

  private String command;

  public LaunchListener(String cmd)
  {
    this.command = cmd;
  }

  /**
   * Handle listener event.
   * 
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {

    LOGGER.info("running command: " + command);
    try
    {
      Runtime.getRuntime().exec(command);
    } catch (IOException e)
    {
      LOGGER.error("Cannot run command", e);
    }
  }

}
