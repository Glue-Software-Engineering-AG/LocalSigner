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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.LanguageConfiguration;
import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.config.resources.SecurityResources;
import ch.admin.localsigner.gui.common.InputDialog;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.PleaseWaitDialog;
import ch.admin.localsigner.gui.common.YesNoDialog;
import ch.admin.localsigner.gui.model.CertDetail;
import ch.admin.localsigner.gui.model.CertDetail.CertType;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.listener.CancelSignListener;
import ch.admin.localsigner.listener.ExitListener;
import ch.admin.localsigner.listener.SaveProfileListener;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.LocalSignerCommandLine;
import ch.admin.localsigner.main.SignatureParameters;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.admin.localsigner.update.UpdateQuery;
import ch.admin.localsigner.validation.PdfAnalyzer;
import ch.admin.localsigner.validation.SHAChecksum;
import ch.glue.securitytools.SignatureHashType;
import ch.glue.securitytools.SignatureType;
import ch.glue.securitytools.keystore.PINInvalidException;
import ch.glue.securitytools.keystore.PINWrongException;
import ch.glue.securitytools.keystore.SignerKeystore;
import ch.glue.securitytools.keystore.SignerKeystorePKCS11;
import ch.glue.securitytools.keystore.SignerKeystorePKCS12;
import ch.glue.securitytools.pdf.PdfSigner;
import ch.glue.securitytools.pkcs11.PKCS11Token;
import ch.glue.securitytools.timestamp.TimestampConfiguration;
import ch.glue.securitytools.timestamp.TimestampInfo;
import ch.glue.securitytools.timestamp.TimestampVerification;
import ch.glue.securitytools.util.CertificateInfo;
import ch.glue.securitytools.util.CertificateStore;
import ch.glue.securitytools.util.ExtensionAttributeChecker;
import java.nio.file.Paths;

/**
 * This class lets the user choose the certificate to be used for signing.
 *
 */
public class SignerGUI
{

  private static final Logger LOGGER = Logger.getLogger(SignerGUI.class);

  private static final String I18N_CANNOT_SIGN_WITH_SEAL_IF_SIGNED = "cannotSignWithSealIfSigned";

  private static final String I18N_NO_CERTIFICATES_FOUND = "noCertificatesFound";

  private static final String I18N_SIGNING_IN_PROGRESS = "signingInProgress";

  private static final String I18N_SIGN_ERROR = "signError";

  private static final String EMPTY_STRING = StringUtils.EMPTY;

  private static final String I18N_TOOL_TIP_CERTIFY = "toolTipCertify";

  private static final String I18N_PLEASE_WAIT = "pleaseWait";

  private static final String I18N_SIGN_CERT = "signCert";

  private static final String I18N_SIGN_CERT_QUALIFIED = "signCertQualified";

  private final class SignClickedHandler extends SelectionAdapter
  {

    private final Shell shell;

    private SignClickedHandler(Shell shell)
    {
      this.shell = shell;
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
      sign();
      // hide window
      shell.close();
    }
  }

  private static final String WRONG_PIN_IDENTIFIER = "WRONG_PIN_IDENTIFIER";

  private static final String INVALID_PIN_IDENTIFIER = "INVALID_PIN_IDENTIFIER";

  private static final String I18N_SEAL_CERT = "signCertRegulatedSeal";

  private static final String I18N_REGULATED_QUALIFIED = "signCertRegulatedQualified";

  private Shell parent = null;

  private SignatureParameters sigParams = null;

  private Table table = null;

  private final MainGUI maingui;

  private PleaseWaitDialog waitDialog = null;

  private Text reason;

  private Button reasonLabelVisible = null;

  private Text location;

  private Text contact;

  private Button contactLabelVisible = null;

  private Button typeCERTButton;

  private List<TsaConfiguration> tsaconfig;

  private Combo timestampingCombo;

  private Combo languageCombo;

  private Map<String, String> languages;

  private Button visibleSignatureButton;

  private final DateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd");

  private final DateFormat isoTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

  private List<CertDetail> certificates;

  private TsaConfiguration profileTsa;

  private String pkcs12Password;

  private final Font font;

  private SignerKeystore signerKeystore;

  private PKCS11Token token;

  private final String sigtype;

  private Button typeSIGButton;

  /**
   * Constructor
   *
   * @param maingui
   * Main GUI
   * @param sigParams
   * The signature parameters The number of Signatures on the Document
   */
  public SignerGUI(final MainGUI maingui, final SignatureParameters sigParams)
  {
    this.parent = maingui.getMainshell();
    this.sigParams = sigParams;
    this.maingui = maingui;
    this.font = maingui.getFont();
    this.sigtype = LocalSigner.sigtype;
  }

