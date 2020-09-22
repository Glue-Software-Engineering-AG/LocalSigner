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

import java.awt.Dimension;
import java.awt.Frame;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.lowagie.text.exceptions.BadPasswordException;
import ch.admin.localsigner.config.ApplicationConfiguration.PdfViewer;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.util.BasePath;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.config.ConfigurationGUI;
import ch.admin.localsigner.gui.profile.Profile;
import ch.admin.localsigner.gui.profile.ProfileCollector;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.gui.viewer.ViewerPanel;
import ch.admin.localsigner.gui.viewer.ViewerPanelFactory;
import ch.admin.localsigner.listener.AddSigFieldListener;
import ch.admin.localsigner.listener.ChooseInputListener;
import ch.admin.localsigner.listener.ChooseOutputListener;
import ch.admin.localsigner.listener.CloseGuiListener;
import ch.admin.localsigner.listener.EditProfileListener;
import ch.admin.localsigner.listener.ExitListener;
import ch.admin.localsigner.listener.LoadProfileListener;
import ch.admin.localsigner.listener.NotarySignatureListener;
import ch.admin.localsigner.listener.SaveModificationListener;
import ch.admin.localsigner.listener.SignListener;
import ch.admin.localsigner.main.InputFile;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.LocalSignerCommandLine;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileOpenException;
import ch.admin.localsigner.notary.update.CantonAndDomainListUpdater;
import ch.admin.localsigner.update.UpdateQuery;
import ch.admin.localsigner.utils.Constants;
import ch.admin.localsigner.validation.PdfAnalyzer;

/**
 * This is the main GUI, showing all the different Widgets, adding the
 * corresponding listeners and offering accessor functions to the widgets
 * content.
 *
 * @author boris
 * @author $Author$
 * @version $Revision$
 */
public class MainGUI
{
  public static final char BACKSPACE_CHARACTER = '\b';

  public static final int DELETE_CHARACTER_INT_VALUE = 127;

  public enum GuiMode
  {
    sign, addSignatureField, addTextField, appendEmptyPage, deletePage, appendDocument, insertAttachment
  }

  private enum ViewerMode
  {
    ADOBE, INTERNAL
  }

  private static final Logger LOGGER = Logger.getLogger(MainGUI.class);

  // main shell and property GUI
  private Shell mainshell;

  private PropertiesGUI propertiesGui;

  private Font font;

  // PDF viewer components
  private Browser browser;

  private ViewerPanel pdfViewerPane;

  private Frame awtFrame;

  private Composite pdfDisplayArea;

  // input and output PDF
  private Text inputFileText;

  private Label inputFileVersion;

  private Text outputFileText;

  private Label outputFileVersion;

  // buttons
  private Button signOrSaveButton;

  private Button actionButton;

  private Button saveAsButton;

  private Button cancelButton;

  private Button signaturePropertiesButton;

  private Button viewerToggle;

  private Button notaryOnlyBtn;

  // signature profiles
  private Combo profileCombo;

  private Button previewCheckButton;

  // dynamic labels
  private Label sigPropsLabel;

  private Label outputFileLabel;

  private SideBar sidebar;

  // current GUI status
  private GuiMode mode = GuiMode.sign;

  // current loaded document
  private final PdfDoc document = new PdfDoc();

  // variables of command line interface
  private String cliViewer;

  private boolean cliQuitAfterSign;

  private String cliSignatureProfile;

  private MainMenu menu;

  private boolean showInputFile = true;

  private Label viewerLabel;

  private SashForm splitSashForm;

  private ViewerMode currentViewerMode;

  private static final Dimension DEFAULT_SIZE = new Dimension(1024, 750);

  private String appmode;

  public boolean isInteractiveMode() {
    return appmode.equalsIgnoreCase(LocalSignerCommandLine.APPMODE_INTERACTIVE);
  }

  public boolean isSubprocessMode()
  {
    return appmode.equalsIgnoreCase(LocalSignerCommandLine.APPMODE_SUBPROCESS);
  }

  /**
   * Builds up the GUI and initiates the event loop.
   *
   * @param display
   *          SWT display variable
   * @param cli
   *          Parameters from command line
   */
  public void showGui(final Display display, final LocalSignerCommandLine cli) {
    showGui(display,cli,null);
  }


   /**
   * Builds up the GUI and initiates the event loop.
   *
   * @param display
   *          SWT display variable
   * @param cli
   *          Parameters from command line
   * @param inputFile If user switches from experienced to simple or contrary the current document file, otherwise null
   *
   */
   void showGui(final Display display, final LocalSignerCommandLine cli, InputFile inputFile)
  {

    if (cli == null) {
        LOGGER.error("LocalSignerCommandLine is null -> setting " + LocalSignerCommandLine.APPMODE_INTERACTIVE);
        appmode = LocalSignerCommandLine.APPMODE_INTERACTIVE;
    } else {
      appmode = cli.getAppmode();
      document.setInteractiveMode(isInteractiveMode());
    }

    // create font
    font = new Font(display, "Arial", LocalSigner.appConfig.getFontSize(), SWT.NORMAL);

    // create the main GUI shell and set icon and title
    this.mainshell = new Shell(display);
    mainshell.forceActive();
    mainshell.forceFocus();

    mainshell.setImage(GuiHelper.loadAppIcon(display));
    mainshell.setText(LocalSigner.i18n("localSigner"));
    mainshell.addDisposeListener(new CloseGuiListener(this));

    // create the signature property window
    propertiesGui = new PropertiesGUI(this);

    // check application configuration. this configuration is done here
    // to be able to use graphical messages for the user
    boolean openConfig = false;
    File profileDir = new File(LocalSigner.appConfig.getUserProfileFolder());
    if (!profileDir.exists())
    {
      openConfig = true;
    }

    //Drag-and-Drop has to be disabled in Subprocess Appmode
    if (isInteractiveMode())
    {
      // the shell is a drop zone for drag-and-drop support.
      createDropZone();
    }

    // create the menu
    this.menu = new MainMenu(this, cli);

    if (cli != null)
    {
      this.cliViewer = cli.getViewer();
      this.cliQuitAfterSign = cli.isQuit();
      this.cliSignatureProfile = cli.getSignatureProfile();
    }

    // create contents
    if (!createMainGuiContent())
    {
      return;
    }

    // finally open the shell
    this.guessPosition();
    mainshell.open();

    // not working with Adobe Reader X
    // testPdfPlugin();

    if (!isInteractiveMode() && (inputFile!=null && getDocument()!=null)) {
      inputFile.setMainGui(this);
      getDocument().setInputFile(inputFile);
      try {
        setInputFile(getDocument().getInputFile().getInputFileName(), false,false);
      } catch (FileOpenException e)
      {
        FileExceptionHandler.showAppropriateErrorMessage(e, inputFile.getInputFileName());
      }
    }

    if (openConfig)
    {
      // show warning
      Message.error(mainshell, LocalSigner.i18n("errorProfilePath"));
      // open config GUI
      new ConfigurationGUI(this);
    }

    // set input and output files
    this.initStartFiles(cli,isInteractiveMode() && inputFile==null );

    if (inputFile==null || isInteractiveMode()) {
      // start update check
      this.updateCheck(display);
    }

    // list and canton update check (Zulassungsbestätigung from UPReg/ZulaB)
    this.updateCantonAndDomainList(display);

    // force active again
    mainshell.forceActive();

    if (System.getProperty("os.name").contains("Linux") && !mainshell.getMaximized())
    {
      // Problem with window size on Linux. Window is correct if resized
      // manually
      mainshell.setSize(mainshell.getSize().x, mainshell.getSize().y - 1);
      mainshell.setSize(mainshell.getSize().x, mainshell.getSize().y);
    }

    // endless loop
    try
    {
      while (!mainshell.isDisposed())
      {
        if (!display.readAndDispatch())
        {
          display.sleep();
        }
      }
    } catch (Exception e)
    {
      // NullPointer may happen on Mac when closing application
      LOGGER.debug("error in display loop", e);
    }
    display.dispose();
    System.exit(0);
  }

