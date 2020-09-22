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

import static ch.admin.localsigner.validation.OnlineValidator.UPREG_FN_MANDANT;
import static ch.admin.localsigner.validation.OnlineValidator.UPREG_FORMULAR_MANDANT;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfPKCS7;
import ch.admin.localsigner.config.resources.ImageResources;
import ch.admin.localsigner.listener.OpenFileListener;
import ch.admin.localsigner.listener.OpenFileListener.OpenableFile.FileType;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.OnlineServices;
import ch.admin.localsigner.utils.SignatureInfo;
import ch.admin.localsigner.validation.OnlineValidator;
import ch.admin.localsigner.validation.PdfAnalyzer;
import ch.admin.suis.client.core.service.to.CertificateQualification;
import ch.admin.suis.client.core.service.to.ReportCertificate;
import ch.admin.suis.client.core.service.to.ReportRevocation;
import ch.admin.suis.client.core.service.to.ReportTimestamp;
import ch.admin.suis.client.core.service.to.ShortReport;
import ch.admin.suis.client.core.service.to.SignatureCoverageStatus;
import ch.admin.suis.client.core.service.to.SignatureReport;
import ch.admin.suis.client.core.service.to.TimestampStatus;
import ch.admin.suis.client.core.service.to.ValidStatus;
import ch.admin.suis.client.core.service.to.ValidationType;
import ch.glue.securitytools.util.CertificateInfo;

/**
 * SideBar with validation report from online validator (accessed by REST
 * service).
 *
 */
public class SideBar extends Composite
{

  public static final String SWISS_REGISTER_OF_NOTARIES_COMMON_NAME_PART = ".*Swiss[ ]+Register[ ]+of[ ]+Notaries.*";

  public static final String MANDANT_I18N_PREFIX = "mandant.";

  private static final Logger LOGGER = Logger.getLogger(SideBar.class);

  private Tree signatureBox;

  private Tree attachmentsBox;

  private OpenFileListener sideBarListener;

  private OpenFileListener attachmentsBarListener;

  private final Font font;

  private final MainGUI maingui;

  private static final int SIDEBAR_WIDTH = 350;

  private final Map<TreeItem, ValidStatus> conlusionWeak = new HashMap<TreeItem, ValidStatus>();

  private final Map<TreeItem, ValidStatus> conlusionStrict = new HashMap<TreeItem, ValidStatus>();

  private final Map<TreeItem, X509Certificate> signerCerts = new HashMap<TreeItem, X509Certificate>();

  private Button strictValidation;

  private Label isOnlineCheck;

  private Label isOnlineCheckImg;

  private ValidStatus overallValidity;

  private SashForm form;

  private Composite commonComposite;


  public SideBar(Composite parent, MainGUI maingui)
  {
    super(parent, SWT.BORDER);

    setLayout(new GridLayout(2, false));

    // init stuff
    this.maingui = maingui;
    this.font = maingui.getFont();

    createCommonInfo();
    createSplitPanel();
    createSignatureBox();
    createAttachmentsBox();

    form.setWeights(new int[]
    {
        70, 30
    });
  }

  private void createSplitPanel()
  {
    form = new SashForm(this, SWT.VERTICAL);
    form.setVisible(true);

    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 2);
    gd.widthHint = SIDEBAR_WIDTH;

