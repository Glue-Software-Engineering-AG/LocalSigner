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
package ch.admin.localsigner.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfReader;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.viewer.ConvertToPDFATemplate;
import ch.admin.localsigner.main.exception.FileOpenException;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.admin.localsigner.utils.ColorToConvert;
import ch.admin.localsigner.utils.SignaturePreview;
import ch.admin.localsigner.validation.PdfAnalyzer;
import ch.admin.localsigner.validation.SHAChecksum;
import ch.glue.securitytools.pdf.PdfAttacher;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * This class handles the input files.
 *
 */
public class InputFile
{
  private static final Logger LOGGER = Logger.getLogger(InputFile.class);
  // files
  private String inputFileName;

  private byte[] tempFileBytes = null;

  private String attachment = StringUtils.EMPTY;

  private byte[] inputFileBytes = null;

  private byte[] mergedFileBytes = null;

  // checksums
  private String originalChecksum;

  private String mergedChecksum;

  public void setMainGui(MainGUI mainGui)
  {
    this.mainGui = mainGui;
  }

  private MainGUI mainGui = null;

  private boolean certified;

  private boolean signed;

  private boolean blankSignatures;

  private int pages;

  private String pdfVersion;

  private PdfAnalyzer analyzer;

  private boolean attachments;

  private InputFileLocking fileLocking = new InputFileLocking();
  /**
   * Constructor
   *
   * @param mainGui
   *            Main GUI
   */
  public InputFile(final MainGUI mainGui) {
    this.mainGui = mainGui;
  }

  public InputFile() {}

  public String getInputFileName() {
    return inputFileName;
  }

  /**
   * Updates the attachment of this Input File. The current input file is merged with the specified file. <br>
   * This method is called whenever the user changes the attachment path in the GUI.
   *
   * @param attach
   *            the file to attach to the current input file.<br>
   *            It should never be <code>null</code> as it is taken from the textfield.getText() method which returns
   *            an empty string if the text field is empty.
   */
  public void updateAttachment(final String attach) {
    // we can not merge anything if this InputFile instance has not
    // been initialized with an original File
    if (inputFileBytes == null) {
      return;
    }

    if (attach.equals(this.attachment)) {
      // already merged or no changes needed
      LOGGER.debug("updateAttachment: no changes needed");
      return;
    }

    if (!canModify())
    {
      LOGGER.debug("no attachment, cannot modify document");
      return;
    }

    // otherwise the given attachment is not the stored one
    if (attach.equals(StringUtils.EMPTY))
    {
      LOGGER.debug("updateAttachment: attachment is empty");
      // there is no attachment for this file
      this.attachment = StringUtils.EMPTY;
      this.mergedFileBytes = null;
      return;
    }

    // merge is needed
    LOGGER.debug("updateAttachment: merge needed");
    this.mergedFileBytes = merge(attach);
    if (mergedFileBytes == null)
    {
      LOGGER.error("Error merging attachment to inputdocument");
      return;
    }
    // store attachment
    this.attachment = attach;

    // finally calculate and store checksum of merged file
    try
    {
      this.mergedChecksum = SHAChecksum.getChecksum(mergedFileBytes);
    } catch (Exception e)
    {
      this.mergedChecksum = null;
      LOGGER.error("Cannot checksum document", e);
    }

    LOGGER.debug("Merge OK");
  }

  /**
   * Returns the file to be signed depending on whether the original file has been merged with an attachment or not.
   *
   * @return The original file if it has not been merged with an attachment or the merged file otherwise.
   *         <code>null</code> is returned if this InputFile instance contains no file.
   */
  public byte[] getFileToSign()
  {
    if (inputFileBytes == null)
    {
      // if not initialized -> return null (should never happen)
      return null;
    }

    if (tempFileBytes != null)
    {
      LOGGER.debug("return temp file");
      return this.tempFileBytes;
    } else if (this.mergedFileBytes != null)
    {
      LOGGER.debug("return merged file");
      return this.mergedFileBytes;
    } else
    {
      LOGGER.debug("return original file");
      return this.inputFileBytes;
    }
  }

