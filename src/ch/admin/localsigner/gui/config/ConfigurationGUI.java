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

import ch.admin.localsigner.config.ApplicationConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ch.admin.localsigner.config.ApplicationConfiguration.PdfViewer;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.LanguageConfiguration;
import ch.admin.localsigner.config.ZulabConfiguration;
import ch.admin.localsigner.config.util.ProxyConfiguratorInitializer;
import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.YesNoDialog;
import ch.admin.localsigner.listener.InputPathListener;
import ch.admin.localsigner.listener.Pkcs11LibListener;
import ch.admin.localsigner.listener.Pkcs11TextListener;
import ch.admin.localsigner.listener.Pkcs12LibListener;
import ch.admin.localsigner.listener.ProfilePathListener;
import ch.admin.localsigner.listener.ResetConfigListener;
import ch.admin.localsigner.listener.ResetProfileListener;
import ch.admin.localsigner.main.LocalSigner;
import ch.glue.proxylibrary.core.system.ProxySetting;
import ch.glue.proxylibrary.swt.view.SWTProxyGUI;


/**
 * This class shows the configuration user interface.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class ConfigurationGUI
{
  // pdf viewer values
  private static final int ADOBE_VIEWER = 0;

  private static final int INTERNAL_VIEWER = 1;

  private static final int DUAL_VIEWER = 2;

  private final Shell shell;

  // config values
  private Combo viewerCombo;

  private Text fontSizeText;

  private Text profilePathText;

  private Text inputPathText;

  private Combo languageCombo;

  private Combo pkcs11LibText;

  private Button chooserButton;

  private Label libraryLabel;

  private Button integrityCheckbox;

  private Button signNonPdfACheckbox;

  private Button sidePanelActiveCheckbox;

  private Button ltvActiveCheckbox;

  private Button ocspActivateCheckbox;

  private boolean restartNeeded = false;

  private final Font font;

  private Label pkcs11LibDescription;

  private Text pkcs12File;

  private Label pkcs12Label;

  private Button pkcs12Button;

  private Button zulabAskEverytimeCheckbox;

  private Combo domainCombo;
  private Combo cantonCombo;

  private static final int ROWS = 4;

    private ConfigurationGUI instance = this;

  private static final Logger LOGGER = Logger.getLogger(ConfigurationGUI.class);

  /**
   * Constructor
   *
   * @param maingui
   *          Main GUI
   */
  public ConfigurationGUI(final MainGUI maingui)
  {
    this.font = maingui.getFont();

    // create new shell
    shell = new Shell(maingui.getMainshell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    shell.setLayout(new GridLayout());
    shell.setText(LocalSigner.i18n("configuration"));
    shell.setImage(GuiHelper.loadAppIcon(maingui.getMainshell().getDisplay()));

    // create contents
    this.createContents();

    // pack
    shell.pack();

    // take calculated size (pack) and place in the middle of the parent
    final Rectangle parentsize = maingui.getMainshell().getBounds();
    // place in the middle
    final Point calculatedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
    shell.setBounds(parentsize.x + (parentsize.width - calculatedSize.x) / 2,
            parentsize.y + (parentsize.height - calculatedSize.y) / 2,
            calculatedSize.x + 100, calculatedSize.y);

    shell.open();

    // and wait for the user to make a selection
    while (!shell.isDisposed())
    {
      if (!maingui.getMainshell().getDisplay().readAndDispatch())
      {
        maingui.getMainshell().getDisplay().sleep();
      }
    }
  }

  private void createContents()
  {
    shell.setLayout(new GridLayout(2, false));

    createGlobalProperiesElements();

    if (LocalSigner.appConfig.isEditable(Config.LTV_ACTIVE_EDIT)
        || LocalSigner.appConfig.isEditable(Config.LTV_OCSP_ACTIVE_EDIT))
    {
      generateLtvConfig();
    }

    if (LocalSigner.appConfig.isEditable(Config.PROXY_EDIT))
    {
      createProxyConfigElements();
    }

    if (!isMinimalMode())
    {
      createCertificateAccessElements();
    }

    if (LocalSigner.appConfig.isFunknachweisAktiv())
    {
      createZulabConfigElements();
    }

    if (!isMinimalMode())
    {
      createResetElements();
    }

    createButtons();
  }

  private void createResetElements()
  {
    final Group group = new Group(shell, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    group.setFont(font);
    group.setText(LocalSigner.i18n("reset"));
    group.setLayout(new GridLayout(ROWS, false));

    Button configReset = new Button(group, SWT.PUSH);
    configReset.setFont(font);
    configReset.setText(LocalSigner.i18n("configReset"));
    configReset.addListener(SWT.Selection, new ResetConfigListener(shell));

    Button profileReset = new Button(group, SWT.PUSH);
    profileReset.setFont(font);
    profileReset.setText(LocalSigner.i18n("profileReset"));
    profileReset.addListener(SWT.Selection, new ResetProfileListener(shell));
  }

  private void createGlobalProperiesElements()
  {
    final Group globalConfig = new Group(shell, SWT.NONE);
    globalConfig.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    globalConfig.setFont(font);
    globalConfig.setText(LocalSigner.i18n("globalconfig"));
    globalConfig.setLayout(new GridLayout(ROWS, false));

    if (LocalSigner.appConfig.isEditable(Config.LANGUAGE_EDIT))
    {
      generateLangConfig(globalConfig);
    }

    // pro mode only
    if (isEnhancedMode())
    {
      if (LocalSigner.appConfig.isEditable(Config.PROFILE_PATH_EDIT))
      {
        generateProfilePathConfig(globalConfig);
      }

      if (LocalSigner.appConfig.isEditable(Config.INPUT_PATH_EDIT))
      {
        generateInputPathConfig(globalConfig);
      }

      if (LocalSigner.appConfig.isEditable(Config.FONTSIZE_EDIT))
      {
        generateFontSizeConfig(globalConfig);
      }

      if (LocalSigner.appConfig.isEditable(Config.INTERNALVIEWER_EDIT))
      {
        generateViewerConfig(globalConfig);
      }
    }

    if (!isMinimalMode())
    {
      if (LocalSigner.appConfig.isEditable(Config.INTEGRITY_CHECK_EDIT))
      {
        generateIntegrityConfig(globalConfig);
      }
    }

    if (LocalSigner.appConfig.isEditable(Config.SIGN_NON_PDF_A_EDIT))
    {
      generateSignNonPdfAConfig(globalConfig);
    }

    if (LocalSigner.appConfig.isEditable(Config.SIDE_PANEL_ACTIVE_EDIT))
    {
      generateSidePanelActiveConfig(globalConfig);
    }
  }

  private void generateIntegrityConfig(final Group globalConfig)
  {
    integrityCheckbox = new Button(globalConfig, SWT.CHECK);
    integrityCheckbox.setText(LocalSigner.i18n("integritycheckhelp"));
    integrityCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS, 1));
    integrityCheckbox.setSelection(LocalSigner.appConfig.isShowIntegrityCheck());
    integrityCheckbox.setFont(font);
  }

  private void generateSignNonPdfAConfig(final Group globalConfig)
  {
    signNonPdfACheckbox = new Button(globalConfig, SWT.CHECK);
    signNonPdfACheckbox.setText(LocalSigner.i18n("signNonPdfAConfigText"));
    signNonPdfACheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS, 1));
    signNonPdfACheckbox.setSelection(LocalSigner.appConfig.isSignNonPdfA());
    signNonPdfACheckbox.setFont(font);
  }

  private void generateSidePanelActiveConfig(final Group globalConfig)
  {
    sidePanelActiveCheckbox = new Button(globalConfig, SWT.CHECK);
    sidePanelActiveCheckbox.setText(LocalSigner.i18n("sideBar.showSideBar"));
    sidePanelActiveCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS, 1));
    sidePanelActiveCheckbox.setSelection(LocalSigner.appConfig.isSidePanelActive());
    sidePanelActiveCheckbox.setFont(font);
  }


  private void generateLtvConfig()
  {
    Group ltvConfig = new Group(shell, SWT.NONE);
    ltvConfig.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    ltvConfig.setFont(font);
    ltvConfig.setText(LocalSigner.i18n("ltvconfig"));
    ltvConfig.setLayout(new GridLayout(2, false));

    if (LocalSigner.appConfig.isEditable(Config.LTV_ACTIVE_EDIT))
    {
      generateLtvActiveConfig(ltvConfig);
    }

    if (LocalSigner.appConfig.isEditable(Config.LTV_OCSP_ACTIVE_EDIT))
    {
      generateOcspActiveConfig(ltvConfig);
    }
  }

  private void generateLtvActiveConfig(final Group widgetGroup)
  {
    ltvActiveCheckbox = new Button(widgetGroup, SWT.CHECK);
    ltvActiveCheckbox.setText(LocalSigner.i18n("ltv.active"));
    ltvActiveCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS, 1));
    ltvActiveCheckbox.setSelection(LocalSigner.appConfig.isLtvActive());
    ltvActiveCheckbox.setFont(font);

    ltvActiveCheckbox.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent event)
      {
        Button btn = (Button) event.getSource();
        if(btn.getSelection())
        {
          ocspActivateCheckbox.setEnabled(true);
          ocspActivateCheckbox.setSelection(true); // desired default behaviour
        } else
        {
          ocspActivateCheckbox.setEnabled(false);
        }
      }
    });
  }

  private void generateOcspActiveConfig(final Group widgetGroup)
  {
    ocspActivateCheckbox = new Button(widgetGroup, SWT.CHECK);
    ocspActivateCheckbox.setText(LocalSigner.i18n("ltv.ocsp.active"));
    ocspActivateCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS, 1));
    ocspActivateCheckbox.setSelection(LocalSigner.appConfig.isOcspActive());
    ocspActivateCheckbox.setFont(font);
    ocspActivateCheckbox.addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent event)
      {
        Button btn = (Button) event.getSource();
        if(!ltvActiveCheckbox.getSelection())
        {
          btn.setEnabled(false);
        } else
        {
          btn.setEnabled(true);
        }
      }
    });
  }

  private void generateViewerConfig(final Group globalConfig)
  {
    final String[] viewers = new String[]
    {
      LocalSigner.i18n("adobeViewer"),
      LocalSigner.i18n("internalViewer"),
      LocalSigner.i18n("dualViewer")
    };

    Label viewerLabel = GuiHelper.label(globalConfig, SWT.NONE, LocalSigner.i18n("viewer") + ":", font);
    viewerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    viewerCombo = new Combo(globalConfig, SWT.READ_ONLY);
    viewerCombo.setFont(font);
    viewerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS - 1, 1));
    viewerCombo.setItems(viewers);
    viewerCombo.setToolTipText(LocalSigner.i18n("tooltipViewer"));

    // set current value
    switch (LocalSigner.appConfig.getViewer())
    {
      case ADOBE:
        viewerCombo.select(ADOBE_VIEWER);
        break;
      case INTERNAL:
        viewerCombo.select(INTERNAL_VIEWER);
        break;
      default:
        viewerCombo.select(DUAL_VIEWER);
        break;
    }
  }

  private void generateFontSizeConfig(final Group globalConfig)
  {
    Label fontSizeLabel = GuiHelper.label(globalConfig, SWT.NONE, LocalSigner.i18n("fontsize") + ":", font);
    fontSizeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    fontSizeText = new Text(globalConfig, SWT.BORDER);
    fontSizeText.setFont(font);
    fontSizeText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, ROWS - 1, 1));
    fontSizeText.setToolTipText(LocalSigner.i18n("tooltipFontsize"));

    // curent size
    fontSizeText.setText(String.valueOf(LocalSigner.appConfig.getFontSize()));
  }

  private void generateProfilePathConfig(final Group globalConfig)
  {
    Label profilePathLabel = GuiHelper.label(globalConfig, SWT.NONE, LocalSigner.i18n("profilepath") + ":", font);
    profilePathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    profilePathText = new Text(globalConfig, SWT.BORDER);
    profilePathText.setFont(font);
    profilePathText.setEditable(false);
    profilePathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    profilePathText.setToolTipText(LocalSigner.i18n("tooltipProfilepath"));

    Button profileChooseButton = new Button(globalConfig, SWT.PUSH);
    profileChooseButton.setFont(font);
    profileChooseButton.setText(LocalSigner.i18n("choose"));
    profileChooseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    profileChooseButton.addSelectionListener(new ProfilePathListener(shell, profilePathText));

    // set current value
    profilePathText.setText(LocalSigner.appConfig.getUserProfileFolder());
  }

  private void generateInputPathConfig(Group globalConfig)
  {
    Label inputPathLabel = GuiHelper.label(globalConfig, SWT.NONE, LocalSigner.i18n("inputpath") + ":", font);
    inputPathLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    inputPathText = new Text(globalConfig, SWT.BORDER);
    inputPathText.setFont(font);
    inputPathText.setEditable(false);
    inputPathText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    inputPathText.setToolTipText(LocalSigner.i18n("tooltipInputpath"));

    Button inputChooseButton = new Button(globalConfig, SWT.PUSH);
    inputChooseButton.setFont(font);
    inputChooseButton.setText(LocalSigner.i18n("choose"));
    inputChooseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    inputChooseButton.addSelectionListener(new InputPathListener(shell, inputPathText));

    // set current value
    inputPathText.setText(LocalSigner.appConfig.getInputpath());
  }

  private void generateLangConfig(Group globalConfig)
  {
    Label languageLabel = GuiHelper.label(globalConfig, SWT.NONE, LocalSigner.i18n("language") + ":", font);
    languageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    languageCombo = new Combo(globalConfig, SWT.READ_ONLY);
    languageCombo.setFont(font);
    languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, ROWS - 1, 1));
    Map<String, String> languages = getLanguages();
    languageCombo.setItems(languages.values().toArray(new String[0]));

    // set current value
    String currentLanguage = LocalSigner.appConfig.getLanguage();
    List<String> langList = new ArrayList<String>(languages.keySet());
    languageCombo.select(langList.indexOf(currentLanguage));
  }

  private Map<String, String> getLanguages()
  {
    Map<String, String> languages = new TreeMap<String, String>();
    languages.put("auto", LocalSigner.i18n("auto"));
    // scan language directory for lang.properties files
    File dir = new File(LanguageConfiguration.getLanguageFolder()).getAbsoluteFile();
    File[] langs = dir.listFiles();
    if (langs!=null)
    {
      for (File lang : langs)
      {
        String name = lang.getName();
        if (name.endsWith(".properties"))
        {
          String key = name.substring(0, name.length() - 11);
          String label = LocalSigner.i18n(key);
          if (label.startsWith("Missing"))
          {
            label = key;
          } else
          {
            label = key + ": " + label;
          }
          languages.put(key, label);
        }
      }
    }
    LOGGER.debug("languages: " + languages);
    return languages;
  }


  private void createProxyConfigElements()
  {
    Group timestampConfig = new Group(shell, SWT.NONE);

    Button btnProxyConfigDialog = new Button(timestampConfig, SWT.PUSH);
    btnProxyConfigDialog.setFont(font);
    btnProxyConfigDialog.setText(LocalSigner.i18n("proxyGUI.proxyconfig"));
    btnProxyConfigDialog.addListener(SWT.Selection, new Listener() {
      @Override
      public void handleEvent(Event event)
      {
        new SWTProxyGUI(instance.getShell(), font, GuiHelper.loadAppIcon(shell.getDisplay()));
        setRestartNeeded(ProxyConfiguratorInitializer.isRestartNeeded());
      }
    });

    timestampConfig.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    timestampConfig.setFont(font);
    timestampConfig.setText(LocalSigner.i18n("proxyGUI.proxyconfig"));
    timestampConfig.setLayout(new GridLayout(2, false));
  }

  private void createCertificateAccessElements()
  {

    final Group certAccessConfig = new Group(shell, SWT.NONE);
    certAccessConfig.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    certAccessConfig.setFont(font);
    certAccessConfig.setText(LocalSigner.i18n("certificateaccess"));
    certAccessConfig.setLayout(new GridLayout(ROWS - 1, false));

    libraryLabel = GuiHelper.label(certAccessConfig, SWT.NONE, LocalSigner.i18n("pkcs11library") + ":", font);
    libraryLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    pkcs11LibText = new Combo(certAccessConfig, SWT.BORDER);
    pkcs11LibText.setFont(font);
    pkcs11LibText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    pkcs11LibText.setToolTipText(LocalSigner.i18n("tooltipPKCS11Lib"));
    Map<String, String> pkcs11Libs = LocalSigner.appConfig.detectPkcs11Lib();
    for (String path : pkcs11Libs.keySet())
    {
      pkcs11LibText.add(path);
    }

    chooserButton = new Button(certAccessConfig, SWT.PUSH);
    chooserButton.setFont(font);
    chooserButton.setText(LocalSigner.i18n("choose"));
    chooserButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    chooserButton.addSelectionListener(new Pkcs11LibListener(shell, pkcs11LibText));

    GuiHelper.makeDummy(certAccessConfig, 1);
    pkcs11LibDescription = GuiHelper.label(certAccessConfig, SWT.NONE, "", font);
    pkcs11LibDescription.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
    pkcs11LibText.addSelectionListener(
      new Pkcs11TextListener(pkcs11LibText, pkcs11LibDescription, pkcs11Libs, certAccessConfig));

    pkcs11LibText.setText(LocalSigner.appConfig.getPkcs11Lib());
    String descr = pkcs11Libs.get(pkcs11LibText.getText());
    if (descr == null)
    {
      descr = "";
    }
    pkcs11LibDescription.setText(descr);

    pkcs12Label = GuiHelper.label(certAccessConfig, SWT.NONE, LocalSigner.i18n("pkcs12File") + ":", font);
    pkcs12File = new Text(certAccessConfig, SWT.BORDER);
    pkcs12File.setFont(font);
    pkcs12File.setText(LocalSigner.appConfig.getPkcs12File());
    pkcs12File.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    pkcs12File.setToolTipText(LocalSigner.i18n("tooltipPKCS12File"));
    pkcs12Button = new Button(certAccessConfig, SWT.PUSH);
    pkcs12Button.setFont(font);
    pkcs12Button.setText(LocalSigner.i18n("choose"));
    pkcs12Button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    pkcs12Button.addSelectionListener(new Pkcs12LibListener(shell, pkcs12File));

    // layout group
    certAccessConfig.layout();
  }

  private void createZulabConfigElements()
  {
    // Border
    final Group zulabConfig = new Group(shell, SWT.NONE);
    zulabConfig.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 5, 1));
    zulabConfig.setFont(font);
    zulabConfig.setText(LocalSigner.i18n("zulab.config.title"));
    zulabConfig.setLayout(new GridLayout(5, false));

    // ask everytime checkbox
    zulabAskEverytimeCheckbox = new Button(zulabConfig, SWT.CHECK);
    zulabAskEverytimeCheckbox.setText(LocalSigner.i18n("zulab.config.askEverytime.label"));
    zulabAskEverytimeCheckbox.setToolTipText(LocalSigner.i18n("zulab.config.askEverytime.tooltip"));
    zulabAskEverytimeCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    zulabAskEverytimeCheckbox.setSelection(LocalSigner.appConfig.isFunktionsnachweisShowDialog());
    zulabAskEverytimeCheckbox.setFont(font);

    // domain label
    GuiHelper.label(zulabConfig, SWT.NONE, LocalSigner.i18n("zulab.config.domain.label") + ":", font);

    // domain combo
    domainCombo = new Combo(zulabConfig, SWT.READ_ONLY);
    domainCombo.setFont(font);
    domainCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    domainCombo.setToolTipText(LocalSigner.i18n("zulab.config.domain.tooltip"));
    fillDomainCombo(domainCombo);

    // canton label
    GuiHelper.label(zulabConfig, SWT.NONE, LocalSigner.i18n("zulab.config.canton.label") + ":", font);

    // canton combo
    cantonCombo = new Combo(zulabConfig, SWT.READ_ONLY);
    cantonCombo.setFont(font);
    cantonCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    cantonCombo.setToolTipText(LocalSigner.i18n("zulab.config.canton.tooltip"));
    fillCantonCombo(cantonCombo);

    // layout group
    zulabConfig.layout();
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

    String[] keyArray = map.keySet().toArray(new String[]{});

    Arrays.sort(keyArray);

    for (String key : keyArray)
    {
      sortedList.add(map.get(key));
    }

    return sortedList;
  }

  private void createButtons()
  {
    Button cancelButton = new Button(shell, SWT.PUSH);
    cancelButton.setFont(font);
    cancelButton.setText(LocalSigner.i18n("cancel"));
    cancelButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
    cancelButton.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetDefaultSelected(final SelectionEvent e)
      {
        widgetSelected(e);
      }

      @Override
      public void widgetSelected(final SelectionEvent e)
      {
        askForRestart();
        close();
      }
    });

    Button okButton = new Button(shell, SWT.PUSH);
    okButton.setFont(font);
    okButton.setText(LocalSigner.i18n("ok"));
    okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
    okButton.setFocus();
    shell.setDefaultButton(okButton);
    okButton.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetDefaultSelected(final SelectionEvent e)
      {
        widgetSelected(e);
      }

      @Override
      public void widgetSelected(final SelectionEvent e)
      {
        saveConfiguration();
      }
    });
  }

  private void saveConfiguration()
  {
    if (errorInConfigurationValues())
    {
      return;
    }

    try
    {
      storeGlobalSettings();

      if (pkcs11LibText != null)
      {
        storeCertificateStoreSettings();
      }
      if (LocalSigner.appConfig.isFunknachweisAktiv())
      {
        storeZulabConfig();
      }
    } catch (ConfigurationException ce)
    {
      LOGGER.error(ce);
      // present error message to user
      Message.error(shell, LocalSigner.i18n("errorsaveconfig"));
      return;
    }

    askForRestart();

    if (!shell.isDisposed())
    {
      shell.close();
    }
  }

  private void askForRestart(){

    // ask user if he wants to restart
    if (restartNeeded)
    {
      YesNoDialog restart = new YesNoDialog(shell,
        LocalSigner.i18n("restartNeededShort"),
        LocalSigner.i18n("restartNeededExtended"));

      if (restart.isUserDecision())
      {
        restart();
      }
    }
  }

  /**
   * Method to close this dialog. It just forwards the close to the internal
   * shell.
   */
  private void close()
  {
    shell.close();
  }

  private void restart()
  {
    Display d = shell.getDisplay();
    shell.getParent().dispose();

    // reload language config
    String lang = LocalSigner.appConfig.getLanguageEvaluatingAuto();

    // now load the correct language configuration
    LanguageConfiguration langConf;
    try
    {
      langConf = new LanguageConfiguration(lang);
    } catch (ConfigurationException e)
    {
      LOGGER.error("Cannot load language", e);
      Message.warning(shell, LocalSigner.i18n("errorReloadingLanguage"));
      return;
    }

    LocalSigner.setLangConf(langConf);

    try
    {
      LocalSigner.appConfig = new ApplicationConfiguration();
    } catch (Exception e)
    {
      LOGGER.error("Cannot init user configuration", e);
      Message.warning(shell, LocalSigner.i18n("errorReloadingConfiguration"));
      return;
    }

    ProxySetting.setupProxy();

    LocalSigner.mainGui = new MainGUI();
    LocalSigner.mainGui.showGui(d, null);
  }

  private boolean errorInConfigurationValues()
  {
    // checks for errors, if the values can be changed by the user

    // check fontsize
    if (LocalSigner.appConfig.isEditable(Config.FONTSIZE_EDIT) && isEnhancedMode())
    {
      final String fontsize = fontSizeText.getText();
      try
      {
        Integer.parseInt(fontsize);
      } catch (NumberFormatException nfe)
      {
        LOGGER.debug("Invalid fontsize entered");
        Message.warning(shell, LocalSigner.i18n("configerror"), LocalSigner.i18n("configerrorfontsize"));
        return true;
      }
    }

    // no errors found
    return false;
  }

  /**
   * Only called, when user is allowed to change values
   *
   * @throws ConfigurationException
   */
  private void storeCertificateStoreSettings() throws ConfigurationException
  {

    if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getPkcs11Lib(), pkcs11LibText.getText()))
    {
      LocalSigner.appConfig.setValue(Config.PKCS11_LIB, pkcs11LibText.getText());
      restartNeeded = true;
    }

    if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getPkcs12File(), pkcs12File.getText()))
    {
      LocalSigner.appConfig.setValue(Config.PKCS12_FILE, pkcs12File.getText());
    }
  }

  private void storeZulabConfig() throws ConfigurationException
  {
    // ask-dialog
    LocalSigner.appConfig.setValue(
        Config.FUNKTIONSNACHWEIS_ZULAB_SHOW_DIALOG, zulabAskEverytimeCheckbox.getSelection());

    // domain
    if (domainCombo.getSelectionIndex() > 0 && domainCombo.getSelectionIndex() < domainCombo.getItemCount())
    {
      Map<String, ZulabConfiguration.Entry> domains = LocalSigner.appConfig.getZulabDomains();
      String selectedDomainValue = "";
      for (ZulabConfiguration.Entry entry : getSortedValues(domains))
      {
        if (entry.getTranslation(LocalSigner.getLocale()).equals(domainCombo.getItem(domainCombo.getSelectionIndex())))
        {
          selectedDomainValue = entry.getValue();
        }
      }
      LocalSigner.appConfig.setValue(Config.FUNKTIONSNACHWEIS_ZULAB_DOMAIN, selectedDomainValue);
    }

    // canton
    if (cantonCombo.getSelectionIndex() > 0 && cantonCombo.getSelectionIndex() < cantonCombo.getItemCount())
    {
      Map<String, ZulabConfiguration.Entry> cantons = LocalSigner.appConfig.getZulabCantons();
      String selectedCantonValue = "";
      for (ZulabConfiguration.Entry entry : getSortedValues(cantons))
      {
        if (entry.getTranslation(LocalSigner.getLocale()).equals(cantonCombo.getItem(cantonCombo.getSelectionIndex())))
        {
          selectedCantonValue = entry.getValue();
        }
      }
      LocalSigner.appConfig.setValue(Config.FUNKTIONSNACHWEIS_ZULAB_CANTON, selectedCantonValue);
    }
  }

  private void storeGlobalSettings() throws ConfigurationException
  {
    // config version
    String version = GuiHelper.getVersion();
    if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getVersion(), version))
    {
      LocalSigner.appConfig.setValue(Config.VERSION, version);
    }

    // language
    if (LocalSigner.appConfig.isEditable(Config.LANGUAGE_EDIT))
    {

      Map<String, String> langs = getLanguages();
      for (Entry<String, String> entry : langs.entrySet())
      {
        // reverse lookup in map
        if (entry.getValue().equals(languageCombo.getText()))
        {
          String key = entry.getKey();
          if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getLanguage(), key))
          {
            // check if store needed
            LocalSigner.appConfig.setValue(Config.LANGUAGE, key);
            LOGGER.debug("set language to: " + key);
            restartNeeded = true;
            break;
          }
        }
      }
    }

    // profile path
    if (LocalSigner.appConfig.isEditable(Config.PROFILE_PATH_EDIT) && isEnhancedMode())
    {

      // check if store needed
      if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getUserProfileFolder(), profilePathText.getText()))
      {
        LocalSigner.appConfig.setValue(Config.PROFILE_PATH, profilePathText.getText());
        LOGGER.debug("set profilepath to: " + profilePathText.getText());
        restartNeeded = true;
      }
    }

    // input path
    if (LocalSigner.appConfig.isEditable(Config.INPUT_PATH_EDIT) && isEnhancedMode())
    {

      // check if store needed
      if (!StringUtils.equalsIgnoreCase(LocalSigner.appConfig.getInputpath(), inputPathText.getText()))
      {
        LocalSigner.appConfig.setValue(Config.INPUT_PATH, inputPathText.getText());
        LOGGER.debug("set inputpath to: " + inputPathText.getText());
        restartNeeded = true;
      }
    }

    // fontsize, checked for errors previously
    if (LocalSigner.appConfig.isEditable(Config.FONTSIZE_EDIT) && isEnhancedMode())
    {

      int fontsize = Integer.parseInt(fontSizeText.getText());

      // check if store needed
      if (fontsize != LocalSigner.appConfig.getFontSize())
      {

        // limit fontsize for DAU's (range 5..30)
        fontsize = Math.max(5, fontsize);
        fontsize = Math.min(30, fontsize);

        LocalSigner.appConfig.setValue(Config.FONTSIZE, "" + fontsize);
        LOGGER.debug("set fontsize to: " + fontsize);
        restartNeeded = true;
      }
    }

    // viewer
    if (LocalSigner.appConfig.isEditable(Config.INTERNALVIEWER_EDIT) && isEnhancedMode())
    {
      PdfViewer currentViewer = LocalSigner.appConfig.getViewer();
      PdfViewer newViewer;
      String configValue;
      switch (viewerCombo.getSelectionIndex())
      {
        case INTERNAL_VIEWER:
          newViewer = PdfViewer.INTERNAL;
          configValue = "true";
          break;
        case ADOBE_VIEWER:
          newViewer = PdfViewer.ADOBE;
          configValue = "false";
          break;
        default:
          newViewer = PdfViewer.DUAL;
          configValue = "dual";
          break;
      }

      if (!currentViewer.equals(newViewer))
      {
        LocalSigner.appConfig.setValue(Config.INTERNALVIEWER, String.valueOf(configValue));
        LOGGER.debug("set internalviewer to: " + configValue);
        restartNeeded = true;
      }
    }

    if (LocalSigner.appConfig.isEditable(Config.INTEGRITY_CHECK_EDIT) && !isMinimalMode())
    {

      // check if store needed
      if (LocalSigner.appConfig.isShowIntegrityCheck() ^ integrityCheckbox.getSelection())
      {
        LocalSigner.appConfig.setValue(Config.INTEGRITY_CHECK, "" + integrityCheckbox.getSelection());
        LOGGER.debug("set showIntegrityCheck to: " + integrityCheckbox.getSelection());
      }
    }

    if (LocalSigner.appConfig.isEditable(Config.SIGN_NON_PDF_A_EDIT))
    {

      // check if store needed
      if (LocalSigner.appConfig.isSignNonPdfA() ^ signNonPdfACheckbox.getSelection())
      {
        LocalSigner.appConfig.setValue(Config.SIGN_NON_PDF_A, "" + signNonPdfACheckbox.getSelection());
        LOGGER.debug("set signNonPdfA to: " + signNonPdfACheckbox.getSelection());
      }
    }

    if (LocalSigner.appConfig.isEditable(Config.SIDE_PANEL_ACTIVE_EDIT))
    {

      // check if store needed
      if (LocalSigner.appConfig.isSidePanelActive() ^ sidePanelActiveCheckbox.getSelection())
      {
        LocalSigner.appConfig.setValue(Config.SIDE_PANEL_ACTIVE, "" + sidePanelActiveCheckbox.getSelection());
        LOGGER.debug("set sidePanelActive to: " + sidePanelActiveCheckbox.getSelection());
      }
    }
    LocalSigner.mainGui.loadSidePanel();

    if (LocalSigner.appConfig.isEditable(Config.LTV_ACTIVE_EDIT))
    {
      LocalSigner.appConfig.setValue(Config.LTV_ACTIVE, "" + ltvActiveCheckbox.getSelection());
      LOGGER.debug("set ltvActive to: " + ltvActiveCheckbox.getSelection());
    }

    if (LocalSigner.appConfig.isEditable(Config.LTV_OCSP_ACTIVE_EDIT))
    {
      LocalSigner.appConfig.setValue(Config.LTV_OCSP_ACTIVE, "" + ocspActivateCheckbox.getSelection());
      LOGGER.debug("set ocspActive to: " + ocspActivateCheckbox.getSelection());
    }
  }

  private boolean isEnhancedMode()
  {
    return LocalSigner.appConfig.getGuiViewMode().isProfessionalMode();
  }

  private boolean isMinimalMode()
  {
    return LocalSigner.appConfig.getGuiViewMode().isMinimalMode();
  }

  public Font getFont()
  {
    return font;
  }

  public Shell getShell(){
    return shell;
  }

  public void setRestartNeeded(boolean restartNeeded){
    this.restartNeeded = restartNeeded;
  }
}
