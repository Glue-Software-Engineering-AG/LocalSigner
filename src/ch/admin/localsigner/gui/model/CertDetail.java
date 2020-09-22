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
package ch.admin.localsigner.gui.model;

/**
 * This is used currently in SignerGUI, for showing all found certificates to
 * the user in a table.
 */
public class CertDetail
{

  public enum CertType
  {
    DIGITAL_SIGNATURE, NON_REPUDIATION, REGULATED_SEAL, REGULATED_QUALIFIED
  }

  private String alias;

  private String subjectName;

  private String issuer;

  private String validity;

  private String description;

  private CertType certType;

  public String getAlias()
  {
    return alias;
  }

  public void setAlias(String alias)
  {
    this.alias = alias;
  }

  public String getSubjectName()
  {
    return subjectName;
  }

  public void setSubjectName(String subjectName)
  {
    this.subjectName = subjectName;
  }

  public String getIssuer()
  {
    return issuer;
  }

  public void setIssuer(String issuer)
  {
    this.issuer = issuer;
  }

  public String getValidity()
  {
    return validity;
  }

  public void setValidity(String validity)
  {
    this.validity = validity;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public void setCertType(CertType certType)
  {
    this.certType = certType;
  }

  public boolean isRegulatedSeal()
  {
    return certType == CertType.REGULATED_SEAL;
  }

  public boolean doShowPerDefaultInGUI()
  {
    return certType != CertType.DIGITAL_SIGNATURE;
  }

}
