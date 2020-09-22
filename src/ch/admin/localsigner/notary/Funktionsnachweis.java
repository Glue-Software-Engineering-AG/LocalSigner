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
package ch.admin.localsigner.notary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.bj.upreg.fn.client.core.FunktionsnachweisAll;
import ch.admin.bj.upreg.fn.client.core.FunktionsnachweisImpl;
import ch.admin.bj.upreg.fn.client.core.exception.FNClientException;
import ch.admin.bj.upreg.fn.client.core.exception.FNWebserviceException;
import ch.admin.bj.upreg.fn.client.core.validation.FunktionsnachweisValidator;
import ch.admin.bj.upreg.fn.client.core.validation.Validatable;
import ch.admin.bj.upreg.fn.client.core.webservice.FNWebserviceWrapper;
import ch.admin.localsigner.gui.MainGUI;
import ch.admin.localsigner.gui.Pkcs11Helper;
import ch.admin.localsigner.gui.common.PleaseWaitDialog;
import ch.admin.localsigner.gui.common.SaveFileDialog;
import ch.admin.localsigner.main.LocalSigner;
import ch.admin.localsigner.main.exception.FileExceptionHandler;
import ch.admin.localsigner.main.exception.FileWriteException;
import ch.admin.localsigner.notary.cantonal.seal.impl.CantonalSealPluginFactoryBuilder;
import ch.admin.localsigner.notary.cantonal.seal.impl.validation.ValidatorExecutor;
import ch.admin.localsigner.notary.cantonal.seal.info.client.CantonalSealConfiguration;
import ch.admin.localsigner.notary.validation.AreAllSignaturesFullQualifiedCheck;
import ch.admin.localsigner.notary.validation.HasNetworkConnectionCheck;
import ch.admin.localsigner.notary.validation.HasOneOrMoreSignaturesCheck;
import ch.admin.localsigner.notary.validation.IsPdfACheck;
import ch.admin.localsigner.utils.Constants;
import ch.glue.localsigner.cantonal.seal.CantonalSealPlugin;
import ch.glue.localsigner.cantonal.seal.configuration.transfer.EndPoints;
import ch.glue.localsigner.cantonal.seal.exception.CantonalSealException;
import ch.glue.localsigner.cantonal.seal.factory.AbstractCantonalSealPluginFactory;
import ch.zulab.proof.transfer.to.RT1GenerateReqTO;
import ch.zulab.proof.transfer.to.RT1GenerateRespTO;
import ch.zulab.proof.transfer.to.RT2SignReqTO;
import ch.zulab.proof.transfer.to.RT2SignRespTO;

public class Funktionsnachweis
{
  private static final Logger LOGGER = Logger.getLogger(Funktionsnachweis.class);

  final private MainGUI maingui;

  final private FunktionsnachweisValidator validator;

  final private FunktionsnachweisAll proofService;

  public Funktionsnachweis(MainGUI maingui) throws Pkcs11Helper.UserCanceledException
  {
    this.maingui = maingui;
    List<Validatable> validierungen = setupValidatorenListe();
    this.validator = new FunktionsnachweisValidator(validierungen);
    FNWebserviceWrapper webservice = WebServiceBuilder.instance().initWebservice();
    this.proofService = new FunktionsnachweisImpl(webservice, validator);
  }

  private List<Validatable> setupValidatorenListe()
  {
    List<Validatable> validatoren = new ArrayList<Validatable>();
    validatoren.add(new HasNetworkConnectionCheck());
    validatoren.add(new IsPdfACheck());
    validatoren.add(new HasOneOrMoreSignaturesCheck());
    validatoren.add(new AreAllSignaturesFullQualifiedCheck());
    return validatoren;
  }

