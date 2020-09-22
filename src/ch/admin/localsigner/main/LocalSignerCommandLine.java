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

import java.io.Console;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.InputMismatchException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.admin.localsigner.main.exception.FileOpenException;
import ch.admin.localsigner.utils.ColorToConvert;

/**
 * This class is for the command line interface of LocalSigner.<br>
 *
 * To add a parameter to <i>Open eGov LocalSigner.exe</i> (Windows only), edit
 * <i>Open eGov LocalSigner.ini</i>:<br>
 *
 * <pre>
 * [Application]
 * Cmd="-i c:/test.pdf"
 * </pre>
 *
 * <pre>
 * usage: LocalSigner.cmd
 * -a,--appmode &lt;arg&gt;            Run Localsigner in interactive or subprocess mode
 * -c --conv                           Convert an ordinary PDF to format PDF/A-1b
 * -d,--debug                          Write to debug.log
 * -i,--input &lt;arg&gt;              File to load as input document
 * -n,--nocheck                        Skip the integity check
 * -m,--colormodel                     Set colormodel to bw, color or grey
 * -o,--output &lt;arg&gt;             File to save signed document
 * -q,--quit                           Exit LocalSigner after signing
 * -r,--resolution                     Set resolution to xxx dpi (200 for grey/color, 300 for bw recommended)
 * -s,--signatureProfile &lt;arg&gt;   Path of signature profile to load
 * -t,--sigtype &lt;arg&gt;            Fix sigtype to sign or lock or choice
 * -v,--viewer &lt;arg&gt;             Start LocalSigner with selected PDF viewer(adobe or builtin)
 * </pre>
 *
 * @author $Author$
 * @version $Revision$
 */
public class LocalSignerCommandLine
{
  private static final Logger LOGGER = Logger.getLogger(LocalSignerCommandLine.class);

  private Options options;

  private String input;

  private String output;

  private String signatureProfile;

  private boolean quit;

  private String viewer;

  private boolean skipCheck;

  private boolean debug;

  private String appmode;

  private String sigtype;

  private boolean conversion;

  private ColorToConvert colorModel;

  private String dpi;

  public static final String VIEWER_ADOBE = "adobe";

  public static final String VIEWER_BUILTIN = "builtin";

  private static final String VIEWER_UNDEFINED = "undefined";

  /**
   * Default value for appmode - LocalSigner is started normally
   */
  public static final String APPMODE_INTERACTIVE = "interactive";

  /**
   * Additional value for appmode - input needs to be given in the options, paths for input- and outputfile are hidden,
   * Drag n Drop for opening files is deactivated, menu entry Datei-&gt;&Ouml;ffnen is hidden
   */
  public static final String APPMODE_SUBPROCESS = "subprocess";

  /**
   * Default value for sigtype - The Sign-Type can be choosen by the user
   */
  public static final String SIGTYPE_CHOICE = "choice";

  /**
   * Additional value for sigtype - The Sign-Type is fixed to lock and can't be changed by the user
   */
  private static final String SIGTYPE_LOCK = "lock";

  /**
   * Additional value for sigtype - The Sign-Type is fixed to sign and can't be changed by the user
   */
  public static final String SIGTYPE_SIGN = "sign";

  /**
   * Constructor
   */
  public LocalSignerCommandLine()
  {
    // handle command line parameters
    options = new Options();
    options.addOption("i", "input", true, "File to load as input document");
    options.addOption("o", "output", true, "File to save signed document");
    options.addOption("s", "signatureProfile", true, "Path of signature profile to load");
    options.addOption("q", "quit", false, "Exit LocalSigner after signing");
    options.addOption("v", "viewer", true, "Start LocalSigner with selected PDF viewer ("
        + VIEWER_ADOBE + " or " + VIEWER_BUILTIN + ")");
    options.addOption("n", "nocheck", false, "Skip the integity check");
    options.addOption("d", "debug", false, "Write to debug.log");
    options.addOption("a", "appmode", true, "Run Localsigner in interactive or subprocess mode");
    options.addOption("t", "sigtype", true, "Fix sigtype to sign or lock or choice");
    options.addOption("c", "conv", false, "Convert an ordinary PDF file to format PDF/A-1b");
    options.addOption("m", "colormodel", true,
        "Convert the PDF file  to colormodel bw, color or grey");
    options.addOption("r", "resolution", true, "Set Resolution to dpi");
  }

