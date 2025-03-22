package com.zyp.ssl.client.okhttp.ignore;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class OkHttpIgnoreCert {

    /**
     * 忽略证书校验通常是指客户端在 HTTPS 请求中跳过对服务端证书的验证，可以绕过 SSL Pinning
     * 但是若是服务也校验客户端证书则无法请求，需要sslContext.init() 第一个参数不能为null
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


        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // 信任所有主机名
            }
        };


        SSLContext sslContext = SSLContext.getInstance("SSL");
//        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // 若是服务端校验客户端证书，则必须添加keyManagerFactory.getKeyManagers()
        sslContext.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

//        OkHttpClient client = new OkHttpClient.Builder()
//                .certificatePinner(certificatePinner)
//                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
//                .build();

        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier(hostnameVerifier)
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
