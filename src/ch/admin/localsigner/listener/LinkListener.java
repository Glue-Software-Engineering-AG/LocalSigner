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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.Method;

/**
 * This class is the listener which is called when the user chooses a link from
 * the help menu.
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class LinkListener implements Listener
{
  private final Shell shell;

  private final String url;

  /**
   * Constructor
   * @param shell
   *          Parent shell
   * @param link
   *          URL of link
   */
  public LinkListener(final Shell shell, final String link)
  {
    this.shell = shell;
    this.url = link;
  }

  /**
   * Handle listener event.
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {
    this.openURL();
  }

  // FIXME ersetzen und dann TESTEN mit Desktop.browse
  private void openURL()
  {
    final String osName = System.getProperty("os.name");
    try
    {
      if (osName.startsWith("Mac OS"))
      {
        final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
        final Method openURL = fileMgr.getDeclaredMethod("openURL",
            String.class);
        openURL.invoke(null, url);
      }
      else
        if (osName.startsWith("Windows"))
        {
          Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        }
        else
        { // assume Unix or Linux
          final String[] browsers =
          {
            "firefox", "opera", "konqueror",
            "epiphany", "mozilla", "netscape"
          };
          String browser = null;
          for (int count = 0; count < browsers.length && browser == null; count++)
          {
            if (Runtime.getRuntime().exec(
                    new String[]
                    {
                      "which", browsers[count]
                    }).waitFor() == 0)
            {
              browser = browsers[count];
            }
          }
          if (browser == null)
          {
            throw new UnsupportedOperationException("Could not find web browser");
          }
          Runtime.getRuntime().exec(new String[]
                  {
                    browser, url
                  });
        }
    } catch (Exception e)
    {
      final MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
      mb.setMessage("Error attempting to launch web browser. Visit " + url);
      mb.open();
    }
  }

}
