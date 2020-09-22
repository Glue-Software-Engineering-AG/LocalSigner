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
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import com.lowagie.text.pdf.PdfReader;
import ch.admin.localsigner.config.Config;
import ch.admin.localsigner.config.util.BasePath;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.exception.AbstractFileException;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileOpenException;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.glue.securitytools.pdf.PdfAttacher;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.configuration.ConfigurationException;

/**
 * This class adds a special PDF document at the end of the loaded document.
 * Note: this feature is only available if {@link Config#SIGNATUREPAGES_BUND} is
 * enabled.
 */
public class AppendDocumentBundListener implements Listener
{
  private static final Logger LOGGER = Logger.getLogger(AppendDocumentBundListener.class);

  private final MainGUI maingui;

  private final AppendContext context;

  public enum Department
  {
    BK, EJPD, EFD, EDI, EDA, WBF, VBS, UVEK
  }

  public enum BundDocumentType
  {
    SINGLE_SIGNATURE("1Sig"), MULTI_SIGNATURE("4Sig"), SINGLE_SEAL("Seal"), INFO_SHEET("Info");

    private final String folderName;

    private BundDocumentType(String folderName)
    {
      this.folderName = folderName;
    }

    public String folderName()
    {
      return this.folderName;
    }
  }

  public enum BundLanguage
  {
    DE("D.pdf", "de"), FR("F.pdf", "fr"), IT("I.pdf", "it"), EN("E.pdf", "en");

    private final String fileName;
    private final String signatureLabelLanguage;

    private BundLanguage(String fileName, String signatureLabelLanguage)
    {
      this.fileName = fileName;
      this.signatureLabelLanguage = signatureLabelLanguage;
    }

    public String getFileName()
    {
      return this.fileName;
    }


    public String getSignatureLabelLanguage()
    {
      return signatureLabelLanguage;
    }
  }

  public static class AppendContext
  {
    private final Department department;

    private final BundDocumentType bundDocumentType;

    private final BundLanguage bundLanguage;

    public AppendContext(Department department, BundDocumentType bundDocumentType, BundLanguage bundLanguage)
    {
      super();
      this.department = department;
      this.bundDocumentType = bundDocumentType;
      this.bundLanguage = bundLanguage;
    }

    public Department getDepartment()
    {
      return department;
    }

    public BundDocumentType getBundDocumentType()
    {
      return bundDocumentType;
    }

    public BundLanguage getBundLanguage()
    {
      return bundLanguage;
    }
  }

  /**
   * Constructor
   *
   * @param maingui
   *          The main GUI, needed to access the current file to sign and to
   *          display the signed file.
   * @param context
   *          The context to find the correct document to append.
   */
  public AppendDocumentBundListener(final MainGUI maingui, final AppendContext context)
  {
    this.maingui = maingui;
    this.context = context;
  }

  @Override
  public void handleEvent(final Event event)
  {
    try
    {
      // get original file
      byte[] inputFile = maingui.getInputFile();
      if (inputFile == null)
      {
        LOGGER.debug("no input file loaded");
        return;
      }

      // read file to attach
      PdfReader attachmentReader = loadFile();
      if (attachmentReader == null)
      {
        LOGGER.debug("no document selected");
        return;
      }

      // decide where to write the merged file to
      String userChosenOutputFile = getTheNameOfTheMergedFile();
      if (StringUtils.isBlank(userChosenOutputFile))
      {
        LOGGER.debug("user pressed cancel or esc");
        return;
      }

      // merge
      PdfAttacher attacher = new PdfAttacher();
      byte[] output = attacher.attachDocument(new PdfReader(inputFile), attachmentReader, 0);

      Path file = Paths.get(userChosenOutputFile);
      maingui.getDocument().getInputFile().write(file, output);

      // update gui with new file
      maingui.setInputFileAndCheck(userChosenOutputFile, true);

      // preset language in sign dialog
      LocalSigner.appConfig.setValue(Config.SIGNATURE_LANG, context.getBundLanguage().getSignatureLabelLanguage());
    } catch (FileWriteException e)
    {
      LOGGER.error("Cannot attach document", e);
      FileExceptionHandler.showAppropriateErrorMessage(e);
    } catch (IOException e)
    {
      LOGGER.error("Cannot attach document", e);
      FileExceptionHandler.showAppropriateErrorMessage(
          new FileWriteException(e, AbstractFileException.Reason.UNSPECIFIED));
    } catch (FileOpenException e)
    {
      LOGGER.error("Could not load the merged file", e);
      FileExceptionHandler.showAppropriateErrorMessage(e, maingui.getInputFileName());
    } catch (ConfigurationException e)
    {
      LOGGER.error("Could not preset language for signature labels", e);
    }
  }

  private String getTheNameOfTheMergedFile()
  {
    String userChosenOutputFile = null;
    // special case, in subprocess mode the user must work on the locked file
    if (maingui.isSubprocessMode())
    {
      userChosenOutputFile = maingui.getOutputFile();
    }
    else
    { // dadurch wird der Originaldatei(!) direkt die neue Seite angehängt. Ohne nachfrage etc. Das ist aber so gewollt.
      userChosenOutputFile = maingui.getInputFileName();
    }
    return userChosenOutputFile;
  }


  private PdfReader loadFile() throws IOException
  {
    String basePath = BasePath.getBundResourcesPath();
    String path = buildFileName(basePath);
    return new PdfReader(path);
  }

  private String buildFileName(String basePath)
  {
    return basePath + context.getDepartment().toString() + File.separatorChar
        + context.getBundDocumentType().folderName() + File.separatorChar + context.getBundLanguage().getFileName();
  }

}