package com.zyp.ssl.client;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;

public class SocketServer {
    public static void main(String[] args) throws Exception {

        String projectPath = System.getProperty("user.dir");
        String resourceDirPath = projectPath + "/src/main/resources";
        // 客户端证书的路径
        String clientKeystorePath = resourceDirPath + "/cert2/client.keystore";
        // 服务端证书的路径
        String serverKeystorePath = resourceDirPath + "/cert2/server.keystore";

        // key store相关信息
        char[] keyStorePwd = "123456".toCharArray();
        char[] keyPwd = keyStorePwd;
        KeyStore keyStore = KeyStore.getInstance("JKS");
        KeyStore trustKeyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(clientKeystorePath), keyStorePwd);//必须先加载 keystore 才能对其进行访问
        trustKeyStore.load(new FileInputStream(serverKeystorePath), "123456".toCharArray());

        //创建管理JKS密钥库的X.509密钥管理器
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, keyPwd);//使用密钥内容源初始化此工厂。    提供者通常使用 KeyStore 来获取在安全套接字协商期间所使用的密钥内容
        KeyManager[] keyManagers = kmf.getKeyManagers();//Keymanager用于选择自己的安全证书，并发送给对方

        TrustManagerFactory tmFactory = TrustManagerFactory.getInstance("SunX509");
        tmFactory.init(trustKeyStore);
        TrustManager[] tManagers = tmFactory.getTrustManagers();//trustmanager决定是否信任对方证书

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, tManagers, new SecureRandom());

        //根据上面配置的SSL上下文来产生SSLServerSocketFactory,与通常的产生方法不同
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();

        SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(7070);

        serverSocket.setUseClientMode(false);//设置处于服务器模式，需要向对方出具安全证书
        serverSocket.setNeedClientAuth(true);//设置需要对方的安全证书，否则连接中断

        serverSocket.setEnabledCipherSuites(serverSocket.getEnabledCipherSuites());

        // 接受连接
        SSLSocket clientSocket = (SSLSocket) serverSocket.accept();

        // 获取输入输出流
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        // 读取客户端发送的数据
        String clientMessage = reader.readLine();
        System.out.println("Received from client: " + clientMessage);

        // 发送响应给客户端
        String response = "Hello from server!";
        writer.println(response);
        writer.flush();
        System.out.println("Sent to client: " + response);

        // 关闭连接
        clientSocket.close();
        serverSocket.close();

    }
}
