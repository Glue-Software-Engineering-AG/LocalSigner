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
package ch.admin.localsigner.notary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.ZulabConfiguration;
import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.main.LocalSigner;

/**
 * Dialog to select canton and domain for Zulassungsbestätigung. This dialog only appears if the checkbox in the main
 * configuration dialog is set to "always ask".
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
class CantonAndDomainDialog
{

  private static final Logger LOGGER = Logger.getLogger(CantonAndDomainDialog.class);

  private final static int OK = 1;

  private final static int CANCEL = 2;

  private int selectedButton = CANCEL;

  private Combo domainCombo;

  private Combo cantonCombo;

  private Button zulabAskEverytimeCheckbox;

  private Button saveBtn;

  private Button continueBtn;

  private String selectedDomain = null;

  private String selectedCanton = null;

  public CantonAndDomainDialog()
  {
    final Shell dialog = new Shell(LocalSigner.mainGui.getMainshell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    dialog.setText(LocalSigner.i18n("zulab.dialog.title"));
    final GridLayout layout = new GridLayout(3, false);
    dialog.setLayout(layout);

    createAndAddText(dialog);

    Label spacer1 = new Label(dialog, SWT.NONE); // spacer
    spacer1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

    createAndDomain(dialog);

    createAndCanton(dialog);

    Label spacer2 = new Label(dialog, SWT.NONE); // spacer
    spacer2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));

    createAndCheckbox(dialog);

    createAndAddCancel(dialog);

    createAndAddSave(dialog);

    createAndAddContinue(dialog);

    // initially enable/disable
    enableOrDisableSaveAndContinueButton();
    enableOrDisableCheckbox();

    dialog.pack();
    // size
    GuiHelper.centerDialogBasedOnMainGUI(LocalSigner.mainGui, dialog);

    dialog.open();

    final Display display = LocalSigner.mainGui.getMainshell().getDisplay();
    while (!dialog.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  private void createAndAddText(final Shell dialog)
  {
    final Label label = new Label(dialog, SWT.WRAP);
    label.setText(LocalSigner.i18n("zulab.dialog.text"));
    label.setFont(LocalSigner.mainGui.getFont());

    GridData gridDataLbl = new GridData();
    gridDataLbl.horizontalAlignment = GridData.FILL;
    gridDataLbl.verticalAlignment = GridData.FILL;
    gridDataLbl.grabExcessHorizontalSpace = false;
    gridDataLbl.grabExcessVerticalSpace = false;
    gridDataLbl.horizontalSpan = 3;

    label.setLayoutData(gridDataLbl);
  }

  private void createAndDomain(final Shell dialog)
  {
    // domain label
    GuiHelper.label(dialog, SWT.NONE, LocalSigner.i18n("zulab.dialog.domain.label") + ":",
        LocalSigner.mainGui.getFont());

    // domain combo
    domainCombo = new Combo(dialog, SWT.READ_ONLY);
    domainCombo.setFont(LocalSigner.mainGui.getFont());
    domainCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
    domainCombo.setToolTipText(LocalSigner.i18n("zulab.dialog.domain.tooltip"));
    domainCombo.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        enableOrDisableSaveAndContinueButton();
        enableOrDisableCheckbox();
      }
    });
    fillDomainCombo(domainCombo);
  }

  private void createAndCanton(final Shell dialog)
  {
    // canton label
    GuiHelper.label(dialog, SWT.NONE, LocalSigner.i18n("zulab.dialog.canton.label") + ":",
        LocalSigner.mainGui.getFont());

    // canton combo
    cantonCombo = new Combo(dialog, SWT.READ_ONLY);
    cantonCombo.setFont(LocalSigner.mainGui.getFont());
    cantonCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
    cantonCombo.setToolTipText(LocalSigner.i18n("zulab.dialog.canton.tooltip"));
    fillCantonCombo(cantonCombo);

    cantonCombo.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        enableOrDisableSaveAndContinueButton();
        enableOrDisableCheckbox();
      }
    });
  }

  private void enableOrDisableSaveAndContinueButton()
  {
    saveBtn.setEnabled(getSelectedCanton() != null && getSelectedDomain() != null);
    continueBtn.setEnabled(getSelectedCanton() != null && getSelectedDomain() != null);
  }

  private void createAndCheckbox(final Shell dialog)
  {
    // ask everytime checkbox
    zulabAskEverytimeCheckbox = new Button(dialog, SWT.CHECK);
    zulabAskEverytimeCheckbox.setText(LocalSigner.i18n("zulab.dialog.notAskAgain.label"));
    zulabAskEverytimeCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 3, 1));
    zulabAskEverytimeCheckbox.setSelection(!LocalSigner.appConfig.isFunktionsnachweisShowDialog());
    zulabAskEverytimeCheckbox.setFont(LocalSigner.mainGui.getFont());
  }

  private void enableOrDisableCheckbox()
  {
    if (getSelectedCanton() != null && getSelectedDomain() != null)
    {
      zulabAskEverytimeCheckbox.setEnabled(true);
      zulabAskEverytimeCheckbox.setToolTipText(LocalSigner.i18n("zulab.dialog.notAskAgain.enabled.tooltip"));
    } else
    {
      zulabAskEverytimeCheckbox.setEnabled(false);
      zulabAskEverytimeCheckbox.setToolTipText(LocalSigner.i18n("zulab.dialog.notAskAgain.disabled.tooltip"));
    }
  }

  private void createAndAddCancel(final Shell dialog)
  {
    final Button cancel = new Button(dialog, SWT.PUSH);
    cancel.setText(LocalSigner.i18n("zulab.dialog.button.cancel"));
    cancel.addListener(SWT.Selection, new Listener()
    {

      @Override
      public void handleEvent(Event arg0)
      {
        selectedButton = CANCEL;
        dialog.close();
      }
    });
    cancel.setLayoutData(new GridData(GridData.END, GridData.FILL, false, false, 1, 1));
    cancel.setFocus();
  }

  private void createAndAddContinue(final Shell dialog)
  {
    continueBtn = new Button(dialog, SWT.PUSH);
    continueBtn.setText(LocalSigner.i18n("zulab.dialog.button.continue"));
    continueBtn.addListener(SWT.Selection, new Listener()
    {

      @Override
      public void handleEvent(Event arg0)
      {
        selectedButton = OK;
        selectedDomain = getSelectedDomain();
        selectedCanton = getSelectedCanton();
        dialog.close();
      }
    });

    dialog.setDefaultButton(continueBtn);
    continueBtn.forceFocus();
    continueBtn.setLayoutData(new GridData(GridData.END, GridData.FILL, false, false, 1, 1));
  }

  private void createAndAddSave(final Shell dialog)
  {
    saveBtn = new Button(dialog, SWT.PUSH);
    saveBtn.setText(LocalSigner.i18n("zulab.dialog.button.save"));
    saveBtn.addListener(SWT.Selection, new Listener()
    {

      @Override
      public void handleEvent(Event arg0)
      {
        try
        {
          // ask-dialog
          LocalSigner.appConfig.setValue(
              Config.FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG, !zulabAskEverytimeCheckbox.getSelection());

          // domain
          String selectedDomainValue = getSelectedDomain();
          if (selectedDomainValue != null)
          {
            LocalSigner.appConfig.setValue(Config.FUNKTIONSNACHWEIS_ZULAB_DOMAIN, selectedDomainValue);
          }

          // canton
          String selectedCantonValue = getSelectedCanton();
          if (selectedCantonValue != null)
          {
            LocalSigner.appConfig.setValue(Config.FUNKTIONSNACHWEIS_ZULAB_CANTON, selectedCantonValue);
          }

          enableOrDisableCheckbox();

        } catch (ConfigurationException ce)
        {
          LOGGER.error(ce);
          // present error message to user
          Message.error(dialog, LocalSigner.i18n("errorsaveconfig"));
          return;
        }
      }
    });
    saveBtn.setLayoutData(new GridData(GridData.END, GridData.FILL, false, false, 1, 1));
  }

  private String getSelectedDomain()
  {
    if (domainCombo.getSelectionIndex() > -1 && domainCombo.getSelectionIndex() < domainCombo.getItemCount())
    {
      Map<String, ZulabConfiguration.Entry> domains = LocalSigner.appConfig.getZulabDomains();
      for (ZulabConfiguration.Entry entry : getSortedValues(domains))
      {
        if (entry.getTranslation(LocalSigner.getLocale()).equals(domainCombo.getItem(domainCombo.
            getSelectionIndex())))
        {
          return entry.getValue();
        }
      }
    }

    return null;
  }

  private String getSelectedCanton()
  {
    // canton
    if (cantonCombo.getSelectionIndex() > -1 && cantonCombo.getSelectionIndex() < cantonCombo.getItemCount())
    {
      Map<String, ZulabConfiguration.Entry> cantons = LocalSigner.appConfig.getZulabCantons();
      for (ZulabConfiguration.Entry entry : getSortedValues(cantons))
      {
        if (entry.getTranslation(LocalSigner.getLocale()).equals(cantonCombo.getItem(cantonCombo.
            getSelectionIndex())))
        {
          return entry.getValue();
        }
      }
    }

    return null;
  }

  public boolean isCancelled()
  {
    return selectedButton == CANCEL;
  }

  public String getCanton()
  {
    return selectedCanton;
  }

  public String getDomain()
  {
    return selectedDomain;
  }

  private void fillDomainCombo(Combo domainCombo)
  {
    Map<String, ZulabConfiguration.Entry> domains = LocalSigner.appConfig.getZulabDomains();

    domainCombo.removeAll();
    ArrayList<String> domainsArray = new ArrayList<String>();
    int selectedIndex = -1, i = 0;
    String configuredDomain = LocalSigner.appConfig.getFunktionsnachweisDomain();
    for (ZulabConfiguration.Entry entry : getSortedValues(domains))
    {
      domainsArray.add(entry.getTranslation(LocalSigner.getLocale()));
      if (entry.getValue().equals(configuredDomain))
      {
        selectedIndex = i;
      } else
      {
        i++;
      }
    }
    domainCombo.setItems(domainsArray.toArray(new String[0]));
    domainCombo.select(selectedIndex);
  }

  private void fillCantonCombo(Combo cantonCombo)
  {
    Map<String, ZulabConfiguration.Entry> cantons = LocalSigner.appConfig.getZulabCantons();

    cantonCombo.removeAll();
    ArrayList<String> cantonsArray = new ArrayList<String>();
    int selectedIndex = -1, i = 0;
    String configuredCanton = LocalSigner.appConfig.getFunktionsnachweisCanton();
    for (ZulabConfiguration.Entry entry : getSortedValues(cantons))
    {
      cantonsArray.add(entry.getTranslation(LocalSigner.getLocale()));
      if (entry.getValue().equals(configuredCanton))
      {
        selectedIndex = i;
      } else
      {
        i++;
      }
    }
    cantonCombo.setItems(cantonsArray.toArray(new String[0]));
    cantonCombo.select(selectedIndex);
  }

  private Collection<ZulabConfiguration.Entry> getSortedValues(Map<String, ZulabConfiguration.Entry> map)
  {
    ArrayList<ZulabConfiguration.Entry> sortedList = new ArrayList<ZulabConfiguration.Entry>(map.size());

    String[] keyArray = map.keySet().toArray(new String[]
    {
    });

    Arrays.sort(keyArray);

    for (String key : keyArray)
    {
      sortedList.add(map.get(key));
    }

    return sortedList;
  }
}