  /**
   * Parse given CLI arguments.
   * @param args Command line arguments
   * @return list of non-recognized arguments
   * @throws ParseException throw PArseException
   */
  public  String[] parseCli(String[] args) throws ParseException, InputMismatchException
  {
    // parse the command line arguments
    final CommandLineParser parser = new PosixParser();
    CommandLine line = parser.parse(options, args);

    input = line.getOptionValue("i");
    output = line.getOptionValue("o");
    signatureProfile = line.getOptionValue("s");
    quit = line.hasOption("q");
    conversion = line.hasOption("c");
    dpi = line.getOptionValue("r");
    setColorModel(line);

    if (line.hasOption("v"))
    {
      viewer = line.getOptionValue("v");
    }
    else
    {
      viewer = VIEWER_UNDEFINED;
    }
    if (line.hasOption("n"))
    {
      skipCheck = true;
    }

    if (line.hasOption("d"))
    {
      debug = true;
    }

    //set default value if option is not given in the commandline
    if (line.hasOption("a"))
    {
      appmode = line.getOptionValue("a");
    } else
    {
      appmode = APPMODE_INTERACTIVE;
    }

    //check if Appmode is valid
    validateAppmode();

    //app
    validateConversion();

    //set default value if option is not given in the commandline
    if (line.hasOption("t"))
    {
      sigtype = line.getOptionValue("t");
    } else
    {
      sigtype = SIGTYPE_CHOICE;
    }

    validateSigtype();

    // non-recognized arguments
    return line.getArgs();
  }

  /**
   * This method validates the Appmode
   *
   * @throws ParseException
   */
  private void validateAppmode() throws ParseException
  {
    if (appmode.equalsIgnoreCase(APPMODE_SUBPROCESS))
    {
      if (getInput() == null)
      {
        showHelp("input needs to be set in subprocess appmode");
        throw new ParseException("input needs to be set in subprocess appmode");
      } else if (getOutput() == null)
      {
        showHelp("input and output are now set to the same path: " + input);
        setOutput(input);
      }
    } else if (appmode.equalsIgnoreCase(APPMODE_INTERACTIVE))
    {
      //Do Nothing
    } else
    {
      showHelp("appmode needs to be set to " + APPMODE_INTERACTIVE + " or " + APPMODE_SUBPROCESS);
      throw new ParseException("invalid appmode: " + appmode);
    }
  }

  private void validateConversion() throws InputMismatchException
  {
    String wrongUsage;
    if(isConversion()) {
      //conversion implies appmode subprocess
      if(!appmode.equalsIgnoreCase(APPMODE_SUBPROCESS)) {
        wrongUsage = "Appmode needs to be set to 'subprocess' when converting to PDF/A.";
        logWrongUsage(wrongUsage);
        String app = (StringUtils.isEmpty(appmode)  ?  "empty" : appmode);
        throw new InputMismatchException("Appmode is currently " + app + "."
            + " When converting to PDF/A only appmode 'subprocess' is allowed.");
      }
    }
    else if (!colorModel.isDefaultValue() || dpi != null)
    {
      wrongUsage = "The option -c (conv) for Conversion is not set.\n"
          + " Therefore the options -m (colormodel) and -r (resolution) are invalid.";
      logWrongUsage(wrongUsage);
      throw new InputMismatchException(wrongUsage);
    }
  }

  private void logWrongUsage(String wrongUsage)
  {
    Console console;
    console = System.console();
    if (console != null)
    {
      console.writer().println(wrongUsage);
    }
    else
    {
      LOGGER.error(wrongUsage);
    }
  }


  /**
   * This method validates the Sigtype
   *
   * @throws ParseException
   */
  private void validateSigtype() throws ParseException
  {
    if (sigtype.equalsIgnoreCase(SIGTYPE_CHOICE) || sigtype.equalsIgnoreCase(SIGTYPE_LOCK)
        || sigtype.equalsIgnoreCase(SIGTYPE_SIGN))
    {
      // Do Nothing
    }
    else
    {
      showHelp("sigtype needs to be set to " + SIGTYPE_CHOICE + ", " + SIGTYPE_LOCK + " or " + SIGTYPE_SIGN);
      throw new ParseException("invalid sigtype: " + sigtype);
    }
  }


