package com.zyp.ssl.client.test;
 
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
 
import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
 
/**
 * https双向认证
 */
public class TwoWayHttpsTest {
 
    //安全传输层协议
    private static final String PROTOCOL = "TLS";
 
    // JKS/PKCS12
    private static final String KEY_KEYSTORE_TYPE = "PKCS12";
 
    public static SSLSocketFactory getSocketFactory(String cerPath, String p12Path, String password) throws Exception {
        InputStream cerInputStream = null;
        InputStream p12InputStream = null;
        SSLSocketFactory socketFactory = null;
        try {
            cerInputStream = new FileInputStream(new File(cerPath));
            p12InputStream = new FileInputStream(new File(p12Path));
            KeyManager[] keyManagers = getKeyManagers(p12InputStream, password);
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(keyManagers, trustManagers);
            socketFactory = sslContext.getSocketFactory();
        } finally {
            if (cerInputStream != null) {
                cerInputStream.close();
            }
            if (p12InputStream != null) {
                p12InputStream.close();
            }
        }
        return socketFactory;
    }
 
    private static SSLContext getSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(keyManagers, trustManagers, new SecureRandom());
        return sslContext;
    }
 
    private static KeyManager[] getKeyManagers(InputStream inputStream, String password) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_TYPE);
        //加载证书
        keyStore.load(inputStream, password.toCharArray());
        keyManagerFactory.init(keyStore, password.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        return keyManagers;
    }
 
    private static TrustManager[] getTrustManagers(InputStream inputStream) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_TYPE);
        //加载证书
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate ca = certificateFactory.generateCertificate(inputStream);
        keyStore.load(null, null);
        //设置公钥
        keyStore.setCertificateEntry("server", ca);
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        return trustManagers;
    }
 
    public static void main(String[] args) throws Exception {
        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources";
        // 客户端证书的路径
        //服务端公钥
//        String cerPath = resourceDirPath + "/cert/server.cer";
//        String cerPath = resourceDirPath + "/cert2/server.cer";
        //String cerPath = resourceDirPath + "/cert3/server.cer";
//        String cerPath = resourceDirPath + "/cert4/localhost.cer";
        String cerPath = resourceDirPath + "/cert5/server.crt";
        //客户端私钥
//        String p12Path = resourceDirPath + "/cert/client.keystore";
//        String p12Path = resourceDirPath + "/cert2/client.keystore";
        //String p12Path = resourceDirPath + "/cert3/client.p12";
//        String p12Path = resourceDirPath + "/cert4/client.p12";
        String p12Path = resourceDirPath + "/cert5/client.p12";
        String password = "123456";
        //获取SSLSocketFactory
//        String certPath = "D:\\workspace\\asyf_demo\\demo\\src\\main\\java\\com\\asyf\\demo\\other_api\\okhttp3\\server.cer";//服务端公钥
//        String p12Path = "D:\\workspace\\asyf_demo\\demo\\src\\main\\java\\com\\asyf\\demo\\other_api\\okhttp3\\client.p12";//客户端私钥
        //SSLSocketFactory socketFactory = getSocketFactory(cerPath, p12Path, "123456");
        //发送请求

        InputStream cerInputStream = null;
        InputStream p12InputStream = null;
        try {
            cerInputStream = new FileInputStream(cerPath);
            p12InputStream = new FileInputStream(p12Path);
            KeyManager[] keyManagers = getKeyManagers(p12InputStream, password);
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(keyManagers, trustManagers);

            OkHttpClient client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagers[0])
                    .hostnameVerifier((host,sslSession) -> {
                        // 校验证书域名
                        return true;
                    })
                    .build();

            Request request = new Request.Builder()
                    .url("https://localhost:443")
                    .build();

            Response response = client.newCall(request).execute();

            System.out.println(response.body().string());
        } finally {
            if (cerInputStream != null) {
                cerInputStream.close();
            }
            if (p12InputStream != null) {
                p12InputStream.close();
            }
        }
    }
 
}