  /**
   * Open the signer window.
   */
  public void open()
  {
    LOGGER.debug("open SignerGUI");
    // Create the dialog window
    final Shell shell = new Shell(parent,
        SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    shell.setImage(GuiHelper.loadAppIcon(shell.getDisplay()));

    if (!createContents(shell))
    {
      return;
    }

    // in minimal mode we do not show the dialog if there is only one
    // certificate
    if (isMinimalGuiMode() && table.getItemCount() == 1)
    {
      new SignClickedHandler(shell).widgetSelected(null);
    } else
    {
      // helper values to determine the position of the dialog
      final int parentx = shell.getParent().getBounds().x;
      final int parenty = shell.getParent().getBounds().y;
      final int parentwidth = shell.getParent().getBounds().width;
      final int parentheight = shell.getParent().getBounds().height;

      shell.pack();
      // position dialog in the middle of the underlying shell
      shell.setLocation(parentx + parentwidth / 2 - shell.getBounds().width / 2,
          parenty + parentheight / 2 - shell.getBounds().height / 2);

      // finally open the shell
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
  }

  /**
   * Creates the window for selecting certificates.
   *
   * @param shell
   * Parent window
   * @return false, if no certificates could be found.
   */
  private boolean createContents(final Shell shell)
  {
    try
    {
      certificates = findCertificates();
    } catch (Pkcs11Helper.UserCanceledException uce)
    {
      LOGGER.debug("User cancelled certificate choice");
      return false;
    }

    this.loadCertificatesPkcs12();

    if (certificates.isEmpty())
    {
      // pkcs11: PIN was correct, but no cert for signing has been
      // found. (incorrect pin and cancel have special identifiers)
      //
      // no certs found, display message and return
      Message.warning(shell, LocalSigner.i18n(I18N_NO_CERTIFICATES_FOUND));
      return false;
    }

    // check for PIN error
    if (certificates.get(0).getAlias().equals(WRONG_PIN_IDENTIFIER))
    {
      // Invalid PIN has been entered. Corresponding message has already been
      // displayed. Just return
      return false;
    }

    // and check for CANCEL
    if (certificates.get(0).getAlias().equals(INVALID_PIN_IDENTIFIER))
    {
      // User cancelled. Do not display anything and return
      return false;
    }

    shell.setText(LocalSigner.i18n("chooseCertificate"));
    shell.setLayout(new GridLayout(4, false));

    final boolean canEdit = !LocalSigner.appConfig.isSystemProfilesOnly();

    // title
    final Label title = new Label(shell, SWT.SINGLE);
    title.setFont(font);
    title.setText(LocalSigner.i18n("pleaseChooseCert"));
    title.setLayoutData(
        new GridData(GridData.BEGINNING, GridData.CENTER, true, false, 3, 1));

    this.createCertificateTable(shell);
    this.loadTextLanguage();
    if (!isMinimalGuiMode())
    {
      this.createSignatureType(shell, canEdit);
      this.createSignatureDescription(shell, canEdit);
      this.createTimeStamp(shell, canEdit);
      this.createTextLanguage(shell);
    }
    this.createButtons(shell);

    // everything ok
    return true;
  }

  /**
   * Searches in the Windows Certificate store for certificates and returns them
   * in the following manner:
   *
   * @return List of certificates String[] certificate = [alias, name, issuer,
   * validity] if no certificates can be found an empty list is
   * returned.
   */
  private List<CertDetail> findCertificates() throws Pkcs11Helper.UserCanceledException
  {
    certificates = new ArrayList<CertDetail>();

    try
    {
      KeyStore ks = null;
      token = new Pkcs11Helper(parent).findCertPkcs11();
      if (token != null)
      {
        ks = token.getKeystore();
      }

      if (ks == null)
      {
        // error
        return certificates;
      }
      LOGGER.debug("Keystore size: " + ks.size());

      // iterate over all certificates and build list of them
      for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();)
      {
        try
        {
          String alias = e.nextElement();
          this.addCertificateToList(ks, alias);
        } catch (NullPointerException nep)
        {
          LOGGER.debug("Cannot access certificate", nep);
        }
      }
    } catch (java.security.ProviderException e)
    {
      LOGGER.debug("PKCS11 Exception happened", e);
      certificates.clear();
    } catch (PINWrongException pex)
    {
      LOGGER.info("Attention: PIN was wrong");
      Message.error(parent, LocalSigner.i18n("wrongPin"));

      certificates.clear();
      CertDetail certDetail = new CertDetail();
      certDetail.setAlias(WRONG_PIN_IDENTIFIER);
      certificates.add(certDetail);
    } catch (PINInvalidException pix)
    {
      // the user pressed cancel....
      LOGGER.debug("user pressed cancel in PKCS11 PIN dialog");
      certificates.clear();
      CertDetail certDetail = new CertDetail();
      certDetail.setAlias(INVALID_PIN_IDENTIFIER);
      certificates.add(certDetail);

    } catch (KeyStoreException kse)
    {
      LOGGER.debug("exception loading key store", kse);
      certificates.clear();

      Message.error(parent, String.format(LocalSigner.i18n("errorKeyStoreNotLoaded"),
          Pkcs11Helper.getPkcs11Exception(kse)));

      throw new Pkcs11Helper.UserCanceledException(); // give up
    } catch (Exception e)
    {
      LOGGER.debug("exception searching for certificates", e);
      // to be sure nothing bad happens, we return the 'empty' list
      certificates.clear();

      if (e instanceof Pkcs11Helper.UserCanceledException)
      {
        throw (Pkcs11Helper.UserCanceledException) e;
      }
    }

    return certificates;
  }

  private String getFingerprint(byte[] parentCert)
  {
    MessageDigest sha1;
    try
    {
      sha1 = MessageDigest.getInstance("SHA1");
      sha1.reset();
      sha1.update(parentCert);
      byte[] result = sha1.digest();
      return new String(Hex.encode(result));
    } catch (NoSuchAlgorithmException e)
    {
      LOGGER.error("Cannot fingerprint certificate", e);
    }

    return EMPTY_STRING;
  }

  /**
   * This method does the signing process. For signing the PdfSigner form the
   * suis-security-tools library is used.
   */
  private void sign()
  {
    // check if document already has a certification
    if (!isMinimalGuiMode() && typeCERTButton.getSelection() && this.checkCertified())
    {
      return;
    }

    // single selection mode in table just allows one, but api allows only to
    // get all
    TableItem[] selectedRows = table.getSelection();
    if (selectedRows.length == 0)
    {
      LOGGER.debug("no certificate selected in table");
      Message.warning(parent, LocalSigner.i18n("noCertificateSelected"));
      return;
    }

    // get certificate alias from table
    TableItem firstRow = selectedRows[0];
    CertDetail selectedRowAsCertDetail = (CertDetail) firstRow.getData();
    LOGGER.debug("sign with certificate alias: " + selectedRowAsCertDetail.getAlias());

    if (selectedRowAsCertDetail.isRegulatedSeal() && currentPdfIsSigned())
    {
      LOGGER.debug("User tried to sign with regulated seal, but file was already signed");
      Message.warning(parent, LocalSigner.i18n(I18N_CANNOT_SIGN_WITH_SEAL_IF_SIGNED));
      return;
    }

    this.configureSigParams();
    this.loadSignaturePropertyTranslations();
    this.showWaitDialog();

    // no error handling here, as the pin has already been entered during
    // certificate look up
    signerKeystore = getPKCS11Keystore(selectedRowAsCertDetail.getAlias());

    if (signerKeystore == null)
    {
      this.closeWaitDialog();
      Message.warning(maingui.getMainshell(),
          LocalSigner.i18n(I18N_NO_CERTIFICATES_FOUND));
      return;
    }

    // Run in a new thread (else the Swing GUI of Aladdin Quo Vadis blocks!)
    Thread t = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        signGUI();
      }

    });
    t.start();
  }

  private boolean checkCertified()
  {
    try (PdfReader reader = new PdfReader(sigParams.getInputFile());)
    {
      if (reader.getCertificationLevel() != PdfSignatureAppearance.NOT_CERTIFIED)
      {
        LOGGER.debug("PDF is already certified");
        Message.warning(parent, LocalSigner.i18n("documentCertifiedCanSign"));
        return true;
      }
    } catch (IOException e)
    {
      LOGGER.error("Cannot read PDF", e);
    }
    return false;
  }

  private boolean currentPdfIsSigned()
  {
    try
    {
      PdfAnalyzer analyzer = new PdfAnalyzer(sigParams.getInputFile());
      return analyzer.isSigned();
    } catch (IOException e)
    {
      throw new IllegalArgumentException("could not open file", e);
    }
  }

  private void configureSigParams()
  {
    if (isMinimalGuiMode())
    {
      return;
    }
    // copy values from signature interface
    sigParams.setCertification(typeCERTButton.getSelection());
    sigParams.setVisibleSignature(visibleSignatureButton.getSelection());
    sigParams.setReason(reason.getText());
    sigParams.setReasonLabelShown(reasonLabelVisible.getSelection());
    sigParams.setLocation(location.getText());
    sigParams.setContact(contact.getText());
    sigParams.setContactLabelShown(contactLabelVisible.getSelection());

    TsaConfiguration tsa = tsaconfig.get(timestampingCombo.getSelectionIndex());
    sigParams.setTsaUrl(tsa.getUrl());
    sigParams.setTsaUser(tsa.getUsername());
    sigParams.setTsaPassword(tsa.getPassword());
    LOGGER.debug("using TSA with key: " + tsa.getLookupKey());

    // save if profile is default profile
    if (maingui.getSelectedProfile().isDefaultType())
    {
      this.saveDefaultProfile(tsa);
    }
  }

  private boolean isMinimalGuiMode()
  {
    return LocalSigner.appConfig.getGuiViewMode().isMinimalMode();
  }

  private void saveDefaultProfile(TsaConfiguration tsa)
  {
    LOGGER.debug("saving default profile");

    PropertiesGUI propertiesGui = maingui.getPropertiesGui();
    propertiesGui.setCertificationType(sigParams.isCertification());
    propertiesGui.setReason(sigParams.getReason());
    propertiesGui.setReasonLabelVisible(reasonLabelVisible.getSelection());
    propertiesGui.setLocation(sigParams.getLocation());
    propertiesGui.setContact(sigParams.getContact());
    propertiesGui.setContactLabelVisible(contactLabelVisible.getSelection());

    propertiesGui.addCustomTsa(tsa);

    // save default profile
    SaveProfileListener spl = new SaveProfileListener(maingui, maingui.getPropertiesGui(),
        false, false);
    spl.setProfileTsa(profileTsa);
    spl.handleEvent(null);
  }

  private void loadSignaturePropertyTranslations()
  {
    String[] langKeys = languages.keySet().toArray(new String[0]);
    String lang;
    if (isMinimalGuiMode())
    {
      // only read, no write back
      lang = getDefaultUserLang();
    } else
    {
      // go for user specified language
      lang = langKeys[languageCombo.getSelectionIndex()];

      LOGGER.debug("signature text language: " + lang);

      // write back to user preferences
      try
      {
        LocalSigner.appConfig.setValue(Config.SIGNATURE_LANG, lang);
      } catch (ConfigurationException e)
      {
        LOGGER.error("Cannot save configuration", e);
      }
    }

    try
    {
      PropertiesConfiguration props = new PropertiesConfiguration();
      props.setDelimiterParsingDisabled(true);
      props
          .load(new File(LanguageConfiguration.getLanguageFolder() + lang + ".properties")
              .getAbsoluteFile());

      // set labels for visible signature
      if (StringUtils.isNotBlank(sigParams.getContact()))
      {
        sigParams.setContactLabel(props.getString("contact"));
      }
      if (StringUtils.isNotBlank(sigParams.getReason()))
      {
        sigParams.setReasonLabel(props.getString("reason"));
      }

      String signatureDigitallySigned = props.getString("signatureDigitallySigned");
      if (StringUtils.isNotBlank(signatureDigitallySigned))
      {
        sigParams.setSignatureDigitallySigned(signatureDigitallySigned + " ");
      }

      sigParams.setSignatureLocalTime(isoDate.format(new Date()));

      String signatureTsaTime = props.getString("signatureTsaTime");
      if (StringUtils.isNotBlank(signatureTsaTime))
      {
        sigParams
            .setSignatureTsaTime(isoDate.format(new Date()) + " " + signatureTsaTime);
      }
    } catch (ConfigurationException e)
    {
      LOGGER.error("Cannot load language file for " + lang, e);
    }
  }

  private void showWaitDialog()
  {
    if (StringUtils.isEmpty(sigParams.getTsaUrl()))
    {
      // no tsa
      waitDialog = new PleaseWaitDialog(maingui, LocalSigner.i18n(I18N_PLEASE_WAIT),
          LocalSigner.i18n(I18N_SIGNING_IN_PROGRESS));
      LOGGER.info("Signing in progress");
    } else
    {
      // tsa
      waitDialog = new PleaseWaitDialog(maingui, LocalSigner.i18n(I18N_PLEASE_WAIT),
          LocalSigner.i18n("tsaSigningInProgress"));
      LOGGER.info("TSA signing in progress");
    }
  }

  private void signGUI()
  {
    PdfSigner signer = this.createSigner();

    byte[] signedFile = signFile(signer, sigParams.isLtv(), sigParams.isEnableOcsp());
    if (signedFile == null)
    {
      LOGGER.warn("file were not signed by signer lib (returned empty byte array)");
      return;
    }

    if (!checkIfSignatureIsValid(signer, signedFile))
    {
      return;
    }

    if (!storeSignedFile(signedFile))
    {
      return;
    }

    LOGGER.debug("signing done");

    // close LocalSigner after signing?
    if (maingui.isQuitAfterSigning())
    {
      closeLocalSigner();
      return;
    }

    parent.getDisplay().syncExec(new Runnable()
    {

      @Override
      public void run()
      {
        maingui.showSignedFile(sigParams.getOutPath());
      }
    });

  }

  private void closeLocalSigner()
  {
    LOGGER.debug("Closing application after signing");
    parent.getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        new ExitListener(parent).handleEvent(null);
      }

    });
  }

  private boolean storeSignedFile(byte[] signedFile)
  {
    try
    {
      String outPath = sigParams.getOutPath();
      maingui.getDocument().getInputFile().write(Paths.get(outPath), signedFile);
      return true;
    } catch (FileWriteException e)
    {
      LOGGER.error("Error storing signed file", e);
      FileExceptionHandler.showAppropriateErrorMessage(e);
      return false;
    }
  }

  private boolean checkIfSignatureIsValid(PdfSigner signer, byte[] signedFile)
  {
    try
    {
      PdfReader reader = new PdfReader(signedFile);

      // check signature
      if (!this.signatureValid(signer.getSignatureName(), reader))
      {
        // error is caught below
        throw new IllegalStateException("Signature invalid");
      }

      // check timestamp
      Date tsaDate = timestampValid(signer.getSignatureName(), reader);
      if (tsaDate != null && sigParams.isVisibleSignature())
      {
        // show warning if signature is visible
        String text = MessageFormat.format(LocalSigner.i18n("TSAWarning"),
            isoDate.format(new Date()), isoTime.format(tsaDate));
        Message.warning(parent, text);
      }
      return true;
    } catch (Exception e)
    {
      Message.warning(parent, LocalSigner.i18n(I18N_SIGN_ERROR),
          LocalSigner.i18n("signErrorInvalidSig"));
      LOGGER.error("Error checking signed file", e);
      return false;
    }
  }

  private byte[] signFile(PdfSigner signer, boolean ltv, boolean enableOcsp)
  {
    // now start signing
    byte[] signedFile;
    try
    {
      // check the hash again
      isFileToSignUnchanged();
      signedFile = signer.sign(ltv, enableOcsp);
      this.closeWaitDialog();
    } catch (IOException ioe)
    {
      this.closeWaitDialog();

      parent.getDisplay().syncExec(new Runnable()
      {
        @Override
        public void run()
        {
          askUserIfNoTsaToSign();
        }

      });
      return null;
    } catch (Exception e)
    {
      this.closeWaitDialog();

      if (sigParams.getHash() == SignatureHashType.SHA256 && e.getCause() != null
          && e.getCause().getMessage() != null
          && e.getCause().getMessage().contains("CKR_DATA_INVALID"))
      {
        parent.getDisplay().syncExec(new Runnable()
        {
          @Override
          public void run()
          {
            askUserIfRetryWithSha1();
          }

        });
        return null;
      } else
      {
        Message.warning(parent, LocalSigner.i18n(I18N_SIGN_ERROR),
            LocalSigner.i18n("signErrorExtended"));
        LOGGER.error("Error signing file", e);
        return null;
      }
    }
    return signedFile;
  }

  private void isFileToSignUnchanged() throws NoSuchAlgorithmException, IOException
  {
    String currentHash = SHAChecksum.getChecksum(sigParams.getInputFile());
    String oldHash = maingui.getDocument().getInputFile().getChecksumOfOriginalFile();
    String oldHashMerge = maingui.getDocument().getInputFile().getChecksumOfMergedFile();
    if (currentHash.equals(oldHash) || currentHash.equals(oldHashMerge))
    {
      LOGGER.debug("Hashes are ok before signing");
    } else
    {
      throw new SecurityException("Checksums do not match - document error");
    }
  }

  private PdfSigner createSigner()
  {
    final PdfSigner signer = new PdfSigner(signerKeystore);

    signer.setProducerText("; signed by LocalSigner " + GuiHelper.getVersion());

    String tsaUrl = sigParams.getTsaUrl();
    if (StringUtils.isNotEmpty(tsaUrl))
    {
      TimestampConfiguration tsaconf = new TimestampConfiguration(tsaUrl);
      tsaconf.setTimeStampingAuthorityAccount(sigParams.getTsaUser());
      tsaconf.setTimeStampingAuthorityPassword(sigParams.getTsaPassword());
      signer.setTimestampingConfiguration(tsaconf);
      signer.setRequireTsa(true);

      LOGGER.info("Signing  with timestamp");
    } else
    {
      LOGGER.info("Signing  without timestamp");
    }

    signer.setInputFile(sigParams.getInputFile());
    signer.setSignatureVisible(sigParams.isVisibleSignature());
    signer.setAllowMultipleSignatures(sigParams.isMultipleSignature());
    if (sigParams.isCertification())
    {
      signer.setSignatureType(SignatureType.CERTIFICATION_FORM_FILLING_ALLOWED);
    } else
    {
      signer.setSignatureType(SignatureType.SIGNATURE);
    }

    if (sigParams.getSignatureBox() == null)
    {
      signer.setSignatureLeftPositionInMM(sigParams.getLeftPos());
      signer.setSignatureTopPositionInMM(sigParams.getTopPos());
      signer.setSignatureWidthInMM(sigParams.getBoxWidth());
      signer.setSignatureHeightInMM(sigParams.getBoxHeight());
    } else
    {
      signer.setSignatureBox(sigParams.getSignatureBox());
    }

    String backgroundImage = sigParams.getBackgroundImage();

    if (sigParams.isSignatureImageVisible() && StringUtils.isNotBlank(backgroundImage))
    {
      // image / text mixed
      if (sigParams.isSignatureTextVisible())
      {
        signer.setJpgImage(new File(backgroundImage));
      } else
      {
        // image only mode
        signer.setJpgBackground(new File(backgroundImage));
        signer.setHideSignatureText(true);
      }
    }

    signer.setSignatureLocation(sigParams.getLocation());
    signer.setSignatureContact(sigParams.getContact());
    signer.setSignatureContactLabel(sigParams.getContactLabel());
    signer.setSignatureContactLabelVisible(sigParams.isContactLabelShown());
    signer.setSignatureReason(sigParams.getReason());
    signer.setSignatureReasonLabel(sigParams.getReasonLabel());
    signer.setSignatureReasonLabelVisible(sigParams.isReasonLabelShown());
    signer.setTextDigitallySigned(sigParams.getSignatureDigitallySigned());
    signer.setTextLocalTime(sigParams.getSignatureLocalTime());
    signer.setTextTsaTime(sigParams.getSignatureTsaTime());

    signer.setSignaturePage(sigParams.getSignaturePage());

    if (sigParams.getSignatureField() != null)
    {
      signer.setSignatureFieldName(sigParams.getSignatureField());
    }

    if (sigParams.getHash() == null)
    {
      sigParams.setHash(SignatureHashType.SHA256);
    }

    signer.setSignatureHashType(sigParams.getHash());

    return signer;
  }

  private void askUserIfNoTsaToSign()
  {
    YesNoDialog dialog = new YesNoDialog(parent, LocalSigner.i18n("TSAErrorShort"),
        LocalSigner.i18n("TSAErrorLong"));
    boolean signNoTsa = dialog.isUserDecision();
    LOGGER.debug("Continue without TSA: " + signNoTsa);
    if (signNoTsa)
    {
      sigParams.setTsaUrl(EMPTY_STRING);
      sigParams.setTsaUser(EMPTY_STRING);
      sigParams.setTsaPassword(EMPTY_STRING);
      // retry signing
      waitDialog = new PleaseWaitDialog(maingui, LocalSigner.i18n(I18N_PLEASE_WAIT),
          LocalSigner.i18n(I18N_SIGNING_IN_PROGRESS));
      LOGGER.info("Signing in progress (no TSA)");
      signGUI();
    }
  }

  private void askUserIfRetryWithSha1()
  {
    YesNoDialog dialog = new YesNoDialog(parent, LocalSigner.i18n(I18N_SIGN_ERROR),
        LocalSigner.i18n("signErrorExtended") + "\n\n"
        + LocalSigner.i18n("signErrorHash"));
    boolean signSha1 = dialog.isUserDecision();
    LOGGER.debug("Continue with SHA1: " + signSha1);
    if (signSha1)
    {
      sigParams.setHash(SignatureHashType.SHA1);
      // retry signing
      waitDialog = new PleaseWaitDialog(maingui, LocalSigner.i18n(I18N_PLEASE_WAIT),
          LocalSigner.i18n(I18N_SIGNING_IN_PROGRESS));
      LOGGER.info("Signing in progress (SHA1)");
      signGUI();
    }
  }

  private void closeWaitDialog()
  {
    parent.getDisplay().syncExec(new Runnable()
    {
      @Override
      public void run()
      {
        waitDialog.close();
      }

    });
  }

  /**
   * Check if the new signature is valid. A signature may be invalid if the
   * windows certificate store allowed to sign with a certificate which is not
   * present (SwissSign problem when root certificates matches)
   *
   * @param sigName
   * name of new signature
   * @param reader
   * PDF of signed document
   * @return true if the signature is valid
   */
  private boolean signatureValid(final String sigName, final PdfReader reader)
  {
    try
    {
      AcroFields af = reader.getAcroFields();
      PdfPKCS7 pk = af.verifySignature(sigName);
      LOGGER.debug(
          "Check signature: " + pk.getSigningCertificate().getSubjectDN().getName());
      if (!pk.verify())
      {
        LOGGER.debug("Document modified, signature not valid");
        return false;
      }
      return true;
    } catch (SignatureException e)
    {
      LOGGER.error("Cannot validate signature", e);
    }
    return false;
  }

  /**
   * Check if the timestamp date is the same than the local date. If the dates
   * do not match, a warning dialog is displayed to the user.
   *
   * @param sigName
   * name of signature
   * @param reader
   * PDF of signed document
   * @return null if date is correct, else the date of the timestamp
   */
  private Date timestampValid(final String sigName, final PdfReader reader)
  {
    try
    {
      AcroFields af = reader.getAcroFields();
      PdfDictionary dict = af.getSignatureDictionary(sigName);
      // create root keystore (not used to validate)
      KeyStore roots = KeyStore.getInstance("JKS");
      roots.load(getClass().getClassLoader().getResourceAsStream(
          SecurityResources.TSA_TRUST_STORE), SecurityResources.TSA_TRUST_STORE_PW);

      TimestampInfo info = TimestampVerification.getTimestampInfo(dict, roots);
      if (info != null)
      {
        Date timestamp = info.getTimestamp();
        LOGGER.debug("Local time: " + new Date());
        LOGGER.debug("Signature timestamp: " + timestamp + " (" + sigName + ")");
        String dateLocal = isoDate.format(new Date());
        String dateTsa = isoDate.format(timestamp);
        if (!dateLocal.equals(dateTsa))
        {
          LOGGER.info("Local date is different to TSA date");
          return timestamp;
        }
      }
    } catch (Exception e)
    {
      LOGGER.error("Cannot check timestamp", e);
    }
    return null;
  }

  private SignerKeystore getPKCS11Keystore(final String alias)
  {
    try
    {
      if (token != null)
      {
        LOGGER.debug("create PKCS11 signer keystore");
        return new SignerKeystorePKCS11(token, alias);
      }
    } catch (Exception e)
    {
      LOGGER.error("Cannot open PKCS11 keystore", e);
    }

    LOGGER.debug("create PKCS12 signer keystore");
    try
    {
      InputStream is = new FileInputStream(LocalSigner.appConfig.getPkcs12File());
      // password is cached from certificate list
      return new SignerKeystorePKCS12(is, pkcs12Password, alias);
    } catch (Exception e)
    {
      LOGGER.error("Cannot open PKCS12 certificate", e);
    }

    return null;
  }

  private void reloadCertificates(boolean showAll)
  {
    table.removeAll();
    for (CertDetail cert : certificates)
    {
      if (cert.doShowPerDefaultInGUI() || showAll)
      {
        TableItem ti = new TableItem(table, SWT.NONE);
        ti.setFont(font);
        ti.setText(new String[]
        {
          cert.getSubjectName(), cert.getIssuer(), cert.getValidity(),
          cert.getDescription()
        });
        // hint: the whole object is stored here as 'data'
        ti.setData(cert);
      }
    }
    table.update();
    for (TableColumn tc : table.getColumns())
    {
      tc.pack();
    }

    // select certificate if there is only one
    table.setSelection(0);
  }

  private void addCertificateToList(KeyStore ks, String alias)
      throws KeyStoreException, CertificateEncodingException, GeneralSecurityException
  {
    // get entry
    X509Certificate x509cert = (X509Certificate) ks.getCertificate(alias);
    X509Certificate[] chain = null;
    Object tmpChain = ks.getCertificateChain(alias);
    if (tmpChain instanceof X509Certificate[])
    {
      chain = (X509Certificate[]) tmpChain;
    } else
    {
      LOGGER.debug("Cannot cast certificate chain");
    }

    if (chain == null || chain.length < 2)
    {
      LOGGER.debug("try to rebuild certificate chain for " + alias);
      chain = new CertificateStore().buildChain(x509cert);
    }

    String subject = x509cert.getSubjectX500Principal().toString();

    // check if private key is available for this alias
    boolean hasPrivateKey = ks.isKeyEntry(alias);
    if (!hasPrivateKey)
    {
      LOGGER.debug("Missing private key: " + subject);
      return;
    }

    LOGGER.info("Certificate found: " + subject + " (" + alias + ")");

    // check if it is a signature certificate
    // according to the standard, keyUsage SHALL be present...
    if (x509cert.getKeyUsage() != null)
    {
      boolean keyUsageDigitalSignature = x509cert.getKeyUsage()[0];
      boolean keyUsageNonRepudiation = x509cert.getKeyUsage()[1];
      boolean isARegulatedSeal = new ExtensionAttributeChecker()
          .isRegulatedSealCertificate(x509cert);
      boolean isARegulatedQualified = new ExtensionAttributeChecker()
          .isRegulatedQualifiedCertificate(x509cert);
      if (keyUsageDigitalSignature || keyUsageNonRepudiation || isARegulatedSeal
          || isARegulatedQualified)
      {
        CertType certType = evalCertType(keyUsageDigitalSignature, keyUsageNonRepudiation,
            isARegulatedSeal, isARegulatedQualified);
        convertTheCertToARowInGUI(alias, x509cert, chain, subject, certType);
      } else
      {
        LOGGER.info("Key usage not sign or non-rep: " + subject);
      }
    } else
    {
      LOGGER.info("Missing key usage: " + subject);
    }
  }

  private CertType evalCertType(boolean keyUsageDigitalSignature,
      boolean keyUsageNonRepudiation, boolean isASealCert, boolean regulatedQualified)
  {
    CertType certType = null;
    if (isASealCert)
    {
      certType = CertType.REGULATED_SEAL;
    } else if (regulatedQualified)
    {
      certType = CertType.REGULATED_QUALIFIED;
    } else if (keyUsageNonRepudiation)
    {
      certType = CertType.NON_REPUDIATION;
    } else if (keyUsageDigitalSignature)
    {
      certType = CertType.DIGITAL_SIGNATURE;
    }
    return certType;
  }

  private void convertTheCertToARowInGUI(String alias, X509Certificate x509cert,
      X509Certificate[] certChain, String subject, CertType certType)
      throws CertificateEncodingException
  {
    SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
    CertDetail row = new CertDetail();
    row.setCertType(certType);

    // the name of the owner
    String name = CertificateInfo.extractSubject(x509cert, BCStyle.CN);
    String title = CertificateInfo.extractSubject(x509cert, BCStyle.T);
    if (StringUtils.isNotBlank(title))
    {
      name += ", " + title;
    }

    row.setSubjectName(name);

    // issuer
    row.setIssuer(CertificateInfo
        .extractSubject(x509cert.getIssuerX500Principal().getName(), BCStyle.CN));

    row.setAlias(alias); // alias to find the certificate in the store

    // validity
    row.setValidity(format.format(x509cert.getNotAfter()));

    // description
    row.setDescription(EMPTY_STRING);
    if (certType == CertType.NON_REPUDIATION)
    {
      row.setDescription(LocalSigner.i18n(I18N_SIGN_CERT));
    } else if (certType == CertType.REGULATED_SEAL)
    {
      row.setDescription(LocalSigner.i18n(I18N_SEAL_CERT));
    } else if (certType == CertType.REGULATED_QUALIFIED)
    {
      row.setDescription(LocalSigner.i18n(I18N_REGULATED_QUALIFIED));
    }

    // TODO Umbau mit lokalem truststore
    // SHA1 fingerprints of ZertES qualified certificates
    List<String> qualifiedFingerprints = buildQualifiedCertsFingerprints();

    // check fingerprint against known SuisseID certificates
    String fingerprint;

    LOGGER.debug("Certificate chain: \n" + CertificateInfo.certChainToString(certChain));
    if (certChain != null && certChain.length > 1)
    {
      X509Certificate parentCert = certChain[1];
      fingerprint = getFingerprint(parentCert.getEncoded());
      LOGGER.debug("Issuer fingerprint: " + fingerprint);
    } else
    {
      LOGGER.debug("using issuer as fingerprint match");
      fingerprint = row.getIssuer();
    }

    if (qualifiedFingerprints.contains(fingerprint))
    {
      row.setDescription(LocalSigner.i18n(I18N_SIGN_CERT_QUALIFIED));
    }

    try
    {
      x509cert.checkValidity();
      certificates.add(row);
    } catch (GeneralSecurityException ge)
    {
      LOGGER.info("Certificate not valid: " + subject + " (" + ge.getMessage() + ")", ge);
    }
  }

  private List<String> buildQualifiedCertsFingerprints()
  {
    List<String> qualifiedFingerprints = new ArrayList<String>();
    // QuoVadis SuisseID (QuoVadis SuisseID Qualified CA)
    qualifiedFingerprints.add("QuoVadis SuisseID Qualified CA");
    qualifiedFingerprints.add("aa2d7445a200c79eb8860d1f5042676712269920");
    // SwissSign SuisseID (SwissSign Platinum CA - G2)
    qualifiedFingerprints.add("SwissSign Platinum CA - G2");
    qualifiedFingerprints.add("f7714398e06575d4dd84463a539b4ff7f8b71621");
    // Swisscom Diamant (Swisscom Diamant CA 1)
    qualifiedFingerprints.add("Swisscom Diamant CA 1");
    qualifiedFingerprints.add("2203afdedf374550904ffd9e7dc5ad7d7298a0a8");
    // BIT class A (AdminCA-A-T01)
    qualifiedFingerprints.add("AdminCA-A-T01");
    qualifiedFingerprints.add("7c14fd21420cbf6335cb2531725dfb8ad86a6194");
    qualifiedFingerprints.add("7df261f662a3b1e8af857c7114d1e6f3a6cd71c2");
    return qualifiedFingerprints;
  }

  private void loadCertificatesPkcs12()
  {
    File file = new File(LocalSigner.appConfig.getPkcs12File());
    if (file.exists())
    {
      LOGGER.debug("loading software certificate from " + file.getAbsolutePath());

      // try load password from settings
      pkcs12Password = LocalSigner.appConfig.getPkcs12Password();
      if (pkcs12Password == null)
      {
        // ask user with dialog
        InputDialog pwDialog = new InputDialog(maingui, file.getName(),
            LocalSigner.i18n("pkcs12Password") + ":", true);
        // cache password for later use
        pkcs12Password = pwDialog.getInput();
      }

      try
      {
        KeyStore ks = java.security.KeyStore.getInstance("PKCS12", "BC");
        ks.load(new FileInputStream(file), pkcs12Password.toCharArray());

        LOGGER.debug("PKCS12 keystore size: " + ks.size());

        for (Enumeration<String> e = ks.aliases(); e.hasMoreElements();)
        {
          String alias = e.nextElement();
          this.addCertificateToList(ks, alias);
        }
      } catch (Exception e)
      {
        LOGGER.error("Cannot load PKCS12 certificate " + file.getName() + " (" + e.getMessage() + ")", e);
      }
    }
  }

  private void createCertificateTable(final Shell shell)
  {
    // filter for all or signing certificates only
    final Button checkbox = new Button(shell, SWT.CHECK);
    checkbox.setFont(font);
    checkbox.setText(LocalSigner.i18n("showAllCerts"));
    checkbox.setSelection(LocalSigner.appConfig.isShowAllCertificates());
    checkbox
        .setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
    checkbox.addSelectionListener(new SelectionListener()
    {
      @Override
      public void widgetSelected(SelectionEvent e)
      {
        reloadCertificates(checkbox.getSelection());
        try
        {
          LocalSigner.appConfig.setValue(Config.SHOW_ALL_CERTIFICATES,
              checkbox.getSelection());
        } catch (ConfigurationException e1)
        {
          LOGGER.error("Cannot write config", e1);
        }
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e)
      {
        this.widgetSelected(e);
      }

    });
    if (isMinimalGuiMode())
    {
      checkbox.setVisible(false);
    }

    table = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
    table.setHeaderVisible(true);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
    gd.heightHint = 100;
    table.setLayoutData(gd);
    table.setLinesVisible(true);

    // columns
    final TableColumn col1 = new TableColumn(table, SWT.LEFT);
    col1.setText(LocalSigner.i18n("holderName"));
    final TableColumn col2 = new TableColumn(table, SWT.LEFT);
    col2.setText(LocalSigner.i18n("issuerName"));
    final TableColumn col3 = new TableColumn(table, SWT.LEFT);
    col3.setText(LocalSigner.i18n("certValidity"));
    final TableColumn col4 = new TableColumn(table, SWT.LEFT);
    col4.setText(LocalSigner.i18n("description"));

    this.reloadCertificates(checkbox.getSelection());

    // add listener to table for selection
    SelectionListener tableListener = new SelectionListener()
    {
      /**
       * This method is called in case of double clicking on an entry.
       */
      @Override
      public void widgetDefaultSelected(final SelectionEvent e)
      {
        sign();
        // hide window
        shell.close();
      }

      @Override
      public void widgetSelected(final SelectionEvent e)
      {
        CertDetail rowData = (CertDetail) ((TableItem) e.item).getData();
        LOGGER.debug("selected " + rowData.getAlias());
        if (rowData.isRegulatedSeal())
        {
          typeCERTButton.setSelection(true);
          typeSIGButton.setSelection(false);
          typeSIGButton.setEnabled(false);
        } else
        {
          typeSIGButton.setEnabled(true);
        }
      }

    };
    table.addSelectionListener(tableListener);
  }

  private void createSignatureType(final Shell shell, final boolean canEdit)
  {
    // signature type selection with default text from params
    Label typeLabel = new Label(shell, SWT.NONE);
    typeLabel.setFont(font);
    typeLabel.setText(LocalSigner.i18n("signatureType"));
    typeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));

    Composite group = new Composite(shell, SWT.NONE);
    group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
    GridLayout grid = new GridLayout(8, false);
    grid.verticalSpacing = 0;
    grid.horizontalSpacing = 0;
    grid.marginHeight = 0;
    grid.marginWidth = 0;
    group.setLayout(grid);

    typeSIGButton = new Button(group, SWT.TOGGLE | SWT.RADIO);
    typeSIGButton.setFont(font);
    typeSIGButton.setToolTipText(LocalSigner.i18n(I18N_TOOL_TIP_CERTIFY));
    typeSIGButton.setText(LocalSigner.i18n("signing"));
    typeSIGButton.setEnabled(canEdit);

    Label spacing = new Label(group, SWT.NONE);
    spacing.setText("       ");

    typeCERTButton = new Button(group, SWT.TOGGLE | SWT.RADIO);
    typeCERTButton.setFont(font);
    typeCERTButton.setToolTipText(LocalSigner.i18n(I18N_TOOL_TIP_CERTIFY));
    typeCERTButton.setText(LocalSigner.i18n("certifying"));
    typeCERTButton.setEnabled(canEdit);

    if (sigParams.isCertification())
    {
      typeSIGButton.setSelection(false);
      typeCERTButton.setSelection(true);
    } else
    {
      typeSIGButton.setSelection(true);
      typeCERTButton.setSelection(false);
    }

    // locks the signtype if requested from the commandline option
    if (!sigtype.equalsIgnoreCase(LocalSignerCommandLine.SIGTYPE_CHOICE))
    {
      typeSIGButton.setVisible(false);
      typeCERTButton.setVisible(false);
      typeLabel.setVisible(false);
      if (sigtype.equalsIgnoreCase(LocalSignerCommandLine.SIGTYPE_SIGN))
      {
        typeSIGButton.setSelection(true);
        typeCERTButton.setSelection(false);
      } else
      {
        typeSIGButton.setSelection(false);
        typeCERTButton.setSelection(true);
      }
    }

    try (PdfReader reader = new PdfReader(sigParams.getInputFile());)
    {
      if (reader.getCertificationLevel() != PdfSignatureAppearance.NOT_CERTIFIED)
      {
        // document is certified, only signing possible
        typeSIGButton.setSelection(true);
        typeCERTButton.setSelection(false);
        typeSIGButton.setEnabled(false);
        typeCERTButton.setEnabled(false);
      }
    } catch (IOException e)
    {
      LOGGER.debug("Cannot read PDF", e);
    }

    // visible signature
    spacing = new Label(group, SWT.NONE);
    spacing.setText("       ");
    visibleSignatureButton = new Button(group, SWT.CHECK);
    visibleSignatureButton.setFont(font);
    visibleSignatureButton.setSelection(sigParams.isVisibleSignature());
    visibleSignatureButton.setText(LocalSigner.i18n("visibleSignature"));
    visibleSignatureButton.setEnabled(canEdit);
  }

  private void createSignatureDescription(final Shell shell, final boolean canEdit)
  {
    // signature reason text field with default text from params
    addReasonLabel(shell, canEdit);
    addReasonField(shell, canEdit);

    // signature contact text field with default text from params
    addContactLabel(shell, canEdit);
    addContactField(shell, canEdit);

    // signature location text field with default text from params
    addLocationLabel(shell);
    addLocationField(shell, canEdit);

    addDateLabel(shell);
    addDateField(shell);
  }

  private void addReasonField(final Shell shell, final boolean canEdit)
  {
    reason = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
    reason.setFont(font);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
    gd.heightHint = 50;
    gd.widthHint = 200;
    reason.setLayoutData(gd);
    reason.setText(sigParams.getReason());
    reason.setToolTipText(LocalSigner.i18n("tooltipReason"));
    reason.setEnabled(canEdit);
    reason.addTraverseListener(new TraverseListener()
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
  }

  private void addReasonLabel(final Shell shell, final boolean canEdit)
  {
    Composite reasonLabelsComp = new Composite(shell, SWT.NONE);
    reasonLabelsComp.setLayout(new GridLayout(2, false));
    reasonLabelsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    Label reasonLabel = new Label(reasonLabelsComp, SWT.NONE);
    reasonLabel.setFont(font);
    reasonLabel.setText(LocalSigner.i18n("reason"));
    reasonLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));

    Label reasonLabelVisibleLabel = GuiHelper.label(reasonLabelsComp, SWT.NONE,
        LocalSigner.i18n("reasonShowLabel"), maingui.getFont());
    reasonLabelVisibleLabel
        .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    reasonLabelVisible = new Button(reasonLabelsComp, SWT.CHECK);
    reasonLabelVisible.setEnabled(canEdit);
    reasonLabelVisible.setSelection(sigParams.isReasonLabelShown());
    reasonLabelVisible.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
  }

  private void addContactField(final Shell shell, final boolean canEdit)
  {
    contact = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
    contact.setFont(font);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
    gd.heightHint = 50;
    gd.widthHint = 200;
    contact.setLayoutData(gd);
    contact.setText(sigParams.getContact());
    contact.setToolTipText(LocalSigner.i18n("tooltipContact"));
    contact.setEnabled(canEdit);
    contact.addTraverseListener(new TraverseListener()
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
  }

  private void addContactLabel(final Shell shell, final boolean canEdit)
  {
    Composite contactLabelsComp = new Composite(shell, SWT.NONE);
    contactLabelsComp.setLayout(new GridLayout(2, false));
    contactLabelsComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    Label contactLabel = new Label(contactLabelsComp, SWT.NONE);
    contactLabel.setFont(font);
    contactLabel.setText(LocalSigner.i18n("contact"));
    contactLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));

    Label contactLabelVisibleLabel = GuiHelper.label(contactLabelsComp, SWT.NONE,
        LocalSigner.i18n("contactShowLabel"), maingui.getFont());
    contactLabelVisibleLabel
        .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));

    contactLabelVisible = new Button(contactLabelsComp, SWT.CHECK);
    contactLabelVisible.setEnabled(canEdit);
    contactLabelVisible.setSelection(sigParams.isContactLabelShown());
    contactLabelVisible
        .setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
  }

  private void addDateField(final Shell shell)
  {
    Label date = new Label(shell, SWT.BORDER);
    date.setFont(font);
    date.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
    date.setText(" " + isoDate.format(new Date()) + " ");
  }

  private void addDateLabel(final Shell shell)
  {
    Label dateLabel = new Label(shell, SWT.NONE);
    dateLabel.setFont(font);
    dateLabel.setText(LocalSigner.i18n("date"));
    dateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
  }

  private void addLocationField(final Shell shell, final boolean canEdit)
  {
    location = new Text(shell, SWT.BORDER | SWT.SINGLE);
    location.setFont(font);
    location.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    location.setText(sigParams.getLocation());
    location.setToolTipText(LocalSigner.i18n("tooltipLocation"));
    location.setEnabled(canEdit);
  }

  private void addLocationLabel(final Shell shell)
  {
    Label locationLabel = new Label(shell, SWT.NONE);
    locationLabel.setFont(font);
    locationLabel.setText(LocalSigner.i18n("location"));
    locationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
  }

  private void createTimeStamp(final Shell shell, final boolean canEdit)
  {
    // timestamp
    Label enableTimestampingLabel = new Label(shell, SWT.NONE);
    enableTimestampingLabel.setFont(font);
    enableTimestampingLabel.setText(LocalSigner.i18n("usetimestamp"));
    enableTimestampingLabel
        .setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

    timestampingCombo = new Combo(shell, SWT.READ_ONLY);
    timestampingCombo.setFont(font);
    timestampingCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
    timestampingCombo.setToolTipText(LocalSigner.i18n("toolTipTsa"));
    // extract preconfigured descriptions
    tsaconfig = LocalSigner.appConfig.getTSAConfig();

    boolean customTsa = profileHasCustomTsaSet();
    if (customTsa)
    {
      // add the custom TSA
      tsaconfig.add(new TsaConfiguration(sigParams.getTsaUrl(), sigParams.getTsaUrl(),
          sigParams.getTsaUser(), sigParams.getTsaPassword(), sigParams.getTsaUrl()));
    }

    String[] descriptions = new String[tsaconfig.size()];
    for (int i = 0; i < descriptions.length; i++)
    {
      descriptions[i] = tsaconfig.get(i).getDisplayText();
    }
    timestampingCombo.setItems(descriptions);
    timestampingCombo.select(0);

    // set selected TSA
    for (int i = 0; i < tsaconfig.size(); i++)
    {
      String url = tsaconfig.get(i).getUrl();
      if (url.equals(sigParams.getTsaUrl()))
      {
        // select this
        timestampingCombo.select(i);
        profileTsa = tsaconfig.get(i);
      }
    }
    timestampingCombo.setEnabled(canEdit);

    if (canEdit)
    {
      // try to autoselect TSA for QuoVadis and SwissSign
      this.autoSelectTsa();
    }
  }

  private boolean profileHasCustomTsaSet()
  {
    // check if profile has a non-default TSA
    boolean customTsa = true;
    for (TsaConfiguration tsa : tsaconfig)
    {
      if (tsa.getUrl().equals(sigParams.getTsaUrl()))
      {
        customTsa = false;
      }
    }
    if (StringUtils.isEmpty(sigParams.getTsaUrl()))
    {
      customTsa = false;
    }
    return customTsa;
  }

  private void loadTextLanguage()
  {
    languages = new TreeMap<String, String>();
    // scan language directory for lang.properties files
    File dir = new File(LanguageConfiguration.getLanguageFolder()).getAbsoluteFile();
    File[] langs = dir.listFiles();
    if (langs != null)
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
          }
          languages.put(key, label);

        }
      }
    }
  }

  private void createTextLanguage(final Shell shell)
  {
    // signature text language
    Label sigLangLabel = new Label(shell, SWT.NONE);
    sigLangLabel.setFont(font);
    sigLangLabel.setText(LocalSigner.i18n("signatureLanguage"));

    languageCombo = new Combo(shell, SWT.READ_ONLY);
    languageCombo.setFont(font);
    languageCombo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
    languageCombo.setToolTipText(LocalSigner.i18n("toolTipSigLang"));

    // fill list and select german (de) as default
    String userLang = getDefaultUserLang();

    int i = 0;
    int select = 0;
    for (Entry<String, String> entry : languages.entrySet())
    {
      if (userLang.equals(entry.getKey()))
      {
        select = i;
      }
      languageCombo.add(entry.getValue());
      i++;
    }
    languageCombo.select(select);
  }

  // returns the signature language if configured or german (de) as default
  private String getDefaultUserLang()
  {
    String userLang = LocalSigner.appConfig.getSignatureLanguage();
    if (StringUtils.isEmpty(userLang))
    {
      userLang = "de";
    }

    return userLang;
  }

  private void createButtons(final Shell shell)
  {
    Composite buttonPanel = new Composite(shell, SWT.RIGHT);
    buttonPanel.setLayout(new GridLayout(4, false));
    buttonPanel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 4, 1));

    // exit button and exit listener
    Button cancelButton = new Button(buttonPanel, SWT.PUSH | SWT.RIGHT);
    cancelButton.setFont(font);
    cancelButton.setText(LocalSigner.i18n("cancel"));
    CancelSignListener csl = new CancelSignListener(shell);
    cancelButton.addListener(SWT.Selection, csl);

    // fillers
    new Label(buttonPanel, SWT.NONE); // dummy
    new Label(buttonPanel, SWT.NONE); // dummy

    // sign button and sign listener
    Button signButton = new Button(buttonPanel, SWT.PUSH | SWT.RIGHT);
    signButton.setFont(font);
    signButton.setText(LocalSigner.i18n("sign"));

    signButton.addSelectionListener(new SignClickedHandler(shell));
    shell.setDefaultButton(signButton);
    signButton.setFocus();
  }

  private void autoSelectTsa()
  {
    boolean isQuoVadis = false;
    boolean isSwissSign = false;
    boolean isSwissGovPKI = false;

    for (CertDetail entry : certificates)
    {
      if (entry.getIssuer() == null)
      {
        continue;
      }
      if (entry.getIssuer().contains("SwissSign"))
      {
        isSwissSign = true;
      }
      if (entry.getIssuer().contains("QuoVadis"))
      {
        isQuoVadis = true;
      }
      if (entry.getIssuer().contains("Swiss Government"))
      {
        isSwissGovPKI = true;
      }
    }

    if (isQuoVadis)
    {
      LOGGER.debug("autoselect TSA QuoVadis");
      String[] items = timestampingCombo.getItems();
      for (int i = 0; i < items.length; i++)
      {
        if ("TSA QuoVadis".equals(items[i]))
        {
          timestampingCombo.select(i);
        }
      }
    }

    if (isSwissSign)
    {
      LOGGER.debug("autoselect TSA SwissSign");
      String[] items = timestampingCombo.getItems();
      for (int i = 0; i < items.length; i++)
      {
        if ("TSA SwissSign".equals(items[i]))
        {
          timestampingCombo.select(i);
        }
      }
    }

    if (isSwissGovPKI)
    {
      LOGGER.debug("autoselect TSA Swiss Government PKI");
      String[] items = timestampingCombo.getItems();
      for (int i = 0; i < items.length; i++)
      {
        if ("TSA Swiss AdminPKI".equals(items[i]))
        {
          timestampingCombo.select(i);
        }
      }
    }

    if (UpdateQuery.getConnectionStatus() == UpdateQuery.ConnectionStatus.UNAVAILABLE)
    {
      LOGGER.debug("autoselect no TSA because of missing connection");
      timestampingCombo.select(0);
    }
  }

}