  /**
   * Signs the given file with a FN and returns the name of the resulting file.
   * If the user cancels the action, NULL will be returned!
   *
   * @param fileName the name of the source file.
   * @param fileAsByteArr the file as byte array on which the Zulassungsbestätigung will be applied.
   * @return the name of the output file or null if the user cancelled the action.
   * @throws IOException
   */
  public String signNotary(String fileName, byte[] fileAsByteArr) throws IOException, CantonalSealException,
      Pkcs11Helper.UserCanceledException
  {
    // read configured canton and domain
    String selectedCanton = LocalSigner.appConfig.getFunktionsnachweisCanton();
    String selectedDomain = LocalSigner.appConfig.getFunktionsnachweisDomain();

    // overwrite in dialog if desired
    if (LocalSigner.appConfig.isFunktionsnachweisShowDialog() ||
        StringUtils.isEmpty(selectedCanton) ||
        StringUtils.isEmpty(selectedDomain))
    {
      CantonAndDomainDialog cantonAndDomainDialog = new CantonAndDomainDialog();

      if (cantonAndDomainDialog.isCancelled())
      {
        return null;
      } else
      {
        selectedCanton = cantonAndDomainDialog.getCanton();
        selectedDomain = cantonAndDomainDialog.getDomain();
      }
    }

    LOGGER.info("Funktionsnachweis for domain "+selectedDomain+" in "+selectedCanton);

    PleaseWaitDialog waitDialog = null;
    try
    {
      waitDialog = new PleaseWaitDialog(maingui, LocalSigner.i18n("pleaseWait"),
          LocalSigner.i18n("signingInProgress"), 200, SWT.TOP);
      waitDialog.updateLabel("Starting Validation");

      // a few validations
      LOGGER.info("validating signed PDF using build in validators");
      proofService.validateSignedPDF(fileAsByteArr, validator);

      waitDialog.updateLabel("Validation finished");

      String chosenFileName = getFilename(fileName);

      ByteArrayOutputStream baos =
          applyConfirmationOfAdmission(fileAsByteArr, selectedCanton, selectedDomain, waitDialog);

      // add cantonal seal if plugin available
      if (CantonalSealPluginFactoryBuilder.existsPluginFactory(selectedCanton, selectedDomain))
      {
        waitDialog.updateLabel("Loading cantonal seals configuration");
        EndPoints endpointConfig = CantonalSealConfiguration.getCantonalSealConfiguration();

        // add cantonal seal if configured
        if (CantonalSealPluginFactoryBuilder.isSealConfigured(selectedCanton, selectedDomain, endpointConfig))
        {

          waitDialog.updateLabel("Loading cantonal seals extesion");
          AbstractCantonalSealPluginFactory factory
              = CantonalSealPluginFactoryBuilder.build(selectedCanton, selectedDomain);
          CantonalSealPlugin plugin = factory.build(endpointConfig);
          LocalSigner.addAdditionalTranslations(plugin.getSpecificTranslations(LocalSigner.getLocale()));

          waitDialog.updateLabel("Validating cantonal requirements");
          if (!new ValidatorExecutor(plugin.getValidators()).executeValidators(baos.toByteArray()))
          {
            return null; // cancelled respectively aborted
          }

          waitDialog.updateLabel("Adding cantonal seal");
          byte[] bytes = plugin.addSeal(baos.toByteArray(), WebServiceBuilder.instance().getKeystore());
          baos = new ByteArrayOutputStream(bytes.length);
          baos.write(bytes, 0, bytes.length);

          LOGGER.info("Successfully added cantonal seal to document.");
        }
      }
      String outputFilename = writeSignedPdfFile(baos, chosenFileName);
      waitDialog.updateLabel("File written");
      return outputFilename;
    }catch(CantonalSealException | FNWebserviceException | FNClientException | IllegalArgumentException |
        IllegalStateException | Pkcs11Helper.UserCanceledException ex)
    { // handled differently (show user friendly message)
      throw ex;
    } catch (Exception ex)
    {
      throw new UnhandledException(ex);
    } finally
    {
      if (waitDialog != null)
      {
        waitDialog.close();
      }
    }
  }

  private ByteArrayOutputStream applyConfirmationOfAdmission(byte[] fileAsByteArr,
      String selectedCanton, String selectedDomain, PleaseWaitDialog waitDialog) throws IOException
  {
    // Server round-trip 1
    RT1GenerateReqTO rt1Req = proofService.createRT1Request(fileAsByteArr, selectedDomain, selectedCanton);

    waitDialog.updateLabel("Extracting signature");

    RT1GenerateRespTO rt1Resp = proofService.callRT1(rt1Req);

    // Server round-trip 2; apply signature
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PdfSignatureAppearance app = proofService.prepareFNSignature(rt1Resp, fileAsByteArr, baos);

    RT2SignReqTO rt2Req = proofService.createRT2Request(app, rt1Resp.getUuid());

    waitDialog.updateLabel("Starting remote sign");
    RT2SignRespTO rt2Resp = proofService.callRT2(rt2Req);

    waitDialog.updateLabel("Remote signing finished");
    proofService.closeAppearance(app, rt2Resp);

    waitDialog.updateLabel("Writing file");
    return baos;
  }

  private String getFilename(String file)
  {
    String outputFileName = null;
    if (maingui.isInteractiveMode())
    { // choose file to store converted PDF if not in SUBPROCESS-Mode
      outputFileName = SaveFileDialog.getOutputFile(file, Constants.FUNKTIONSNACHWEIS_SUFFIX);

      if (StringUtils.isEmpty(outputFileName))
      {
        LOGGER.info("user canceled storing file with applied Zulassungsbestätigung in file dialog");
        return null;
      }
    } else
    {
      if (maingui.isInputFile(outputFileName))
      {
        outputFileName = file;
      } else
      {
        outputFileName = maingui.getOutputFile();
      }
    }

    return outputFileName;
  }

  private String writeSignedPdfFile(ByteArrayOutputStream pdfData, String outputFileName)
      throws IOException, DocumentException
  {
    try
    {
      maingui.getDocument().getInputFile().write(Paths.get(outputFileName), pdfData.toByteArray());
    } catch (FileWriteException e)
    {
      LOGGER.error("Error storing file with applied Zulassungsbestätigung", e);
      FileExceptionHandler.showAppropriateErrorMessage(e);
      return null;
    }
    return outputFileName;
  }
}