  /**
   * Builds up all components of the main GUI.
   *
   * @return false if an error happened during build up of GUI
   */
  private boolean createMainGuiContent()
  {
    // create a grid
    GridLayout gridLayout = new GridLayout(7, false);
    mainshell.setLayout(gridLayout);

    splitSashForm = new SashForm(mainshell, SWT.HORIZONTAL);
    splitSashForm.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, gridLayout.numColumns, 4));

    pdfDisplayArea = new Composite(splitSashForm, SWT.BORDER | SWT.EMBEDDED);
    pdfDisplayArea.setLayout(new FillLayout());

    if (LocalSigner.appConfig.getValidatorUrl() != null)
    {
      this.sidebar = new SideBar(splitSashForm, this);
      splitSashForm.setWeights(new int[] { 70, 30 });
      loadSidePanel(); // make it visible/invisible
    }

    currentViewerMode = ViewerMode.INTERNAL;
    if (LocalSignerCommandLine.VIEWER_ADOBE.equals(cliViewer))
    {
      currentViewerMode = ViewerMode.ADOBE;
    }
    else if (LocalSignerCommandLine.VIEWER_BUILTIN.equals(cliViewer))
    {
      currentViewerMode = ViewerMode.INTERNAL;
    }
    else if (LocalSigner.appConfig.getViewer() == PdfViewer.ADOBE)
    {
      currentViewerMode = ViewerMode.ADOBE;
    }
    boolean succ = this.insertPdfViewer(currentViewerMode);
    if (!succ)
    {
      return false;
    }

    // main composite
    Composite mainFunctionComposite = new Group(mainshell, SWT.NONE);
    mainFunctionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
        gridLayout.numColumns, 1));
    mainFunctionComposite.setLayout(new GridLayout(4, false));

    this.createMainGuiFields(mainFunctionComposite);
    this.createSignatureProfileSelection(mainFunctionComposite);

    Composite buttonComposite = new Composite(mainshell, SWT.NONE);
    buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, gridLayout.numColumns, 1));
    buttonComposite.setLayout(new GridLayout(9, false));
    this.createMainGuiButtons(buttonComposite);

    return true;
  }

  private void createMainGuiFields(final Composite mainFunctionComposite)
  {
    Label inputFileLabel = GuiHelper.label(mainFunctionComposite, SWT.NONE,
        LocalSigner.i18n("fileToSign"), font);
    inputFileLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    inputFileText = new Text(mainFunctionComposite, SWT.SINGLE | SWT.BORDER);
    inputFileText.setFont(font);
    inputFileText.setBackground(mainshell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    inputFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    inputFileText.setToolTipText(LocalSigner.i18n("tooltipInput"));
    inputFileText.addKeyListener(new KeyListener()
    {
      @Override
      public void keyPressed(KeyEvent ke)
      {
        // not used
      }

      private boolean isPrintableCharOrDelete(char c)
      {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (c == BACKSPACE_CHARACTER || c == (char) DELETE_CHARACTER_INT_VALUE ||
            (!Character.isISOControl(c)) // no control character
             && c != java.awt.event.KeyEvent.CHAR_UNDEFINED // not something undefined
             && block != null // not within a unicode block
             && block != Character.UnicodeBlock.SPECIALS); // not within the special chars block
      }

      @Override
      public void keyReleased(KeyEvent ke)
      {
        if (!isPrintableCharOrDelete(ke.character))
        {
          return;
        }

        File f = new File(inputFileText.getText());
        if (f.exists())
        {
          // load it
          try {
            setInputFileAndCheck(f.getAbsolutePath(), false);
            reloadInputFile(false);
          } catch (FileOpenException e)
          {
            FileExceptionHandler.showAppropriateErrorMessage(e, f.getPath());
          }
        }
      }
    });

    // This is a Empty Label. It is not supposed to show the Version of the File anymore.
    inputFileVersion = GuiHelper.label(mainFunctionComposite, SWT.NONE, "", font);
    GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd.widthHint = 80;
    inputFileVersion.setLayoutData(gd);

    Button inputFileSelector = new Button(mainFunctionComposite, SWT.PUSH);
    inputFileSelector.setFont(font);
    inputFileSelector.setText(LocalSigner.i18n("choose"));
    inputFileSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    // add a listener to the input file selector
    ChooseInputListener cil = new ChooseInputListener(this);
    inputFileSelector.addListener(SWT.Selection, cil);

    //input file is deactivated in subprocess-mode
    if (isSubprocessMode())
    {
      inputFileLabel.setVisible(false);
      inputFileSelector.setVisible(false);
      inputFileText.setVisible(false);
    }

    // output file selector
    outputFileLabel = GuiHelper.label(mainFunctionComposite, SWT.NONE,
        LocalSigner.i18n("outputFileName"), font);
    outputFileLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    outputFileText = new Text(mainFunctionComposite, SWT.SINGLE | SWT.BORDER);
    outputFileText.setFont(font);
    outputFileText.setBackground(mainshell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    outputFileText.setEditable(true);
    outputFileText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    outputFileText.setToolTipText(LocalSigner.i18n("tooltipOutput"));
    outputFileText.addModifyListener(new ModifyListener()
    {
      @Override
      public void modifyText(ModifyEvent e)
      {
        getDocument().setOutputFile(outputFileText.getText());
      }

    });

    // This is a Empty Label. It is not supposed to show the Version of the File anymore.
    outputFileVersion = GuiHelper.label(mainFunctionComposite, SWT.NONE, "", font);
    gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd.widthHint = 60;
    outputFileVersion.setLayoutData(gd);

    Button outputFileSelector = new Button(mainFunctionComposite, SWT.PUSH);
    outputFileSelector.setFont(font);
    outputFileSelector.setText(LocalSigner.i18n("choose"));
    outputFileSelector
        .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    // add a listener to the output file selector
    ChooseOutputListener col = new ChooseOutputListener(this);
    outputFileSelector.addListener(SWT.Selection, col);

    if (appmode.equalsIgnoreCase(LocalSignerCommandLine.APPMODE_SUBPROCESS))
    //if (isSubMode())
    {
      outputFileLabel.setVisible(false);
      outputFileSelector.setVisible(false);
      outputFileText.setVisible(false);
    }
  }

  private void createMainGuiButtons(final Composite buttonComposite)
  {
    createExitButton(buttonComposite);
    createViewerToggle(buttonComposite);
    createViewerLabel(buttonComposite);
    createAddSignatureFieldButton(buttonComposite);
    createCancelButton(buttonComposite);
    createSaveAsButton(buttonComposite);

    if (shouldAddNotarySignButton())
    {
      createAndAddNotarySignButton(buttonComposite);
    }

    createSignOrSaveButton(buttonComposite);
  }

  private void createSignOrSaveButton(final Composite buttonComposite)
  {
    signOrSaveButton = new Button(buttonComposite, SWT.PUSH);
    signOrSaveButton.setFont(font);
    // add same space so the button is wide enough for a later label change
    signOrSaveButton.setText(LocalSigner.i18n("sign"));
    signOrSaveButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
    signOrSaveButton.setSize(500, 10);
    SignListener sl = new SignListener(this);
    signOrSaveButton.addListener(SWT.Selection, sl);
    signOrSaveButton.setEnabled(false);
  }

  private void createSaveAsButton(final Composite buttonComposite)
  {
    saveAsButton = new Button(buttonComposite, SWT.PUSH);
    saveAsButton.setFont(font);
    saveAsButton.setText(LocalSigner.i18n("saveas"));
    saveAsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
    saveAsButton.addListener(SWT.Selection, new SaveModificationListener(this, true));
    setButtonVisibility(saveAsButton, false);
  }

  /**
   * This Method is used, so the Button is completely toggeled
   *
   * @param buttonToToggle buttonToToggle
   * @param visible visible
   */
  private void setButtonVisibility(Button buttonToToggle, boolean visible)
  {
    ((GridData) (buttonToToggle.getLayoutData())).exclude = !visible;
    buttonToToggle.setVisible(visible);
    buttonToToggle.getParent().getParent().redraw();
  }

  private void createCancelButton(final Composite buttonComposite)
  {
    cancelButton = new Button(buttonComposite, SWT.PUSH);
    cancelButton.setFont(font);
    cancelButton.setText(LocalSigner.i18n("cancel"));
    cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
    cancelButton.addListener(SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent(Event event)
      {
        // just delete tmp file and reload input file
        LOGGER.debug("cancel changes");
        getDocument().getInputFile().setTemporaryFile(null);
        switchMode(GuiMode.sign);
        reloadInputFile(false);
        boolean modify = isInputFileModifieable();
        menu.canModify(modify);
        menu.canDelete(modify && hasInputFileMoreThanOnePage());
      }

    });
    setButtonVisibility(cancelButton, false);
  }

  private void createAddSignatureFieldButton(final Composite buttonComposite)
  {
    // action button (add signature field)
    actionButton = new Button(buttonComposite, SWT.PUSH);
    actionButton.setFont(font);
    actionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, true, false, 1, 1));
    AddSigFieldListener sfl = new AddSigFieldListener(this);
    actionButton.addListener(SWT.Selection, sfl);
    actionButton.setVisible(false);
  }

  private void createViewerLabel(final Composite buttonComposite)
  {
    viewerLabel = new Label(buttonComposite, SWT.FILL);
    viewerLabel.setFont(font);
    viewerLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    if (this.isInternalViewer())
    {
      viewerLabel.setText(LocalSigner.i18n("internalViewerMode"));
    }
    else
    {
      viewerLabel.setText(LocalSigner.i18n("adobeViewerMode"));
    }
    viewerLabel.setVisible(!LocalSigner.appConfig.getGuiViewMode().isMinimalMode());
  }

  private void createViewerToggle(final Composite buttonComposite)
  {
    viewerToggle = new Button(buttonComposite, SWT.PUSH);
    viewerToggle.setFont(font);
    viewerToggle.setText(LocalSigner.i18n("viewerToggle"));
    viewerToggle.setEnabled(false);
    viewerToggle.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
    viewerToggle.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        switchPdfViewer();
      }

    });

    viewerToggle.setVisible(!LocalSigner.appConfig.getGuiViewMode().isMinimalMode());
  }

  private void createExitButton(final Composite buttonComposite)
  {
    Button exit = new Button(buttonComposite, SWT.PUSH);
    exit.setFont(font);
    exit.setText(LocalSigner.i18n("quit"));
    exit.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
    ExitListener el = new ExitListener(mainshell);
    exit.addListener(SWT.Selection, el);
  }

  private boolean shouldAddNotarySignButton()
  {
    return LocalSigner.appConfig.isFunknachweisAktiv() && LocalSigner.appConfig.getGuiViewMode().isProfessionalMode();
  }

  private void createAndAddNotarySignButton(final Composite buttonComposite)
  {
    notaryOnlyBtn = new Button(buttonComposite, SWT.PUSH | SWT.RIGHT);
    notaryOnlyBtn.setFont(font);
    notaryOnlyBtn.setEnabled(false);
    notaryOnlyBtn.setText(LocalSigner.i18n("signOnlyNotary"));

    NotarySignatureListener listener = new NotarySignatureListener(this);
    notaryOnlyBtn.addListener(SWT.Selection, listener);
  }

  private boolean insertPdfViewer(final ViewerMode viewerMode)
  {
    if (viewerMode == ViewerMode.INTERNAL)
    {
      try
      {
        // insert internal viewer
        awtFrame = SWT_AWT.new_Frame(pdfDisplayArea);
        pdfViewerPane = ViewerPanelFactory.createViewerPanel(this);
        awtFrame.add(pdfViewerPane);
        return true;
      } catch (SWTError e)
      {
        LOGGER.warn("Could not embed pdf viewer.", e);
        return false;
      }
    }
    else
    {
      try
      {
        // insert adobe viewer
        browser = new Browser(pdfDisplayArea, SWT.NONE);
        return true;
      } catch (SWTError e)
      {
        Message.error(mainshell, LocalSigner.i18n("noAcrobat"));
        LOGGER.warn("Could not instantiate Browser " ,e);
        return false;
      }
    }
  }

  private void switchPdfViewer()
  {
    // dispose old viewer
    if (isInternalViewer())
    {
      LOGGER.debug("switch from BFO to Adobe");
      destroyAwtFrame();
      if (!showAdobeViewer())
      {
        return;
      }
    }
    else
    {
      LOGGER.debug("switch from Adobe to BFO");
      destroySwtBrowser();
      if (!showBfoViewer())
      {
        return;
      }
    }

    // position the new widget.
    pdfDisplayArea.layout();
    LOGGER.debug("switch viewer done");

    if (showInputFile)
    {
      this.reloadInputFile(false);
      // update menu
      this.menu.canModify(isInputFileModifieable());
    }
    else
    {
      this.reloadOutputFile(false);
    }
  }

  public boolean isInputFile(String filePath) {
    return getDocument()!= null && getDocument().getInputFile()!=null &&  getDocument().getInputFile().isInputFile(filePath);
  }

  private boolean isInputFileModifieable()
  {
    return getDocument().getInputFile().canModify();
  }

  private boolean showBfoViewer()
  {
    boolean couldEmbedViewer = this.insertPdfViewer(ViewerMode.INTERNAL);
    if (!couldEmbedViewer)
    {
      return false;
    }
    viewerLabel.setText(LocalSigner.i18n("internalViewerMode"));
    mainshell.layout();
    if (previewCheckButton != null)
    {
      this.previewCheckButton.setVisible(false);
      this.previewCheckButton.setSelection(true);
    }

    updatePdfAStatusOnSwitch();
    return true;
  }

  private void updatePdfAStatusOnSwitch()
  {
    if (isInternalViewer())
    {
      if (isInputFileShown())
      {
        getPdfViewerPane().updateStatus(getDocument().getInputFile().getAnalyzer());
      }
      else if (isOutputFileShown())
      {
        try
        {
          getPdfViewerPane().updateStatus(new PdfAnalyzer(getOutputFileData()));
        } catch (IOException e)
        {
          LOGGER.error(e);
          Message.error(mainshell, "Cannot analyze outputfile");
        }
      }
    }
  }

  private boolean isOutputFileShown()
  {
    return !showInputFile;
  }

  private boolean isInputFileShown()
  {
    return getInputFile() != null && showInputFile;
  }

  private void destroySwtBrowser()
  {
    currentViewerMode = ViewerMode.INTERNAL;
    if (browser != null)
    {
      browser.dispose();
      browser = null;
    }
  }

  private boolean showAdobeViewer()
  {
    boolean couldEmbedViewer = this.insertPdfViewer(ViewerMode.ADOBE);
    if (!couldEmbedViewer)
    {
      return false;
    }
    viewerLabel.setText(LocalSigner.i18n("adobeViewerMode"));
    mainshell.layout();
    if (previewCheckButton != null)
    {
      this.previewCheckButton.setVisible(true);
      if (this.getDocument().getInputFile().isSigned())
      {
        this.previewCheckButton.setSelection(false);
      }
      else
      {
        this.previewCheckButton.setSelection(true);
      }
    }
    return true;
  }

  /**
   * Note: because of the mixing of the swt / awt rendering threads, it is not
   * reliable to check for the state of the awt component. Instead we use a flag
   * to immediatly see the (future) state of the component.
   */
  private void destroyAwtFrame()
  {
    currentViewerMode = ViewerMode.ADOBE;
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        if (awtFrame != null)
        {
          awtFrame.dispose();
          awtFrame = null;
        }
      }
    });
  }

  /**
   * Creates the drop listener for this shell and allows PDF documents to be
   * dragged and dropped onto the shell. The dropped files are then filled into
   * the 'inputPath' field. This method does almost the same as the
   * ChooseInputListener.
   */
  private void createDropZone()
  {
    DropTarget dt = new DropTarget(mainshell, DND.DROP_DEFAULT | DND.DROP_MOVE);
    dt.setTransfer(new Transfer[]
    {
      FileTransfer.getInstance()
    });

    dt.addDropListener(new DropTargetAdapter()
    {
      @Override
      public void drop(DropTargetEvent event)
      {
        documentDropped(event);
      }

    });
  }

  private void documentDropped(DropTargetEvent event)
  {
    FileTransfer ft = FileTransfer.getInstance();
    if (ft.isSupportedType(event.currentDataType))
    {
      String[] fileList = (String[]) event.data;
      if (fileList == null)
      {
        return;
      }
      String filename = fileList[0];
      try {
        setInputFileAndCheck(filename, true);
      } catch (FileOpenException e) {
        FileExceptionHandler.showAppropriateErrorMessage(e, filename);
      }
    }
  }

  /**
   * Refresh the GUI and show the input file.
   *
   * @param autoswitchViewer
   *          true to switch between internal and Adobe viewer
   */
  public void reloadInputFile(final boolean autoswitchViewer)
  {
    byte[] fileData;
    boolean preview = isPreviewSignature();
    try
    {

      fileData = this.document.getInputFile().getFileToDisplay(preview);
    } catch (BadPasswordException bpe)
    {
      LOGGER.debug("PDF is read protected");
      Message.warning(mainshell, LocalSigner.i18n("pdfEncrypted"));
      return;
    } catch (Exception e)
    {
      LOGGER.debug("No input file to reload");
      return;
    }

    showInputFile = true;
    LOGGER.debug("reloadInputFile " + this.document.getInputFile().getInputFileName() + " (autoswitch: " + autoswitchViewer
        + ")");
    // autoswitch to internal viewer
    if (!isInternalViewer() && autoswitchViewer && areBothViewerModesEnabled())
    {
      switchPdfViewer();
    }

    showFile(fileData);
    this.changeInputButtons();
  }

  private boolean isPreviewSignature()
  {
    boolean preview = true;

    if (previewCheckButton != null)
    {
      if (isSignatureInvisible())
      {
        previewCheckButton.setSelection(false);
        previewCheckButton.setVisible(false);
      }

      preview = previewCheckButton.getSelection();
      if (this.isInternalViewer())
      {
        preview = true;
      }
    }
    return preview;
  }

  private void changeInputButtons()
  {
    actionButton.setVisible(mode == GuiMode.addSignatureField
        || mode == GuiMode.addTextField);

    setButtonVisibility(saveAsButton, mode != GuiMode.sign);
    setButtonVisibility(cancelButton, mode != GuiMode.sign);

    signOrSaveButton.setVisible(true);

    if (mode == GuiMode.sign || !getDocument().getSigFields().isEmpty())
    {
      signOrSaveButton.setEnabled(true);
      saveAsButton.setEnabled(true);
    }
    else
    {
      signOrSaveButton.setEnabled(getDocument().getInputFile().isTemporaryFile());
      saveAsButton.setEnabled(getDocument().getInputFile().isTemporaryFile());
    }

    viewerToggle.setEnabled(true);

    // default button
    Button defaultButton = signOrSaveButton;
    if (actionButton.getVisible())
    {
      defaultButton = actionButton;
    }
    mainshell.setDefaultButton(defaultButton);
  }

  /**
   * Refresh the GUI and show the output file.
   *
   * @param autoswitchViewer
   *          true to switch between internal and Adobe viewer
   */
  private void reloadOutputFile(final boolean autoswitchViewer)
  {
    showInputFile = false;
    LOGGER.debug("reloadOutputFile (autoswitch: " + autoswitchViewer + ")");
    // autoswitch to internal viewer
    if (isInternalViewer() && autoswitchViewer && areBothViewerModesEnabled())
    {
      switchPdfViewer();
    }

    String filename = this.getOutputFile();

    showFile(filename);
    outputFileVersion.setText("");

    // change buttons
    actionButton.setVisible(false);
    setButtonVisibility(saveAsButton, false);
    setButtonVisibility(cancelButton, false);
    signOrSaveButton.setVisible(true);
    signOrSaveButton.setEnabled(false);
    viewerToggle.setEnabled(true);
  }

  private PdfAnalyzer getPDFAnalyzer(String fileName) throws IOException {
    if (isInputFile(fileName)) {
      return new PdfAnalyzer(getDocument().getInputFile().getFileToDisplay(false));
    } else {
      return new PdfAnalyzer(fileName);
    }
  }

  private boolean areBothViewerModesEnabled()
  {
    return LocalSigner.appConfig.getViewer() == PdfViewer.DUAL;
  }

  /**
   * Shows the given file in the browser window.
   *
   * @param fileData
   *          Address of the file to show
   */
  private void showFile( byte[] fileData)
  {
    if (fileData==null)
    {
      LOGGER.debug("No URL to display");
      return;
    }

    showFileThread(null, fileData);
  }


  /**
   * Shows the given file in the browser window.
   *
   * @param url
   *          Address of the file to show
   */
  private void showFile(final String url)
  {
    LOGGER.debug("show file: " + url);
    if (StringUtils.isBlank(url))
    {
      LOGGER.debug("No URL to display");
      return;
    }

    showFileThread(url, null);
  }

  private boolean inputFileHasOrigFile()
  {
    return document.getInputFile() != null
        && document.getInputFile().getOriginalFile() != null;
  }


