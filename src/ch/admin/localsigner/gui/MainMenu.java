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
package ch.admin.localsigner.gui;

import static ch.admin.localsigner.main.LocalSigner.i18n;
import java.awt.Desktop;
import java.io.File;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import com.transparentech.opensource.CocoaUIEnhancer;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.GuiViewMode;
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.listener.AboutListener;
import ch.admin.localsigner.listener.AppendDocumentBundListener;
import ch.admin.localsigner.listener.AppendDocumentBundListener.AppendContext;
import ch.admin.localsigner.listener.AppendDocumentBundListener.BundDocumentType;
import ch.admin.localsigner.listener.AppendDocumentBundListener.BundLanguage;
import ch.admin.localsigner.listener.AppendDocumentBundListener.Department;
import ch.admin.localsigner.listener.AppendDocumentListener;
import ch.admin.localsigner.listener.AppendEmptyPageListener;
import ch.admin.localsigner.listener.ChooseInputListener;
import ch.admin.localsigner.listener.ConfigurationListener;
import ch.admin.localsigner.listener.DeletePageListener;
import ch.admin.localsigner.listener.ExitListener;
import ch.admin.localsigner.listener.InsertAttachmentListener;
import ch.admin.localsigner.listener.LinkListener;
import ch.admin.localsigner.listener.PdfAValidationListener;
import ch.admin.localsigner.main.InputFile;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.LocalSignerCommandLine;
import ch.admin.localsigner.notary.ActivateNotaryFunction;

/**
 * This class creates the menu bar and the menus of the main GUI.
 *
 * @author $Author$
 * @version $Revision$
 */
public class MainMenu
{

  private static final String SIG_PAGES_BUND_PREFIX = "sigPagesBund.";

  private static final String I18N_SIG_PAGES_BUND_MENU = "sigPagesBund.menu";

  private static final Logger LOGGER = Logger.getLogger(MainMenu.class);

  private static final String AMPERSAND = "&";

  private MenuItem signMenu;

  private MenuItem addSigFieldMenu;

  private MenuItem addTextFieldMenu;

  private MenuItem appendEmptyPageMenu;

  private MenuItem deletePageMenu;

  private MenuItem appendDocumentMenu;

  private MenuItem insertAttachmentMenu;

  private MenuItem simpleModeMenu;

  private MenuItem proModeMenu;

  private final MainGUI maingui;

  // Wiki URL for help content
  private static final String DOC_URL = "http://www.openegov.ch/ls25/doc";

  private static final String RELEASENOTES_URL = "http://www.openegov.ch/ls25/relnotes";

  private static final String VALIDATOR_URL = "http://www.openegov.ch/ls25/validator";

  private static final String ADOBE_URL = "http://www.openegov.ch/ls25/adobe";

  private static final String FORMS_URL = "http://www.openegov.ch/ls25/forms";

  private static final String CREATE_URL = "http://www.openegov.ch/ls25/createDoc";

  private static final String EGOV_URL = "http://www.openegov.ch";

  private final LocalSignerCommandLine cli;

  private Menu signaturePagesBundMainMenu;

  /**
   * Constructor
   *
   * @param maingui
   *          Main SWT GUI
   * @param cli
   */
  public MainMenu(final MainGUI maingui, final LocalSignerCommandLine cli)
  {
    this.cli = cli;

    this.maingui = maingui;
    final Shell shell = maingui.getMainshell();

    // create menu
    Menu menu = new Menu(shell, SWT.BAR);

    fileMenu(shell, menu);

    boolean hideSettings = LocalSigner.appConfig.isHideSettings();
    if (LocalSigner.appConfig.userConfigurationAllowed() && !hideSettings)
    {
      // only show it, when user CAN make configuration changes
      configMenu(shell, menu);
    }

    if (isProfessionalMode())
    {
      editMenu(shell, menu);
      extrasMenu(shell, menu);
    }

    if (featureSignaturePagesBundActive())
    {
      addSignaturePagesBund(shell, menu);
    }

    helpMenu(shell, menu);
    shell.setMenuBar(menu);
  }