  /**
   * Returns the file to be displayed in the browser window. Depending on the attachment and preview settings this is
   * the either:
   * <ul>
   * <li>The original file.</li>
   * <li>The original file with the signature preview box.</li>
   * <li>The original file merged with an attachment without preview.</li>
   * <li>The original file merged with an attachment with preview.</li>
   * </ul>
   *
   * @param preview
   *          if true signature preview is rendered
   *
   * @return String specifying the path to the file to display or
   *         <code>null</code> if the input file has not already be set.
   * @throws BadPasswordException
   *           BadPasswordException on wrong password
   */
  public byte[] getFileToDisplay(final boolean preview) throws BadPasswordException
  {
    byte[] toDisplay;

    if (this.inputFileBytes == null)
    {
      return null;
    }

    if (tempFileBytes != null)
    {
      LOGGER.debug("display temp file");
      toDisplay = tempFileBytes;
    }
    else if (mergedFileBytes != null)
    {
      LOGGER.debug("display merged file");
      toDisplay = this.mergedFileBytes;
    }
    else
    {
      LOGGER.debug("display original file");
      toDisplay = this.inputFileBytes;
    }

    LOGGER.debug("display is" + (preview ? " " : " not ") + "previewed");
    if (preview)
    {
      return addPreview(toDisplay, mainGui);
    }
    return toDisplay;
  }

  private byte[] addPreview(final byte[] input, final MainGUI maingui) throws BadPasswordException
  {
    return SignaturePreview.getInstance().createPreview(input, maingui);
  }

  private byte[] merge(final String attach)
  {
    PdfReader originalPdf;
    try
    {
      originalPdf = new PdfReader(inputFileBytes);
    } catch (IOException ioe)
    {
      LOGGER.error("error opening original input file", ioe);
      Message.warning(mainGui.getMainshell(), LocalSigner.i18n("mergeError"),
          LocalSigner.i18n("mergeInputError"));
      return null;
    }

    PdfReader attachmentPdf;
    try
    {
      // open attachment
      attachmentPdf = new PdfReader(attach);
    } catch (IOException ioe)
    {
      LOGGER.error("Attachment not found", ioe);
      Message.warning(mainGui.getMainshell(), LocalSigner.i18n("mergeError"),
          LocalSigner.i18n("attachmentError") + "\n\n" + Paths.get(attachment).toAbsolutePath());
      originalPdf.close();
      return null;
    }

    try
    {
      PdfAttacher attacher = new PdfAttacher();
      return attacher.attachDocument(originalPdf, attachmentPdf, 0);
    } catch (Exception e)
    {
      // ugly but helpful
      LOGGER.error("Error merging attachment with original", e);
      originalPdf.close();
      attachmentPdf.close();
      return null;
    }
  }

  /**
   * Returns the currently selected 'original' file. This is the file without any PDF attached or preview painted on.
   *
   * @return the original selected file
   */
  public byte[] getOriginalFile()
  {
    return inputFileBytes;
  }

  private void reset()
  {
    inputFileName = null;
    tempFileBytes = null;
    attachment = StringUtils.EMPTY;
    inputFileBytes = null;
    mergedFileBytes = null;
    originalChecksum = null;
    mergedChecksum = null;
    certified = false;
    signed = false;
    blankSignatures = false;
    pdfVersion = null;
    analyzer = null;
    attachments = false;
  }

