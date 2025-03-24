package com.zyp.ssl.client;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.Iterator;

public class TruststoreTest {
    public static void main(String[] args) throws Exception {
        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources/";

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(resourceDirPath + "ca/ca_jks_convert.p12")) {
            trustStore.load(fis, "123456".toCharArray());
        }
        Enumeration<String> aliases = trustStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            System.out.println("Alias: " + alias);
        }
        Certificate cert = trustStore.getCertificate("myca");
        System.out.println("trustStore size: " + trustStore.size() + ", Certificate: " + cert);

        // 使用别名加载证书
        Certificate cert1 = trustStore.getCertificate("1"); // 尝试默认别名 "1"
        if (cert1 != null) {
            System.out.println("Certificate: " + cert1);
        } else {
            System.out.println("Certificate not found for alias '1'");
        }

        KeyStore trustStoreJks = KeyStore.getInstance("JKS");
        try (InputStream trustStoreStream = new FileInputStream(resourceDirPath + "ca/ca.jks")) {
            trustStoreJks.load(trustStoreStream, "123456".toCharArray());
        }
        if (trustStoreJks.size() == 0) {
            throw new RuntimeException("TrustStore is empty!");
        }
        Certificate certJks = trustStoreJks.getCertificate("myca");
        System.out.println("trustStore size: " + trustStoreJks.size() + ", Certificate: " + certJks);
        Enumeration<String> aliasesJks = trustStoreJks.aliases();
        while (aliasesJks.hasMoreElements()) {
            String alias = aliasesJks.nextElement();
            System.out.println("aliasesJks: " + alias);
        }
    }
}