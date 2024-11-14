package com.zyp.ssl.client;

import com.zyp.ssl.util.PublicKeyUtils;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class SecureClient {

    public static OkHttpClient createSecureClient() throws Exception {
        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources/";
        String originPin0 = "147662965191AE7F5FDDD5E6E55097E6AE80976DF59C191A4D740391DC3D55F0";
        String originPin1 = "14:76:62:96:51:91:AE:7F:5F:DD:D5:E6:E5:50:97:E6:AE:80:97:6D:F5:9C:19:1A:4D:74:03:91:DC:3D:55:F0";
        // 配置SSL Pinning
        CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("yourserver.com", "sha256/ckdsjmr+84IPbzCdB8n5tZFpclOAVRFP+wRCmxGPs0I=") // 替换为实际哈希值
                .add("RootCA", "sha256/ckdsjmr+84IPbzCdB8n5tZFpclOAVRFP+wRCmxGPs0I=") // 替换为实际哈希值
                .add("192.168.9.65", "sha256/ckdsjmr+84IPbzCdB8n5tZFpclOAVRFP+wRCmxGPs0I=") // 替换为实际哈希值
//                .add("192.168.9.65", "sha256/147662965191AE7F5FDDD5E6E55097E6AE80976DF59C191A4D740391DC3D55F0") // 替换为实际哈希值
                .build();

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        FileInputStream ca = new FileInputStream(resourceDirPath + "certs/rootCA.pem");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(ca);
        String pin1 = CertificatePinner.pin(certificate);
        System.out.println("rootCA okhttp base64: " + pin1);
        String pin2 = PublicKeyUtils.sha256HashBase64(certificate);
        System.out.println("rootCA pem base64: " + pin2);
        String sha256Fingerprint = PublicKeyUtils.getPublicKeySha256Fingerprint(certificate);
        System.out.println("rootCA Sha256Fingerprint: " + sha256Fingerprint);

        System.out.println(certificate.getPublicKey().getEncoded().length);
        System.out.println(certificate.getEncoded().length);
        System.out.println(certificate.getSignature().length);

        String password = "12345678";
        // 加载客户端证书
        KeyStore clientKeyStore = KeyStore.getInstance("jks");
        FileInputStream clientCert = new FileInputStream(resourceDirPath + "cert_keytool/keystore.jks"); // 客户端证书路径
        clientKeyStore.load(clientCert, password.toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, password.toCharArray());

        // 加载服务器的根证书
        KeyStore trustKeyStore = KeyStore.getInstance("jks");
        FileInputStream serverCA = new FileInputStream(resourceDirPath + "cert_keytool/truststore.jks");
        trustKeyStore.load(serverCA, password.toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);

//        X509Certificate rootCA = (X509Certificate) trustKeyStore.getCertificate("rootCA");
//        String pin3 = PublicKeyUtils.sha256HashBase64(rootCA);
//        String sha256Fingerprint2 = PublicKeyUtils.getPublicKeySha256Fingerprint(rootCA);
//        System.out.println("rootCA Sha256Fingerprint2: " + sha256Fingerprint2);
//        System.out.println("rootCA p12 base64: " + pin3);

        // 配置SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        // 创建OkHttpClient
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
//                .certificatePinner(certificatePinner)
                .hostnameVerifier((host,sslSession) -> {
                    // 校验证书域名
                    System.out.println("host=> " + host);
                    return true;
                })
                .build();

    }

    public static void main(String[] args) {
        try {
            OkHttpClient client = createSecureClient();

            // 发送请求
            Request request = new Request.Builder()
                    .url("https://192.168.9.65/api/secure-data")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println(response.body().string());
                } else {
                    System.out.println("Request failed: " + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
