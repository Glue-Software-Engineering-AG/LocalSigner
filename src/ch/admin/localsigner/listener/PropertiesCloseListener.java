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

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;

/**
 * This listener is called when the user closes the properties GUI.
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class PropertiesCloseListener implements ShellListener
{
  private final Shell shell;

  /**
   * Constructor
   * @param shell
   *          Parent shell
   */
  public PropertiesCloseListener(final Shell shell)
  {
    this.shell = shell;
  }

  @Override
  public void shellClosed(final ShellEvent e)
  {
    e.doit = false;
    shell.setVisible(false);
  }

  @Override
  public void shellActivated(final ShellEvent e)
  {
    // not used
  }

  @Override
  public void shellDeactivated(final ShellEvent e)
  {
    // not used
  }

  @Override
  public void shellDeiconified(final ShellEvent e)
  {
    // not used
  }

  @Override
  public void shellIconified(final ShellEvent e)
  {
    // not used
  }

}
