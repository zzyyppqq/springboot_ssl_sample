package com.zyp.ssl.util;

import java.io.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import okhttp3.CertificatePinner;
import org.bouncycastle.cert.*;
import org.bouncycastle.crypto.digests.*;
import org.bouncycastle.util.encoders.*;
import org.bouncycastle.util.io.pem.*;

public class FingerPrintGen {
    // 14:76:62:96:51:91:AE:7F:5F:DD:D5:E6:E5:50:97:E6:AE:80:97:6D:F5:9C:19:1A:4D:74:03:91:DC:3D:55:F0
    private static String cert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDNDCCAhwCCQDYf3ru7tlYLjANBgkqhkiG9w0BAQsFADBcMQswCQYDVQQGEwJV\n" +
            "UzEOMAwGA1UECAwFU3RhdGUxDTALBgNVBAcMBENpdHkxEDAOBgNVBAoMB0NvbXBh\n" +
            "bnkxCzAJBgNVBAsMAklUMQ8wDQYDVQQDDAZSb290Q0EwHhcNMjQxMTEzMDcxMjE1\n" +
            "WhcNMzQxMTExMDcxMjE1WjBcMQswCQYDVQQGEwJVUzEOMAwGA1UECAwFU3RhdGUx\n" +
            "DTALBgNVBAcMBENpdHkxEDAOBgNVBAoMB0NvbXBhbnkxCzAJBgNVBAsMAklUMQ8w\n" +
            "DQYDVQQDDAZSb290Q0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCi\n" +
            "mPUVBZQ/+UAgc1LWAZgfbDdUs4FKdUH1mzAMWDTwWu29p7FNMMVDlqrg4PCTgko1\n" +
            "Lbxdxb9LzEZcTpX1e6Fmh0lNUprgM1uhTnF5bO53ZUUZqNLCgBLrMrrWxr2JnUwD\n" +
            "bQ79VPXbIflK6SqxDUiNNCkcWYAoXUtPabOJhQMYbXaSAeI1pdhzFxqPmwrorsoK\n" +
            "uwr8tQWHFsBuFVLcpmS+fgDSjxXikM08ORqltA8YD0ku1H/dmUZVqHvT0NNB+LKQ\n" +
            "ZY+4uNBz9BmmhZ+U6sERzizuMg0/mLjJ048DqVlAy+mGL+cf4mM/HiuDTQ2aAphM\n" +
            "GMc7nukOZRO+2YRmQt0ZAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAC5/AhAA4z5C\n" +
            "XfR3kuYE802B57vauF9KnP8TdY6z624NF9sn3d9gbFnqB8vNo7BpybbUR6lDy87Y\n" +
            "19gPDc9z1JmzzHG3+Gu/6Vx+bPAToRMNiznbAMsXJF6a6QuQk8x+nRlJYVK/xKKv\n" +
            "LMf6xgwoyQBvk2ZkxmSDJ3D1fCgeGC6mzAlRlmu4WUueotXvVXpwVUpiTOWhnFnV\n" +
            "mEM1s2gP9wnbPYg0isddgdoO8z/YXeDHIZ2lM7MUFcsmyBuIaSBpJLToOiy8tB69\n" +
            "+b8eQsfH/lEkgmp4rM6HMjSE5AAQ2hV3PjMk9m2BSStdQs6CVYgDXjf6CPDy5Jal\n" +
            "MrU9vWH/m1M=\n" +
            "-----END CERTIFICATE-----";

    public static void main(String[] args) throws Exception {
        String fp = getFingerprint(cert);
        System.out.println(fp);

        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources/";
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream ca = new FileInputStream(resourceDirPath + "certs/rootCA.pem");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(ca);

        String pin1 = CertificatePinner.pin(certificate);
        System.out.println("rootCA okhttp base64: " + pin1);
        String pin2 = PublicKeyUtils.sha256HashBase64(certificate);
        System.out.println("rootCA pem base64: " + pin2);
        String sha256Fingerprint = PublicKeyUtils.getPublicKeySha256Fingerprint(certificate);
        String certSha256Fingerprint = PublicKeyUtils.getCertSha256Fingerprint(certificate);
        System.out.println("rootCA publicKey Sha256Fingerprint: " + sha256Fingerprint);
        System.out.println("rootCA cert Sha256Fingerprint: " + certSha256Fingerprint);
        String publicKeySha265 = PublicKeyUtils.getPublicKeySha265(certificate);
        System.out.println("rootCA publicKeySha265: " + publicKeySha265);


        // certificate.getEncoded() 为DER 编码格式的字节数组
        X509CertificateHolder certHolder = new X509CertificateHolder(certificate.getEncoded());
        String fingerprint = fingerprint(certHolder.toASN1Structure());
        System.out.println("fingerprint: " + fingerprint);
    }

    public static String getFingerprint(String file) throws Exception {
        try (PemReader pemReader = new PemReader(new StringReader(file))) {
            PemObject pemObject = pemReader.readPemObject();
            X509CertificateHolder certHolder = new X509CertificateHolder(pemObject.getContent());
            return fingerprint(certHolder.toASN1Structure());
        }
    }

    /**
     * The following two methods are taken from a Bouncy castle test package.
     * https://github.com/bcgit/bc-java/blob/master/tls/src/test/java/org/bouncycastle/tls/test/TlsTestUtils.java
     */
    static String fingerprint(org.bouncycastle.asn1.x509.Certificate c)
            throws IOException {
        byte[] der = c.getEncoded();
        byte[] sha1 = sha256DigestOf(der);
        byte[] hexBytes = Hex.encode(sha1);
        String hex = new String(hexBytes, "ASCII").toUpperCase();

        StringBuffer fp = new StringBuffer();
        int i = 0;
        fp.append(hex.substring(i, i + 2));
        while ((i += 2) < hex.length()) {
            fp.append(':');
            fp.append(hex.substring(i, i + 2));
        }
        return fp.toString();
    }

    static byte[] sha256DigestOf(byte[] input) {
        SHA256Digest d = new SHA256Digest();
        d.update(input, 0, input.length);
        byte[] result = new byte[d.getDigestSize()];
        d.doFinal(result, 0);
        return result;
    }

}