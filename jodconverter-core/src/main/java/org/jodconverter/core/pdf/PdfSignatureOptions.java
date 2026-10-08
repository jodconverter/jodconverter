/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jodconverter.core.pdf;

import java.util.Map;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import org.jodconverter.core.util.AssertUtils;

/**
 * Options to sign a PDF document digitally. The certificate is either found by its subject name in
 * the certificate store used by the office installation, or given in the PEM format.
 *
 * <p>Two limitations of LibreOffice:
 *
 * <ul>
 *   <li>On Linux (observed with LibreOffice 26.2), an office process can only sign if its first PDF
 *       export is a signed one. Once it has exported a PDF document without signature, every signed
 *       export fails until the process is restarted. Use an office manager dedicated to the signed
 *       conversions.
 *   <li>On Windows (observed with LibreOffice 25.2 and 26.2), a certificate in the PEM format
 *       cannot be used: the conversion fails. Use a certificate of the Windows certificate store,
 *       by its subject name.
 * </ul>
 *
 * <p>When the certificate cannot be found or used, the conversion fails with an {@link
 * org.jodconverter.core.office.OfficeException}.
 *
 * <p>LibreOffice versions older than 7.4 can only sign with a certificate object given through the
 * {@code SignatureCertificate} property, which can be set with {@link
 * PdfOptions.Builder#filterData(String, Object)}.
 *
 * @see PdfOptions.Builder#signature(java.util.function.Consumer)
 */
public final class PdfSignatureOptions extends AbstractPdfOptionGroup {

  /* default */ PdfSignatureOptions(final Map<PdfOption, Object> values) {
    super(values);
  }

  /**
   * Signs the document with the certificate that has the given subject name, such as {@code "CN=My
   * Company"}, taken from the certificate store used by the office installation: the Windows
   * certificate store on Windows, an NSS database on Linux.
   *
   * <p>FilterData: {@code SignPDF} set to {@code true}, and {@code SignCertificateSubjectName}.
   * Requires LibreOffice 7.4 or later.
   *
   * @param subjectName The subject name of the certificate. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions certificateSubjectName(final @Nullable String subjectName) {
    setText(PdfOption.SIGN_CERTIFICATE_SUBJECT_NAME, subjectName);
    set(PdfOption.SIGN_PDF, subjectName == null ? null : Boolean.TRUE);
    return this;
  }

  /**
   * Signs the document with the given certificate and private key. It does not need a certificate
   * store, but it does not work on Windows.
   *
   * <p>FilterData: {@code SignPDF} set to {@code true}, {@code SignCertificateCertPem} and {@code
   * SignCertificateKeyPem}. Requires LibreOffice 25.2 or later.
   *
   * @param certificate The certificate, in the PEM format.
   * @param privateKey The private key of the certificate, in the PEM format.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions certificatePem(
      final @NonNull String certificate, final @NonNull String privateKey) {
    AssertUtils.notBlank(certificate, "certificate must not be blank");
    AssertUtils.notBlank(privateKey, "privateKey must not be blank");
    set(PdfOption.SIGN_CERTIFICATE_CERT_PEM, certificate);
    set(PdfOption.SIGN_CERTIFICATE_KEY_PEM, privateKey);
    set(PdfOption.SIGN_PDF, Boolean.TRUE);
    return this;
  }

  /**
   * Specifies the certificates of the certificate authorities to trust when signing with a {@link
   * #certificatePem(String, String) PEM certificate}.
   *
   * <p>FilterData: {@code SignCertificateCaPem}. Requires LibreOffice 25.2 or later.
   *
   * @param certificates The certificates, in the PEM format. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions caPem(final @Nullable String certificates) {
    setText(PdfOption.SIGN_CERTIFICATE_CA_PEM, certificates);
    return this;
  }

  /**
   * Specifies the password of the private key of the certificate.
   *
   * <p>FilterData: {@code SignaturePassword}. Requires LibreOffice 4.0 or later.
   *
   * @param password The password. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions password(final @Nullable String password) {
    setText(PdfOption.SIGNATURE_PASSWORD, password);
    return this;
  }

  /**
   * Specifies the location written in the signature.
   *
   * <p>FilterData: {@code SignatureLocation}. Requires LibreOffice 4.0 or later.
   *
   * @param location The location. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions location(final @Nullable String location) {
    setText(PdfOption.SIGNATURE_LOCATION, location);
    return this;
  }

  /**
   * Specifies the reason written in the signature.
   *
   * <p>FilterData: {@code SignatureReason}. Requires LibreOffice 4.0 or later.
   *
   * @param reason The reason. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions reason(final @Nullable String reason) {
    setText(PdfOption.SIGNATURE_REASON, reason);
    return this;
  }

  /**
   * Specifies the contact information written in the signature.
   *
   * <p>FilterData: {@code SignatureContactInfo}. Requires LibreOffice 4.0 or later.
   *
   * @param contactInfo The contact information. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions contactInfo(final @Nullable String contactInfo) {
    setText(PdfOption.SIGNATURE_CONTACT_INFO, contactInfo);
    return this;
  }

  /**
   * Specifies the URL of the time stamping authority used to timestamp the signature.
   *
   * <p>FilterData: {@code SignatureTSA}. Requires LibreOffice 5.0 or later.
   *
   * @param url The URL of the time stamping authority. {@code null} removes the option.
   * @return This group.
   */
  public @NonNull PdfSignatureOptions timestampAuthority(final @Nullable String url) {
    setText(PdfOption.SIGNATURE_TSA, url);
    return this;
  }
}
