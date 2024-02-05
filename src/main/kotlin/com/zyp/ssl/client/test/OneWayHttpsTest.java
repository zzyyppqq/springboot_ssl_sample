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
 * https单向认证
 */
public class OneWayHttpsTest {
 
    //安全传输层协议
    private static final String PROTOCOL = "TLS";
 
    // JKS/PKCS12
    private static final String KEY_KEYSTORE_TYPE = "PKCS12";
 
    private static SSLSocketFactory getSocketFactory(String cerPath) throws Exception {
        SSLSocketFactory socketFactory = null;
        try (InputStream cerInputStream = new FileInputStream(new File(cerPath))) {
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(trustManagers);
            socketFactory = sslContext.getSocketFactory();
        }
        return socketFactory;
    }
 
    private static SSLContext getSslContext(TrustManager[] trustManagers) throws Exception {
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
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
        String cerPath = resourceDirPath + "/cert3/server.cer";
        //获取SSLSocketFactory
//        String certPath = "D:\\workspace\\asyf_demo\\demo\\src\\main\\java\\com\\asyf\\demo\\other_api\\okhttp3\\server.cer";//服务端公钥
//        SSLSocketFactory socketFactory = getSocketFactory(certPath);
        SSLSocketFactory socketFactory = null;
        try (InputStream cerInputStream = new FileInputStream(new File(cerPath))) {
            TrustManager[] trustManagers = getTrustManagers(cerInputStream);
            SSLContext sslContext = getSslContext(trustManagers);
            socketFactory = sslContext.getSocketFactory();

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
        }
    }
 
}