  /**
   * Sets the original File. This is the one the user has chosen in the GUI.
   *
   * @param originalFileName
   *          Original file
   * @param check
   *          Do PDF file checks
   * @throws ch.admin.localsigner.main.exception.FileOpenException
   */
  public void setOriginalFile(final String originalFileName, final boolean check) throws FileOpenException
  {
    if (originalFileName == null)
    {
      LOGGER.error("no original file");
      return;
    }
    if (isInputEqualToOutputFileName(originalFileName))
    {
      // set same file. don't unlock but read all again
      reset();
      this.inputFileName = originalFileName;
      if (needsLock() && !fileLocking.isLocked())
      {
        // subprocess mode, reopen file ?????
        try
        {
          fileLocking.lockFile(getInputFileName());
        } catch (AccessDeniedException e)
        {
          LOGGER.error("setOriginalFile", e);
          throw new FileOpenException(e, FileOpenException.Reason.ACCESS_DENIED);
        } catch (FileSystemException e)
        {
          LOGGER.error("setOriginalFile", e);
          throw new FileOpenException(e, FileOpenException.Reason.FILE_IS_LOCKED);
        } catch (IOException e)
        {
          LOGGER.error("setOriginalFile", e);
          throw new FileOpenException(e, FileOpenException.Reason.UNSPECIFIED);
        }
      }
    } else
    {
      fileLocking.unlockFile(getInputFileName());
      reset();
      this.inputFileName = originalFileName;
      try
      {
        if (needsLock())
        {
          fileLocking.lockFile(getInputFileName());
        }
      } catch (AccessDeniedException e)
      {
        LOGGER.error("setOriginalFile", e);
        throw new FileOpenException(e, FileOpenException.Reason.ACCESS_DENIED);
      } catch (FileSystemException e)
      {
        LOGGER.error("setOriginalFile", e);
        throw new FileOpenException(e, FileOpenException.Reason.FILE_IS_LOCKED);
      } catch (IOException e)
      {
        LOGGER.error("setOriginalFile", e);
        throw new FileOpenException(e, FileOpenException.Reason.UNSPECIFIED);
      }
    }

    try
    {
      inputFileBytes = getFileContents();
      LOGGER.debug("input file loaded and locked");

    } catch (AccessDeniedException e)
    {
      LOGGER.error("setOriginalFile: ", e);
      throw new FileOpenException(e, FileOpenException.Reason.ACCESS_DENIED);
    } catch (NoSuchFileException e)
    {
      LOGGER.error("setOriginalFile: ", e);
      throw new FileOpenException(e, FileOpenException.Reason.FILE_NOT_FOUND);
    } catch (FileSystemException e)
    {
      LOGGER.error("setOriginalFile", e);
      throw new FileOpenException(e, FileOpenException.Reason.FILE_IS_LOCKED);
    } catch (IOException e)
    {
      LOGGER.error("setOriginalFile: ", e);
      throw new FileOpenException(e, FileOpenException.Reason.UNSPECIFIED);
    }
    if (check)
    {
      boolean valid = this.checkDocument(inputFileBytes);
      if (!valid)
      {
        fileLocking.unlockFile(inputFileName);
        return;
      }
    }

    // calculate checksum
    try
    {
      this.originalChecksum = SHAChecksum.getChecksum(inputFileBytes);
    } catch (Exception e)
    {
      this.originalChecksum = null;
      LOGGER.error("Cannot checksum document", e);
    }

    try
    {
      // delete any old merged files
      this.mergedFileBytes = null;

      // merge new file with already chosen attachment. only add attachment if
      // not yet signed
      if (!attachment.equals(StringUtils.EMPTY))
      {
        final String tmp = this.attachment;
        this.attachment = StringUtils.EMPTY;
        updateAttachment(tmp);
      }
    } catch (Exception e)
    {
      LOGGER.error("Cannot read document", e);
    }
  }

  private boolean isInputEqualToOutputFileName(final String originalFileName)
  {
    return getInputFileName() != null && inputFileNameAsFile().equals(Paths.get(originalFileName));
  }

