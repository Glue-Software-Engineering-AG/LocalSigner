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
package ch.admin.localsigner.gui.profile;

/**
 * This enum hold the types of signature profiles.
 * <ul>
 * <li>
 * SYSTEM_PROFILE = shared, global profiles (normally read-only)</li>
 * <li>
 * DEFAULT_PROFILE = default.properties, normally from users home</li>
 * <li>
 * USER_PROFILE = users profiles, additional profiles, normally from users home</li>
 * <li>
 * CUSTOM_PROFILE = only used for profile specified in CLI mode</li>
 * </ul>
 * 
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
enum ProfileType
{
  DEFAULT_PROFILE, USER_PROFILE, SYSTEM_PROFILE, CUSTOM_PROFILE
}