private void showFileThread(final String fileName, final byte[] fileData)  {
    if (isInternalViewer())
    {
      if (getInputFile() != null)
      {
        getPdfViewerPane().showInfoPanel();
      }

      // save current page
      getPdfViewerPane().setDisableDrawing(!canDraw());
      if (isSignatureInvisible() && inputFileHasOrigFile())
      {
        // invisible signature
        getPdfViewerPane().setDisableDrawing(true);
        // show original file
        getPdfViewerPane().openFile(document.getInputFile().getOriginalFile());
      }
      else
      {
        boolean preview = isPreviewSignature();
        int pos = getPdfViewerPane().getScrollPosition();
        byte[] fileDataToUse = null;
        if (fileData==null) {
          if (isInputFile(fileName)) {
            try {
              fileDataToUse = getDocument().getInputFile().getFileToDisplay(preview);
            } catch (BadPasswordException e) {
              //false => no exception
            }
          } else {
            try {
              fileDataToUse = FileUtils.readFileToByteArray(new File(fileName));
            } catch (IOException e) {
              LOGGER.error("Can't read file data for " + fileName, e);
            }
          }
        } else {
          fileDataToUse = fileData;
        }
        getPdfViewerPane().openFile(fileDataToUse);
        waitForSignPanel();
        getPdfViewerPane().setScrollPosition(pos);
      }
      pdfDisplayArea.setFocus();
    }
    else
    {
      if (browser != null && !browser.isDisposed())
      {
        // show file in Adobe viewer
        try {
          File tempFile;
          if (fileData==null) {
            tempFile = File.createTempFile(fileName, "preview");
            FileUtils.copyFile(new File(fileName), tempFile);
          } else {
            tempFile = File.createTempFile(getDocument().proposeOutputNameIntermediate(), "preview");
            FileUtils.writeByteArrayToFile(tempFile,fileData);
          }
          browser.setUrl(tempFile.getAbsolutePath());
          browser.setFocus();
        } catch (IOException e) {
          LOGGER.error("showFileThread: cannot create temp file for browser",e);
        }
      }
    }
  }


  private void waitForSignPanel()
  {
    try
    {
      Thread.sleep(500);
    } catch (InterruptedException e)
    {
      LOGGER
          .warn(
              "Could not sleep 500msec after opening file in bfo viewer. Ignore and continue.",
              e);
    }
  }

  private boolean isSignatureInvisible()
  {
    return !propertiesGui.isVisibleSignature();
  }

  /**
   * Show static startup help file. Show English file if not found for current
   * language.
   */
  private void showStartupFile()
  {
    String lang = LocalSigner.appConfig.getLanguageEvaluatingAuto();

    String viewer = this.isInternalViewer() ? "internal" : "acro";
    String filename = "Start_" + viewer + "_" + lang + ".pdf";

    // check existence before modifying resource path
    File langFile = new File(BasePath.getEGovResourcesPath() + filename).getAbsoluteFile();
    boolean exists = langFile.exists();

    String filepath;
    if (this.isInternalViewer())
    {
      filepath = langFile.getAbsolutePath();
    }
    else
    {
      String userDir = System.getProperties().getProperty("user.dir");
      filepath = "file://" + userDir + "/" + BasePath.getEGovResourcesPath() + filename;
    }
    if (exists)
    {
      LOGGER.debug("Showing home file: " + filepath);
      this.showFile(filepath);
    }
    else
    {
      // show English file if here
      filepath = filepath.replaceAll(lang + ".pdf", "en.pdf");
      filename = filename.replaceAll(lang + ".pdf", "en.pdf");
      LOGGER.debug("Showing file: " + filepath);
      File fallback = new File(BasePath.getEGovResourcesPath() + filename).getAbsoluteFile();
      if (fallback.exists())
      {
        this.showFile(fallback.getAbsolutePath());
      }
    }
  }

  /**
   * Switch the GUI mode (from menu bar).
   *
   * @param guimode
   *          selected mode
   */
  public void switchMode(final GuiMode guimode)
  {
    if (this.mode == guimode)
    {
      return;
    }

    LOGGER.debug("switch mode to " + guimode);

    // first disable all
    actionButton.setVisible(false);
    signaturePropertiesButton.setVisible(false);
    setButtonVisibility(saveAsButton, false);
    setButtonVisibility(cancelButton, false);

    sigPropsLabel.setVisible(false);
    profileCombo.setVisible(false);

    // set mode
    this.mode = guimode;

    switch (guimode)
    {
    case appendEmptyPage:
      this.switchEmpty();
      break;
    case addSignatureField:
      this.switchSigField();
      break;
    case addTextField:
      this.switchTextField();
      break;
    case appendDocument:
      this.switchAppend();
      break;
    case deletePage:
      this.switchDelete();
      break;
    case insertAttachment:
      this.switchAttachment();
      break;
    case sign:
      this.switchSign();
      break;
    }

    // update menu selection
    this.reloadInputFile(true);
    menu.switchMode(guimode);

    // update layout (button size change
    actionButton.getParent().layout();
    mainshell.layout();
  }

  private void evalFNButtonForInputFile()
  {
    InputFile inputFile = getDocument().getInputFile();
    if (notaryOnlyBtn != null)
    {
      if (inputFile != null && inputFile.isSigned())
      {
        notaryOnlyBtn.setEnabled(true);
      }
      else
      {
        notaryOnlyBtn.setEnabled(false);
      }
    }
  }

  private void evalFNButtonAfterSign()
  {
    if (notaryOnlyBtn != null)
    {
      notaryOnlyBtn.setEnabled(true);
    }
  }

  private void switchEmpty()
  {
    switchToDefaults();
  }

  private void switchSigField()
  {
    actionButton.setVisible(true);
    actionButton.setText(LocalSigner.i18n("sigFieldCreate"));
    signOrSaveButton.setText(LocalSigner.i18n("save"));
    this.setOutputFile(getDocument().proposeOutputNameIntermediate(), false);
    outputFileLabel.setText(LocalSigner.i18n("outputFileNameSigField"));

  }

  private void switchTextField()
  {
    actionButton.setVisible(true);
    actionButton.setText(LocalSigner.i18n("addTextField"));
    signOrSaveButton.setEnabled(true);
    signOrSaveButton.setText(LocalSigner.i18n("save"));
    this.setOutputFile(getDocument().proposeOutputNameIntermediate(), false);
    outputFileLabel.setText(LocalSigner.i18n("outputFileNameSigField"));
  }

  private void switchAppend()
  {
    switchToDefaults();
  }

  private void switchDelete()
  {
    switchToDefaults();
  }

  private void switchAttachment()
  {
    switchToDefaults();
  }

  private void switchSign()
  {
    signOrSaveButton.setText(LocalSigner.i18n("sign"));
    this.setOutputFile(getDocument().proposeOutputNameFinal(), false);
    outputFileLabel.setText(LocalSigner.i18n("outputFileName"));
    sigPropsLabel.setVisible(true);
    profileCombo.setVisible(true);
    signaturePropertiesButton.setVisible(true);
  }

  private void switchToDefaults() {
    setButtonVisibility(saveAsButton, true);
    setButtonVisibility(cancelButton, true);
    signOrSaveButton.setText(LocalSigner.i18n("save"));
    this.setOutputFile(getDocument().proposeOutputNameIntermediate(), false);
    outputFileLabel.setText(LocalSigner.i18n("outputFileNameSigField"));
  }

  /**
   * Create GUI composite which displays the profile selector.
   *
   * @param composite the Composite
   */
  private void createSignatureProfileSelection(final Composite composite)
  {
    LOGGER.debug("GUI mode: " + LocalSigner.appConfig.getGuiViewMode().getConfigurationValue());

    EditProfileListener editListener = new EditProfileListener(propertiesGui, mainshell);

    if (LocalSigner.appConfig.getGuiViewMode().isMinimalMode())
    {
      // load GUI components as usual
      signaturePropertiesButton = new Button(composite, SWT.PUSH);
      signaturePropertiesButton.setFont(font);
      signaturePropertiesButton.setText(LocalSigner.i18n("edit"));
      signaturePropertiesButton.addSelectionListener(editListener);

      profileCombo = new Combo(composite, SWT.READ_ONLY);
      profileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

      this.loadProfileCombo();
      profileCombo.select(0);

      // fill combo with profiles
      LoadProfileListener lpl = new LoadProfileListener(this);
      profileCombo.addListener(SWT.Selection, lpl);
      profileCombo.notifyListeners(SWT.Selection, null);

      // ...and hide them
      profileCombo.setVisible(false);
      signaturePropertiesButton.setVisible(false);
      return;
    }

    // Label: signatureinstellungen
    sigPropsLabel = GuiHelper.label(composite, SWT.NONE,
        LocalSigner.i18n("signatureProperties"), font);
    sigPropsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    if (LocalSigner.appConfig.getGuiViewMode().isSimpleMode())
    {
      // only show edit button for default profile
      signaturePropertiesButton = new Button(composite, SWT.PUSH);
      signaturePropertiesButton.setFont(font);
      // default caption for default profile
      signaturePropertiesButton.setText(LocalSigner.i18n("edit"));
      signaturePropertiesButton.addSelectionListener(editListener);

      // add an invisible profile combo
      profileCombo = new Combo(composite, SWT.READ_ONLY);
      profileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
      profileCombo.setVisible(false);
      this.loadProfileCombo();
      profileCombo.select(0);

      LoadProfileListener lpl = new LoadProfileListener(this);
      profileCombo.addListener(SWT.Selection, lpl);

      profileCombo.notifyListeners(SWT.Selection, null);

      return;
    }

    // Combo: Profilauswahl
    profileCombo = new Combo(composite, SWT.READ_ONLY);
    LoadProfileListener lpl = new LoadProfileListener(this);
    profileCombo.addListener(SWT.Selection, lpl);
    profileCombo.setFont(font);
    profileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

    // preview button
    previewCheckButton = new Button(composite, SWT.CHECK);
    previewCheckButton.setFont(font);
    previewCheckButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    previewCheckButton.setText(LocalSigner.i18n("previewSigPos"));
    previewCheckButton.setSelection(this.isInternalViewer());
    previewCheckButton.setVisible(!this.isInternalViewer());
    previewCheckButton.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetDefaultSelected(SelectionEvent e)
      {
        widgetSelected(e);
      }

      @Override
      public void widgetSelected(SelectionEvent e)
      {
        if (showInputFile)
        {
          reloadInputFile(false);
        }
      }

    });

    // instantiate button before selecting the last used profile
    // as its caption is set by 'selectLastActiveProfile'
    signaturePropertiesButton = new Button(composite, SWT.PUSH);
    signaturePropertiesButton.setFont(font);
    // default caption for default profile
    signaturePropertiesButton.setText(LocalSigner.i18n("changeSigProps"));
    signaturePropertiesButton.addSelectionListener(editListener);

    if (cliSignatureProfile == null)
    {
      // select last used profile
      try
      {
        Properties props = new Properties();
        props.load(new FileInputStream(LocalSigner.appConfig.getUserInternalConfig()));
        final String lastUsed = props.getProperty(Config.LAST_USED_PROFILE);
        LOGGER.debug("last used profile is: "+lastUsed);

        this.loadProfileCombo();
        this.setSelectedProfile(lastUsed);
      } catch (IOException e)
      {
        // default profile already selected as this method is only called
        // once after startup of the application
        LOGGER.warn("could not find internalconfig.properties file");

        // if this is a fresh installation, the internatlconfig.properties is not yet present.
        this.loadProfileCombo();
        profileCombo.select(0);
        profileCombo.notifyListeners(SWT.Selection, null);
      }
    } else
    {
      this.loadProfileCombo();
    }
  }

  /**
   * Initializes the combo control displaying all profiles stored in the
   * configured profile directory. Then the internal config file is read for the
   * last used profile. The default profile is selected. If the internal
   * configuration file can not be found (normally users do not change...) the
   * default profile is selected.
   */
  public void loadProfileCombo()
  {
    // delete old content
    profileCombo.setItems(new String[0]);

    List<Profile> profiles = ProfileCollector.loadProfileCombo(cliSignatureProfile);
    int i = 0;
    for (Profile profile : profiles)
    {
      profileCombo.add(profile.getName());
      profileCombo.setData(Integer.toString(i), profile);
      i++;
    }

    profileCombo.notifyListeners(SWT.Selection, null);

  }

  /**
   * Sets the given profile and updates the GUI captions dependent on this type.
   * For now the following GUI elements have to be updated:
   * <ul>
   * <li>the signature properties button</li>
   * </ul>
   *
   * @param selectProfile
   *          Name of profile
   */
  public void setSelectedProfile(final String selectProfile)
  {
    // select the desired profile
    String[] items = profileCombo.getItems();
    for (int i = 0; i < items.length; i++)
    {
      if (items[i].equals(selectProfile))
      {
        profileCombo.select(i);
        profileCombo.notifyListeners(SWT.Selection, null);
        LOGGER.debug("selected profile " + items[i]);
        return;
      }
    }
    LOGGER.debug("profile to select not found: " + selectProfile);
    if (profileCombo.getItems() != null && profileCombo.getItems().length > 0)
    {
      LOGGER.debug("select first profile in list");
      profileCombo.select(0);
    }

    // update the signature properties button
    if (getSelectedProfile().isDefaultType())
    {
      this.signaturePropertiesButton.setText(LocalSigner.i18n("changeSigProps"));
    }
    else
    {
      this.signaturePropertiesButton.setText(LocalSigner.i18n("changeProfile"));
    }

    if (LocalSigner.appConfig.getGuiViewMode().isSimpleMode())
    {
      this.signaturePropertiesButton.setText(LocalSigner.i18n("edit"));
    }

    // adapt size to different capions
    this.signaturePropertiesButton.setSize(signaturePropertiesButton.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    // and relayout the entouring composite
    mainshell.layout();

    // update selected description
    int position = profileCombo.getSelectionIndex();
    String description = getSelectedProfile().getDescription();
    profileCombo.setItem(position, description);
    profileCombo.notifyListeners(SWT.Selection, null);
  }

  /**
   * Getter for main shell.
   *
   * @return main shell
   */
  public Shell getMainshell()
  {
    return mainshell;
  }

  public PropertiesGUI getPropertiesGui()
  {
    return propertiesGui;
  }

  public Profile getSelectedProfile()
  {
    return (Profile) profileCombo.getData(Integer.toString( profileCombo.getSelectionIndex()));
  }

  public Font getFont()
  {
    return font;
  }

  boolean isQuitAfterSigning()
  {
    return cliQuitAfterSign;
  }

  public boolean isInternalViewer()
  {
    return currentViewerMode == ViewerMode.INTERNAL;
  }

  public PdfDoc getDocument()
  {
    return this.document;
  }

  /**
   * Helper method to get input file
   */
  public byte[] getInputFile()
  {
    if (this.getDocument().getInputFile() == null)
    {
      return null;
    }
    return this.getDocument().getInputFile().getFileToSign();
  }


  /**
   * Helper method to get input file
   */
  public String getInputFileName()
  {
    if (this.getDocument().getInputFile() == null)
    {
      return null;
    }
    return this.getDocument().getInputFile().getInputFileName();
  }

  public void setInputFileAndCheck(final String input, final boolean reload) throws FileOpenException {
    setInputFile(input,reload,true);
  }
  /**
   * Helper method to set input file
   *
   * @param input
   *          File to load
   * @param reload
   *          true if viewer needs a reload
   */
  private void setInputFile(final String input, final boolean reload, boolean check) throws FileOpenException
  {
    LOGGER.debug("loading file " + input);

    if (!isPDF(input))
    {
      // not a PDF document
      Message.warning(mainshell, LocalSigner.i18n("errorNotPdf"));
      return;
    }

    // switch to sign mode
    this.switchMode(GuiMode.sign);
    InputFile inputFile;
    if (getDocument()!=null && getDocument().getInputFile()!=null) {
      inputFile = getDocument().getInputFile();
    } else {
      inputFile = new InputFile(this);
    }
      inputFile.setOriginalFile(input, check);

    loadSidePanel();

    this.getDocument().setInputFile(inputFile);
    String outputFile = getDocument().proposeOutputNameFinal();
    this.getDocument().setOutputFile(outputFile);
    this.getDocument().setSigFields(null);

    // if there is no output directory specified just store
    // the output file.
    // If there is an output directory specified, the output
    // file path is adapted

    if (propertiesGui.getOutputDir().equals(StringUtils.EMPTY))
    {
      this.setOutputFile(outputFile, false);
    }
    else
    {
      // add a file separator if necessary
      String outputdir = propertiesGui.getOutputDir();
      if (!outputdir.endsWith(File.separator))
      {
        outputdir = outputdir + File.separator;
      }
      int index = input.lastIndexOf(File.separator);
      this.setOutputFile(outputdir + outputFile.substring(index + 1), false);
    }

    final String attachment = propertiesGui.getPdfAttachment();
    getDocument().getInputFile().updateAttachment(attachment);

    // update gui
    this.inputFileText.setText(input);

    if (sidebar != null && LocalSigner.appConfig.isSidePanelActive())
      {
        try
        {
          sidebar.update(inputFile.getAnalyzer());
        } catch (Exception e)
        {
          LOGGER.error("Error while updating sidebar", e);
        }
    }

    if (pdfViewerPane != null && inputFile.getAnalyzer() != null)
    {
      pdfViewerPane.updateStatus(inputFile.getAnalyzer());
    }

    boolean modify = isInputFileModifieable();
    this.menu.canModify(modify);
    this.menu.canDelete(modify && hasInputFileMoreThanOnePage());

    evalFNButtonForInputFile();

    if (reload)
    {
      this.reloadInputFile(true);
    }
  }

  private boolean hasInputFileMoreThanOnePage()
  {
    return getDocument().getInputFile().getPages() > 1;
  }

  private boolean isPDF(final String input)
  {
    return input.toLowerCase().endsWith(Constants.PDF_FILE_SUFFIX);
  }

   /**
   * Helper method to get output file
   */
  public String getOutputFile()
  {
    return this.getDocument().getOutputFile();
  }
   /**
   * Helper method to get output file
   */
  private byte[] getOutputFileData() throws IOException {
    return this.getDocument().getOutputFileData();
  }

  /**
   * Helper method to set output file
   *
   * @param output
   *          File to load
   * @param reload
   *          true if viewer needs a reload
   */
  public void setOutputFile(final String output, final boolean reload)
  {
    // update gui
    this.outputFileText.setText(output);
    this.outputFileVersion.setText("");
    this.getDocument().setOutputFile(output);

    if (reload && sidebar != null)
    {
      try
      {
        PdfAnalyzer analyzer = getPDFAnalyzer(output);
        getPdfViewerPane().updateStatus(analyzer);

        if (LocalSigner.appConfig.isSidePanelActive())
        {
            sidebar.update(analyzer);
        }
      } catch (Exception e)
      {
        LOGGER.error("Error while updating sidebar", e);
      }
    }

    if (reload)
    {
      this.menu.canDelete(false);
      this.menu.canModify(false);
      this.menu.canSign(false);

      this.reloadOutputFile(true);
    }
  }

  public GuiMode getGuiMode()
  {
    return mode;
  }

  /**
   * Check if user can draw a box with the mouse in the current mode.
   *
   * @return true if draw is enabled
   */
  public boolean canDraw()
  {
    if (mode == GuiMode.appendEmptyPage || mode == GuiMode.appendDocument
        || mode == GuiMode.deletePage || mode == GuiMode.insertAttachment)
    {
      return false;
    }

    if (document.getInputFile() == null)
    {
      return false;
    }

    if (document.getInputFile().hasBlankSignatures())
    {
      return false;
    }

    return !isOutputFileShown();
  }

  public MainMenu getMenu()
  {
    return menu;
  }

  private void updateCheck(Display display)
  {
    final String url = LocalSigner.appConfig.getUpdateCheckUrl();
    if (StringUtils.isBlank(url))
    {
      LOGGER.debug("Update-URL not set, skipping update check");
      return;
    }

    display.asyncExec(new Runnable()
    {

      @Override
      public void run()
      {
        new UpdateQuery(MainGUI.this, url).run();
      }
    });
  }

  private void updateCantonAndDomainList(Display display)
  {
    if (LocalSigner.appConfig.isFunknachweisAktiv())
    {
      final String url = LocalSigner.appConfig.getUpdateCantonAndDomainListUrl();
      if (StringUtils.isBlank(url))
      {
        LOGGER.debug("URL for canton and domain list update not set, skipping update check");
        return;
      }

      display.asyncExec(new Runnable()
      {

        @Override
        public void run()
        {
          new CantonAndDomainListUpdater(MainGUI.this, url).run();
        }
      });
    } else
    {
      LOGGER.info("Extension for UPReg is not active. Not checking for canton and domain list update.");
    }
  }

  /**
   * Refresh the PDF viewer.
   */
  public void refreshViewer()
  {
    try
    {
      if (isInternalViewer() && !pdfDisplayArea.isDisposed())
      {

        LOGGER.debug("refresh frame");
        pdfDisplayArea.setVisible(false);
        pdfDisplayArea.setVisible(true);
        pdfDisplayArea.setFocus();

      }
    } catch (Exception e)
    {
      LOGGER.debug("Cannot refresh view", e);
    }
  }

  public Browser getBrowser()
  {
    return browser;
  }

  public ViewerPanel getPdfViewerPane()
  {
    return pdfViewerPane;
  }

  private void guessPosition()
  {
    Rectangle position = GuiHelper.getScreenPosition(mainshell, DEFAULT_SIZE.width,
        DEFAULT_SIZE.height);
    String pos = LocalSigner.appConfig.getWindowPosition();
    if (StringUtils.isNotBlank(pos))
    {
      String[] split = pos.split("/");
      if (split.length == 4)
      {
        position = new Rectangle(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            Integer.parseInt(split[2]), Integer.parseInt(split[3]));
      }
    }

    mainshell.setBounds(position);
    boolean fullscreen = GuiHelper.needsMaximize(mainshell);
    if ("max".equals(pos))
    {
      // maximized from last use
      fullscreen = true;
    }
    mainshell.setMaximized(fullscreen);
  }

  private void initStartFiles(LocalSignerCommandLine cli,boolean showStartUpFile)
  {
    String input = null;
    if (cli != null)
    {
      input = cli.getInput();
    }
    if (input == null)
    { if ( showStartUpFile)
      {
        this.showStartupFile();
      }
    }
    else
    {
      try {
        this.setInputFileAndCheck(input, false);
        this.reloadInputFile(false);
      } catch (FileOpenException e) {
        FileExceptionHandler.showAppropriateErrorMessage(e, input);
      }
    }

    String output = null;
    if (cli != null)
    {
      output = cli.getOutput();
    }
    if (output != null)
    {
      this.setOutputFile(output, false);
    }
  }

  public SideBar getSidebar()
  {
    return sidebar;
  }

  public void loadSidePanel()
  {
    if (LocalSigner.appConfig.isSidePanelActive())
    {
      showValidationPanel();
    } else
    {
      hideValidationPanel();
    }
  }

  private void showValidationPanel()
  {
    splitSashForm.setMaximizedControl(null);
  }

  private void hideValidationPanel()
  {
    splitSashForm.setMaximizedControl(pdfDisplayArea);
  }

  /**
   * show the signed PDF in the browser if user requested 'show signed
   * document'.
   *
   * @param name filename
   */
  public void showSignedFile(final String name)
  {
    if (!isInteractiveMode()) {
      getMenu().disableMenueMode();
    }
    if (getPropertiesGui().isDisplaySignedDocument())
    {
      if (isInteractiveMode()) {
        document.getInputFile().unlockFile();
      }
      setOutputFile(name, true);
      // if signature is not visible inform with message box
      if (!getPropertiesGui().isVisibleSignature())
      {
        // inform the user with a popup in case of invisible signature
        Message.warning(getMainshell(), LocalSigner.i18n("signOkShort"),
            LocalSigner.i18n("signOk"));
      }
    }
    else
    {
      // we do not update the view but inform the user with a popup
      Message.warning(getMainshell(), LocalSigner.i18n("signOkShort"),
          LocalSigner.i18n("signOk"));
    }

    evalFNButtonAfterSign();
  }

  public void closeInputFile()
  {
    if (getDocument() != null && getDocument().getInputFile() != null)
    {
      getDocument().getInputFile().unlockFile();
    }
  }
}
