package jenkins.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.FormValidation;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.output.TeeOutputStream;
import org.jvnet.hudson.crypto.CertificateUtil;
import org.jvnet.hudson.crypto.SignatureOutputStream;

public class JSONSignatureValidator {
  private final String name;
  
  public JSONSignatureValidator(String name) { this.name = name; }
  
  @SuppressFBWarnings(value = {"WEAK_MESSAGE_DIGEST_SHA1"}, justification = "SHA-1 is only used as a fallback if SHA-512 is not available")
  public FormValidation verifySignature(JSONObject o) throws IOException {
    try {
      FormValidation warning = null;
      JSONObject signature = o.getJSONObject("signature");
      if (signature.isNullObject())
        return FormValidation.error("No signature block found in " + this.name); 
      o.remove("signature");
      List<X509Certificate> certs = new ArrayList<X509Certificate>();
      CertificateFactory cf = CertificateFactory.getInstance("X509");
      for (Object cert : signature.getJSONArray("certificates")) {
        try {
          X509Certificate c = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(cert.toString().getBytes(StandardCharsets.UTF_8))));
          try {
            c.checkValidity();
          } catch (CertificateExpiredException e) {
            warning = FormValidation.warning(e, String.format("Certificate %s has expired in %s", new Object[] { cert, this.name }));
          } catch (CertificateNotYetValidException e) {
            warning = FormValidation.warning(e, String.format("Certificate %s is not yet valid in %s", new Object[] { cert, this.name }));
          } 
          LOGGER.log(Level.FINE, "Add certificate found in JSON document:\n\tsubjectDN: {0}\n\tissuer: {1}\n\tnotBefore: {2}\n\tnotAfter: {3}", new Object[] { c
                .getSubjectDN(), c.getIssuerDN(), c.getNotBefore(), c.getNotAfter() });
          LOGGER.log(Level.FINEST, () -> "Certificate from JSON document: " + c);
          certs.add(c);
        } catch (IllegalArgumentException ex) {
          throw new IOException("Could not decode certificate", ex);
        } 
      } 
      CertificateUtil.validatePath(certs, loadTrustAnchors(cf));
      if (certs.isEmpty())
        return FormValidation.error("No certificate found in %s. Cannot verify the signature", new Object[] { this.name }); 
      FormValidation resultSha512 = null;
      try {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initVerify((Certificate)certs.get(0));
        resultSha512 = checkSpecificSignature(o, signature, digest, "correct_digest512", sig, "correct_signature512", "SHA-512");
        switch (null.$SwitchMap$hudson$util$FormValidation$Kind[resultSha512.kind.ordinal()]) {
          case 1:
            return resultSha512;
          case 2:
            LOGGER.log(Level.INFO, "JSON data source '" + this.name + "' does not provide a SHA-512 content checksum or signature. Looking for SHA-1.");
            break;
          case 3:
            break;
          default:
            throw new AssertionError("Unknown form validation kind: " + resultSha512.kind);
        } 
      } catch (NoSuchAlgorithmException nsa) {
        LOGGER.log(Level.WARNING, "Failed to verify potential SHA-512 digest/signature, falling back to SHA-1", nsa);
      } 
      MessageDigest digest = MessageDigest.getInstance("SHA1");
      Signature sig = Signature.getInstance("SHA1withRSA");
      sig.initVerify((Certificate)certs.get(0));
      FormValidation resultSha1 = checkSpecificSignature(o, signature, digest, "correct_digest", sig, "correct_signature", "SHA-1");
      switch (null.$SwitchMap$hudson$util$FormValidation$Kind[resultSha1.kind.ordinal()]) {
        case 1:
          return resultSha1;
        case 2:
          if (resultSha512.kind == FormValidation.Kind.WARNING)
            return FormValidation.error("No correct_signature or correct_signature512 entry found in '" + this.name + "'."); 
          break;
        case 3:
          break;
        default:
          throw new AssertionError("Unknown form validation kind: " + resultSha1.kind);
      } 
      if (warning != null)
        return warning; 
      return FormValidation.ok();
    } catch (GeneralSecurityException e) {
      return FormValidation.error(e, "Signature verification failed in " + this.name);
    } 
  }
  
  private FormValidation checkSpecificSignature(JSONObject json, JSONObject signatureJson, MessageDigest digest, String digestEntry, Signature signature, String signatureEntry, String digestName) throws IOException {
    OutputStream nos = OutputStream.nullOutputStream();
    DigestOutputStream dos = new DigestOutputStream(nos, digest);
    SignatureOutputStream sos = new SignatureOutputStream(signature);
    String providedDigest = signatureJson.optString(digestEntry, null);
    if (providedDigest == null)
      return FormValidation.warning("No '" + digestEntry + "' found"); 
    String providedSignature = signatureJson.optString(signatureEntry, null);
    if (providedSignature == null)
      return FormValidation.warning("No '" + signatureEntry + "' found"); 
    json.writeCanonical(new OutputStreamWriter(new TeeOutputStream(dos, sos), StandardCharsets.UTF_8)).close();
    if (!digestMatches(digest.digest(), providedDigest)) {
      String msg = digestName + " digest mismatch: expected=" + digestName + " in '" + providedDigest + "'";
      if (LOGGER.isLoggable(Level.SEVERE)) {
        LOGGER.severe(msg);
        LOGGER.severe(json.toString(2));
      } 
      return FormValidation.error(msg);
    } 
    if (!verifySignature(signature, providedSignature))
      return FormValidation.error(digestName + " based signature in the update center doesn't match with the certificate in '" + digestName + "'"); 
    return FormValidation.ok();
  }
  
  private boolean verifySignature(Signature signature, String providedSignature) {
    try {
      if (signature.verify(Hex.decodeHex(providedSignature.toCharArray())))
        return true; 
    } catch (SignatureException|org.apache.commons.codec.DecoderException signatureException) {}
    try {
      if (signature.verify(Base64.getDecoder().decode(providedSignature)))
        return true; 
    } catch (SignatureException|IllegalArgumentException signatureException) {}
    return false;
  }
  
  private boolean digestMatches(byte[] digest, String providedDigest) { return (providedDigest.equalsIgnoreCase(Hex.encodeHexString(digest)) || providedDigest.equalsIgnoreCase(Base64.getEncoder().encodeToString(digest))); }
  
  @SuppressFBWarnings(value = {"NP_LOAD_OF_KNOWN_NULL_VALUE", "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE"}, justification = "https://github.com/spotbugs/spotbugs/issues/756")
  protected Set<TrustAnchor> loadTrustAnchors(CertificateFactory cf) throws IOException {
    Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
    Jenkins j = Jenkins.get();
    for (String cert : j.servletContext.getResourcePaths("/WEB-INF/update-center-rootCAs")) {
      Certificate certificate;
      if (cert.endsWith("/") || cert.endsWith(".txt"))
        continue; 
      try {
        InputStream in = j.servletContext.getResourceAsStream(cert);
        try {
          if (in == null) {
            if (in != null)
              in.close(); 
            continue;
          } 
          certificate = cf.generateCertificate(in);
          if (certificate instanceof X509Certificate) {
            X509Certificate c = (X509Certificate)certificate;
            LOGGER.log(Level.FINE, "Add CA certificate found in webapp resources:\n\tsubjectDN: {0}\n\tissuer: {1}\n\tnotBefore: {2}\n\tnotAfter: {3}", new Object[] { c.getSubjectDN(), c.getIssuerDN(), c.getNotBefore(), c.getNotAfter() });
          } 
          LOGGER.log(Level.FINEST, () -> "CA certificate from webapp resource " + cert + ": " + certificate);
          if (in != null)
            in.close(); 
        } catch (Throwable throwable) {
          if (in != null)
            try {
              in.close();
            } catch (Throwable throwable1) {
              throwable.addSuppressed(throwable1);
            }  
          throw throwable;
        } 
      } catch (CertificateException e) {
        LOGGER.log(Level.WARNING, String.format("Webapp resources in /WEB-INF/update-center-rootCAs are expected to be either certificates or .txt files documenting the certificates, but %s did not parse as a certificate. Skipping this resource for now.", new Object[] { cert }), e);
        continue;
      } 
      try {
        TrustAnchor certificateAuthority = new TrustAnchor((X509Certificate)certificate, null);
        anchors.add(certificateAuthority);
      } catch (IllegalArgumentException e) {
        LOGGER.log(Level.WARNING, 
            String.format("The name constraints in the certificate resource %s could not be decoded. Skipping this resource for now.", new Object[] { cert }), e);
      } 
    } 
    File[] cas = (new File(j.root, "update-center-rootCAs")).listFiles();
    if (cas != null)
      for (File cert : cas) {
        if (!cert.isDirectory() && !cert.getName().endsWith(".txt")) {
          Certificate certificate;
          try {
            InputStream in = Files.newInputStream(cert.toPath(), new java.nio.file.OpenOption[0]);
            try {
              certificate = cf.generateCertificate(in);
              if (certificate instanceof X509Certificate) {
                X509Certificate c = (X509Certificate)certificate;
                LOGGER.log(Level.FINE, "Add CA certificate found in Jenkins home:\n\tsubjectDN: {0}\n\tissuer: {1}\n\tnotBefore: {2}\n\tnotAfter: {3}", new Object[] { c
                      .getSubjectDN(), c.getIssuerDN(), c.getNotBefore(), c.getNotAfter() });
              } 
              LOGGER.log(Level.FINEST, () -> "CA certificate from Jenkins home " + cert + ": " + certificate);
              if (in != null)
                in.close(); 
            } catch (Throwable throwable) {
              if (in != null)
                try {
                  in.close();
                } catch (Throwable throwable1) {
                  throwable.addSuppressed(throwable1);
                }  
              throw throwable;
            } 
          } catch (InvalidPathException e) {
            throw new IOException(e);
          } catch (CertificateException e) {
            LOGGER.log(Level.WARNING, String.format("Files in %s are expected to be either certificates or .txt files documenting the certificates, but %s did not parse as a certificate. Skipping this file for now.", new Object[] { cert

                    
                    .getParentFile().getAbsolutePath(), cert
                    .getAbsolutePath() }), e);
          } 
          try {
            TrustAnchor certificateAuthority = new TrustAnchor((X509Certificate)certificate, null);
            anchors.add(certificateAuthority);
          } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, 
                String.format("The name constraints in the certificate file %s could not be decoded. Skipping this file for now.", new Object[] { cert.getAbsolutePath() }), e);
          } 
        } 
      }  
    return anchors;
  }
  
  private static final Logger LOGGER = Logger.getLogger(JSONSignatureValidator.class.getName());
}
