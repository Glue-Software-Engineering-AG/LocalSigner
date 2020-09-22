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

import ch.glue.securitytools.SignatureBox;
import ch.glue.securitytools.SignatureHashType;

/**
 * This class contains all parameters used for the digital signature.
 */
public class SignatureParameters
{
  // the input file
  private byte[] inputFile;

  // position of the signature box from the left margin in MM
  private int leftPos;

  // position of the signature box from the top margin in MM
  private int topPos;

  // widht of the signature box in MM
  private int boxWidth;

  // height of the signature box in MM
  private int boxHeight;

  /**
   * This signature box can be used to directly specify signature box in Pixels
   * (PdfUnits).
   */
  private SignatureBox signatureBox;

  // path to signed file
  private String outPath;
  // if the signature is visible or no

  private boolean visibleSignature;

  /**
   * Should the text be included in the visible signature?
   */
  private boolean isSignatureTextVisible;

  /**
   * Should the image be included in the visible signature?
   */
  private boolean isSignatureImageVisible;

  // if the document is additionally certified (in addition to the digital
  // signature)

  private boolean certification;
  // if an additional signature is applied to a document, which
  // already contains (a) digital signature(s)

  private boolean multipleSignature = false;

  // the image to be used as background
  private String backgroundImage;

  // the location
  private String location;

  // the contact info
  private String contact;

  private String contactLabel;

  private boolean contactLabelShown;

  // the reason for the signature
  private String reason;

  private String reasonLabel;

  private boolean reasonLabelShown;

  // the page to apply the signature onto
  private int signaturePage = 1;

  // tsa url, user and password
  private String tsaUrl;

  private String tsaUser;

  private String tsaPassword;

  private String sigField;

  // visible text on signature
  private String signatureDigitallySigned;

  private String signatureLocalTime;

  private String signatureTsaTime;

  // signature hash
  private SignatureHashType hash;

  private boolean ltv;

  private boolean enableOcsp;

  public boolean isLtv()
  {
    return ltv;
  }

  public void setLtv(boolean ltv)
  {
    this.ltv = ltv;
  }

  public boolean isEnableOcsp()
  {
    return enableOcsp;
  }

  public void setEnableOcsp(boolean enableOcsp)
  {
    this.enableOcsp = enableOcsp;
  }

  public int getBoxHeight()
  {
    return boxHeight;
  }

  public void setBoxHeight(final int boxHeight)
  {
    this.boxHeight = boxHeight;
  }

  public int getBoxWidth()
  {
    return boxWidth;
  }

  public void setBoxWidth(final int boxWidth)
  {
    this.boxWidth = boxWidth;
  }

  public boolean isCertification()
  {
    return certification;
  }

  public void setCertification(final boolean certification)
  {
    this.certification = certification;
  }

  public int getLeftPos()
  {
    return leftPos;
  }

  public void setLeftPos(final int leftPos)
  {
    this.leftPos = leftPos;
  }

  public boolean isMultipleSignature()
  {
    return multipleSignature;
  }

  public void setMultipleSignature(final boolean multipleSignature)
  {
    this.multipleSignature = multipleSignature;
  }

  public String getOutPath()
  {
    return outPath;
  }

  public void setOutPath(String outPath)
  {
    this.outPath = outPath;
  }

  public int getTopPos()
  {
    return topPos;
  }

  public void setTopPos(int topPos)
  {
    this.topPos = topPos;
  }

  public boolean isVisibleSignature()
  {
    return visibleSignature;
  }

  public void setVisibleSignature(boolean visibleSignature)
  {
    this.visibleSignature = visibleSignature;
  }

  public String getBackgroundImage()
  {
    return backgroundImage;
  }

  public void setBackgroundImage(String backgroundImage)
  {
    this.backgroundImage = backgroundImage;
  }

  public String getLocation()
  {
    return location;
  }

  public void setLocation(String location)
  {
    this.location = location;
  }

  public int getSignaturePage()
  {
    return signaturePage;
  }

  public void setSignaturePage(int signaturePage)
  {
    this.signaturePage = signaturePage;
  }

  public String getContact()
  {
    return contact;
  }

  public void setContact(String contact)
  {
    this.contact = contact;
  }

  public String getContactLabel()
  {
    return contactLabel;
  }

  public void setContactLabel(String contactLabel)
  {
    this.contactLabel = contactLabel;
  }

  public boolean isContactLabelShown()
  {
    return contactLabelShown;
  }

  public void setContactLabelShown(boolean contactLabelShown)
  {
    this.contactLabelShown = contactLabelShown;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }

  public String getReasonLabel()
  {
    return reasonLabel;
  }

  public void setReasonLabel(String reasonLabel)
  {
    this.reasonLabel = reasonLabel;
  }

  public boolean isReasonLabelShown()
  {
    return reasonLabelShown;
  }

  public void setReasonLabelShown(boolean reasonLabelShown)
  {
    this.reasonLabelShown = reasonLabelShown;
  }

  public byte[] getInputFile()
  {
    return inputFile;
  }

  public void setInputFile(byte[] inputFile)
  {
    this.inputFile = inputFile;
  }

  public String getTsaPassword()
  {
    return tsaPassword;
  }

  public void setTsaPassword(String tsaPassword)
  {
    this.tsaPassword = tsaPassword;
  }

  public String getTsaUrl()
  {
    return tsaUrl;
  }

  public void setTsaUrl(String tsaUrl)
  {
    this.tsaUrl = tsaUrl;
  }

  public String getTsaUser()
  {
    return tsaUser;
  }

  public void setTsaUser(String tsaUser)
  {
    this.tsaUser = tsaUser;
  }

  public void setSignatureField(String name)
  {
    this.sigField = name;
  }

  public String getSignatureField()
  {
    return sigField;
  }

  public String getSignatureDigitallySigned()
  {
    return signatureDigitallySigned;
  }

  public void setSignatureDigitallySigned(final String signatureDigitallySigned)
  {
    this.signatureDigitallySigned = signatureDigitallySigned;
  }

  public String getSignatureLocalTime()
  {
    return signatureLocalTime;
  }

  public void setSignatureLocalTime(final String signatureLocalTime)
  {
    this.signatureLocalTime = signatureLocalTime;
  }

  public String getSignatureTsaTime()
  {
    return signatureTsaTime;
  }

  public void setSignatureTsaTime(final String signatureTsaTime)
  {
    this.signatureTsaTime = signatureTsaTime;
  }

  public SignatureHashType getHash()
  {
    return hash;
  }

  public void setHash(SignatureHashType hash)
  {
    this.hash = hash;
  }

  public boolean isSignatureTextVisible()
  {
    return isSignatureTextVisible;
  }

  public void setSignatureTextVisible(boolean isSignatureTextVisible)
  {
    this.isSignatureTextVisible = isSignatureTextVisible;
  }

  public boolean isSignatureImageVisible()
  {
    return isSignatureImageVisible;
  }

  public void setSignatureImageVisible(boolean isSignatureImageVisible)
  {
    this.isSignatureImageVisible = isSignatureImageVisible;
  }

  public SignatureBox getSignatureBox()
  {
    return signatureBox;
  }

  public void setSignatureBox(SignatureBox signatureBox)
  {
    this.signatureBox = signatureBox;
  }

}
