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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.MainGUI.GuiMode;
import ch.admin.localsigner.gui.SignerGUI;
import ch.admin.localsigner.gui.common.ButtonsDialog;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.YesNoDialog;
import ch.admin.localsigner.gui.profile.ProfileFileLoader;
import ch.admin.localsigner.gui.profile.PropertiesGUI;
import ch.admin.localsigner.main.InputFile;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.SignatureParameters;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.admin.localsigner.notary.validation.IsPdfACheck;
import ch.admin.localsigner.validation.SHAChecksum;
import ch.glue.securitytools.SignatureBox;

/**
 * This class is instantiated by pressing the 'sign' button in the main view. It
 * tests the given input data and initiates the actual signing process.
 *
 */
public class SignListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(SignListener.class);

  private final MainGUI maingui;

  private final PropertiesGUI propertiesGui;

  private final Shell shell;

  private PdfReader reader;

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file
   */
  public SignListener(final MainGUI maingui)
  {
    this.propertiesGui = maingui.getPropertiesGui();
    this.maingui = maingui;
    this.shell = maingui.getMainshell();
  }

  /**
   * This method is called by pressing the 'sign' button.
   */
  @Override
  public void handleEvent(final Event event)
  {
    LOGGER.debug("handle sign mode: " + maingui.getGuiMode());
    if (maingui.getGuiMode() != GuiMode.sign)
    {
      // don't sign, but add the signature fields
      new SaveModificationListener(maingui, false).handleEvent(event);
      return;
    }

    // check if input file is given. if no input file is given we return.
    if (maingui.getInputFile() == null)
    {
      LOGGER.debug("no input file specified - abort");
      Message.warning(shell, LocalSigner.i18n("errorNoPdfSpecified"));
      return;
    }

    // check if the input file is still the same !
    if (checkSumError())
    {
      Message.warning(shell, LocalSigner.i18n("errorFileToSignHasBeenChanged"));
      return;
    }

    // check input file
    reader = checkInputFile();
    if (reader == null)
    {
      Message.warning(shell, LocalSigner.i18n("errorInvalidPdf"));
      LOGGER.debug("invalid source pdf");
      return;
    }

    // check if the file is encrypted and no modifications are allowed
    if (reader.isEncrypted())
    {
      LOGGER.debug("pdf is encrypted");
      Message.warning(shell, LocalSigner.i18n("pdfEncrypted"));
      return;
    }

    // check input file for certification level, if the file is certified
    // and changes are not allowed we inform the user and return
    if (reader.getCertificationLevel() == PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED)
    {
      Message.warning(shell, LocalSigner.i18n("documentCertified"));
      LOGGER.debug("document is certified");
      return;
    }

    // check if allow to sign document that are not PDF/A conformant
    if (!LocalSigner.appConfig.isSignNonPdfA())
    { // it shall not be possible to sign a document that is not PDF/A
      // conformant
      IsPdfACheck pdfAValidator = new IsPdfACheck();

      byte[] fileAsByteArr = maingui.getInputFile();

      if (!pdfAValidator.validate(fileAsByteArr))
      { // not a PDF/A
        LOGGER.debug("pdf is not PDF/A but config does not allow to sign non-PDF/A");
        Message.warning(shell, LocalSigner.i18n("signNonPdfANotConfigured"));
        return;
      }
    }

    // the file is ok
    final SignatureParameters sigParams = new SignatureParameters();

    // check for existing signatures
    final int sigs = countExistingSignatures();
    if (sigs > 0)
    {
      // document contains digital signatures
      // ask the user if he wants to cancel or apply multiple signatures
      final String text = MessageFormat.format(LocalSigner.i18n("multipleSignature"),
          sigs);
      final YesNoDialog dialog = new YesNoDialog(shell, LocalSigner.i18n("warning"), text);
      if (dialog.isUserDecision())
      {
        sigParams.setMultipleSignature(true);
      }
      else
      {
        LOGGER.debug("user does not want multiple signature");
        return;
      }
    }

    sigParams.setInputFile(maingui.getInputFile());

    // we should check for form pdfs and pdfs with an empty signature filed
    // to handle this cases as well.
    if (!this.checkExisingSignatureFields(sigParams))
    {
      // don't continue
      return;
    }

    // check output file
    if (!checkOutputFile())
    {
      LOGGER.debug("check output = false");
      return;
    }

    // check page for signature
    sigParams.setSignaturePage(propertiesGui.getSignaturePageSignature(reader.getNumberOfPages()));

    // check if signature image exists
    // if not, user has the possibility to sign without image.
    // the default is no signature image
    sigParams.setBackgroundImage(StringUtils.EMPTY);
    String imageFile = propertiesGui.getBackgroundImage();


    float floatBoxHeightInPixel = propertiesGui.getBoxHeightInPdfUnits();
    float floatBoxWidthInPixel = propertiesGui.getBoxWidthInPdfUnits();

    if (StringUtils.isNotEmpty(imageFile))
    {
      File image = ProfileFileLoader.loadFileFromPath(imageFile,
          propertiesGui.getProfilePath());

      if (image==null || !image.exists())
      {
        if (noImageAskUserIfStopWanted(image))
        {
          return;
        }
      }
      else
      {
        // file exists
        if (hasNoJpegExtension(imageFile))
        {
          if (invalidImageAskUserIfstopWanted())
          {
            return;
          }
        }
        else
        {
          // use the specified image
          sigParams.setBackgroundImage(image.getAbsolutePath());

          // image-only and fixed size, then use the image sizes
          if (propertiesGui.isImageOnlyAndFixedSize())
          {
            floatBoxHeightInPixel = propertiesGui.getBoxHeightImage();
            floatBoxWidthInPixel = propertiesGui.getBoxWidthImage();
          }

        }
      }
    }

    sigParams.setSignatureTextVisible(propertiesGui.shouldShowTextInSignature());
    sigParams.setSignatureImageVisible(propertiesGui.shouldShowImageInSignature());

    // check parameters for signature
    // calculate and check sizes OF THE CHOSEN PAGE!
    final Rectangle pageRect = reader.getPageSizeWithRotation(sigParams
        .getSignaturePage());
    final float leftPixel = propertiesGui.getLeftPosInPdfUnits();


    final float topPixel = (propertiesGui.getTopPosPdfUnit() + floatBoxHeightInPixel);
    final float bottomPixel = pageRect.getHeight() - topPixel;

    int xfrom = (int) leftPixel;
    int yfrom = (int) bottomPixel;

    int boxwidthInPixel = (int) (floatBoxWidthInPixel);
    int boxheightInPixel = (int) (floatBoxHeightInPixel);

    // if the sig box goes over the felft hand side
    if (xfrom < 0){
      xfrom = 0;
    }

    // if the sig box goes over the bottom
    if (yfrom < 0){
      yfrom = 0;
    }

    // if the sig box goes over the top
    if ((yfrom + boxheightInPixel) > pageRect.getHeight())
    {
       int diff = (yfrom + boxheightInPixel) - (int) pageRect.getHeight();
       boxheightInPixel = boxheightInPixel - diff - 1;
    }

    // if the sig box goes over the right hand side
    if ((xfrom + boxwidthInPixel) > pageRect.getWidth())
    {
      int diff = (xfrom + boxwidthInPixel) - (int) pageRect.getWidth();
      boxwidthInPixel = boxwidthInPixel - diff - 1;
    }


    if (!checkPositionParameters(xfrom, yfrom, boxwidthInPixel, boxheightInPixel,
        pageRect))
    {
      LOGGER.debug("check position parameters failed");
      return;
    }

    SignatureBox sigBox = new SignatureBox(Math.round(leftPixel), Math.round(bottomPixel), boxwidthInPixel,
        boxheightInPixel);

    sigParams.setSignatureBox(sigBox);

    // arguments ok - start signing
    sign(sigParams);
  }

  private boolean invalidImageAskUserIfstopWanted()
  {
    final YesNoDialog dialog2 = new YesNoDialog(shell, LocalSigner.i18n("invalidImage"),
        LocalSigner.i18n("invalidImageExtended"));
    return !dialog2.isUserDecision();
  }

  private boolean noImageAskUserIfStopWanted(File image)
  {
    LOGGER.info("Cannot find signature image " + image != null ? image.getAbsolutePath() : "");
    final YesNoDialog dialog = new YesNoDialog(shell, LocalSigner.i18n("imageNotFound"),
        LocalSigner.i18n("imageNotFoundExtended"));
    return !dialog.isUserDecision();
  }

  private boolean hasNoJpegExtension(String imageFile)
  {
    return !imageFile.toLowerCase().endsWith(".jpg")
        && !imageFile.toLowerCase().endsWith(".jpeg");
  }


  @SuppressWarnings("unchecked")
  private boolean checkExisingSignatureFields(final SignatureParameters sigParams)
  {
    final ArrayList<String> sigFieldNames = reader.getAcroFields()
        .getBlankSignatureNames();
    // reverse the list
    Collections.reverse(sigFieldNames);
    LOGGER.debug("found " + sigFieldNames.size() + " signature fields");
    if (!sigFieldNames.isEmpty())
    {
      final Map<String, String> names = new TreeMap<String, String>();
      final int POSITION_PAGE_VALUE_IDX = 0;
      for (String sf : sigFieldNames)
      {
        float[] pos = reader.getAcroFields().getFieldPositions(sf);
        names.put(sf, sf + " (" + LocalSigner.i18n("sigPage") + " " + (int) pos[POSITION_PAGE_VALUE_IDX] + ")");
      }

      final ButtonsDialog fieldsDialog = new ButtonsDialog(shell,
          LocalSigner.i18n("sigField"), new ArrayList<String>(names.values()),
          LocalSigner.i18n("sigFieldFound"));
      int selection = fieldsDialog.getUserDecision();
      LOGGER.debug("selected button " + (selection + 1));

      if (selection < 0)
      {
        LOGGER.debug("user did not choose a button, exit");
        return false;
      }

      if (selection < sigFieldNames.size())
      {
        String sigField = new ArrayList<String>(names.keySet()).get(selection);
        sigParams.setSignatureField(sigField);
        LOGGER.debug("using signature field " + sigField);
      }
    }
    return true;
  }

  private void sign(final SignatureParameters sigParams)
  {
    LOGGER.debug("prepare signing");
    sigParams.setOutPath(maingui.getOutputFile());
    sigParams.setVisibleSignature(propertiesGui.isVisibleSignature());
    sigParams.setCertification(propertiesGui.isCertificationType());
    sigParams.setLocation(propertiesGui.getLocation());
    sigParams.setReason(propertiesGui.getReason());
    sigParams.setReasonLabelShown(propertiesGui.getReasonLabelVisible());
    sigParams.setContact(propertiesGui.getContact());
    sigParams.setContactLabelShown(propertiesGui.getContactLabelVisible());

    TsaConfiguration tsa = propertiesGui.getTsaSelection();
    if (StringUtils.isNotEmpty(tsa.getUrl()))
    {
      sigParams.setTsaUrl(tsa.getUrl());
      sigParams.setTsaUser(tsa.getUsername());
      sigParams.setTsaPassword(tsa.getPassword());
    }

    sigParams.setLtv(LocalSigner.appConfig.isLtvActive());
    sigParams.setEnableOcsp(LocalSigner.appConfig.isOcspActive());

    final SignerGUI cc = new SignerGUI(maingui, sigParams);

    cc.open();
  }

  /**
   * Checks if the specified input file is a PDF file and if it has any content.
   * Returns the PdfReader representing the input file if it is valid, null
   * otherwise.
   *
   * @return PDRReader for input file data
   */
  private PdfReader checkInputFile()
  {
    PdfReader pdf;

    try
    {
      byte[] file = maingui.getInputFile();
      LOGGER.debug("check input file ");
      pdf = new PdfReader(file);
    } catch (IOException e)
    {
      LOGGER.error("error opening source pdf", e);
      return null;
    }

    LOGGER.debug("PDF has " + pdf.getNumberOfPages() + " pages");
    if (pdf.getNumberOfPages() == 0)
    {
      LOGGER.warn("pdf does not contain any pages");
      return null;
    }

    return pdf;
  }

  /**
   * Checks the following:
   * <ul>
   * <li>if the specified output file exists. Let the user decide if he wants to
   * override or cancel.</li>
   * <li>if it exists and the user wants to override it, then check if we can
   * write to it e.g. it is not currently open...This is PLATFORM DEPENDANT!</li>
   * </ul>
   *
   * @return true if output file exists
   */
  private boolean checkOutputFile()
  {
    // check if output file exists and/or if it is open
    final File file = new File(maingui.getOutputFile());
    LOGGER.debug("check output file " + file.getAbsolutePath());

    if (LocalSigner.mainGui.isInteractiveMode() && file.exists())
    {
      return handleOutputFileExists(file);
    }
    else
    {
      return canWriteOutputFile(file);
    }
  }

  private boolean canWriteOutputFile(final File file)
  {
    try
    {
      File.createTempFile("check", null, file.getParentFile()).delete();
      return true;
    } catch (IOException e)
    {
      Message.error(shell, LocalSigner.i18n("errorTargetFileNotWritable"));
      LOGGER.debug("cannot write the file to location: ", e);
      return false;
    }
  }

  private boolean handleOutputFileExists(final File file)
  {
    final YesNoDialog dialog = new YesNoDialog(shell, LocalSigner.i18n("warning"),
        LocalSigner.i18n("fileExists"));

    LOGGER.debug("user decision: " + dialog.isUserDecision());

    if (dialog.isUserDecision())
    {
      // user wants to overwrite - check if we can write the file (e.g. it is
      // not already opened)
      try
      {
        readAndWriteToBeSure(file);
        return true;
      } catch (FileWriteException e)
      {
        LOGGER.error("file can't be written: ", e);
        FileExceptionHandler.showAppropriateErrorMessage(e);
      }
    }
    return false;
  }

  private void readAndWriteToBeSure(final File outputFile) throws FileWriteException
  {
    try (OutputStream fos =
        Files.newOutputStream(outputFile.toPath(),StandardOpenOption.CREATE, StandardOpenOption.WRITE))
    {
      byte[] data = Files.readAllBytes(outputFile.toPath());

      fos.write(data);
      LOGGER.debug("file can be written");
    }catch(AccessDeniedException e)
    {
      throw new FileWriteException(e, FileWriteException.Reason.ACCESS_DENIED);
    } catch(FileSystemException e)
    {
      throw new FileWriteException(e, FileWriteException.Reason.FILE_IS_LOCKED);
    } catch (IOException e)
    {
      if(!Files.isReadable(outputFile.toPath())||!Files.isWritable(outputFile.toPath()))
      {
        throw new FileWriteException(e, FileWriteException.Reason.ACCESS_DENIED);
      }
      throw new FileWriteException(e, FileWriteException.Reason.UNSPECIFIED);
    }
  }

  /**
   * Checks the position parameters for the signature field.
   *
   * @param xfrom
   * @param yfrom
   * @param boxwidth
   * @param boxheight
   * @param rect
   * @return
   */
  private boolean checkPositionParameters(final int xfrom, final int yfrom,
      final int boxwidth, final int boxheight, final Rectangle rect)
  {
    // error checking left pos and width
    if (xfrom < 0)
    {
      return false;
    }
    if ((xfrom + boxwidth) > rect.getWidth())
    {
      Message.warning(shell, LocalSigner.i18n("sigBoxOutside"));
      LOGGER.debug("left pos or width to big");
      return false;
    }

    // error checking top pos and height
    if (yfrom < 0)
    {
      Message.warning(shell, LocalSigner.i18n("sigBoxOutside"));
      LOGGER.debug("top pos or height to big");
      return false;
    }

    if ((yfrom + boxheight) > rect.getHeight())
    {
      Message.warning(shell, LocalSigner.i18n("sigBoxOutside"));
      LOGGER.debug("top pos or height to big");
      return false;
    }
    return true;
  }

  /**
   * Checks if the given document contains any digital signatures.
   *
   * @return count of digital signatures
   */
  @SuppressWarnings("rawtypes")
  private int countExistingSignatures()
  {
    final AcroFields af = reader.getAcroFields();
    final ArrayList names = af.getSignatureNames();

    for (Object name1 : names) {
      final String name = (String) name1;
      LOGGER.debug("Signature name: " + name);
    }
    return names.size();
  }

  /**
   * Checks if the SHA of the current file to be signed is the same as of the
   * original or merged (original) document. This is necessary as a BAD program
   * may replace the user selected file after the user has loaded it into
   * LocalSigner
   *
   * @return true, if there is an error with the checksums
   */
  private boolean checkSumError()
  {
    InputFile input = maingui.getDocument().getInputFile();
    final byte[] fileToSign = input.getFileToSign();
    final String currentHash;
    try
    {
      currentHash = SHAChecksum.getChecksum(fileToSign);
    } catch (Exception e)
    {
      LOGGER.error("Cannot checksum document", e);
      return true;
    }

    // it is ok if its the original file
    if (currentHash.equals(input.getChecksumOfOriginalFile()))
    {
      LOGGER.debug("Hashes are ok (original file)");
      return false;
    }

    // otherwise it should be the merged file
    if (currentHash.equals(input.getChecksumOfMergedFile()))
    {
      LOGGER.debug("Hashes are ok (merged file)");
      return false;
    }

    LOGGER.debug("Checksums do not match - document error");
    return true;
  }

}
