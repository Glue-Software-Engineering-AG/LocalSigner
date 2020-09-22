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
package ch.admin.localsigner.gui.viewer;

import ch.admin.localsigner.gui.MainGUI;

public class ViewerPanelFactory
{

    public static ViewerPanel createViewerPanel(final MainGUI swtMainGui) {
        Thread.currentThread().setName("run TheCreator");
        ViewerPanel panel = new ViewerPanel(swtMainGui);
        return panel;

    }

}
