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

import ch.admin.localsigner.config.TsaConfiguration;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.utils.OnlineServices;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Calendar;
import java.util.List;

/**
 * This class displays the about dialog. This dialog additionally contains a list of all preconfigured TSAs and whether
 * they are accessible or not.
 *
 * @author Adrian Greiler
 * @author $Author$
 * @version $Revision$
 */
public class AboutDialog
{
  private static final String TITLE = "Open eGov LocalSigner";

  private final int currentYear = Calendar.getInstance().get(Calendar.YEAR);

  private final String aboutMessage
      =    "Open eGov LocalSigner " + GuiHelper.getVersion() + "\n\n"
      + LocalSigner.i18n("about.ideaAndConcept") + ":\n"
      + LocalSigner.i18n("about.federalOfficeOfJustice") + ", Bern, " + LocalSigner.i18n("about.switzerland") + "\n"
      + "Adrian Bl\u00F6chlinger\n\n"
      + LocalSigner.i18n("about.implementation") + ":\n"
      + "Glue Software Engineering AG, Bern, " + LocalSigner.i18n("about.switzerland") + "\n"
      + "Dr. Igor Metz\n"
      + "Dr. Stephan Amann\n"
      + "Boris Zweim\u00FCller\n"
      + "Rafael Wampfler\n"
      + "Beat Weisskopf\n"
      + "Adrian Greiler\n"
      + "Daniel Sch\u00E4fer\n"
      + "Christian Niggemeyer\n\n\n"
      + "\u00A9 2008 - 2014 " + LocalSigner.i18n("about.federalOfficeOfJustice") + "\n"
      + "\u00A9 2015 - " + currentYear + " " + LocalSigner.i18n("about.federalAuthoritiesOfTheSwissConfederation")
      + "\n";


  private MainGUI mainGUI;

  public AboutDialog(final MainGUI mainGUI)
  {
    this.mainGUI = mainGUI;

    // get the display
    final Display display = mainGUI.getMainshell().getDisplay();

    // create new shell
    final Shell box = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
    GridLayout boxGridLayout = new GridLayout(1, false);
    boxGridLayout.marginWidth = 0;
    boxGridLayout.marginHeight = 0;
    boxGridLayout.marginTop = 0;
    boxGridLayout.verticalSpacing = 0;
    boxGridLayout.horizontalSpacing = 0;
    boxGridLayout.marginBottom = 0;
    box.setLayout(boxGridLayout);
    box.setText(TITLE);
    box.setImage(GuiHelper.loadAppIcon(display));

    final Font font = mainGUI.getFont();

    addAboutMessage(box, font);

    addTsaCheckerContainer(box, font);

    addCloseButton(box, font);

    // pack
    box.pack();

    // size
    final Rectangle parentsize = mainGUI.getMainshell().getBounds();

    // place in the middle
    final Point boxSize = box.getSize();
    box.setBounds(
      parentsize.x + parentsize.width / 2 - boxSize.x / 2,
      parentsize.y + parentsize.height / 2 - boxSize.y / 2,
      boxSize.x,
      boxSize.y);

    box.open();

    while (!box.isDisposed())
    {
      if (!display.readAndDispatch())
      {
        display.sleep();
      }
    }
  }