    form.setLayout(new GridLayout());
    form.setLayoutData(gd);
  }

  private void createSignatureBox()
  {
    Group signatureContainer = new Group(form, SWT.NONE);
    signatureContainer.setLayout(new GridLayout());

    Label signaturesTitle = new Label(signatureContainer, SWT.NONE);
    signaturesTitle.setText(LocalSigner.i18n("sideBar.signaturesTitle"));

    signatureBox = new Tree(signatureContainer, SWT.BORDER);
    signatureBox.setLayoutData(new GridData(GridData.FILL_BOTH));

    sideBarListener = new OpenFileListener(signatureBox);
    signatureBox.addListener(SWT.MouseDoubleClick, sideBarListener);
  }

  private void createAttachmentsBox()
  {
    Group attachmentsContainer = new Group(form, SWT.NONE);
    attachmentsContainer.setLayout(new GridLayout());

    Label attachmentsTitle = new Label(attachmentsContainer, SWT.NONE);
    attachmentsTitle.setText(LocalSigner.i18n("sideBar.attachementsTitle"));

    attachmentsBox = new Tree(attachmentsContainer, SWT.BORDER);

    attachmentsBox.setLayoutData(new GridData(GridData.FILL_BOTH));
    attachmentsBarListener = new OpenFileListener(attachmentsBox);
    attachmentsBox.addListener(SWT.MouseDoubleClick, attachmentsBarListener);
  }

  private void createCommonInfo()
  {
    commonComposite = new Composite(this, SWT.FILL);
    commonComposite.setLayout(new GridLayout(2, false));
    GridData gd = new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1);
    commonComposite.setLayoutData(gd);

    isOnlineCheckImg = new Label(commonComposite, SWT.NONE);

    isOnlineCheck = new Label(commonComposite, SWT.NONE);
    isOnlineCheck.setFont(font);
    commonComposite.pack();
  }

  /**
   * Update the side bar when a new PDF is loaded.
   *
   * @param analyzer
   *          PDF analyzer results
   */
  public void update(PdfAnalyzer analyzer)
  {
    // remove old tree
    signatureBox.removeAll();
    attachmentsBox.removeAll();
    conlusionStrict.clear();
    conlusionWeak.clear();
    overallValidity = null;

    if (analyzer == null)
    {
      return;
    }

    // certification
    if (analyzer.hasCertification())
    {
      GuiHelper.treeItem(signatureBox, LocalSigner.i18n("validationCertified"), font)
          .setImage(GuiHelper.loadImage(ImageResources.IMG_ROSETTE));
    }

    // trigger events
    if (analyzer.hasTriggerEvents())
    {
      GuiHelper.treeItem(signatureBox, LocalSigner.i18n("validationTrigger"), font)
          .setImage(GuiHelper.loadImage(ImageResources.IMG_LIGHTNING));
    }

    this.updatePdfAStatus(analyzer);

    SignatureCoverageStatus signedAndModified = analyzer.isSignedAndModified();

    if (SignatureCoverageStatus.MODIFIED_INVALID.equals(signedAndModified))
    {
      GuiHelper.treeItem(signatureBox,
          LocalSigner.i18n("sideBar.documentChangedAfterSignature"), font).setImage(
          GuiHelper.validatorImage(ValidStatus.INVALID));
    }

    this.updateSignature(analyzer);
    this.updateBlankSignature(analyzer);
    this.updateAttachment(analyzer);
    this.updateFields(analyzer);

    if (signatureBox.getItemCount() == 0)
    {
      TreeItem itm = GuiHelper.treeItem(signatureBox, LocalSigner.i18n("validationEmpty"), font);
      GuiHelper.treeItem(itm, LocalSigner.i18n("checkDocumentSignatures"), font);
      GuiHelper.treeItem(itm, LocalSigner.i18n("checkDocumentSigFields"), font);
      GuiHelper.treeItem(itm, LocalSigner.i18n("checkDocumentFields"), font);
      GuiHelper.treeItem(itm, LocalSigner.i18n("checkDocumentAttachments"), font);
    }

    // expand all items to level 1
    for (TreeItem i : signatureBox.getItems())
    {
      i.setExpanded(true);
    }
  }

  private void updatePdfAStatus(final PdfAnalyzer analyzer)
  {
    if (analyzer.getValidationResults().isSupportedPdfA() && !analyzer.getValidationResults().isError()
        && analyzer.getValidationResults().isCompliant())
    {
      String compliantStandard = analyzer.getValidationResults().getValidatedFlavourAsString();
      String i18n = LocalSigner.i18n("format.isPDFA");

      GuiHelper.treeItem(signatureBox, String.format(i18n, compliantStandard), font);
    }
    else
    {
      GuiHelper.treeItem(signatureBox, LocalSigner.i18n("format.noPDFA"), font);
    }
  }

  private void updateSignature(final PdfAnalyzer analyzer)
  {
    Map<Integer, SignatureInfo> signatures = analyzer.getSignatures();

    boolean onlineValidatorUp = new OnlineServices(LocalSigner.appConfig).isOnlineValidatorUp();
    setStatusOnline(onlineValidatorUp);

    List<TreeItem> signatureItems = new LinkedList<TreeItem>();
    List<String> mandants = new LinkedList<String>();

    try
    {
      String issuerDN = null;
      String signerCertCN = null;
      for (final SignatureInfo sig : signatures.values())
      {
        PdfPKCS7 pk = sig.getPkcs7();
        String name = sig.getName();

        Certificate[] chain = pk.getSignCertificateChain();
        X509Certificate signerCert = (X509Certificate) chain[0];
        X509Certificate issuerCert = null;
        if (chain.length > 1)
        {
          issuerCert = (X509Certificate) chain[1];
        }

        signerCertCN = CertificateInfo.extractSubject(signerCert, BCStyle.CN);
        String issuerCertCN = CertificateInfo.extractSubject(signerCert.getIssuerX500Principal().getName(), BCStyle.CN);

        issuerDN = signerCert.getIssuerDN().getName();

        final TreeItem sigItem = GuiHelper.treeItem(signatureBox, signerCertCN, font);
        signerCerts.put(sigItem, signerCert);

        TreeItem revItem = GuiHelper.treeItem(sigItem,
            LocalSigner.i18n("validationRevision") + " " + sig.getRevision(), font);
        revItem.setImage(GuiHelper.loadImage(ImageResources.IMG_ACROBAT));
        this.sideBarListener.register(revItem, "rev" + sig.getRevision(), FileType.PDF, sig.getRevisionData());

        TreeItem sigName = GuiHelper.treeItem(sigItem,
            LocalSigner.i18n("validationSignatureLabel") + " " + signerCertCN, font);
        sigName.setImage(GuiHelper.loadImage(ImageResources.IMG_KEY));
        this.sideBarListener.register(sigName, name, FileType.CERT, signerCert.getEncoded());

        if (chain.length <= 1 && signerCertCN.equals(issuerCertCN))
        {
          LOGGER.debug("Self signed certificate");
          sigName.setText(sigName.getText() + " (" + LocalSigner.i18n("validationSelfSigned") + ")");
        }
        else
        {
          TreeItem sigIssuer = GuiHelper.treeItem(sigItem,
              LocalSigner.i18n("validationIssuerLabel") + " " + issuerCertCN, font);
          sigIssuer.setImage(GuiHelper.loadImage(ImageResources.IMG_KEY));
          if (issuerCert != null)
          {
            this.sideBarListener.register(sigIssuer, issuerCertCN, FileType.CERT, issuerCert.getEncoded());
          }
        }

        signatureItems.add(sigItem);
        mandants.add(getActualTenantForIssuerDNOrSubjectCN(issuerDN, signerCertCN, mandants));
      }

      if (!signatureItems.isEmpty() && onlineValidatorUp)
      {
        startOnlineValidationInThread(analyzer, signatureItems, mandants);
      }

    } catch (Exception ex)
    {
      LOGGER.error("Cannot read signature", ex);
    }
  }

  private void setStatusOnline(boolean onlineValidatorUp)
  {
    if (onlineValidatorUp)
    {
      isOnlineCheckImg.setImage(GuiHelper.validatorImage(ValidStatus.VALID));
      isOnlineCheck.setText(LocalSigner.i18n("sideBar.onlineCheck"));
    }
    else
    {
      isOnlineCheckImg.setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));
      isOnlineCheck.setText(LocalSigner.i18n("sideBar.localCheckOnly"));
    }
    commonComposite.pack();
  }

  private void startOnlineValidationInThread(final PdfAnalyzer analyzer,
      final List<TreeItem> sigItems, final List<String> mandants)
  {
    OnlineValidator validator = new OnlineValidator(maingui, sigItems, analyzer, mandants);
    maingui.getMainshell().getDisplay().asyncExec(validator);
  }

  /**
   * Update results from online check.
   *
   * @param parentItem
   *          Parent in tree
   * @param sigReport
   *          validator response with short reports
   * @param actualMandant
   *          The name of the main mandant as fallback if mandant is not given in sigReport
   */
  public void updateOnline(TreeItem parentItem, SignatureReport sigReport, String actualMandant)
  {
    LOGGER.debug("updating online validator results");
    Map<ValidationType, ShortReport> reports = new EnumMap<ValidationType, ShortReport>(
        ValidationType.class);
    for (ShortReport sr : sigReport.getReports())
    {
      reports.put(sr.getType(), sr);
    }

    // update image!!!
    if (overallValidity == null)
    {
      // init
      overallValidity = sigReport.isValid();
    }
    else
    {
      // get least
      switch (overallValidity)
      {
      case INVALID:
        // do nothing, it can't get better anymore
        break;
      case UNSURE:
        if (sigReport.isValid() == ValidStatus.INVALID)
        {
          overallValidity = ValidStatus.INVALID;
        }
        break;
      case VALID:
      case INFO:
        if (sigReport.isValid() == ValidStatus.INVALID)
        {
          overallValidity = ValidStatus.INVALID;
        }
        else if (sigReport.isValid() == ValidStatus.UNSURE)
        {
          overallValidity = ValidStatus.UNSURE;
        }
      default:
        break;
      }
    }

    ReportCertificate certDetail = sigReport.getCertificateDetails();

    if (certDetail != null)
    {
      if (certDetail.isApproved())
      {
        if (certDetail.isSwiss())
        {
          GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationApprovedSwiss"),
              font);
        }
        else
        {
          GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationApproved"), font);
        }
      }
      else
      {
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationNotApproved"), font);
      }

      if (certDetail.isHardware())
      {
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationSigHardware"), font);
      }
      else
      {
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationSigSoftware"), font);
      }

      if (certDetail.getQualification() == CertificateQualification.QUALIFIZIERT)
      {
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationQualifiziert"), font);
      }
      if (certDetail.getQualification() == CertificateQualification.FORTGESCHRITTEN)
      {
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationFortgeschritten"),
            font);
      }
    }

    ShortReport integrity = reports.get(ValidationType.INTEGRITY);
    ShortReport certificate = reports.get(ValidationType.CERTIFICATE);
    ShortReport revocation = reports.get(ValidationType.REVOCATION);
    ShortReport timestamp = reports.get(ValidationType.TIMESTAMP);
    ShortReport mandator = reports.get(ValidationType.MANDATOR);

    this.updateOnlineIntegrity(parentItem, integrity);
    this.updateOnlineCertificate(parentItem, certificate, sigReport.getCertificateDetails());
    this.updateOnlineRevocation(parentItem, revocation, sigReport.getRevocationDetails());
    this.updateOnlineTimestamp(parentItem, timestamp, sigReport.getTimestampDetails());
    this.updateOnlineMandator(parentItem, mandator, actualMandant);

    ValidStatus weak = this.checkWeakValidation(integrity, certificate, revocation,
        timestamp, mandator, sigReport.getTimestampDetails());
    ValidStatus strict = this.checkStrictValidation(integrity, certificate, revocation,
        timestamp, mandator, sigReport.getTimestampDetails());

    // Coclusion in bold font
    FontData[] fd = font.getFontData();
    fd[0].setStyle(SWT.BOLD);
    Font bold = new Font(maingui.getMainshell().getDisplay(), fd);

    TreeItem conclusion = GuiHelper.treeItem(parentItem, "", bold);
    this.conlusionWeak.put(conclusion, weak);
    this.conlusionStrict.put(conclusion, strict);

    this.updateConclusion();
  }

  private void updateConclusion()
  {
    boolean strict = isStrictValidation();
    Map<TreeItem, ValidStatus> list;
    if (strict)
    {
      LOGGER.debug("strict validation");
      list = conlusionStrict;
    }
    else
    {
      LOGGER.debug("weak validation");
      list = conlusionWeak;
    }

    for (Entry<TreeItem, ValidStatus> item : list.entrySet())
    {
      // update each conclusion item
      ValidStatus valid = item.getValue();
      item.getKey().setImage(GuiHelper.validatorImage(valid));
      switch (valid)
      {
      case VALID:
      case INFO:
        item.getKey().setText(LocalSigner.i18n("validationConclusion"));
        break;
      case INVALID:
        item.getKey().setText(LocalSigner.i18n("validationConclusionFail"));
        break;
      case UNSURE:
        item.getKey().setText(LocalSigner.i18n("validationConclusionUnsure"));
        break;
      }
    }
  }

  private void updateOnlineIntegrity(TreeItem parentItem, ShortReport report)
  {
    if (report == null)
    {
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));

      return;
    }

    ValidStatus integrityStatus = report.getValid();
    switch (integrityStatus)
    {
    case VALID:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationSig"), font).setImage(
          GuiHelper.validatorImage(integrityStatus));
      break;
    default:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationSigFail"), font)
          .setImage(GuiHelper.validatorImage(integrityStatus));
    }
  }

  private void updateOnlineCertificate(TreeItem parentItem, ShortReport report,
      ReportCertificate details)
  {
    if (report == null)
    {
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));

      return;
    }

    X509Certificate cert = signerCerts.get(parentItem);
    String date = this.formatDate(cert.getNotAfter(), false);

    ValidStatus certStatus = report.getValid();
    switch (certStatus)
    {
    case VALID:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationCert") + " " + date,
          font).setImage(GuiHelper.validatorImage(certStatus));
      break;
    default:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationCertFail"), font)
          .setImage(GuiHelper.validatorImage(certStatus));
    }
  }

  private void updateOnlineRevocation(TreeItem parentItem, ShortReport report,
      ReportRevocation details)
  {
    if (report == null)
    {
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));

      return;
    }

    String date = "?";
    if (details != null)
    {
      date = this.formatDate(details.getDate(), false);
    }

    ValidStatus revokStatus = report.getValid();
    switch (revokStatus)
    {
    case VALID:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevok"), font).setImage(
          GuiHelper.validatorImage(revokStatus));
      break;
    case INVALID:
      GuiHelper.treeItem(parentItem,
          LocalSigner.i18n("validationRevokFail") + " " + date, font).setImage(
          GuiHelper.validatorImage(revokStatus));
      break;
    default:
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(revokStatus));
    }
  }

  private void updateOnlineTimestamp(TreeItem parentItem, ShortReport report,
      ReportTimestamp details)
  {
    if (report == null)
    {
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));

      return;
    }

    if (details != null)
    {
      // Format: TimestampStatus;String(Subject DN);long(signature time)
      TimestampStatus status = details.getStatus();
      String subject = details.getSubject();
      if (subject != null)
      {
        subject = CertificateInfo.extractSubject(subject, BCStyle.CN);
      }
      String date = this.formatDate(details.getSignatureDate(), true);

      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationSigTimeLabel") + ": "
          + date, font);

      switch (status)
      {
      case VALID:
        GuiHelper.treeItem(parentItem,
            LocalSigner.i18n("validationTsa") + " (" + subject + ")", font).setImage(
            GuiHelper.validatorImage(ValidStatus.VALID));
        break;
      case MISSING:
        // local computer time
        GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationTsaMissing"), font)
            .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));
        break;
      default:
        GuiHelper.treeItem(parentItem,
            LocalSigner.i18n("validationTsaFail") + " (" + subject + ")", font).setImage(
            GuiHelper.validatorImage(ValidStatus.INVALID));
      }
    }
  }

  private void updateOnlineMandator(TreeItem parentItem, ShortReport report,
      String actualMandant)
  {
    if (report == null)
    {
      GuiHelper.treeItem(parentItem, LocalSigner.i18n("validationRevokUnsure"), font)
          .setImage(GuiHelper.validatorImage(ValidStatus.UNSURE));

      return;
    }

    ValidStatus mandantStatus = report.getValid();

    switch (mandantStatus)
    {
    case VALID:
      GuiHelper.treeItem(parentItem,
          getValidSignatureTranslationByTenant(actualMandant), font)
          .setImage(GuiHelper.validatorImage(mandantStatus));
      break;
    default:
      GuiHelper.treeItem(parentItem,
          getInvalidSignatureTranslationByTenant(actualMandant), font)
          .setImage(GuiHelper.validatorImage(mandantStatus));
    }
  }

  /**
   * Get actual signature tenant dependent on issuerDN, subjectCN or existing UPREG_FN_MANDANT
   * in previous signatures. The complete List of tenants is relevant to display a correct message
   * for valid and invalid signatures.
   *
   * Replaces in a more generic manner the earlier tenants.xml mechanisms.
   *
   * @param issuerDN DN of Issuer of current signing certificate
   * @param subjectCN CN of current signing certificate
   * @param preceedingTenants
   * @return UPREG_FORMULAR_MANDANT if SWISS_REGISTER_OF_NOTARIES_COMMON_NAME is part of IssuerDN
   *         UPREG_FN_MANDANT if SWISS_REGISTER_OF_NOTARIES_COMMON_NAME is part of subjectCN
   *         UPREG_FN_MANDANT for all signatures following an UPREG_FN_MANDANT signature
   *         default tenant Qualified or FullQualified as configured otherwise
   */
  protected static String getActualTenantForIssuerDNOrSubjectCN(String issuerDN, String subjectCN, List<String> preceedingTenants)
  {

    if (Pattern.matches(SWISS_REGISTER_OF_NOTARIES_COMMON_NAME_PART, subjectCN) ||
        UPREG_FN_MANDANT.equals(OnlineValidator.getMainTenant(preceedingTenants)))
    {
      return UPREG_FN_MANDANT;
    }

    if (Pattern.matches(SWISS_REGISTER_OF_NOTARIES_COMMON_NAME_PART, issuerDN))
    {
      return UPREG_FORMULAR_MANDANT;
    }

    return LocalSigner.appConfig.getDefaultTenant();
  }

  protected static String getValidSignatureTranslationByTenant(String tenant)
  {
    return LocalSigner.i18n(MANDANT_I18N_PREFIX + tenant + ".valid");
  }

  protected static String getInvalidSignatureTranslationByTenant(String tenant)
  {
    return LocalSigner.i18n(MANDANT_I18N_PREFIX + tenant + ".invalid");
  }

  private void updateBlankSignature(PdfAnalyzer analyzer)
  {
    List<String> blankSigs = analyzer.getBlankSignatures();
    TreeItem blankItem = null;
    if (!blankSigs.isEmpty())
    {
      blankItem = GuiHelper.treeItem(signatureBox,
          LocalSigner.i18n("checkDocumentSigFields"), font);
    }
    for (String name : blankSigs)
    {
      GuiHelper.treeItem(blankItem, name, font).setImage(
          GuiHelper.loadImage(ImageResources.IMG_PAGE_WITH_KEY));
    }
  }

  private void updateAttachment(PdfAnalyzer analyzer)
  {
    Map<String, PRStream> atts = analyzer.getAttachments();

    if (atts.isEmpty())
    {
      GuiHelper.treeItem(attachmentsBox, LocalSigner.i18n("noAttachementsInBox"), font);
    }
    else
    {
      TreeItem attsItem = GuiHelper.treeItem(attachmentsBox,
          LocalSigner.i18n("checkDocumentAttachments"), font);

      for (String name : atts.keySet())
      {
        TreeItem item = GuiHelper.treeItem(attsItem, name, font);
        item.setImage(GuiHelper.loadImage(ImageResources.IMG_PAPERCLIP));
        attachmentsBarListener.register(item, name, FileType.ATTACHMENT, atts.get(name));
      }
      attsItem.setExpanded(true);
    }
  }

  private void updateFields(PdfAnalyzer analyzer)
  {
    List<String> fields = analyzer.getAcroFieldsNotSignature();
    TreeItem fieldsItem = null;
    if (!fields.isEmpty())
    {
      fieldsItem = GuiHelper.treeItem(signatureBox,
          LocalSigner.i18n("checkDocumentFields"), font);
    }
    for (String name : fields)
    {
      GuiHelper.treeItem(fieldsItem, name, font).setImage(
          GuiHelper.loadImage(ImageResources.IMG_EDIT));
    }
  }

  private String formatDate(Date date, boolean time)
  {
    if (date == null)
    {
      return null;
    }

    if (time)
    {
      SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
      format.setTimeZone(TimeZone.getTimeZone("UTC"));
      return format.format(date) + " UTC";
    }
    else
    {
      SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
      return format.format(date);
    }
  }

  private ValidStatus checkWeakValidation(ShortReport integrity, ShortReport certificate,
      ShortReport revocation, ShortReport timestamp, ShortReport mandator, ReportTimestamp tsaDetails)
  {

    if (integrity != null && integrity.getValid() != ValidStatus.VALID)
    {
      LOGGER.debug("weak nok: integrity " + integrity.getValid());
      return ValidStatus.INVALID;
    }
    if (certificate != null && certificate.getValid() != ValidStatus.VALID)
    {
      LOGGER.debug("weak nok: certificate " + certificate.getValid());
      return ValidStatus.INVALID;
    }

    // mandator may be invalid
    if (revocation != null && revocation.getValid() == ValidStatus.INVALID)
    {
      LOGGER.debug("weak nok: revocation " + revocation.getValid());
      return ValidStatus.INVALID;
    }
    if (revocation != null && revocation.getValid() == ValidStatus.UNSURE)
    {
      if (tsaDetails != null
          && (tsaDetails.getStatus() == TimestampStatus.VALID || tsaDetails.getStatus() == TimestampStatus.MISSING))
      {
        LOGGER.debug("weak unsure: revocation " + revocation.getValid());
        return ValidStatus.UNSURE;
      }
      else
      {
        LOGGER.debug("weak nok: revocation " + revocation.getValid());
        return ValidStatus.INVALID;
      }
    }
    // revocation.getValid() == ValidStatus.VALID

    if (timestamp != null && timestamp.getValid() != ValidStatus.VALID)
    {
      if (tsaDetails != null && tsaDetails.getStatus() != TimestampStatus.MISSING)
      {
        // timestamp not missing, it is really invalid
        LOGGER.debug("weak nok: timestamp " + timestamp.getValid());
        return ValidStatus.INVALID;
      }
    }

    // all checks VALID
    LOGGER.debug("weak ok");
    return ValidStatus.VALID;
  }

  private ValidStatus checkStrictValidation(ShortReport integrity,
      ShortReport certificate, ShortReport revocation, ShortReport timestamp,
      ShortReport mandator, ReportTimestamp tsaDetails)
  {
    if (integrity == null || certificate == null || revocation == null ||
        timestamp == null || mandator == null || tsaDetails == null)
    {
      LOGGER.debug("weak nok: one of the ShortReports is NULL");
      return ValidStatus.UNSURE;
    }

    if (integrity.getValid() != ValidStatus.VALID)
    {
      LOGGER.debug("strict nok: integrity " + integrity.getValid());
      return ValidStatus.INVALID;
    }
    if (certificate.getValid() != ValidStatus.VALID)
    {
      LOGGER.debug("strict nok: certificate " + certificate.getValid());
      return ValidStatus.INVALID;
    }
    if (mandator.getValid() != ValidStatus.VALID)
    {
      LOGGER.debug("strict nok: mandator " + mandator.getValid());
      return ValidStatus.INVALID;
    }

    if (revocation.getValid() == ValidStatus.INVALID)
    {
      LOGGER.debug("strict nok: revocation " + revocation.getValid());
      return ValidStatus.INVALID;
    }
    if (revocation.getValid() == ValidStatus.UNSURE)
    {
      if (tsaDetails.getStatus() == TimestampStatus.VALID
          || tsaDetails.getStatus() == TimestampStatus.MISSING)
      {
        LOGGER.debug("strict unsure: revocation " + revocation.getValid());
        return ValidStatus.UNSURE;
      }
      else
      {
        LOGGER.debug("strict nok: revocation " + revocation.getValid());
        return ValidStatus.INVALID;
      }
    }
    // revocation.getValid() == ValidStatus.VALID

    if (timestamp.getValid() != ValidStatus.VALID)
    {
      if (tsaDetails.getStatus() != TimestampStatus.MISSING)
      {
        // timestamp not missing, it is really invalid
        LOGGER.debug("strict nok: timestamp " + timestamp.getValid());
        return ValidStatus.INVALID;
      }
      else
      {
        LOGGER.debug("strict unsure: timestamp " + timestamp.getValid());
        return ValidStatus.UNSURE;
      }
    }

    // all checks VALID
    LOGGER.debug("strict ok");
    return ValidStatus.VALID;
  }

  private boolean isStrictValidation()
  {
    if (strictValidation == null)
    {
      return false;
    }
    return strictValidation.getSelection();
  }
}
