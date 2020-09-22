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

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.profile.Profile;
import ch.admin.localsigner.gui.profile.ProfileLoader;
import ch.admin.localsigner.main.LocalSigner;

/**
 * This class loads the signature profiles.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class LoadProfileListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(LoadProfileListener.class);

  private final MainGUI maingui;

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI
   */
  public LoadProfileListener(final MainGUI maingui)
  {
    this.maingui = maingui;
  }

  /**
   * Handle listener event.
   *
   * @param event
   */
  @Override
  public void handleEvent(final Event event)
  {
    final Profile profile = maingui.getSelectedProfile();
    if (profile == null)
    {
      return;
    }

    ProfileLoader.loadProfile(maingui, profile);

    // update last used profile as the currently loaded profile
    storeLastUsedProfile(profile.getName());

    // update GUI view
    maingui.reloadInputFile(false);
  }

  /**
   * Stores the last used profile in the internal config property file
   *
   * @param profileName
   *          Name of profile
   */
  private void storeLastUsedProfile(final String profileName)
  {
    final Properties properties = new Properties();
    // Write properties file.
    try
    {
      properties.setProperty(Config.LAST_USED_PROFILE, profileName);
      properties.store(new FileOutputStream(LocalSigner.appConfig.getUserInternalConfig()), null);
      LOGGER.debug("updated lastUsedProfile to: " + profileName);
    } catch (IOException e)
    {
      LOGGER.warn("could not store last used profile - simply procceed", e);
    }
  }
}