  private boolean checkDocument(byte[] fileBytes)
  {
    try
    {
      analyzer = new PdfAnalyzer(fileBytes);

      this.certified = analyzer.hasCertification();
      LOGGER.debug("Check certification: " + this.certified);

      this.blankSignatures = !analyzer.getBlankSignatures().isEmpty();
      LOGGER.debug("Check blank signatures: " + this.blankSignatures);

      this.signed = !analyzer.getSignatures().isEmpty();
      LOGGER.debug("Check signatures: " + this.signed);

      boolean fieldsNotSig = !analyzer.getAcroFieldsNotSignature().isEmpty();
      LOGGER.debug("Acro fields: " + fieldsNotSig);

      boolean triggerEvents = analyzer.hasTriggerEvents();
      LOGGER.debug("Trigger events: " + triggerEvents);

      this.attachments = !analyzer.getAttachments().isEmpty();
      LOGGER.debug("Attachments: " + attachments);

      this.pages = analyzer.getNumberOfPages();
      LOGGER.debug("Pages: " + this.pages);

      // do not load unknown forms
      if (fieldsNotSig)
      {
        Message.warning(mainGui.getMainshell(), LocalSigner.i18n("checkDocumentForm"));
        LOGGER.debug("abort load of PDF form");
        return false;
      }

      if (shouldShowWarnDialog(triggerEvents))
      {
        String list = LocalSigner.i18n("checkDocumentIntro") + "\n";
        String text = "\n\n";

        if (triggerEvents)
        {
          list += "\n- " + LocalSigner.i18n("checkDocumentTriggers");
        }
        if (blankSignatures)
        {
          list += "\n- " + LocalSigner.i18n("checkDocumentSigFields");
          text += "\n\n" + LocalSigner.i18n("checkDocumentSigFieldsText");

          // load profile for existing signature fields
          loadProfileForExistingSignatureFields();
        }
        if (attachments)
        {
          list += "\n- " + LocalSigner.i18n("checkDocumentAttachments");
        }

        // set signature infos for the viewer
        Message.warning(mainGui.getMainshell(), list + text);
      }
    } catch (BadPasswordException bpe)
    {
      LOGGER.debug("PDF is read protected", bpe);
      Message.warning(mainGui.getMainshell(), LocalSigner.i18n("pdfEncrypted"));
      return false;
    } catch (Exception e)
    {
      LOGGER.error("Cannot read document", e);
      Message.warning(mainGui.getMainshell(), LocalSigner.i18n("errorInvalidPdf"));
      return false;
    }

    // everything ok
    return true;
  }

  /**
   * The profile to load is hard coded, TODO: do it better!
   */
  private void loadProfileForExistingSignatureFields()
  {
    // TODO find a way to set the original profile after signing

    String pName = "formsign" + Config.SYSTEM_PROFILE_MARK;
    mainGui.setSelectedProfile(pName);
  }

  /**
   * show warning if: // - document has trigger events // - document has blank signature fields // - document has
   * fields that are not signature fields // - document has attachments
   */
  private boolean shouldShowWarnDialog(boolean triggerEvents)
  {
    return triggerEvents || blankSignatures || attachments;
  }

  /**
   * Return checksum (SHA) of original input file
   *
   * @return checksum of the original file
   */
  public String getChecksumOfOriginalFile()
  {
    return this.originalChecksum;
  }

  /**
   * Return checksum (SHA) of merged file
   *
   * @return checksum of the merged file
   */
  public String getChecksumOfMergedFile()
  {
    return this.mergedChecksum;
  }

  public boolean isSigned()
  {
    return certified || signed;
  }

  public boolean hasBlankSignatures()
  {
    return blankSignatures;
  }

  /**
   * A file can be modified if it is not signed or certified.
   *
   * @return true if modifications are allowed without breaking something
   */
  public boolean canModify()
  {
    return !signed && !certified;
  }

  public boolean isTemporaryFile()
  {
    return tempFileBytes != null;
  }

  /**
   * Set the data of the in memory temporary file
   *
   * @param data
   *          the data of the temporary file
   */
  public void setTemporaryFile(final byte[] data)
  {
    if (data == null)
    {
      LOGGER.debug("throw away tmp file");
      tempFileBytes = null;
      mainGui.getMenu().canSign(true);
      return;
    }
    try
    {
      tempFileBytes = data;
      mainGui.getMenu().canSign(false);
      PdfReader pdfReader = new PdfReader(data);
      mainGui.getMenu().canDelete(pdfReader.getNumberOfPages() > 1);
      pdfReader.close();
    } catch (IOException e)
    {
      LOGGER.error("Cannot create temp file", e);
    }
  }

  public int getPages()
  {
    return pages;
  }

  public String getPdfVersion()
  {
    if (pdfVersion == null)
    {
      return "";
    }
    return pdfVersion;
  }

  public PdfAnalyzer getAnalyzer()
  {
    return analyzer;
  }

  protected void processFile(String outputFileName, boolean lock, ColorToConvert colorModel, String dpi)
  {
    try
    {
      convertToPDFA(getFileContents(), outputFileName, colorModel, dpi);

      if(fileLocking.isLocked()) {
        fileLocking.unlockFile(inputFileName);
      }
    } catch(IOException | InterruptedException | OverlappingFileLockException e)
    {
      LOGGER.error("Exception: ", e);
    }
  }

