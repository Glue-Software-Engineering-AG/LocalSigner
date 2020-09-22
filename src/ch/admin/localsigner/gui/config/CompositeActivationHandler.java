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
package ch.admin.localsigner.gui.config;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CompositeActivationHandler
{

  private final List<Composite> composites = new ArrayList<>();

  public void addComposite(Composite composite){
    composites.add(composite);
  }

  public void deactivateAllComposites(){
    composites.forEach((composite) -> {activationHandling(composite,false);});
  }

  public void activateSelectedComposite(Composite compositeToActivate){
    deactivateAllComposites();
    activationHandling(compositeToActivate, true);
  }


  public void activationHandling(Composite composite, boolean activate){
    for(Control con : composite.getChildren()){
      con.setEnabled(activate);
      if(con instanceof Composite){
        Composite com = (Composite) con;
        activationHandling(com,activate);
      }
    }
  }





}
