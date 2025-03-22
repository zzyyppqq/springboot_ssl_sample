//package com.zyp.ssl.config;
//
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.server.WebServerFactoryCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.apache.catalina.connector.Connector;
//import org.apache.tomcat.util.net.SSLHostConfig;
//import org.apache.tomcat.util.net.SSLHostConfigCertificate;
//
//import java.io.InputStream;
//import java.security.KeyStore;
//import java.security.cert.CertificateFactory;
//import java.security.cert.X509Certificate;
//
//@Configuration
//public class SSLConfig {
//
//    @Bean
//    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> sslCustomizer() {
//        return factory -> {
//            factory.addConnectorCustomizers(connector -> {
//                connector.setProperty("SSLEnabled", "true");
//                connector.setProperty("sslProtocol", "TLS");
//                connector.setProperty("clientAuth", "true");
//
//                try {
//                    // 加载 CA 证书
//                    InputStream caCertStream = getClass().getResourceAsStream("/ca/ca.crt");
//                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//                    X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(caCertStream);
//
//                    // 配置信任库
//                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//                    trustStore.load(null, null);
//                    trustStore.setCertificateEntry("ca", caCert);
//
//                    // 配置 SSL
//                    SSLHostConfig sslHostConfig = new SSLHostConfig();
//                    sslHostConfig.setTruststore(trustStore);
//                    sslHostConfig.setCertificateVerification("required");
//
//                    connector.addSslHostConfig(sslHostConfig);
//                } catch (Exception e) {
//                    throw new RuntimeException("Failed to configure SSL", e);
//                }
//            });
//        };
//    }
//}