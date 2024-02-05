package com.zyp.ssl.client;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SocketClient {
    public static void main(String[] args) throws Exception {
        // key store相关信息
        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources";
        // 客户端证书的路径
        String clientKeystorePath = resourceDirPath + "/cert2/client.keystore";
        // 服务端证书的路径
        String serverKeystorePath = resourceDirPath + "/cert2/server.keystore";

        char[] keyStorePwd = "123456".toCharArray();
        char[] keyPwd = keyStorePwd;

        KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore trustKeyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(clientKeystorePath),keyStorePwd);//必须先加载 keystore 才能对其进行访问
        trustKeyStore.load(new FileInputStream(serverKeystorePath),"123456".toCharArray());
        //创建管理JKS密钥库的X.509密钥管理器
        KeyManagerFactory kmf=KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore,keyPwd);//使用密钥内容源初始化此工厂。    提供者通常使用 KeyStore 来获取在安全套接字协商期间所使用的密钥内容

        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509");
        tmFactory.init(trustKeyStore);

        //初始sslcontext
//            SSLContext sslContext=SSLContext.getInstance("SSLv3");
        SSLContext sslContext=SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(),tmFactory.getTrustManagers(),new SecureRandom()); ////trustmanager决定是否信任对方证书
        // 生成套接字
        SSLSocketFactory socketFactory = sslContext.getSocketFactory();

        SSLSocket socket = (SSLSocket) socketFactory.createSocket("127.0.0.1", 7070); //主机IP地址以及端口

        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        socket.setUseClientMode(true);//false设置处于服务器模式
        socket.setEnableSessionCreation( true );
    }
}
