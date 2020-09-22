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

import static ch.admin.localsigner.main.LocalSignerCommandLine.APPMODE_INTERACTIVE;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.Security;
import java.util.InputMismatchException;
import java.util.Locale;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import ch.admin.localsigner.config.ApplicationConfiguration;
import ch.admin.localsigner.config.LanguageConfiguration;
import ch.admin.localsigner.config.util.ProxyConfiguratorInitializer;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.utils.Constants;
import ch.glue.proxylibrary.core.system.ProxySetting;

/**
 * This class is responsible for starting up the whole application.
 */
public final class LocalSigner
{

  private static final Logger LOGGER = Logger.getLogger(LocalSigner.class);

  private static final int ERROR_VALUE = 99;

  public static final String USER_LANGUAGE_PROPERTY = "user.language";

  public static final String AUTOMATIC_LANGUAGE_PROPERTY = "auto";

  private static SWTSplash splash;

  private static LanguageConfiguration langConfig;

  public static ApplicationConfiguration appConfig;

  public static MainGUI mainGui;

  private static boolean inDebugMode = false;

  public static String sigtype;

  private LocalSigner()
  {
    // no public constructor
  }

  /**
   * Main class to launch LocalSigner.
   *
   * @param args
   *          startup args
   */
  public static void main(String[] args)
  {

    initLogging();
    Security.addProvider(new BouncyCastleProvider());

    LocalSignerCommandLine cli = handleCommandLineOptions(args);
    sigtype = cli.getSigtype();

    loadApplicationConfig();

    // add debug logger after app config (appconfig of user directory required)
    if (cli.isDebug() || appConfig.isDebug())
    {
      addDebugLevelLogger();
    }

    // print variables to log
    appConfig.debugVariables();

    LOGGER.debug("Start LocalSigner with " + args.length + " arguments");
    for (String arg : args)
    {
      LOGGER.debug("Argument: " + arg);
    }

    loadCorrectLanguageConfiguration();

    new ProxyConfiguratorInitializer();
    ProxySetting.setupProxy();

    caseConversion(cli);

    Display display = null;
    try
    {
      // set application name (used in OS X menu bar)
      Display.setAppName("LocalSigner");
      display = new Display();
    } catch (Throwable e)
    {
      LOGGER.fatal("cannot open SWT window", e);
      if (display != null)
      {
        display.dispose();
      }
      // show Swing dialog, because SWT is not working
      String msg = "Error cause: " + e.getMessage() + "\nPlease redownload the correct version for your architecture";

      JDialog dialog = new JDialog(new JFrame(), "Error", false);

      dialog.add(new JOptionPane(msg, JOptionPane.ERROR_MESSAGE));

      dialog.pack();
      dialog.setAlwaysOnTop(true);
      dialog.setVisible(true);

      try
      {
        Thread.sleep(5000);
      } catch (InterruptedException ie)
      {
        LOGGER.debug("waiting 5s before exit");
      }

      System.exit(ERROR_VALUE);
      return;
    }

    failOnUnsupportedJavaVersion();

    // clean up temporary files of older instance
    TempFilesCleanerUtil.cleanOldTemporaryFiles();
    // open splash screen with integrity check if not disabled
    if (appConfig.isShowIntegrityCheck())
    {
      String os = System.getProperty("os.name");
      LOGGER.debug("show integrity check splash for " + os);
      splash = new SWTSplash(display);

      if (cli.isSkipCheck())
      {
        LOGGER.debug("Skip integrity check from command line option");
      }
      else
      {
        final SecurityCheck check = new SecurityCheck();
        // do security checks first!
        Thread t = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            check.check(splash);
            splash.close();
          }

        });
        t.start();
        splash.open();

