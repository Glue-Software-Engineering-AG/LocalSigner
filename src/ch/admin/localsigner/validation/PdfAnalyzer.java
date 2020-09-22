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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPKCS7;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import ch.admin.localsigner.utils.SignatureInfo;
import ch.admin.suis.client.core.service.to.SignatureCoverageStatus;
import ch.glue.ltvdetector.diff.DiffFinder;
import ch.glue.ltvdetector.diff.DiffResult;
import ch.glue.securitytools.pdf.PdfAttacher;

/**
 * This class can check PDF files.
 *
 * @author Rafael Wampfler
 * @author Roland Keller
 * @author $Author$
 * @version $Revision$
 */
public class PdfAnalyzer
{

  private static final Logger LOGGER = Logger.getLogger(PdfAnalyzer.class);

  private byte[] fileBytes;

  private PdfReader reader;

  private OfflinePdfValidator offlineValidator;

  public PdfAnalyzer(final byte[] fileBytes) throws IOException
  {
    init(fileBytes);
  }

  public PdfAnalyzer(final String file) throws IOException
  {
    byte[] fileByteArray = FileUtils.readFileToByteArray(new File(file));
    init(fileByteArray);
  }

  private void init(byte[] fileBytes) throws IOException
  {
    Security.addProvider(new BouncyCastleProvider());
    this.reader = new PdfReader(fileBytes);
    this.fileBytes = fileBytes;
    this.offlineValidator = new OfflinePdfValidator(fileBytes);
  }

  /**
   * Start the filevalidation with VeraPdf.
   */
  public void validatePdfA()
  {
    offlineValidator.getValidationResults();
  }

  /**
   * Get the ValidationResults from the VeraPdf validation.
   *
   * @return ValidationResults
   */
  public PdfAValidationResults getValidationResults()
  {
    return offlineValidator.getValidationResults();
  }

  public boolean isNotAccessible()
  {
    return getValidationResults().isNotAccessible();
  }

  public boolean isNotAPdfA()
  {
    return getValidationResults().isNotAPdfA();
  }

  /**
   * Get number of pages in document.
   *
   * @return number of pages
   */
  public int getNumberOfPages()
  {

    return reader.getNumberOfPages();

  }

  /**
   * Check if document has embedded file attachments.
   *
   * @return list of attachments names
   */
  public Map<String, PRStream> getAttachments()
  {
    try
    {
      PdfAttacher attacher = new PdfAttacher();
      return attacher.extractAttachments(reader);
    } catch (Exception e)
    {
      // may happen often
      LOGGER.debug("Cannot check attachments");
    }
    return new HashMap<String, PRStream>();
  }

  /**
   * Check if document has trigger events (AA entries, e.g. JavaScript).
   *
   * @return true if events found
   */
  public boolean hasTriggerEvents()
  {
    int pages = reader.getNumberOfPages();
    for (int i = 1; i <= pages; ++i)
    {
      PdfDictionary pageDict = reader.getPageN(i);
      if (pageDict != null)
      {
        // search for AA dictionary entries (trigger events)
        if (pageDict.contains(PdfName.AA))
        {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Check if document has acro fields that are NOT signature fields.
   *
   * @return list of signature fields
   */
  public List<String> getAcroFieldsNotSignature()
  {
    List<String> fields = new ArrayList<String>();
    List<String> offendingTextFields = new ArrayList<String>();
    boolean letPass = false;
    for (Object o : reader.getAcroFields().getFields().keySet())
    {
      String key = (String) o;
      int type = reader.getAcroFields().getFieldType(key);
      if (type != AcroFields.FIELD_TYPE_SIGNATURE)
      {
        if (checkForNonSignatureFormField(type))
        {
          // TODO verbessern wenn definitiv
          if (key.startsWith("LSTextfield") || key.contains("GLUE_LS_ENABLED") || key.contains("tan")
              || key.contains("OpeneGovForm_LS_enabled"))
          {
            letPass = true;
          }
          offendingTextFields.add(key);
        } else
        {
          // not a LocalSigner field
          fields.add(key);
        }
      }
    }

    if (!letPass)
    {
      fields.addAll(offendingTextFields);
    }

    return fields;
  }

  private boolean checkForNonSignatureFormField(int type)
  {
    return (type == AcroFields.FIELD_TYPE_TEXT || type == AcroFields.FIELD_TYPE_CHECKBOX
        || type == AcroFields.FIELD_TYPE_RADIOBUTTON || type == AcroFields.FIELD_TYPE_COMBO
        || type == AcroFields.FIELD_TYPE_LIST || type == AcroFields.FIELD_TYPE_NONE
        || type == AcroFields.FIELD_TYPE_PUSHBUTTON);
  }

  /**
   * Check if document has signatures.
   *
   * @return list of signatures
   */
  public Map<Integer, SignatureInfo> getSignatures()
  {
    Map<Integer, SignatureInfo> map = new TreeMap<Integer, SignatureInfo>();
    AcroFields af = reader.getAcroFields();
    for (Object obj : af.getSignatureNames())
    {
      String name = (String) obj;
      PdfPKCS7 pk = af.verifySignature(name);

      int rev = af.getRevision(name);
      SignatureInfo info = new SignatureInfo();
      info.setName(name);
      info.setPkcs7(pk);
      info.setRevision(rev);
      info.setCoveringWholeDocument(af.signatureCoversWholeDocument(name));

      try
      {
        InputStream revData = af.extractRevision(name);
        info.setRevisionData(revData);
      } catch (IOException e)
      {
        LOGGER.error("Cannot extract revision", e);
      }

      map.put(rev, info);
    }
    return map;
  }

  public boolean isSigned()
  {
    return !getSignatures().isEmpty();
  }

  /**
   * Check if document has blank signature fields.
   *
   * @return list of blank field
   */
  public List<String> getBlankSignatures()
  {
    return reader.getAcroFields().getBlankSignatureNames();
  }

  /**
   * Check if document has certification
   *
   * @return true if certified
   */
  public boolean hasCertification()
  {
    return reader.getCertificationLevel() != PdfSignatureAppearance.NOT_CERTIFIED;
  }

  /**
   * Get content of PDF as byte array.
   *
   * @return PDF content
   * @throws IOException throwy ans IOException
   */
  byte[] getFileContent() throws IOException
  {
    return fileBytes;
  }

  private SignatureCoverageStatus oneSignatureCoversWholeDocument()
  {
    Map<Integer, SignatureInfo> signatures = getSignatures();
    for (SignatureInfo signature : signatures.values())
    {
      if (signature.isCoveringWholeDocument())
      {
        return SignatureCoverageStatus.COVERED;
      }
    }
    DiffFinder diffFinder = new DiffFinder();
    try
    {
      DiffResult diffResult = diffFinder.checkFile(fileBytes);
      if (diffResult.areAllDifferencesAllowed())
      {
        return SignatureCoverageStatus.MODIFIED_ACCEPTABLE;
      } else
      {
        return SignatureCoverageStatus.MODIFIED_INVALID;
      }
    } catch (IOException e)
    {
      LOGGER.error("Error calling diffFinder.checkFile() ", e);
    }
    return SignatureCoverageStatus.MODIFIED_INVALID;
  }

  public SignatureCoverageStatus isSignedAndModified()
  {
    if (!isSigned())
    {
      return SignatureCoverageStatus.UNSIGNED;
    } else
    {
      return oneSignatureCoversWholeDocument();
    }
  }

}