  private void showHelp(final String error)
  {
    // we are printing path within header information. A too long path provoque a IndexOutOfBounds exception.
    int LEN_OF_HEADER = 1000;
    LOGGER.warn("Command line problem: " + error);
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(LEN_OF_HEADER);
    formatter.printHelp("LocalSigner.cmd", error, options , null);
  }

  String helpAsString()
  {
    HelpFormatter formatter = new HelpFormatter();
    StringWriter stringWriter = new StringWriter();
    formatter.printHelp(new PrintWriter(stringWriter), 200, "LocalSigner", null, options,
        0, 10, null);
    return stringWriter.toString();
  }

  public String getInput()
  {
    if (input == null)
    {
      return null;
    }

    File file = new File(input);
    if (file.exists())
    {
      return input;
    }

    showHelp("input file " + file.getAbsolutePath() + " not found");
    return null;
  }

  public void setInput(String input)
  {
    this.input = input;
  }

  public String getOutput()
  {
    // no validation, file does not exist in most cases
    return output;
  }

  public void setOutput(String output)
  {
    this.output = output;
  }

  public String getSignatureProfile()
  {
    if (signatureProfile == null)
    {
      return null;
    }

    File file = new File(signatureProfile);
    if (file.exists())
    {
      return signatureProfile;
    }

    showHelp("signature profile " + file.getAbsolutePath() + " not found");
    return null;
  }

  public boolean isQuit()
  {
    return quit;
  }

  public boolean isDebug()
  {
    return debug;
  }

  boolean isSkipCheck()
  {
    return skipCheck;
  }

  public String getViewer()
  {
    if (VIEWER_UNDEFINED.equals(viewer))
    {
      return null;
    }

    if (VIEWER_ADOBE.equals(viewer) || VIEWER_BUILTIN.equals(viewer))
    {
      return viewer;
    }

    showHelp("unknown viewer: " + viewer);
    return null;
  }

  /**
   * Returns the appmode given in the Commandline Options
   * If a nonexistent value is given the default appmode is returned
   *
   * @return appmode String
   * @see #APPMODE_INTERACTIVE is the default value.
   */
  public String getAppmode()
  {
    if (appmode.equalsIgnoreCase(APPMODE_INTERACTIVE) || appmode.equalsIgnoreCase(APPMODE_SUBPROCESS))
    {
      return appmode;
    } else
    {
      showHelp("unknown appmode: " + appmode + ". Using default appmode");
      return APPMODE_INTERACTIVE;
    }
  }

  /**
   * Returns the sigtype given in the Commandline Options
   * If a nonexistent value is given the Default sigtype is returned
   *
   * @return sigtype String
   */
  String getSigtype()
  {
    if (sigtype.equalsIgnoreCase(SIGTYPE_LOCK) || sigtype.equalsIgnoreCase(SIGTYPE_CHOICE) || sigtype.
        equalsIgnoreCase(SIGTYPE_SIGN))
    {
      return sigtype;
    } else
    {
      showHelp("unknown sigtype: " + sigtype + ".");
      return SIGTYPE_CHOICE;
    }
  }

  public boolean isConversion()
  {
    return conversion;
  }

  public void setConversion(boolean conversion)
  {
    this.conversion = conversion;
  }

  public void doConversion(final boolean lock)
  {
    InputFile inputFile = new InputFile()
    {
      @Override
      public boolean needsLock()
      { // quite ugly, but app-mode (needsLock returns true if app mode is "subprocess") is only known by MainGUI... m)
        return lock;
      }
    };

    try
    {
      inputFile.setOriginalFile(input, false);
      inputFile.processFile(getOutput(), lock, colorModel, dpi);
    } catch (FileOpenException e)
    {
      LOGGER.error("Could not open File: ", e);
    }
  }

  private void setColorModel(CommandLine line)
  {
    String cm = line.getOptionValue("m");
    colorModel = ColorToConvert.COLOR;
    if (cm != null)
    {
      String col = cm.toUpperCase();
      if (ColorToConvert.BW.toString().equals(col))
      {
        colorModel = ColorToConvert.BW;
        colorModel.setDefaultValue(false);
      }
      else if (ColorToConvert.GREY.toString().equals(col))
      {
        colorModel = ColorToConvert.GREY;
        colorModel.setDefaultValue(false);
      }
      else if (ColorToConvert.COLOR.toString().equals(col))
      {
        colorModel = ColorToConvert.COLOR;
        colorModel.setDefaultValue(false);
      }
    }
  }

}
