package com.zyp.ssl.client.okhttp;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class OkHttpGoP12Cert {


    /**
     * 客户端使用p12格式，ca使用crt格式
     * 验证通过OK
     */
    public static void main(String[] args) throws Exception {

        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources/";
        // 加载客户端证书和私钥
        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = new FileInputStream(resourceDirPath + "ca/client.p12")) {
            clientKeyStore.load(keyStoreStream, "".toCharArray());
        }
        if (clientKeyStore.size() == 0) {
            throw new RuntimeException("ClientKeyStore is empty!");
        }

        // 初始化 KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, "".toCharArray());


        // 加载 CA 根证书
        // 转换p12必须有私钥，才能进行双向认证，适用于双向认证
        // crt生成p12必须包含-inkey ca.key，不能使用-nokeys
        // openssl pkcs12 -export -out ca.p12 -inkey ca.key -in ca.crt
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream trustStoreStream = new FileInputStream(resourceDirPath + "ca/ca.p12")) {
            trustStore.load(trustStoreStream, "".toCharArray());
        }
        if (trustStore.size() == 0) {
            throw new RuntimeException("TrustStore is empty!");
        }

        // 初始化 TrustManagerFactory
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);


        // 配置 SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());


        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("zyp.com", "sha256/3bce9c7954cec6ec7f1866f761ae994049459bd3dbfa0cf7d0fbf59615b05e45")
                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .certificatePinner(certificatePinner)
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
                .build();

        Request request = new Request.Builder()
                .url("https://127.0.0.1:8443")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("请求成功: " + response.body().string());
            } else {
                System.out.println("请求失败: " + response.code());
            }
        }
    }
}
