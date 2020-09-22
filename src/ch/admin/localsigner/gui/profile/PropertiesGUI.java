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

import java.awt.Dimension;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.config.resources.ImageResources;
import ch.admin.localsigner.gui.GuiHelper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.listener.ChooseImageListener;
import ch.admin.localsigner.listener.ChooseOutputDirListener;
import ch.admin.localsigner.listener.ChoosePdfExtensionListener;
import ch.admin.localsigner.listener.ClearOutputDirListener;
import ch.admin.localsigner.listener.ClearPdfExtensionListener;
import ch.admin.localsigner.listener.ClosePropertyWindowListener;
import ch.admin.localsigner.listener.PropertiesCloseListener;
import ch.admin.localsigner.listener.SaveProfileListener;
import ch.admin.localsigner.listener.SigTypeCertificationListener;
import ch.admin.localsigner.listener.SigTypeSignatureListener;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.Constants;

/**
 * This class shows the signature properties user interface.
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class PropertiesGUI
{
  private static final float ZERO_FLOAT = 0.0f;

  private static final Logger LOGGER = Logger.getLogger(PropertiesGUI.class);

  private Label fileLabel = null;

  private Text outputDirText = null;

  private Button signatureTypeSIGButton = null;

  private Button signatureTypeCERTButton = null;

  private Button showSignedDocumentButton = null;

  private Text locationText = null;

  private Label positionLeftLabel = null;

  private Label positionTopLabel = null;

  private Label signatureBoxWidthLabel = null;

  private Label signatureBoxHeightLabel = null;

  private Spinner positionLeftSpinner = null;

  private Spinner positionTopSpinner = null;

  private Spinner signatureBoxWidthSpinner = null;

  private Spinner signatureBoxHeightSpinner = null;

  private Text pdfAttachmentText = null;

  private Button pdfAttachmentClearer = null;

  private Button pdfAttachmentSelector = null;

  // private Text imageText = null;

  // private Button imageChooseButton = null;

  private Text contactText = null;

  private Button contactLabelVisible = null;

  private Button outputDirClearer = null;

  private Text reasonText = null;

  private Button reasonLabelVisible = null;

  private Button outputDirSelector = null;

  // private Button visibleSignatureButton = null;

  private Label signaturePageLabel = null;

  private Combo signaturePageCombo = null;

  private int signaturePageDrawn;

  private Button saveButton = null;

  private Button saveAsButton = null;

  private Combo timestampingCombo = null;

  private Color background;

  private final MainGUI maingui;

  private Shell propertydialog;

  private static final int INCREMENT = 5;

  private static final int ROWS = 4;

  private String profilePath;

  private Button signatureVisible;

  private Text imageText;

  private Button imageChooseButton;

  private Button showTextInSignatureButton;

  private Button showImageInSignatureButton;

  private Button imgScalable;

  private Button sizeFromImage;

  private Button sizeFixedByUser;

  private Group signatureRepresentationGroup;

  private Text widthOfImgBox;

  private Label widthOfImg;

  private Text heightOfImgBox;

  private Label heightOfImg;

  private Button alignSecondVisbleSignature;

  private Group signaturePositonGroup;

  private float topPosPdfUnit;

  private float leftPosInPdfUnits;

  private float boxWidthInPdfUnits;

  private float boxHeightsInPdfUnits;

  private Button userDefinedSizeBoundButton;

  private boolean isUserDefinedSizeBound;

  private float userDefinedSizeProportion;

  /**
   * Constructor
   *
   * @param maingui
   *          Main GUI
   */
  public PropertiesGUI(final MainGUI maingui)
  {
    this.maingui = maingui;

    this.createPropertyWindow();

    propertydialog.addShellListener(new PropertiesCloseListener(propertydialog));
  }

  /**
   * Creates the signature properties dialog
   */
  private void createPropertyWindow()
  {
    propertydialog = new Shell(maingui.getMainshell(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    propertydialog.setImage(GuiHelper.loadAppIcon(propertydialog.getDisplay()));

    propertydialog.setText(LocalSigner.i18n("changeProfile"));
    propertydialog.setLayout(new GridLayout(ROWS, false));

    background = maingui.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE);

    // path of the current profile file
    fileLabel = GuiHelper.label(propertydialog, SWT.NORMAL, "", maingui.getFont());
    final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, false, ROWS, 1);
    gd.minimumWidth = 700;
    fileLabel.setLayoutData(gd);

    createPathGroup();
    createSignatureGroup();
    createTextsGroup();
    createSignatureRepresentationGroup();
    createPositionGroup();
    createTimestampGroup();

    // save profile button
    saveButton = new Button(propertydialog, SWT.PUSH);
    saveButton.setFont(maingui.getFont());
    saveButton.setText(LocalSigner.i18n("save"));
    SaveProfileListener spl = new SaveProfileListener(maingui, this, false, true);
    saveButton.addListener(SWT.Selection, spl);

    // save as button
    if (!LocalSigner.appConfig.getGuiViewMode().isSimpleMode())
    {
      saveAsButton = new Button(propertydialog, SWT.PUSH);
      saveAsButton.setFont(maingui.getFont());
      saveAsButton.setText(LocalSigner.i18n("saveas"));
      // saveAsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false,
      // false, 1, 1));
      spl = new SaveProfileListener(maingui, this, true, true);
      saveAsButton.addListener(SWT.Selection, spl);
    }

    // close button
    Button propertyDialogCloseButton = new Button(propertydialog, SWT.PUSH);
    propertyDialogCloseButton.setFont(maingui.getFont());
    propertyDialogCloseButton.setText(LocalSigner.i18n("cancel"));
    propertyDialogCloseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 1, 1));
    final ClosePropertyWindowListener cpwl = new ClosePropertyWindowListener(this);
    propertyDialogCloseButton.addListener(SWT.Selection, cpwl);

    if (isReadOnly())
    {
      // disable everything
      this.disableElements();
    }

    propertydialog.pack();
  }

  private boolean isReadOnly()
  {
    return LocalSigner.appConfig.isSystemProfilesOnly();
  }

  private void disableElements()
  {
    outputDirText.setEnabled(false);
    signatureTypeSIGButton.setEnabled(false);
    signatureTypeCERTButton.setEnabled(false);
    showSignedDocumentButton.setEnabled(false);
    locationText.setEnabled(false);
    pdfAttachmentText.setEnabled(false);
    pdfAttachmentClearer.setEnabled(false);
    pdfAttachmentSelector.setEnabled(false);
    imageText.setEnabled(false);
    imageChooseButton.setEnabled(false);
    contactText.setEnabled(false);
    contactLabelVisible.setEnabled(false);
    outputDirClearer.setEnabled(false);
    reasonText.setEnabled(false);
    reasonLabelVisible.setEnabled(false);
    outputDirSelector.setEnabled(false);
    signatureVisible.setEnabled(false);
    signaturePageCombo.setEnabled(false);
    saveButton.setEnabled(false);
    timestampingCombo.setEnabled(false);

    GuiHelper.disableAll(signaturePositonGroup);
    GuiHelper.disableAll(signatureRepresentationGroup);

    if (saveAsButton != null)
    {
      // not in simple mode
      saveAsButton.setEnabled(false);
    }
  }

  /**
   * Get the SWT shell of the properties GUI
   *
   * @return property GUI shell
   */
  public Shell getShell()
  {
    return propertydialog;
  }

  private void createTimestampGroup()
  {
    final Group group = new Group(propertydialog, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, ROWS, 1));
    group.setLayout(new GridLayout(ROWS, false));

    Label enableTimestampingLabel =
        GuiHelper.label(group, SWT.NONE, LocalSigner.i18n("usetimestamp"), maingui.getFont());
    enableTimestampingLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    timestampingCombo = new Combo(group, SWT.READ_ONLY);
    timestampingCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
    timestampingCombo.setFont(maingui.getFont());
    // extract preconfigured descriptions
    List<TsaConfiguration> tsaconfig = LocalSigner.appConfig.getTSAConfig();

    for (TsaConfiguration tsa : tsaconfig)
    {
      timestampingCombo.add(tsa.getDisplayText());
      timestampingCombo.setData(tsa.getDisplayText(), tsa);
    }

    timestampingCombo.select(0);
  }

  private void createPositionGroup()
  {
    signaturePositonGroup = new Group(signatureRepresentationGroup, SWT.NONE);
    signaturePositonGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false,
        ROWS, 1));
    signaturePositonGroup.setLayout(new GridLayout(ROWS, false));
    signaturePositonGroup.setText(LocalSigner
        .i18n("propertiesGUI.groupInitialPositionSignature"));
    signaturePositonGroup.setFont(maingui.getFont());

    positionLeftLabel = new Label(signaturePositonGroup, SWT.NONE);
    positionLeftLabel.setFont(maingui.getFont());
    positionLeftLabel.setText(LocalSigner.i18n("leftPos"));
    positionLeftLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    positionLeftSpinner = new Spinner(signaturePositonGroup, SWT.BORDER);
    positionLeftSpinner.setFont(maingui.getFont());
    positionLeftSpinner.setMinimum(0);
    positionLeftSpinner.setMaximum(300);
    positionLeftSpinner.setSelection(120);
    positionLeftSpinner.setIncrement(INCREMENT);
    positionLeftSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    positionLeftSpinner.pack();

    // label for box width
    signatureBoxWidthLabel = new Label(signaturePositonGroup, SWT.NONE);
    signatureBoxWidthLabel.setFont(maingui.getFont());
    signatureBoxWidthLabel.setText(LocalSigner.i18n("signatureWidth"));
    signatureBoxWidthLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureBoxWidthSpinner = new Spinner(signaturePositonGroup, SWT.BORDER);
    signatureBoxWidthSpinner.setFont(maingui.getFont());
    signatureBoxWidthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
    signatureBoxWidthSpinner.setMinimum(0);
    signatureBoxWidthSpinner.setMaximum(300);
    signatureBoxWidthSpinner.setSelection(80);
    signatureBoxWidthSpinner.setIncrement(INCREMENT);
    signatureBoxWidthSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureBoxWidthSpinner.pack();

    // label for top position
    positionTopLabel = new Label(signaturePositonGroup, SWT.NONE);
    positionTopLabel.setFont(maingui.getFont());
    positionTopLabel.setText(LocalSigner.i18n("topPos"));
    positionTopLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    positionTopSpinner = new Spinner(signaturePositonGroup, SWT.BORDER);
    positionTopSpinner.setFont(maingui.getFont());
    positionTopSpinner.setMinimum(0);
    positionTopSpinner.setMaximum(300);
    positionTopSpinner.setSelection(10);
    positionTopSpinner.setIncrement(INCREMENT);
    positionTopSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    positionTopSpinner.pack();

    // label for box height
    signatureBoxHeightLabel = new Label(signaturePositonGroup, SWT.NONE);
    signatureBoxHeightLabel.setFont(maingui.getFont());
    signatureBoxHeightLabel.setText(LocalSigner.i18n("signatureHeight"));
    signatureBoxHeightLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureBoxHeightSpinner = new Spinner(signaturePositonGroup, SWT.BORDER);
    signatureBoxHeightSpinner.setFont(maingui.getFont());
    signatureBoxHeightSpinner.setMinimum(0);
    signatureBoxHeightSpinner.setMaximum(300);
    signatureBoxHeightSpinner.setSelection(25);
    signatureBoxHeightSpinner.setIncrement(INCREMENT);
    signatureBoxHeightSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureBoxHeightSpinner.pack();

    // signature page
    signaturePageLabel = new Label(signaturePositonGroup, SWT.NONE);
    signaturePageLabel.setFont(maingui.getFont());
    signaturePageLabel.setText(LocalSigner.i18n("signOn"));
    signaturePageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    signaturePageCombo = new Combo(signaturePositonGroup, SWT.READ_ONLY);
    signaturePageCombo.addModifyListener(new ModifyListener()
    {
      @Override
      public void modifyText(ModifyEvent e)
      {
        // invalidate drawn signature page
        signaturePageDrawn = 0;
      }

    });
    signaturePageCombo
        .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    signaturePageCombo.setFont(maingui.getFont());
    signaturePageCombo.setItems(new String[]
            {
              LocalSigner.i18n("firstPage"), LocalSigner.i18n("penultimatePage"),
              LocalSigner.i18n("lastPage")
            });
    signaturePageCombo.select(0);
  }

  private void createSignatureRepresentationGroup()
  {
    signatureRepresentationGroup = new Group(propertydialog, SWT.NONE);
    signatureRepresentationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, ROWS, 1));
    signatureRepresentationGroup.setLayout(new GridLayout(ROWS, false));
    signatureRepresentationGroup.setText(LocalSigner
        .i18n("propertiesGUI.groupSignatureRepresentation"));
    signatureRepresentationGroup.setFont(maingui.getFont());

    signatureVisible = new Button(signatureRepresentationGroup, SWT.CHECK);
    signatureVisible.setText(LocalSigner.i18n("visibleSignature"));
    signatureVisible.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 4, 1));
    signatureVisible.addSelectionListener(new OnChangeUpdateVisibleSignaturePart());
    signatureVisible.setFont(maingui.getFont());

    new Label(signatureRepresentationGroup, SWT.NONE);

    Composite subElements = new Composite(signatureRepresentationGroup, SWT.NONE);
    subElements.setLayout(new GridLayout(4, false));
    subElements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

    showVisualSignatureOptions(subElements);

    new Label(signatureRepresentationGroup, SWT.NONE);
    
    alignSecondVisbleSignature = new Button(signatureRepresentationGroup, SWT.CHECK);
    alignSecondVisbleSignature.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false,
        false, 3, 1));
    alignSecondVisbleSignature.setText(LocalSigner
        .i18n("propertiesGUI.secondSignatureFixedYAxis"));
    alignSecondVisbleSignature.setFont(maingui.getFont());

  }

  private void showVisualSignatureOptions(Composite parent)
  {
    showTextInSignatureButton = new Button(parent, SWT.CHECK);
    showTextInSignatureButton.setText(LocalSigner
        .i18n("propertiesGUI.showTextInSignature"));
    showTextInSignatureButton.setFont(maingui.getFont());
    showTextInSignatureButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false,
        false, 4, 1));
    showTextInSignatureButton
        .addSelectionListener(new OnChangeUpdateVisibleSignaturePart());

    showImageInSignatureButton = new Button(parent, SWT.CHECK);
    showImageInSignatureButton.setText(LocalSigner
        .i18n("propertiesGUI.showImageInSignature"));
    showImageInSignatureButton.setFont(maingui.getFont());
    showImageInSignatureButton.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false,
        false, 4, 1));

    showImageInSignatureButton
        .addSelectionListener(new OnChangeUpdateVisibleSignaturePart());

    new Label(parent, SWT.NONE);



    Composite imageSubElements = new Composite(parent, SWT.NONE);
    imageSubElements.setLayout(new GridLayout(4, false));
    imageSubElements.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

    createSignatureImageSubelements(imageSubElements);
  }

  private void createSignatureImageSubelements(Composite parent)
  {
    // image
    Label imageLabel = new Label(parent, SWT.NONE);
    imageLabel.setFont(maingui.getFont());
    imageLabel.setText(LocalSigner.i18n("backgroundImage"));
    imageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    imageText = GuiHelper.text(parent, SWT.SINGLE | SWT.BORDER, maingui.getFont());
    imageText.setFont(maingui.getFont());
    imageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    imageText.setToolTipText(LocalSigner.i18n("tooltipImage"));

    imageChooseButton = new Button(parent, SWT.PUSH);
    imageChooseButton.setFont(maingui.getFont());
    imageChooseButton.setText(LocalSigner.i18n("choose"));
    imageChooseButton
        .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    ChooseImageListener imgListener = new ChooseImageListener(this, propertydialog);
    imageChooseButton.addListener(SWT.Selection, imgListener);
    imageChooseButton.addSelectionListener(new OnChangeInitUserDefinedImageSizeFields());

    imgScalable = new Button(parent, SWT.RADIO);
    imgScalable.setText(LocalSigner.i18n("propertiesGUI.imageScalable"));
    imgScalable.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 4, 1));
    imgScalable.addSelectionListener(new OnChangeUpdateVisibleSignaturePart());
    imgScalable.setFont(maingui.getFont());

    sizeFromImage = new Button(parent, SWT.RADIO);
    sizeFromImage.setText(LocalSigner.i18n("propertiesGUI.useSizeOfImage"));
    sizeFromImage.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 4, 1));
    sizeFromImage.addSelectionListener(new OnChangeUpdateVisibleSignaturePart());
    sizeFromImage.setFont(maingui.getFont());

    sizeFixedByUser = new Button(parent, SWT.RADIO);
    sizeFixedByUser.setText(LocalSigner.i18n("propertiesGUI.useCustomImageSize"));
    sizeFixedByUser.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 4, 1));
    sizeFixedByUser.addSelectionListener(new OnChangeUpdateVisibleSignaturePart());
    sizeFixedByUser.addSelectionListener(new OnChangeInitUserDefinedImageSizeFields());
    sizeFixedByUser.setFont(maingui.getFont());

    Composite userDefinedSizes= new Composite(parent, SWT.NONE);
    userDefinedSizes.setLayout(new GridLayout(6, false));
    userDefinedSizes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
    userDefinedSizes.setFont(maingui.getFont());

    new Label(userDefinedSizes, SWT.NONE);

    heightOfImg = new Label(userDefinedSizes, SWT.NONE);
    heightOfImg.setText(LocalSigner.i18n("propertiesGUI.customImageHeight"));
    heightOfImg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    heightOfImg.setFont(maingui.getFont());

    heightOfImgBox = new Text(userDefinedSizes, SWT.SINGLE | SWT.BORDER);
    heightOfImgBox.addVerifyListener(new NumericOnlyVerifier());
    heightOfImgBox.setTextLimit(3);
    heightOfImgBox.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
    heightOfImgBox.setFont(maingui.getFont());
    heightOfImgBox.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        if (!isUserDefinedSizeBound() || noImgProportionCalculated())
        {
          return;
        }

        String currentValue = heightOfImgBox.getText();
        if (StringUtils.isEmpty(currentValue))
        {
          return;
        }

        int heightInMM = Integer.parseInt(currentValue);
        if (heightInMM > 0)
        {
          int widthNewInMM = Math.round(heightInMM / userDefinedSizeProportion);
          if (String.valueOf(widthNewInMM).equals(widthOfImgBox.getText()))
          {
            return;
          }
          widthOfImgBox.setText(String.valueOf(widthNewInMM));
        }

      }
    });


    widthOfImg = new Label(userDefinedSizes, SWT.NONE);
    widthOfImg.setText(LocalSigner.i18n("propertiesGUI.customImageWidth"));
    widthOfImg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    widthOfImg.setFont(maingui.getFont());

    widthOfImgBox = new Text(userDefinedSizes, SWT.SINGLE | SWT.BORDER);
    widthOfImgBox.addVerifyListener(new NumericOnlyVerifier());
    widthOfImgBox.setTextLimit(3);
    widthOfImgBox.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
    widthOfImgBox.setFont(maingui.getFont());

    widthOfImgBox.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyReleased(KeyEvent e)
      {
        if (!isUserDefinedSizeBound() || noImgProportionCalculated())
        {
          return;
        }

        String currentValue = widthOfImgBox.getText();
        if (StringUtils.isEmpty(currentValue))
        {
          return;
        }

        int widthInMm = Integer.parseInt(currentValue);
        if (widthInMm > 0)
        {
          int heightNewInMM = Math.round(widthInMm * userDefinedSizeProportion);
          if (String.valueOf(heightNewInMM).equals(heightOfImgBox.getText()))
          {
            return;
          }
          heightOfImgBox.setText(String.valueOf(heightNewInMM));
        }

      }
    });

    isUserDefinedSizeBound = true;
    userDefinedSizeBoundButton = new Button(userDefinedSizes, SWT.PUSH);
    userDefinedSizeBoundButton.setLayoutData(new GridData(SWT.LEFT, SWT.LEFT, false, false, 1, 1));
    userDefinedSizeBoundButton.setFont(maingui.getFont());
    userDefinedSizeBoundButton.setImage(GuiHelper.loadImage(ImageResources.IMG_VCHAIN));
    userDefinedSizeBoundButton.setToolTipText(LocalSigner
        .i18n("propertiesGUI.useCustomImageSize.proportional"));
    userDefinedSizeBoundButton.addSelectionListener(new SelectionAdapter()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        setUserDefinedSizeBound(!isUserDefinedSizeBound());
        if (!isUserDefinedSizeBound())
        {
          userDefinedSizeBoundButton.setImage(GuiHelper
              .loadImage(ImageResources.IMG_VCHAIN_BROKEN));
        }
        else
        {
          userDefinedSizeBoundButton.setImage(GuiHelper
              .loadImage(ImageResources.IMG_VCHAIN));

          String height = getFixedImageHeight();
          int heightInt = 0;
          if (StringUtils.isNotEmpty(height) && StringUtils.isNumeric(height))
          {
            heightInt = Integer.parseInt(height);
          }

          String width = getFixedImageWidth();
          int widthInt = 0;
          if (StringUtils.isNotEmpty(width) && StringUtils.isNumeric(width))
          {
            widthInt = Integer.parseInt(width);
          }

          setUserDefinedSizeProportion(heightInt, widthInt);
        }
      }

    });
  }

  private void createTextsGroup()
  {
    Group group = new Group(propertydialog, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, ROWS, 2));
    group.setLayout(new GridLayout(ROWS - 1, false));
    group.setText(LocalSigner.i18n("propertiesGUI.groupSignatureText"));
    group.setFont(maingui.getFont());

    // reason
    GridData mulitlineTextFieldGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
    mulitlineTextFieldGridData.heightHint = 50;
    mulitlineTextFieldGridData.widthHint = 200;

    Composite reasonLabelsComp = new Composite(group, SWT.NONE);
    reasonLabelsComp.setLayout(new GridLayout(2, false));
    reasonLabelsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    Label reasonLabel = GuiHelper.label(reasonLabelsComp, SWT.NONE, LocalSigner.i18n("reason"), maingui.getFont());
    reasonLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

    Label reasonLabelVisibleLabel = GuiHelper.label(reasonLabelsComp, SWT.NONE, LocalSigner.i18n("reasonShowLabel"),
        maingui.getFont());
    reasonLabelVisibleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    reasonLabelVisible = new Button(reasonLabelsComp, SWT.CHECK);
    reasonLabelVisible.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    reasonText = GuiHelper.text(group, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL, maingui.getFont());
    reasonText.setLayoutData(mulitlineTextFieldGridData);
    reasonText.setEditable(true);
    reasonText.setToolTipText(LocalSigner.i18n("tooltipReason"));
    reasonText.addTraverseListener(new TraverseListener()
    {
      @Override
      public void keyTraversed(final TraverseEvent e)
      {
        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
        {
          e.doit = true;
        }
      }

    });

    // contact
    Composite contactLabelsComp = new Composite(group, SWT.NONE);
    contactLabelsComp.setLayout(new GridLayout(2, false));
    contactLabelsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    Label contactLabel = GuiHelper.label(contactLabelsComp, SWT.NONE, LocalSigner.i18n("contact"), maingui.getFont());
    contactLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

    Label contactLabelVisibleLabel = GuiHelper.label(contactLabelsComp, SWT.NONE, LocalSigner.i18n("contactShowLabel"),
        maingui.getFont());
    contactLabelVisibleLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    contactLabelVisible = new Button(contactLabelsComp, SWT.CHECK);
    contactLabelVisible.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    contactText = GuiHelper.text(group, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL, maingui.getFont());
    contactText.setFont(maingui.getFont());
    contactText.setLayoutData(mulitlineTextFieldGridData);
    contactText.setToolTipText(LocalSigner.i18n("tooltipContact"));
    contactText.addTraverseListener(new TraverseListener()
    {
      @Override
      public void keyTraversed(final TraverseEvent e)
      {
        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS)
        {
          e.doit = true;
        }
      }

    });

    // location
    Label locationLabel = new Label(group, SWT.NONE);
    locationLabel.setFont(maingui.getFont());
    locationLabel.setText(LocalSigner.i18n("location"));
    locationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    locationText = GuiHelper.text(group, SWT.SINGLE | SWT.BORDER, maingui.getFont());
    locationText.setFont(maingui.getFont());
    locationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    locationText.setToolTipText(LocalSigner.i18n("tooltipLocation"));
  }

  private void createSignatureGroup()
  {
    Group group = new Group(propertydialog, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, ROWS, 1));
    group.setLayout(new GridLayout(ROWS, false));

    // signature type
    Label documentLabel = GuiHelper.label(group, SWT.NONE, LocalSigner.i18n( "signatureType"), maingui.getFont());
    documentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    Composite signierenComposite = new Composite(group, SWT.NONE);
    signierenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    signierenComposite.setLayout(new GridLayout(2, false));

    Label signatureTypeSIGLabel =
        GuiHelper.label(signierenComposite, SWT.NONE, LocalSigner.i18n("signing"), maingui.getFont());
    signatureTypeSIGLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    String certifyTip = LocalSigner.i18n("toolTipCertify");
    signatureTypeSIGLabel.setToolTipText(certifyTip);
    signatureTypeSIGButton = new Button(signierenComposite, SWT.TOGGLE | SWT.RADIO);
    signatureTypeSIGButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureTypeSIGButton.setFont(maingui.getFont());
    signatureTypeSIGButton.setSelection(true);
    signatureTypeSIGButton.setToolTipText(certifyTip);
    SigTypeSignatureListener stsl = new SigTypeSignatureListener(this);
    signatureTypeSIGButton.addListener(SWT.Selection, stsl);

    Composite zertifizierenComposite = new Composite(group, SWT.NONE);
    zertifizierenComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    zertifizierenComposite.setLayout(new GridLayout(2, false));

    Label signatureTypeCERTLabel =
        GuiHelper.label(zertifizierenComposite, SWT.NONE, LocalSigner.i18n("certifying"), maingui.getFont());
    signatureTypeCERTLabel.setToolTipText(certifyTip);
    signatureTypeCERTLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    signatureTypeCERTButton = new Button(zertifizierenComposite, SWT.TOGGLE | SWT.RADIO);
    signatureTypeCERTButton.setToolTipText(certifyTip);
    signatureTypeCERTButton.setFont(maingui.getFont());
    signatureTypeCERTButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    SigTypeCertificationListener stcl = new SigTypeCertificationListener(this);
    signatureTypeCERTButton.addListener(SWT.Selection, stcl);

    Composite viewDocComposite = new Composite(group, SWT.NONE);
    viewDocComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    viewDocComposite.setLayout(new GridLayout(2, false));

    // show signed document
    Label showSignedDocumentLabel =
        GuiHelper.label(viewDocComposite, SWT.NONE, LocalSigner.i18n("displaySignedDocument"), maingui.getFont());
    showSignedDocumentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    showSignedDocumentButton = new Button(viewDocComposite, SWT.CHECK);
    showSignedDocumentButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    showSignedDocumentButton.setSelection(true);
  }

  private void createPathGroup()
  {
    Group group = new Group(propertydialog, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, ROWS, 1));
    group.setLayout(new GridLayout(ROWS, false));

    // fill the composite
    // output directory selector
    Label outputDirLabel = GuiHelper.label(group, SWT.NORMAL, LocalSigner.i18n("outputFileDir"), maingui.getFont());
    outputDirLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    outputDirText = GuiHelper.text(group, SWT.SINGLE | SWT.BORDER, maingui.getFont());
    outputDirText.setFont(maingui.getFont());
    outputDirText.setBackground(background);
    outputDirText.setEditable(false);
    outputDirText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    outputDirText.setToolTipText(LocalSigner.i18n("tooltipChangedir"));
    outputDirClearer = new Button(group, SWT.PUSH);
    outputDirClearer.setFont(maingui.getFont());
    outputDirClearer.setText(LocalSigner.i18n("clear"));
    outputDirClearer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    ClearOutputDirListener clearListener = new ClearOutputDirListener(this, maingui);
    outputDirClearer.addListener(SWT.Selection, clearListener);
    outputDirSelector = new Button(group, SWT.PUSH);
    outputDirSelector.setFont(maingui.getFont());
    outputDirSelector.setText(LocalSigner.i18n("choose"));
    outputDirSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    // add listener to the output directory selector
    ChooseOutputDirListener codl = new ChooseOutputDirListener(maingui);
    outputDirSelector.addListener(SWT.Selection, codl);

    // extension pdf label, text and selector
    Label pdfAttachmentLabel = GuiHelper.label(group, SWT.NONE, LocalSigner.i18n("pdfattachment"), maingui.getFont());
    pdfAttachmentLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
    pdfAttachmentText = GuiHelper.text(group, SWT.SINGLE | SWT.BORDER, maingui.getFont());
    pdfAttachmentText.setFont(maingui.getFont());
    pdfAttachmentText.setBackground(background);
    pdfAttachmentText.setEditable(false);
    pdfAttachmentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    pdfAttachmentText.setToolTipText(LocalSigner.i18n("tooltipAttachment"));
    pdfAttachmentClearer = new Button(group, SWT.PUSH);
    pdfAttachmentClearer.setFont(maingui.getFont());
    pdfAttachmentClearer.setText(LocalSigner.i18n("clear"));
    pdfAttachmentClearer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    ClearPdfExtensionListener cl = new ClearPdfExtensionListener(this);
    pdfAttachmentClearer.addListener(SWT.Selection, cl);
    pdfAttachmentSelector = new Button(group, SWT.PUSH);
    pdfAttachmentSelector.setFont(maingui.getFont());
    pdfAttachmentSelector.setText(LocalSigner.i18n("choose"));
    pdfAttachmentSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    // add listener to the output directory selector
    ChoosePdfExtensionListener cpel = new ChoosePdfExtensionListener(maingui);
    pdfAttachmentSelector.addListener(SWT.Selection, cpel);
  }

  /**
   * Get the path of the output directory.
   *
   * @return path of output directory
   */
  public String getOutputDir()
  {
    return this.outputDirText.getText();
  }

  /**
   * Set the output directory.
   *
   * @param path
   *          Path of output directory
   */
  public void setOutputDir(final String path)
  {
    String tmp = path;
    if (tmp == null)
    {
      tmp = "";
    }
    this.outputDirText.setText(tmp);
  }

  /**
   * Get the path of the PDF attachment.
   *
   * @return path of PDF attachment
   */
  public String getPdfAttachment()
  {
    return this.pdfAttachmentText.getText();
  }

  /**
   * Set the PDF attachment.
   *
   * @param path
   *          Path of PDF attachment
   */
  public void setPdfAttachment(final String path)
  {
    String tmp = path;
    if (tmp == null)
    {
      tmp = "";
    }
    LOGGER.debug("PDF attachment " + path);
    this.pdfAttachmentText.setText(tmp);
  }

  /**
   * Return true, if the user selected to use certification instead of simple
   * signature.
   *
   * @return true if certification, false for signature
   */
  public boolean isCertificationType()
  {
    return this.signatureTypeCERTButton.getSelection();
  }

  /**
   * Set the signature type. Only one certification is allowed, but multiple
   * signatures are possible before certification.
   *
   * @param isCertification isCertification
   */
  public void setCertificationType(final boolean isCertification)
  {
    // set both buttons as we use them as toggle buttons
    this.signatureTypeCERTButton.setSelection(isCertification);
    this.signatureTypeSIGButton.setSelection(!isCertification);
  }

  /**
   * Get the signature reason text.
   *
   * @return text of signature reason
   */
  public String getReason()
  {
    return this.reasonText.getText();
  }

  /**
   * Set the signature reason text.
   *
   * @param text
   *          The signature text to be set
   */
  public void setReason(final String text)
  {
    String tmp = text;
    if (tmp == null)
    {
      tmp = "";
    }
    this.reasonText.setText(tmp);
  }

  public void setReasonLabelVisible(final boolean visible)
  {
    reasonLabelVisible.setSelection(visible);
  }

  public boolean getReasonLabelVisible()
  {
    return reasonLabelVisible.getSelection();
  }

  /**
   * Returns if the signed document should be displayed
   *
   * @return true, if the user wants to display the signed document
   */
  public boolean isDisplaySignedDocument()
  {
    return this.showSignedDocumentButton.getSelection();
  }

  /**
   * Sets if the user wants to display the signed document
   *
   * @param display
   *          true if yes, false otherwise
   */
  public void setDisplaySignedDocument(final boolean display)
  {
    this.showSignedDocumentButton.setSelection(display);
  }

  /**
   * Returns the currently set path for the background image of the signature
   *
   * @return Path of the currently set background image
   */
  public String getBackgroundImage()
  {
    return this.imageText.getText();
  }

  /**
   * Sets the path to the background image to be used in the signature
   *
   * @param imagePath
   *          Path of the background image
   */
  public void setBackgroundImage(final String imagePath)
  {
    String tmp = imagePath;
    if (tmp == null)
    {
      tmp = StringUtils.EMPTY;
    }
    this.imageText.setText(tmp);
  }

  /**
   * Returns the location set into the signature
   */
  public String getLocation()
  {
    return this.locationText.getText();
  }

  /**
   * Sets the location into the signature
   *
   * @param location
   *          Signature location
   */
  public void setLocation(final String location)
  {
    String tmp = location;
    if (tmp == null)
    {
      tmp = StringUtils.EMPTY;
    }
    this.locationText.setText(tmp);
  }

  /**
   * Returns the contact information in the signature
   *
   * @return contact of the signature
   */
  public String getContact()
  {
    return this.contactText.getText();
  }

  /**
   * Sets the contact information for the signature
   *
   * @param contact
   *          The contact to set into the signature
   */
  public void setContact(final String contact)
  {
    String tmp = contact;
    if (tmp == null)
    {
      tmp = StringUtils.EMPTY;
    }
    this.contactText.setText(tmp);
  }

  public void setContactLabelVisible(final boolean visible)
  {
    contactLabelVisible.setSelection(visible);
  }

  public boolean getContactLabelVisible()
  {
    return contactLabelVisible.getSelection();
  }

  /**
   * Returns the left position of the visible signature
   *
   * @return Left position of the signature
   */
  public int getLeftPos()
  {
    return this.positionLeftSpinner.getSelection();
  }

  /**
   * Sets the left position of the visible signature
   *
   * @param leftpos
   *          Left position of the visible signature
   */
  public void setLeftPos(final int leftpos)
  {
    this.positionLeftSpinner.setSelection(leftpos);
    this.leftPosInPdfUnits = leftpos * Constants.ONE_MILLIMETER_IN_PDF;
  }

  public void setLefPosPdfUnits(final float leftpos)
  {
    setLeftPos(Math.round(leftpos / Constants.ONE_MILLIMETER_IN_PDF));
    this.leftPosInPdfUnits = leftpos;
  }

  /**
   * Gets top position of the visible signature
   *
   * @return Top position of the signature
   */
  public int getTopPos()
  {
    return this.positionTopSpinner.getSelection();
  }

  /**
   * Sets the top position of the visible signature
   *
   * @param toppos
   *          Top position of the visible signature
   */
  public void setTopPos(final int toppos)
  {
    this.positionTopSpinner.setSelection(toppos);
    this.topPosPdfUnit = toppos * Constants.ONE_MILLIMETER_IN_PDF;
  }

  public void setTopPosPdfUnits(final float toppos)
  {
    setTopPos(Math.round(toppos / Constants.ONE_MILLIMETER_IN_PDF));
    this.topPosPdfUnit = toppos;
  }

  /**
   * Gets width of the visible signature
   *
   * @return Width position of the signature
   */
  public int getBoxWidth()
  {
    return this.signatureBoxWidthSpinner.getSelection();
  }

  /**
   * Sets the width of the visible signature
   *
   * @param width
   *          Width of the visible signature
   */
  public void setBoxWidth(final int width)
  {
    this.signatureBoxWidthSpinner.setSelection(width);
    this.boxWidthInPdfUnits = width * Constants.ONE_MILLIMETER_IN_PDF;
  }

  public void setBoxWidthInPdfUnits(final float width)
  {
    setBoxWidth(Math.round(width / Constants.ONE_MILLIMETER_IN_PDF));
    this.boxWidthInPdfUnits = width;
  }

  public float getBoxWidthInPdfUnits()
  {
    if (this.boxWidthInPdfUnits == ZERO_FLOAT)
    {
      return getBoxWidth() * Constants.ONE_MILLIMETER_IN_PDF;
    }
    return this.boxWidthInPdfUnits;
  }

  /**
   * Extracts the width of the current image, as pixels.
   * 
   * @return
   */
  public float getBoxWidthImage()
  {
    if (useImageSize())
    {
      return (float) new ImageHelper().getBackgroundImageDimension(getBackgroundImage(),
          getProfilePath()).getWidth();
    }
    else if (useCustomImageSize())
    {
      String widthValue = widthOfImgBox.getText();
      if (StringUtils.isBlank(widthValue))
      {
        return 0;
      }
      return Integer.valueOf(widthValue) * Constants.ONE_MILLIMETER_IN_PDF;
    }

    return 0;
  }

  /**
   * Extracts the height of the current image, as pixels.
   * 
   * @return
   */
  public float getBoxHeightImage()
  {
    if (useImageSize())
    {
      return (float) new ImageHelper().getBackgroundImageDimension(getBackgroundImage(),
          getProfilePath()).getHeight();
    }
    else if (useCustomImageSize())
    {
      String heightValue = heightOfImgBox.getText();
      if (StringUtils.isBlank(heightValue))
      {
        return 0;
      }
      return Integer.valueOf(heightValue) * Constants.ONE_MILLIMETER_IN_PDF;
    }

    return 0;

  }

  public boolean useCustomImageSize()
  {
    return getImageMode() == ImageMode.FROM_USER;
  }

  public boolean useImageSize()
  {
    return getImageMode() == ImageMode.FROM_IMAGE;
  }

  /**
   * Gets height of the visible signature
   *
   * @return Height position of the signature
   */
  public int getBoxHeight()
  {
    return this.signatureBoxHeightSpinner.getSelection();
  }

  public float getBoxHeightInPdfUnits()
  {
    if (this.boxHeightsInPdfUnits == ZERO_FLOAT)
    {
      return getBoxHeight() * Constants.ONE_MILLIMETER_IN_PDF;
    }
    return this.boxHeightsInPdfUnits;
  }

  /**
   * Sets the height of the visible signature
   *
   * @param height
   *          Height of the visible signature
   */
  public void setBoxHeight(final int height)
  {
    this.signatureBoxHeightSpinner.setSelection(height);
    this.boxHeightsInPdfUnits = height * Constants.ONE_MILLIMETER_IN_PDF;
  }

  public void setBoxHeightInPdfUnits(final float height)
  {
    setBoxHeight(Math.round(height / Constants.ONE_MILLIMETER_IN_PDF));
    this.boxHeightsInPdfUnits = height;
  }

  /**
   * Get the signature page.
   * @param numberOfPages Number of pages in document
   */
  public int getSignaturePageSignature(final int numberOfPages)
  {
    if (this.signaturePageDrawn > 0)
    {
      if (this.signaturePageDrawn > numberOfPages)
      {
        return numberOfPages;
      }
      // drawn signature page
      return this.signaturePageDrawn;
    }

    int selection = this.signaturePageCombo.getSelectionIndex();
    switch (selection)
    {
      case 1:
        // penultimate page
        int page = numberOfPages - 1;
        if (page <= 0)
        {
          page = 1;
        }
        return page;
      case 2:
        // last page
        return numberOfPages;
      default:
        // first page
        return 1;
    }
  }

  /**
   * Get the signature page for the profile (0, 1 or 2)
   */
  public int getSignaturePageProfile()
  {
    int selection = this.signaturePageCombo.getSelectionIndex();
    switch (selection)
    {
      case 1:
        return 2; // penultimate page
      case 2:
        return 1; // last page
      default:
        return 0; // first page
    }
  }

  /**
   * Sets the page to sign on.
   * 0 = first page
   * 1 = last page
   * 2 = penultimate page (2nd last)
   *
   * @param index
   *          0 for first page, 1 for last page, 2 for penultimate page
   */
  public void setSignaturePageProfile(final int index)
  {
    this.signaturePageDrawn = 0;

    switch (index)
    {
      case 1:
        this.signaturePageCombo.select(2);
        break;
      case 2:
        this.signaturePageCombo.select(1);
        break;
      default:
        this.signaturePageCombo.select(0);
        break;
    }
  }

  /**
   * Sets the real signature page drawn in BFO viewer. Any page is possible.
   * @param index page number
   */
  public void setSignaturePageDraw(final int index)
  {
    this.signaturePageDrawn = index;
  }

  /**
   * Returns if signature is visible or not.
   *
   * @return true, if signature is visible, false otherwise
   */
  public boolean isVisibleSignature()
  {
    return this.signatureVisible.getSelection();
  }

  /**
   * Sets if the signature should be visible or not. The method also enables or
   * disables the according widgets for signature placement depending on the
   * visibility of the signature.
   *
   * @param visible
   *          true if visible signature requested
   */
  public void setVisibleSignature(final boolean visible)
  {
    this.signatureVisible.setSelection(visible);
    // and update the widgets for signature placement
    updateVisibleSignature();
  }

  /**
   * This method is called when a gui element for signature representation is
   * selected.
   */
  private void updateVisibleSignature()
  {
    if (isReadOnly())
    {
      return;
    }

    // if visible signatures are not selected
    boolean isVisibleSig = isVisibleSignature();

    positionLeftLabel.setEnabled(isVisibleSig);
    positionLeftSpinner.setEnabled(isVisibleSig);
    positionTopLabel.setEnabled(isVisibleSig);
    positionTopSpinner.setEnabled(isVisibleSig);
    signatureBoxHeightLabel.setEnabled(isVisibleSig);
    signatureBoxHeightSpinner.setEnabled(isVisibleSig);
    signatureBoxWidthLabel.setEnabled(isVisibleSig);
    signatureBoxWidthSpinner.setEnabled(isVisibleSig);
    signaturePageLabel.setEnabled(isVisibleSig);
    signaturePageCombo.setEnabled(isVisibleSig);
    alignSecondVisbleSignature.setEnabled(isVisibleSig);


    showTextInSignatureButton.setEnabled(isVisibleSig);
    showImageInSignatureButton.setEnabled(isVisibleSig);
    boolean isImageModeEnabled = shouldShowImageInSignature()
        && showImageInSignatureButton.isEnabled();
    imageChooseButton.setEnabled(isImageModeEnabled);
    imageText.setEnabled(isImageModeEnabled);
    imgScalable.setEnabled(isImageModeEnabled);
    sizeFixedByUser.setEnabled(isImageModeEnabled);
    sizeFromImage.setEnabled(isImageModeEnabled);

    boolean isTextModeEnabled = showTextInSignatureButton.getSelection()
        && showTextInSignatureButton.isEnabled();

    if (isImageModeEnabled && isTextModeEnabled && isVisibleSig)
    {
      imgScalable.setSelection(true);
      sizeFixedByUser.setSelection(false);
      sizeFixedByUser.setEnabled(false);
      sizeFromImage.setSelection(false);
      sizeFromImage.setEnabled(false);
    }
    
    boolean isSizeFromUserEnabled = sizeFixedByUser.getSelection()
        && sizeFixedByUser.isEnabled();
    heightOfImgBox.setEnabled(isSizeFromUserEnabled);
    widthOfImgBox.setEnabled(isSizeFromUserEnabled);
    heightOfImg.setEnabled(isSizeFromUserEnabled);
    widthOfImg.setEnabled(isSizeFromUserEnabled);
    userDefinedSizeBoundButton.setEnabled(isSizeFromUserEnabled);
    
    boolean isSizeFromImageEnabled = sizeFromImage.getSelection()
        && sizeFromImage.isEnabled();

    signatureBoxHeightSpinner.setEnabled(!isSizeFromUserEnabled
        && !isSizeFromImageEnabled && isVisibleSig);
    signatureBoxWidthSpinner.setEnabled(!isSizeFromUserEnabled && !isSizeFromImageEnabled
        && isVisibleSig);

  }

  public void doUpdateGuiAfterProfileLoaded()
  {
    updateVisibleSignature();
  }



  public void setProfilePath(final String text)
  {
    profilePath = text;
    fileLabel.setText(LocalSigner.i18n("file") + ": " + profilePath);
  }

  public String getProfilePath()
  {
    return profilePath;
  }

  /**
   * Enables or disables the save button.
   * @param enabled
   */
  public void enableSaveButton(final boolean enabled)
  {
    saveButton.setEnabled(enabled);
  }

  /**
   * Close the GUI.
   */
  public void close()
  {
    // the following actions only take place, when a document has been chosen
    // for signing. therefore test this case !
    if (maingui.getInputFile()==null)
    {
      propertydialog.close();
      return;
    }

    // update attachment
    maingui.getDocument().getInputFile().updateAttachment(
            this.getPdfAttachment());

    // actually close property window
    propertydialog.close();

    // update browser view
    maingui.reloadInputFile(true);
  }

  /**
   * Get selected TSA configuration
   * @return selected configuration
   */
  public TsaConfiguration getTsaSelection()
  {
    String name = timestampingCombo.getItem(
            timestampingCombo.getSelectionIndex());
    return (TsaConfiguration) timestampingCombo.getData(name);
  }

  /**
   * Add a custom timestamp configuration.
   * @param tsaConfiguration TSA configuration
   */
  public void addCustomTsa(TsaConfiguration tsaConfiguration)
  {
    if (tsaConfiguration.getLookupKey() == null)
    {
      LOGGER.error("Cannot add empty TSA URL");
      return;
    }

    // select a given tsa if name matches
    for (int itemNr = 0; itemNr < timestampingCombo.getItemCount(); itemNr++)
    {
      String displayText = timestampingCombo.getItem(itemNr);
      TsaConfiguration listTsa = (TsaConfiguration) timestampingCombo.getData(
              displayText);
      if (listTsa.getUrl().equals(tsaConfiguration.getUrl())
          || listTsa.getLookupKey().equals(tsaConfiguration.getLookupKey()))
      {
        // found match, select this
        timestampingCombo.select(itemNr);
        return;
      }
    }

    // no match found; add new
    timestampingCombo.add(tsaConfiguration.getDisplayText());
    timestampingCombo.setData(tsaConfiguration.getDisplayText(), tsaConfiguration);
    timestampingCombo.select(timestampingCombo.getItemCount() - 1);
    LOGGER.debug("Add custom TSA: " + tsaConfiguration.getUrl());
  }

  /**
   * Select no TSA (entry 1)
   */
  public void selectNoTsa()
  {
    timestampingCombo.select(0);
  }

  private final class OnChangeInitUserDefinedImageSizeFields extends SelectionAdapter
  {
    @Override
    public void widgetSelected(SelectionEvent e)
    {
      if (sizeFixedByUser.getSelection() && StringUtils.isNotEmpty(getBackgroundImage())
          && StringUtils.isEmpty(heightOfImgBox.getText())
          && StringUtils.isEmpty(widthOfImgBox.getText()))
      {
        Dimension imgDimension = new ImageHelper().getBackgroundImageDimension(
            getBackgroundImage(), getProfilePath());
        int heightInMm = Math.round((int) imgDimension.getHeight()
            / Constants.ONE_MILLIMETER_IN_PDF);
        setFixedImageHeight(heightInMm);
        int widthInMm = Math.round((int) imgDimension.getWidth()
            / Constants.ONE_MILLIMETER_IN_PDF);
        setFixedImageWidth(widthInMm);
        setUserDefinedSizeProportion(heightInMm, widthInMm);
      }

    }
  }

  private final class OnChangeUpdateVisibleSignaturePart extends SelectionAdapter
  {
    @Override
    public void widgetSelected(SelectionEvent e)
    {
      updateVisibleSignature();
    }
  }

  private static final class NumericOnlyVerifier implements VerifyListener
  {
    @Override
    public void verifyText(VerifyEvent e)
    {
      String string = e.text;
      char[] chars = new char[string.length()];
      string.getChars(0, chars.length, chars, 0);
      for (char aChar : chars) {
        if (!('0' <= aChar && aChar <= '9')) {
          e.doit = false;
          return;
        }
      }
    }
  }

  public void setTextShownInSignature(boolean textShown)
  {
    showTextInSignatureButton.setSelection(textShown);
  }

  public boolean shouldShowTextInSignature()
  {
    return showTextInSignatureButton.getSelection();
  }

  public void setImageShownInSignature(boolean imageShown)
  {
    showImageInSignatureButton.setSelection(imageShown);
  }

  public enum ImageMode
  {
    SCALED, FROM_IMAGE, FROM_USER, NONE
  }

  public ImageMode getImageMode()
  {
    if (!isVisibleSignature() || !shouldShowImageInSignature())
    {
      return ImageMode.NONE;
    }

    if (imgScalable.getSelection())
    {
      return ImageMode.SCALED;
    }
    else if (sizeFixedByUser.getSelection())
    {
      return ImageMode.FROM_USER;
    }
    else if (sizeFromImage.getSelection())
    {
      return ImageMode.FROM_IMAGE;
    }
    throw new IllegalStateException("Image mode could not be detected");
  }

  public boolean shouldShowImageInSignature()
  {
    return showImageInSignatureButton.getSelection() && isVisibleSignature();
  }

  public void setImageMode(String imageMode)
  {
    ImageMode mode = ImageMode.valueOf(imageMode);
    switch (mode)
    {
    case FROM_IMAGE:
      sizeFromImage.setSelection(true);
      sizeFixedByUser.setSelection(false);
      imgScalable.setSelection(false);
      break;
    case NONE:
      sizeFromImage.setSelection(false);
      sizeFixedByUser.setSelection(false);
      imgScalable.setSelection(false);
      break;
    case FROM_USER:
      sizeFixedByUser.setSelection(true);
      sizeFromImage.setSelection(false);
      imgScalable.setSelection(false);
      break;
    case SCALED:
      imgScalable.setSelection(true);
      sizeFixedByUser.setSelection(false);
      sizeFromImage.setSelection(false);
      break;

    default:
      throw new IllegalStateException("Unknown ImageMode in signature-profile!");
    }

  }

  public void setFixedImageHeight(int heightInMM)
  {
    heightOfImgBox.setText("" + heightInMM);
    if (noImgProportionCalculated())
    {
      String width = getFixedImageWidth();
      if (StringUtils.isNotEmpty(width) && StringUtils.isNumeric(width))
      {
        int widthInt = Integer.parseInt(width);
        setUserDefinedSizeProportion(heightInMM, widthInt);
      }
    }
  }

  private void setUserDefinedSizeProportion(int heightInMM, int widthInt)
  {
    userDefinedSizeProportion = (float) heightInMM / (float) widthInt;
  }

  public void setFixedImageWidth(int widthInMM)
  {
    widthOfImgBox.setText("" + widthInMM);
    if (noImgProportionCalculated())
    {
      String height = getFixedImageHeight();
      if (StringUtils.isNotEmpty(height) && StringUtils.isNumeric(height))
      {
        int heightInt = Integer.parseInt(height);
        setUserDefinedSizeProportion(heightInt, widthInMM);
      }
    }
  }

  public String getFixedImageHeight()
  {
    return heightOfImgBox.getText();
  }

  public String getFixedImageWidth()
  {
    return widthOfImgBox.getText();
  }

  public boolean isImageOnlyAndFixedSize()
  {
    return isVisibleSignature()
        && !shouldShowTextInSignature()
        && shouldShowImageInSignature()
        && (useImageSize() || useCustomImageSize());
  }

  public boolean shouldSecondSignatureYAxisBeFixed()
  {
    return alignSecondVisbleSignature.getSelection();
  }

  public void setSecondSignatureYAxisFixed(boolean enable)
  {
    alignSecondVisbleSignature.setSelection(enable);
  }


  public float getTopPosPdfUnit()
  {
    if (topPosPdfUnit == ZERO_FLOAT)
    {
      return getTopPos() * Constants.ONE_MILLIMETER_IN_PDF;
    }
    return topPosPdfUnit;
  }

  public float getLeftPosInPdfUnits()
  {
    if (leftPosInPdfUnits == ZERO_FLOAT)
    {
      return getLeftPos() * Constants.ONE_MILLIMETER_IN_PDF;
    }
    return leftPosInPdfUnits;
  }

  public boolean isUserDefinedSizeBound()
  {
    return isUserDefinedSizeBound;
  }

  public void setUserDefinedSizeBound(boolean isUserDefinedSizeBound)
  {
    this.isUserDefinedSizeBound = isUserDefinedSizeBound;
  }

  private boolean noImgProportionCalculated()
  {
    return userDefinedSizeProportion == ZERO_FLOAT;
  }

}
