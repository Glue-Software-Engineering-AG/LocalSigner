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
package ch.admin.localsigner.utils;

import java.io.InputStream;
import com.lowagie.text.pdf.PdfPKCS7;

/**
 * Helper class to hold content of a digital signature.
 * 
 * @author Rafael Wampfler
 * @author $Author$
 * @version $Revision$
 */
public class SignatureInfo
{
  private String name;

  private PdfPKCS7 pkcs7;

  private int revision;

  private InputStream revisionData;

  private boolean coveringWholeDocument;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public PdfPKCS7 getPkcs7()
  {
    return pkcs7;
  }

  public void setPkcs7(PdfPKCS7 pkcs7)
  {
    this.pkcs7 = pkcs7;
  }

  public int getRevision()
  {
    return revision;
  }

  public void setRevision(int revision)
  {
    this.revision = revision;
  }

  public InputStream getRevisionData()
  {
    return revisionData;
  }

  public void setRevisionData(InputStream revisionData)
  {
    this.revisionData = revisionData;
  }

  public boolean isCoveringWholeDocument()
  {
    return coveringWholeDocument;
  }

  public void setCoveringWholeDocument(boolean coveringWholeDocument)
  {
    this.coveringWholeDocument = coveringWholeDocument;
  }

}
