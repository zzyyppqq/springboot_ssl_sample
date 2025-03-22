package com.zyp.ssl.client.okhttp;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;

public class OkHttpGoDerCert {

    /**
     *
     * 客户端使用Crt格式，ca使用Der格式
     * 验证ok
     */
    public static void main(String[] args) throws Exception {

        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources/";

        // 加载客户端证书
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate clientCert;
        try (InputStream certStream = new FileInputStream(resourceDirPath + "ca/client.crt")) {
            clientCert = certificateFactory.generateCertificate(certStream);
        }

        // 加载客户端私钥
        // openssl genrsa 生成的私钥文件默认是 PEM 格式的，而 PKCS8EncodedKeySpec 需要 DER 格式的私钥。
        // 将 PEM 格式的私钥转换为 PKCS8 DER 格式
        // openssl pkcs8 -topk8 -inform PEM -outform DER -in client.key -out client.der -nocrypt
        PrivateKey privateKey;
        try (InputStream keyStream = new FileInputStream(resourceDirPath + "ca/client.der")) {
            byte[] keyBytes = IOUtils.toByteArray(keyStream);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(keySpec);
        }


        // 创建 KeyStore
        KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        clientKeyStore.load(null, null);
        clientKeyStore.setCertificateEntry("client", clientCert);
        clientKeyStore.setKeyEntry("client", privateKey, "".toCharArray(), new Certificate[]{clientCert});

        // 初始化 KeyManagerFactory
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, "".toCharArray());

        // 加载 CA 根证书（如果需要）
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null); // 创建一个空的信任库
        try (InputStream caCertStream = new FileInputStream(resourceDirPath + "ca/ca.crt")) {
            CertificateFactory certificateFactoryCA = CertificateFactory.getInstance("X.509");
            Certificate caCert = certificateFactoryCA.generateCertificate(caCertStream);
            trustStore.setCertificateEntry("ca", caCert);
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
