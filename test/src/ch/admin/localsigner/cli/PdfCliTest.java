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
package ch.admin.localsigner.cli;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.faceless.pdf2.OutputProfile;
import org.faceless.pdf2.PDF;
import org.faceless.pdf2.PDFImage;
import org.faceless.pdf2.PDFPage;
import org.faceless.pdf2.PDFParser;
import org.faceless.pdf2.PDFReader;
import org.faceless.pdf2.PagePainter;
import org.junit.Test;
import ch.admin.localsigner.main.LocalSignerCommandLine;
import ch.admin.localsigner.utils.Constants;

public class PdfCliTest
{

  private static final Logger LOGGER = Logger.getLogger(PdfCliTest.class);

  private FileChannel inputFileChannel = null;

  private FileLock inputFileLock = null;

  private String inputFile = "test/test.pdf";

  private String outputName = "test/test-conv.pdf";

  @Test
  public void testCliInterface()
  {

    String[] args = new String[]
    {
        "-n", "-a", "subprocess", "-i", "test/test.pdf", "-o", "test/test-conv.pdf", "-c"
    };

    // handle command line parameters - LocalSigner.handleCommandLineOptions
    LocalSignerCommandLine cli = null;
    cli = new LocalSignerCommandLine();
    testForInvalidOptions(args, cli);
    cli.isConversion();
    inputFile = cli.getInput();
    outputName = cli.getOutput();
    cli.getAppmode();

    processFile();

    File toDelete = new File(outputName);
    toDelete.delete();

  }

  // from InputFile
  private void processFile()
  {
    try (RandomAccessFile raf = new RandomAccessFile(inputFile, "rwd");
        FileLock inputFileLock = raf.getChannel().tryLock())
    {

      this.inputFileLock = inputFileLock;
      // InputFile.lockFile()
      lockFile(raf, inputFileLock);

      // class ConvertToPdfAListener$PdfConverter
      convertToPDFA(raf);

    } catch (Exception e)
    {
      e.printStackTrace();
    } finally
    {
      if (inputFileChannel != null)
      {
        try
        {
          inputFileChannel.close();
          inputFileChannel = null;
        } catch (IOException e)
        {
          LOGGER.error("unlockFile: inputFileChannel close error in finally", e);
        }
      }
    }
  }

  private void lockFile(RandomAccessFile raf, FileLock inputFileLock)
  {
    if (inputFileChannel != null)
    {
      String problem = "lockFile: inputFileChannel not null";
      LOGGER.error(problem);
    }

    inputFileChannel = raf.getChannel();

    if (inputFileLock == null)
    {
      String problem = "lockFile: inputFileLock == null => no lock acquired";
      LOGGER.error(problem);

    }
    else if (!inputFileLock.isValid())
    {
      String problem = "lockFile: inputFileLock not valid";
      LOGGER.error(problem);
    }

    LOGGER.debug("lockFile: locked file " + inputFile + "  valid: " + inputFileLock.isValid());
  }

  private void convertToPDFA(RandomAccessFile raf) throws IOException, InterruptedException, FileNotFoundException
  {
    long start = System.currentTimeMillis();
    LOGGER.debug("started convertToPDFA: " + System.currentTimeMillis());
    // class ConvertToPdfAListener$PdfConverter
    PDF oldpdf = new PDF(new PDFReader(Channels.newInputStream(raf.getChannel())));

    PDFParser parser = new PDFParser(oldpdf);
    ICC_Profile icc = ICC_Profile.getInstance(ColorSpace.CS_sRGB);
    OutputProfile profile = new OutputProfile(OutputProfile.PDFA1b_2005, "sRGB", null, "http://www.color.org", null,
        icc);

    PDF newpdf = new PDF(profile);

    int numberOfPages = oldpdf.getNumberOfPages();
    long paintingStart = System.currentTimeMillis();
    LOGGER.debug("started painting:  " + (paintingStart - start));
    for (int i = 0; i < numberOfPages; i++)
    {
      PDFPage oldpage = oldpdf.getPage(i);
      PagePainter painter = parser.getPagePainter(oldpage);
      // BufferedImage image = painter.getImage(200, PDFParser.RGB);
      BufferedImage image = painter.getImage(300, PDFParser.BLACKANDWHITE);
      // BufferedImage image = painter.getImage(200, PDFParser.GRAYSCALE);
      PDFImage pdfimage = new PDFImage(image);
      PDFPage newpage = newpdf.newPage(oldpage.getWidth(), oldpage.getHeight());
      newpage.drawImage(pdfimage, 0, 0, oldpage.getWidth(), oldpage.getHeight());
    }
    long paintingEnd = System.currentTimeMillis();
    LOGGER.debug("ended painting:  " + (paintingEnd - start));
    unlockFile();

    FileOutputStream fileOutputStream = new FileOutputStream(outputName);
    newpdf.render(fileOutputStream);
    newpdf.close();
    fileOutputStream.close();
    long end = System.currentTimeMillis();
    LOGGER.debug("ended convertToPDFA: " + (end - paintingEnd));
    // class ConvertToPdfAListener$PdfConverter
  }

  // InputFile.unlockFile
  private void unlockFile()
  {
    try
    {
      // Release the lock - if it is not null!
      if (inputFileLock != null)
      {
        if (inputFileLock.isValid())
        {
          inputFileLock.release();
          inputFileLock = null;
          LOGGER.debug("unlockFile: Unlocked " + (inputFile != null ? inputFile : "noFile"));
        }
        else
        {
          LOGGER.debug("unlockFile: Input lock was not valid, filename " + (inputFile != null ? inputFile : "noFile"));
        }
      }
      else
      {
        LOGGER.debug("unlockFile: Input lock was null, filename " + (inputFile != null ? inputFile : "noFile"));
      }
    } catch (Exception exc)
    {
      LOGGER.error("unlockFile: error unlocking", exc);
    }
  }

  private void testForInvalidOptions(String[] args, LocalSignerCommandLine cli)
  {
    try
    {

      String[] nonRecognized = cli.parseCli(args);

      if (nonRecognized.length > 0)
      {
        String file = nonRecognized[0];
        if (file.toLowerCase().endsWith(Constants.PDF_FILE_SUFFIX) && cli.getInput() == null)
        {
          LOGGER.debug("Handle " + file + " as input file (drag and drop on icon)");
          cli.setInput(file);
        }
      }

    } catch (ParseException | IllegalArgumentException | SecurityException e)
    {
      if (e.getMessage().contains("option: -psn") || e.getMessage().contains("option: -XstartOnFirstThread"))
      {
        // happens on Mac, no problem
        LOGGER.debug("Mac message: " + e.getMessage());
      }
      else
      {
        LOGGER.fatal("Cannot parse CLI", e);
        System.exit(0);
      }
    }
  }
}