  private void addAboutMessage(final Shell box, final Font font)
  {
    Composite aboutMessageContainer = new Composite(box, SWT.NONE);
    aboutMessageContainer.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    aboutMessageContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 0, 0));

    GridLayout aboutMessageContainerGridLayout = new GridLayout(1, false);
    aboutMessageContainerGridLayout.marginWidth = 21;
    aboutMessageContainerGridLayout.marginTop = 21;
    aboutMessageContainerGridLayout.verticalSpacing = 10;
    aboutMessageContainerGridLayout.marginBottom = 8;
    aboutMessageContainer.setLayout(aboutMessageContainerGridLayout);

    // message
    final Label aboutText = new Label(aboutMessageContainer, SWT.NONE);
    aboutText.setText(aboutMessage);
    aboutText.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 5, 5));
    aboutText.setFont(font);
    aboutText.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    final Label spacer = new Label(aboutMessageContainer, SWT.SEPARATOR | SWT.HORIZONTAL);
    GridData spacerGridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
    spacer.setLayoutData(spacerGridData);
  }

  private void addTsaCheckerContainer(final Shell box, final Font font)
  {
    Composite tsaList = new Composite(box, SWT.NONE);

    tsaList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 0, 0));

    GridLayout tsaListGridLayout = new GridLayout(1, false);
    tsaListGridLayout.marginWidth = 21;
    tsaListGridLayout.marginBottom = 21;
    tsaListGridLayout.marginTop = -7;
    tsaList.setLayout(tsaListGridLayout);

    tsaList.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    List<TsaConfiguration> tsaConfigurationList = getPreconfiguredTSAs();

    for (TsaConfiguration tsaConf : tsaConfigurationList)
    {
      if (StringUtils.isEmpty(tsaConf.getUrl()))
      { // "kein Zeitstempel"
        continue;
      }

      Label stateLabel = getTsaCheck(tsaList, tsaConf.getDisplayText(),
          LocalSigner.i18n("about.tsa.searching"), font);
      asynchronouslyValidateService(stateLabel, tsaConf);
    }
  }

  private void addCloseButton(final Shell box, final Font font)
  {

    Composite buttonArea = new Composite(box, SWT.NONE);

    buttonArea.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 0, 0));

    GridLayout buttonAreaGridLayout = new GridLayout(1, false);
    buttonAreaGridLayout.marginRight = 3;
    buttonAreaGridLayout.marginBottom = 8;
    buttonAreaGridLayout.marginTop = 7;
    buttonArea.setLayout(buttonAreaGridLayout);

    // close button
    final Button close = new Button(buttonArea, SWT.PUSH);
    close.setFont(font);
    close.setText(LocalSigner.i18n("close"));
    close.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, false, 5, 5));
    close.addListener(SWT.Selection, new Listener()
    {
      @Override
      public void handleEvent(final Event arg0)
      {
        box.close();
      }

    });
    box.setDefaultButton(close);
  }

  private Label getTsaCheck(final Composite tsaList, final String tsaName, final String tsaState, final Font font)
  {
    Composite tsaComp = new Composite(tsaList, SWT.NONE);
    tsaComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 0, 0));
    GridLayout tsaCompGridLayout = new GridLayout(2, false);
    tsaCompGridLayout.marginWidth = 0;
    tsaCompGridLayout.marginHeight = 0;
    tsaCompGridLayout.marginBottom = 0;
    tsaCompGridLayout.marginTop = 0;
    tsaComp.setLayout(tsaCompGridLayout);
    tsaComp.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));

    Label tsaNameLabel = GuiHelper.label(tsaComp, SWT.NONE, tsaName+": ", mainGUI.getFont());
    tsaNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 0, 0));
    tsaNameLabel.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
    tsaNameLabel.setFont(font);

    Label tsaStateLabel = GuiHelper.label(tsaComp, SWT.NONE, tsaState, mainGUI.getFont());
    tsaStateLabel.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 0, 0));
    tsaStateLabel.setBackground(mainGUI.getMainshell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
    tsaStateLabel.setFont(font);

    return tsaStateLabel;
  }

  private void asynchronouslyValidateService(final Label tsaStateLabel, final TsaConfiguration tsaConfig)
  {
    mainGUI.getMainshell().getDisplay().asyncExec(
      new Thread()
      {

        @Override
        public void run()
        {
          if (OnlineServices.isTSAReady(tsaConfig.getUrl()))
          {
            if (tsaStateLabel.isDisposed())
            {
              // dialog already closed
            } else
            {
              tsaStateLabel.setText(LocalSigner.i18n("about.tsa.ok"));
              tsaStateLabel.pack();
            }
          }
          else
          {
            if (tsaStateLabel.isDisposed())
            {
              // dialog already closed
            } else
            {
              tsaStateLabel.setText(LocalSigner.i18n("about.tsa.nok"));
              tsaStateLabel.pack();
            }
          }
        }
      });
  }

  private List<TsaConfiguration> getPreconfiguredTSAs()
  {
    return LocalSigner.appConfig.getTSAConfig();
  }
}
