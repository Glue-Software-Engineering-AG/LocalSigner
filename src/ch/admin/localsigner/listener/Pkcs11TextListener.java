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

import java.util.Map;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * This class handles text changes of the PKCS11 description label.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class Pkcs11TextListener implements SelectionListener
{
  private final Combo pkcs11LibText;

  private final Map<String, String> pkcs11Libs;

  private final Label pkcs11LibDescription;

  private final Group certAccessConfig;

  public Pkcs11TextListener(Combo pkcs11LibText,
          Label pkcs11LibDescription,
          Map<String, String> pkcs11Libs, Group certAccessConfig)
  {
    this.pkcs11LibText = pkcs11LibText;
    this.pkcs11LibDescription = pkcs11LibDescription;
    this.pkcs11Libs = pkcs11Libs;
    this.certAccessConfig = certAccessConfig;
  }

  @Override
  public void widgetSelected(SelectionEvent e)
  {
    widgetDefaultSelected(e);
  }

  @Override
  public void widgetDefaultSelected(SelectionEvent e)
  {
    String descr = pkcs11Libs.get(pkcs11LibText.getText());
    if (descr == null)
    {
      descr = "";
    }
    pkcs11LibDescription.setText(descr);
    certAccessConfig.layout();
  }

}