  private boolean featureSignaturePagesBundActive()
  {
    return LocalSigner.appConfig.isSignaturepagesBundAktiv();
  }

  private void addSignaturePagesBund(Shell shell, Menu menu)
  {
    String menuText = i18n(I18N_SIG_PAGES_BUND_MENU);

    signaturePagesBundMainMenu = createNonClickableMenu(shell, menu, menuText);

    for (Department department : Department.values())
    {
      Menu departmentSubMenu = createNonClickableMenu(shell, signaturePagesBundMainMenu,
          i18n(SIG_PAGES_BUND_PREFIX + department.toString()));
      for (BundDocumentType documentType : BundDocumentType.values())
      {
        Menu documentTypeSubMenu = createNonClickableMenu(shell, departmentSubMenu,
            i18n(SIG_PAGES_BUND_PREFIX + documentType.toString()));
        for (BundLanguage language : BundLanguage.values())
        {
          createClickableMenu(documentTypeSubMenu, i18n(language.toString().toLowerCase()),
              new AppendDocumentBundListener(maingui, new AppendContext(department, documentType, language)));
        }
      }
      departmentSubMenu.getParentItem().setEnabled(false);
    }
  }

  private MenuItem createClickableMenu(Menu toAttach, String menuText, Listener actionOnClick)
  {
    MenuItem clickableMenuItem = new MenuItem(toAttach, SWT.PUSH);
    clickableMenuItem.setText(menuText);
    clickableMenuItem.addListener(SWT.Selection, actionOnClick);
    return clickableMenuItem;
  }

  private Menu createNonClickableMenu(Shell shell, Menu parentMenu, String menuText)
  {
    MenuItem nonClickableMenuItem = new MenuItem(parentMenu, SWT.CASCADE);
    nonClickableMenuItem.setText(menuText);
    Menu menu = new Menu(shell, SWT.DROP_DOWN);
    nonClickableMenuItem.setMenu(menu);
    return menu;
  }

  private boolean isProfessionalMode()
  {
    return LocalSigner.appConfig.getGuiViewMode().isProfessionalMode();
  }

  private void helpMenu(final Shell shell, Menu menu)
  {
    Menu helpmenu = createNonClickableMenu(shell, menu, "?");

    // Help
    createClickableMenu(helpmenu, LocalSigner.i18n("help"), new LinkListener(shell, DOC_URL));
    createClickableMenu(helpmenu, LocalSigner.i18n("adobeHelp"), new LinkListener(shell, ADOBE_URL));
    createClickableMenu(helpmenu, LocalSigner.i18n("formsHelp"), new LinkListener(shell, FORMS_URL));
    createClickableMenu(helpmenu, LocalSigner.i18n("createHelp"), new LinkListener(shell, CREATE_URL));
    createClickableMenu(helpmenu, LocalSigner.i18n("releaseNotes"), new LinkListener(shell, RELEASENOTES_URL));

    if (LocalSigner.isInDebugMode())
    {
      createClickableMenu(helpmenu, "Show Debuglog", new Listener()
      {
        @Override
        public void handleEvent(Event event)
        {
          try
          {
            Desktop.getDesktop().open(new File(LocalSigner.appConfig.getDebugFile()));
          } catch (Exception e)
          {
            LOGGER.error("Cannot open debug logfile", e);
            Message.error(shell, LocalSigner.i18n("jpanelOpenFileError"));
          }
        }
      });
    }
    addMenuSeparator(helpmenu);

    addValidatorWebAppMenuEntry(shell, helpmenu);

    addMenuSeparator(helpmenu);

    createClickableMenu(helpmenu, LocalSigner.i18n("about"), new AboutListener());
    createClickableMenu(helpmenu, LocalSigner.i18n("aboutEGov"), new LinkListener(shell, EGOV_URL));
  }

  private void addMenuSeparator(Menu helpmenu)
  {
    new MenuItem(helpmenu, SWT.SEPARATOR);
  }

