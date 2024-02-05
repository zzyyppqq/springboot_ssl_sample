package com.zyp.ssl.client;

import java.io.File;
import java.net.Socket;
import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

public class MyClientCertTest {


    private static final String KEYPASS = "123456";


    public static void main(String[] args) {
        try {
            new MyClientCertTest().performClientRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void performClientRequest() throws Exception {
        try {
            String projectPath = System.getProperty("user.dir");
            String resourceDirPath = projectPath + "/src/main/resources";
            // 客户端证书的路径
            String keystorePath = resourceDirPath + "/cert2/client.keystore";
            if (!new File(keystorePath).exists()) {
                System.out.println("keystorePath: " + keystorePath + " is not exist!");
                return;
            } else {
                System.out.println("keystorePath: " + keystorePath);
            }
            // keystore的密码
            String keystorePassword = "123456";

            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(new URI("file:" + keystorePath).toURL(), KEYPASS.toCharArray(), KEYPASS.toCharArray(), new PrivateKeyStrategy() {
                        @Override
                        public String chooseAlias(Map<String, PrivateKeyDetails> aliases, Socket socket) {
                            return "client";
                        }
                    })
                    .loadTrustMaterial(new File(keystorePath), KEYPASS.toCharArray(), (chain, authType) -> true)
                    .build();

            SSLConnectionSocketFactory sslsf = new
                    SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
            HttpResponse response = httpClient.execute(new HttpPost("https://localhost:443"));
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();

                System.out.println("----------------------------------------");
                System.out.println(response.getAllHeaders());
                EntityUtils.consume(entity);
            } else {
                System.out.println("StatusCode: " + response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}