  private void convertToPDFA(byte[] inputFileBytes, String outputName, final ColorToConvert colorModel,
      final String dpi)
      throws InterruptedException, FileNotFoundException, IOException
  {
    class ConvertToPDFACommandLine extends ConvertToPDFATemplate {

      @Override
      protected void buildPDFA(byte[] inputFile, String outputName)
          throws InterruptedException, IOException
      {
        setColorModel(colorModel);
        setDpi(dpi);
        init(inputFile);
        createProfile();
        createPDFDocument();
        renderPDF(outputName);
      }

      @Override
      protected void createPDFDocument() throws InterruptedException
      {
        int numberOfPages = oldpdf.getNumberOfPages();
        for (int i = 0; i < numberOfPages; i++)
        {
          LOGGER.info("Create page " + (i + 1) + " from " + numberOfPages);
          createPage(i);
        }
      }
    }

    new ConvertToPDFACommandLine().buildPDFA(inputFileBytes, outputName);
  }

  private byte[] getFileContents() throws IOException
  {
    if (!needsLock())
    {
      return Files.readAllBytes(Paths.get(getInputFileName()));
    }

    return fileLocking.getDataFromLockedFile();

  }

  public boolean isInputFile(Path file)
  {
    return file != null && file.equals(Paths.get(getInputFileName()));
  }

  public boolean isInputFile(String filePath)
  {
    if (StringUtils.isBlank(filePath))
    {
      return false;
    }
    Path file = Paths.get(filePath);
    return isInputFile(file);
  }

  public void write(Path outputFile, byte[] fileData) throws FileWriteException
  {
    if (inputFileNameAsFile().equals(outputFile) && needsLock())
    {
      // input and output are same
      overWriteLockedInputFile(fileData);
    } else
    {
      try
      {
        // first we try to append one byte ('0') to the end of the file, if that works, all is fine...
        Files.write(outputFile, new byte[]{0},
            StandardOpenOption.APPEND, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      } catch (AccessDeniedException e)
      {
        throw new FileWriteException(e, FileWriteException.Reason.ACCESS_DENIED);
      } catch (FileSystemException e)
      {
        throw new FileWriteException(e, FileWriteException.Reason.FILE_IS_LOCKED);
      } catch (IOException e)
      {
        throw new FileWriteException(e, FileWriteException.Reason.UNSPECIFIED);
      }

      try
      {
        // ...and we can write the new content to the file.
        Files.write(outputFile, fileData); // surplus bytes will be truncated automatically
      } catch (AccessDeniedException e)
      {
        throw new FileWriteException(e, FileWriteException.Reason.ACCESS_DENIED);
      } catch (FileSystemException e)
      {
        throw new FileWriteException(e, FileWriteException.Reason.FILE_IS_LOCKED);
      } catch (IOException ex)
      {
        throw new FileWriteException(ex, FileWriteException.Reason.UNSPECIFIED);
      }
    }
    inputFileBytes = fileData;
  }

  private Path inputFileNameAsFile()
  {
    return Paths.get(getInputFileName());
  }

  private void overWriteLockedInputFile(byte[] fileData) throws FileWriteException
  {
    if (!fileLocking.isWriteable())
    {
      LOGGER.error("input file can not overwritten with new content because there is no valid file lock");
      throw new FileWriteException("Inputfile not writetable because there is no valid file lock",
          FileWriteException.Reason.ACCESS_DENIED);
    }

    try
    {
      fileLocking.overWriteInputFile(fileData);
      LOGGER.debug("input file overwritten with new content");
    } catch (IOException e)
    {
      LOGGER.error("input file can not overwritten with new content", e);
      throw new FileWriteException(e, FileWriteException.Reason.UNSPECIFIED);
    }
  }

  @Override
  protected void finalize() throws Throwable
  {
    fileLocking.unlockFile(getInputFileName());
    super.finalize();
  }

  public boolean needsLock()
  {
    return mainGui.isSubprocessMode();
  }

  public boolean isWriteable()
  {
    return fileLocking.isWriteable();
  }

  public void unlockFile()
  {
    fileLocking.unlockFile(getInputFileName());
  }

  public boolean isLocked()
  {
    return fileLocking.isLocked();
  }
}