  private void extrasMenu(final Shell shell, Menu menu)
  {
    String menuText = AMPERSAND + LocalSigner.i18n("extras");

    Menu extraMenu = createNonClickableMenu(shell, menu, menuText);

    // PDF/A
    addPdfAValidationFunction(extraMenu);

    // Validator
    addActivateNotaryFunction(shell, extraMenu);
  }

  private void addPdfAValidationFunction(Menu extraMenu)
  {
    createClickableMenu(extraMenu, LocalSigner.i18n("validatePdfA"), new PdfAValidationListener());
  }

  private void addActivateNotaryFunction(Shell shell, Menu extraMenu)
  {
    createClickableMenu(extraMenu, LocalSigner.i18n("notarySign.activationMenuEntry"),
        new ActivateNotaryFunction(shell));
  }

  private void addValidatorWebAppMenuEntry(final Shell shell, Menu menu)
  {
    createClickableMenu(menu, LocalSigner.i18n("validation.menuEntry"), new LinkListener(shell, VALIDATOR_URL));
  }

  private void editMenu(final Shell shell, Menu menu)
  {
    MenuItem action = new MenuItem(menu, SWT.CASCADE);
    action.setText(AMPERSAND + LocalSigner.i18n("edit"));
    Menu actionmenu = new Menu(shell, SWT.DROP_DOWN);
    action.setMenu(actionmenu);
    // mode selection
    signMenu = new MenuItem(actionmenu, SWT.RADIO);
    signMenu.setText(LocalSigner.i18n("sign"));
    signMenu.setSelection(true);
    signMenu.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.widget;
        if (item.getSelection())
        {
          maingui.switchMode(GuiMode.sign);
        }
      }

    });

    addMenuSeparator(actionmenu);
    appendEmptyPageMenu = new MenuItem(actionmenu, SWT.RADIO);
    appendEmptyPageMenu.setText(LocalSigner.i18n("appendEmptyPage"));
    appendEmptyPageMenu.setEnabled(false);
    appendEmptyPageMenu.addListener(SWT.Selection, new AppendEmptyPageListener(maingui));

    deletePageMenu = new MenuItem(actionmenu, SWT.RADIO);
    deletePageMenu.setText(LocalSigner.i18n("deletePage"));
    deletePageMenu.setEnabled(false);
    deletePageMenu.addListener(SWT.Selection, new DeletePageListener(maingui));

    appendDocumentMenu = new MenuItem(actionmenu, SWT.RADIO);
    appendDocumentMenu.setText(LocalSigner.i18n("appendDocument"));
    appendDocumentMenu.setEnabled(false);
    appendDocumentMenu.addListener(SWT.Selection, new AppendDocumentListener(maingui));

    addMenuSeparator(actionmenu);
    insertAttachmentMenu = new MenuItem(actionmenu, SWT.RADIO);
    insertAttachmentMenu.setText(LocalSigner.i18n("insertAttachment"));
    insertAttachmentMenu.setEnabled(false);
    insertAttachmentMenu.addListener(SWT.Selection, new InsertAttachmentListener(maingui));

    addMenuSeparator(actionmenu);
    addTextFieldMenu = new MenuItem(actionmenu, SWT.RADIO);
    addTextFieldMenu.setText(LocalSigner.i18n("addTextField"));
    addTextFieldMenu.setEnabled(false);
    addTextFieldMenu.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.widget;
        if (item.getSelection())
        {
          maingui.switchMode(GuiMode.addTextField);
        }
      }

    });

    addSigFieldMenu = new MenuItem(actionmenu, SWT.RADIO);
    addSigFieldMenu.setText(LocalSigner.i18n("sigFieldCreate"));
    addSigFieldMenu.setEnabled(false);
    addSigFieldMenu.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.widget;
        if (item.getSelection())
        {
          maingui.switchMode(GuiMode.addSignatureField);
        }
      }

    });
  }

  private void configMenu(final Shell shell, Menu menu)
  {
    Menu settingsmenu = createNonClickableMenu(shell, menu, AMPERSAND + LocalSigner.i18n("settingsmenu"));

    // config submenu
    createClickableMenu(settingsmenu, AMPERSAND + LocalSigner.i18n("configuration"),
        new ConfigurationListener(maingui));

    if (LocalSigner.appConfig.getGuiViewMode().isMinimalMode())
    {
      return;
    }

    addMenuSeparator(settingsmenu);

    // simple mode
    simpleModeMenu = new MenuItem(settingsmenu, SWT.RADIO);
    simpleModeMenu.setText(LocalSigner.i18n("simpleMode"));
    simpleModeMenu.setSelection(LocalSigner.appConfig.getGuiViewMode().isSimpleMode());
    simpleModeMenu.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.widget;
        if (item.getSelection() && !LocalSigner.appConfig.getGuiViewMode().isSimpleMode())
        {
          try
          {
            LocalSigner.appConfig.setValue(Config.GUI_VIEW_MODE, GuiViewMode.SIMPLE_MODE.getConfigurationValue());
            reconfigureGuiAfterGuiModeSwitch(shell);
          } catch (ConfigurationException ce)
          {
            LOGGER.error(ce);
            // present error message to user
            Message.error(shell, LocalSigner.i18n("errorsaveconfig"));
          }
        }
      }

    });

    // not possible with system profiles only
    if (LocalSigner.appConfig.isSystemProfilesOnly())
    {
      simpleModeMenu.setEnabled(false);
    }

    // pro mode
    proModeMenu = new MenuItem(settingsmenu, SWT.RADIO);
    proModeMenu.setText(LocalSigner.i18n("proMode"));
    proModeMenu.setSelection(!LocalSigner.appConfig.getGuiViewMode().isSimpleMode());
    proModeMenu.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        MenuItem item = (MenuItem) e.widget;
        if (item.getSelection() && LocalSigner.appConfig.getGuiViewMode().isSimpleMode())
        {
          try
          {
            LocalSigner.appConfig.setValue(Config.GUI_VIEW_MODE, GuiViewMode.PROFESSIONAL_MODE.getConfigurationValue());
            reconfigureGuiAfterGuiModeSwitch(shell);
          } catch (ConfigurationException ce)
          {
            LOGGER.error(ce);
            // present error message to user
            Message.error(shell, LocalSigner.i18n("errorsaveconfig"));
          }
        }
      }

    });
  }

  private void fileMenu(final Shell shell, Menu menu)
  {
    boolean mac = System.getProperty("os.name").contains("Mac");
    if (mac)
    {
      // add Cocoa UI enhancer
      try
      {
        CocoaUIEnhancer enhancer = new CocoaUIEnhancer(null);
        enhancer.hookApplicationMenu(maingui.getMainshell().getDisplay(), new ExitListener(shell), new AboutListener(),
            new ConfigurationListener(maingui));
      } catch (Exception e)
      {
        LOGGER.error("Cannot add Mac menu hook", e);
      }
    }

    MenuItem file = new MenuItem(menu, SWT.CASCADE);
    file.setText(AMPERSAND + LocalSigner.i18n("filemenu"));
    Menu filemenu = new Menu(shell, SWT.DROP_DOWN);
    file.setMenu(filemenu);

    // Open is not available in subprocess-mode
    if (maingui.isInteractiveMode())
    {
      MenuItem openItem = new MenuItem(filemenu, SWT.PUSH);
      openItem.setText(AMPERSAND + LocalSigner.i18n("open") + "\tCtrl+O");
      openItem.setAccelerator(SWT.MOD1 + 'O');
      openItem.addListener(SWT.Selection, new ChooseInputListener(maingui));
    }

    if (!mac)
    {
      addMenuSeparator(filemenu);

      MenuItem exitItem = new MenuItem(filemenu, SWT.PUSH);
      exitItem.setText(AMPERSAND + LocalSigner.i18n("quit") + "\tCtrl+Q");
      exitItem.addListener(SWT.Selection, new ExitListener(shell));
      exitItem.setAccelerator(SWT.MOD1 + 'Q');
    }
  }

  /**
   * Switch GUI mode and change menu items accordingly.
   *
   * @param mode
   *          Currently selected GUI mode
   */
  public void switchMode(final GuiMode mode)
  {
    LOGGER.debug("switch menu to " + mode);
    if (signMenu != null)
    {
      signMenu.setSelection(mode == GuiMode.sign);
    }

    if (appendEmptyPageMenu != null)
    {
      appendEmptyPageMenu.setSelection(mode == GuiMode.appendEmptyPage);
    }

    if (deletePageMenu != null)
    {
      deletePageMenu.setSelection(mode == GuiMode.deletePage);
    }

    if (appendDocumentMenu != null)
    {
      appendDocumentMenu.setSelection(mode == GuiMode.appendDocument);
    }

    if (insertAttachmentMenu != null)
    {
      insertAttachmentMenu.setSelection(mode == GuiMode.insertAttachment);
    }

    if (addTextFieldMenu != null)
    {
      addTextFieldMenu.setSelection(mode == GuiMode.addTextField);
    }

    if (addSigFieldMenu != null)
    {
      addSigFieldMenu.setSelection(mode == GuiMode.addSignatureField);
    }
  }

  /**
   * Toggle modify menu item.
   *
   * @param modify
   *          true to enable
   */
  public void canModify(boolean modify)
  {

    if (signaturePagesBundMainMenu != null)
    {
      signaturePagesBundMainMenu.setEnabled(modify);
      for (MenuItem item : signaturePagesBundMainMenu.getItems())
      {
        item.setEnabled(modify);
      }
      signaturePagesBundMainMenu.getItems();
    }

    if (signMenu == null)
    {
      // simple mode, don't change anything else
      return;
    }

    // signing always
    signMenu.setEnabled(true);

    appendEmptyPageMenu.setEnabled(modify);
    deletePageMenu.setEnabled(modify);
    appendDocumentMenu.setEnabled(modify);

    insertAttachmentMenu.setEnabled(modify);

    if (maingui.isInternalViewer())
    {
      addTextFieldMenu.setEnabled(modify);
      addSigFieldMenu.setEnabled(modify);
    }
    else
    {
      // cannot do that in adobe viewer
      addTextFieldMenu.setEnabled(false);
      addSigFieldMenu.setEnabled(false);
    }
  }

  /**
   * Toggle sign menu item.
   *
   * @param sign
   *          true to enable
   */
  public void canSign(boolean sign)
  {
    if (signMenu == null)
    {
      // simple mode, don't change anything
      return;
    }
    signMenu.setEnabled(sign);
  }

  /**
   * Toggle delete menu item.
   *
   * @param delete
   *          true to enable
   */
  public void canDelete(boolean delete)
  {
    if (deletePageMenu == null)
    {
      // simple mode, don't change anything
      return;
    }
    deletePageMenu.setEnabled(delete);
  }

  public void disableMenueMode()
  {
    if (simpleModeMenu != null)
    {
      simpleModeMenu.setEnabled(false);
    }
    if (proModeMenu != null)
    {
      proModeMenu.setEnabled(false);
    }
  }

  private void reconfigureGuiAfterGuiModeSwitch(final Shell shell)
  {
    InputFile inputFile = maingui.getDocument().getInputFile();
    if (!maingui.isInteractiveMode() && inputFile != null)
    {
      inputFile.setMainGui(null);
      maingui.getDocument().setInputFile(null);
    }

    Display d = shell.getDisplay();
    shell.dispose();
    LocalSigner.mainGui = new MainGUI();
    if (!maingui.isInteractiveMode() && inputFile != null)
    {
      inputFile.setMainGui(LocalSigner.mainGui);
      LocalSigner.mainGui.showGui(d, cli, inputFile);
    }
    else
    {
      LocalSigner.mainGui.showGui(d, cli);
    }
  }

}