        if (check.getProblem())
        {
          LocalSigner.showErrorDialog("General Error",
              "Error: \n\nThis LocalSigner installation has been altered. Please download the application again.");

          LOGGER.fatal("LocalSigner has been altered. Exit");
          System.exit(LocalSigner.ERROR_VALUE);
          return;
        }
      }
    }

    // start the GUI
    try
    {
      LOGGER.debug("start main window");
      mainGui = new MainGUI();
      // apply command line parameters to GUI
      mainGui.showGui(display, cli);
    } catch (Exception e)
    {
      LOGGER.error("Unrecoverable application error.", e);

      handleUnrecoverableError(e, e.getClass().getName());

      System.exit(ERROR_VALUE);
    }
  }

  private static void failOnUnsupportedJavaVersion()
  {
    String javaVersionString = getJavaVersion();
    if (!javaVersionString.startsWith("1.8"))
    {
      String message = "LocalSigner currently only runs on Java 8, you are running Java version: " + javaVersionString;
      handleUnrecoverableError(new IllegalStateException(message), message);
      System.exit(ERROR_VALUE);
    }
  }

  private static String getJavaVersion()
  {
    return System.getProperty("java.version");
  }

  private static void caseConversion(LocalSignerCommandLine cli)
  {
    if (cli.isConversion())
    {
      // just convert and exit
      cli.doConversion(cli.getAppmode().equals(APPMODE_INTERACTIVE));
      System.exit(0);
    }
  }

  private static LocalSignerCommandLine handleCommandLineOptions(String[] args) throws InputMismatchException
  {
    // handle command line parameters
    LocalSignerCommandLine cli = null;
    try
    {
      cli = new LocalSignerCommandLine();
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

    } catch (ParseException e)
    {
      if (e.getMessage().contains("option: -psn") || e.getMessage().contains("option: -XstartOnFirstThread"))
      {
        // happens on Mac, no problem
        LOGGER.debug("Mac message: " + e.getMessage());
      }
      else
      {
        LOGGER.fatal("Cannot parse CLI", e);
        showErrorDialog("CLI Error", "Unexpected exception while parsing the command line arguments: \n"
            + e.getMessage() + "\n\n" + cli.helpAsString());
        System.exit(ERROR_VALUE);
      }
    }
    return cli;
  }

  private static void addDebugLevelLogger()
  {
    try
    {
      String debugFile = appConfig.getDebugFile();
      inDebugMode = true;
      LOGGER.info("add debug logger " + debugFile);
      FileAppender fa = new RollingFileAppender(new PatternLayout("%d [%-5p] %C{1} - %m%n"), debugFile);
      Logger.getRootLogger().addAppender(fa);
      Logger.getRootLogger().setLevel(Level.DEBUG);
    } catch (IOException e1)
    {
      LOGGER.error("Cannot add debug logger", e1);
    }
  }

  private static void loadApplicationConfig()
  {
    try
    {
      appConfig = new ApplicationConfiguration();
    } catch (Exception e)
    {
      LOGGER.error("Error loading application configuration", e);
      if (splash != null)
      {
        splash.close();
      }
      handleUnrecoverableError(e, "Error loading application configuration: \n" + e.getMessage());

      // stop the application
      System.exit(ERROR_VALUE);
    }
  }

  private static void loadCorrectLanguageConfiguration()
  {
    // now load the correct language configuration
    try
    {
      // extract language
      String lang = appConfig.getLanguageEvaluatingAuto();

      LOGGER.debug("using language: " + lang);

      langConfig = new LanguageConfiguration(lang);
    } catch (Exception e)
    {
      if (splash != null)
      {
        splash.close();
      }
      LOGGER.error("Error loading language configuration");
      handleUnrecoverableError(e, "Error loading language configuration");

      // stop the application
      System.exit(ERROR_VALUE);
    }
  }

  private static void initLogging()
  {
    // Initialize log4j
    final URL log4j = ClassLoader.getSystemClassLoader().getResource("log4j.properties");
    PropertyConfigurator.configure(log4j);

    LOGGER.info("Java " + getJavaVersion() + " from " + System.getProperty("java.vendor") + " ("
        + System.getProperty("java.home") + ")");
    LOGGER.info("JVM Bit size: " + System.getProperty("sun.arch.data.model"));
  }

  /**
   * Writes a dump file and presents an info dialog
   *
   * @param e
   *          The exception happened
   */
  private static void handleUnrecoverableError(final Exception e, final String message)
  {
    LOGGER.fatal("Unrecoverable Error: " + message, e);
    PrintStream ps;

    if (appConfig != null)
    {
      File errorfile = new File(appConfig.getUserConfigFolder() + File.separator + "errorDescription.txt");

      try
      {
        ps = new PrintStream(errorfile);
        e.printStackTrace(ps);
        ps.close();
      } catch (IOException ioe)
      {
        LOGGER.error("handleUnrecoverableError", ioe);
      }

      showErrorDialog("Application Error",
          "An unrecoverable application error happened!\n\n" + message + "\n\n" + "An error report has been saved at:\n"
              + errorfile.getAbsolutePath() + "\n\nPlease send this file together with a short description\n"
              + "of what you have done to: " + "\n\nlocalsigner@open-egov.ch");
    }
    else
    {
      LOGGER.error("no application config");
      showErrorDialog("Application Error", "An unrecoverable application error happened!\n\n" + message);
    }

  }

  /**
   * Shows a SWT error dialog.
   *
   * @param title
   *          Dialog title
   * @param message
   *          Dialog message
   */
  private static void showErrorDialog(final String title, final String message)
  {
    final Shell shell = new Shell(Display.getDefault());
    final MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.ON_TOP);
    mb.setText(title);
    mb.setMessage(message);
    mb.open();
  }

  /**
   * Internationalization of text contents (german, french, italian, english,
   * greek).
   *
   * @param key
   *          Language key
   * @return value for given key in language file
   */
  public static String i18n(final String key)
  {
    return langConfig.get(key);
  }

  /**
   * Set the language for internationalization.
   *
   * @param lang
   *          Selected language
   */
  public static void setLangConf(final LanguageConfiguration lang)
  {
    langConfig = lang;
  }

  /**
   * Get the selected locale.
   *
   * @return current locale
   */
  public static Locale getLocale()
  {
    return langConfig.getLocale();
  }

  /**
   * This method is to append third party properties to the language bundles of
   * LocalSigner. This is used e.g. for cantonal seal plugins.
   *
   * @param additionalTranslations
   *          Additional translations to be added to the internal translations.
   *          If a key in the additionalProperties is the same as in the
   *          original LocalSigner translations file the value will be
   *          overridden!
   */
  public static void addAdditionalTranslations(Configuration additionalTranslations)
  {
    langConfig.addConfiguration(additionalTranslations);
  }

  public static boolean isInDebugMode()
  {
    return inDebugMode;
  }
}
