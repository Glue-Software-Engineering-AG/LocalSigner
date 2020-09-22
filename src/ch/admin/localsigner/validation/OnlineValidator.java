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
package ch.admin.localsigner.validation;

import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.TreeItem;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.common.Message;
import ch.admin.localsigner.gui.common.PleaseWaitDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.suis.client.core.service.to.ShortReport;
import ch.admin.suis.client.core.service.to.SignatureReport;
import ch.admin.suis.client.core.service.to.ValidationResponseV2;

/**
 * Checks a signature on the signature validator using the REST client. This validator is used by the SideBar.
 *
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class OnlineValidator extends Thread
{

  public static final String UPREG_FORMULAR_MANDANT = "upreg-formular";

  public static final String UPREG_FN_MANDANT = "upreg-fn";

  private static final Logger LOGGER = Logger.getLogger(OnlineValidator.class);

  private final List<TreeItem> signatureItems;

  private final PdfAnalyzer analyzer;

  private final List<String> signatureMandants;

  private final MainGUI maingui;

  private PleaseWaitDialog waitDialog;

  public OnlineValidator(MainGUI maingui, List<TreeItem> signatureItems, PdfAnalyzer analyzer,
      List<String> signatureMandants)
  {
    this.signatureItems = signatureItems;
    this.analyzer = analyzer;
    this.signatureMandants = signatureMandants;
    this.maingui = maingui;
  }

  @Override
  public void run()
  {
    maingui.getMainshell().getDisplay().syncExec(new Runnable()
    {

      @Override
      public void run()
      {
        waitDialog = new PleaseWaitDialog(maingui, LocalSigner
            .i18n("validation.progressdialog.title"), LocalSigner
            .i18n("validation.progressdialog.message"));
        // "Die Online-Validierung der Signaturen läuft gerade und kann einige Zeit in Anspruch nehmen.");
      }
    });

    try
    {
      String mainTenant = getMainTenant(signatureMandants);
      long start = System.currentTimeMillis();
      final ValidationResponseV2 results = OnlineValidation.validateSignatures(
          analyzer, mainTenant);
      long duration = System.currentTimeMillis() - start;
      if (results.getError() != null)
      {
        LOGGER.error("Validator error: " + results.getError());
      }
      LOGGER.info(results.isValid() + " (duration: " + duration
          + "ms, tenant: " + mainTenant + ")");

      if (results.getFileReports().isEmpty()) {
        // the validator has no such mandant
        Message.error(maingui.getMainshell(), results.getError());
        waitDialog.close();
        return;
      }

      for (int signatureIndex = 0; signatureIndex < signatureItems.size(); signatureIndex++)
      {
        final TreeItem currentItem = signatureItems.get(signatureIndex);
        final String actualMandant = signatureMandants.get(signatureIndex);
        final SignatureReport sigReport = results.getFileReports().get(0)
            .getSignatureReports().get(signatureIndex);
        for (ShortReport r : sigReport.getReports())
        {
          LOGGER.debug(r.getType() + ": " + r.getValid() + ", " + r.getMessage());
        }

        maingui.getMainshell().getDisplay().asyncExec(new Runnable()
        {

          @Override
          public void run()
          {
            maingui.getSidebar().updateOnline(currentItem, sigReport, actualMandant);
            waitDialog.close();
          }

        });
      }
    } catch (Exception e)
    {
      LOGGER.error("Cannot validate signature online with ", e);
    }
  }

  public static String getMainTenant(List<String> signatureMandants)
  {
    if (!signatureMandants.isEmpty() && UPREG_FORMULAR_MANDANT.equals(signatureMandants.get(0)))
    {
      return UPREG_FORMULAR_MANDANT;
    }

    for (String actualMandant : signatureMandants)
    {
      if (UPREG_FN_MANDANT.equals(actualMandant))
      {
        return UPREG_FN_MANDANT;
      }
    }

    return LocalSigner.appConfig.getDefaultTenant();
  }
}
