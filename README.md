# springboot_ssl_sample

## https ssl-pinning

SSL Pinning（SSL证书绑定）是一种安全技术，用于防止网络攻击（例如中间人攻击，MITM）。通过SSL Pinning，应用程序可以确保它只信任特定的服务器证书，即使有恶意代理或篡改的服务器伪装成目标服务器，它们也无法绕过这一安全验证。SSL Pinning常用于移动应用（如Android和iOS）以及WebView中的HTTPS请求中。

工作原理
通常情况下，应用程序会验证服务器的SSL证书是否受信任的证书颁发机构（CA）签发。然而，这样的验证方式存在一定的风险，因为如果攻击者能够欺骗CA或通过不安全的网络环境（例如公共Wi-Fi），就有可能中间人攻击用户的数据。

SSL Pinning通过绑定应用程序与特定的服务器证书来加强验证。也就是说，应用程序不只验证证书的有效性，还会检查证书是否与预期的证书相匹配，确保只有目标服务器的证书能够通过验证。

实现方式
实现SSL Pinning有两种主要方式：

证书Pinning（Certificate Pinning）：将服务器的SSL证书（或证书的SHA-256哈希值）嵌入到应用程序中。应用程序验证连接的服务器证书与嵌入的证书是否匹配。
公钥Pinning（Public Key Pinning）：将服务器的公钥（而非完整证书）嵌入到应用程序中。应用程序在每次连接时只需验证服务器证书的公钥与嵌入的公钥是否一致。
在Android中的实现
在Android中，可以通过使用OkHttp或自定义的TrustManager实现SSL Pinning。以下是使用OkHttp库进行SSL Pinning的示例。


## https ssl-pinnig + 双向认证如何实现

实现SSL Pinning加上双向认证（Mutual TLS Authentication）能够进一步提升客户端和服务器之间的安全性。这种组合实现方式在保护敏感数据的通信中尤为有效，防止中间人攻击和未经授权的访问。以下是实现步骤：

工作原理
SSL Pinning：客户端绑定特定服务器的证书或公钥，以确保只与受信任的服务器通信。
双向认证：客户端和服务器相互验证对方的证书。服务器需要验证客户端的身份（即客户端证书），客户端也需要验证服务器的身份（通过SSL Pinning）。
实现步骤
服务器端配置：

配置服务器以启用双向TLS认证，要求客户端提供证书。
服务器保存并信任客户端的证书或证书颁发机构（CA），以便在客户端连接时进行验证。
客户端配置：

客户端启用SSL Pinning来绑定服务器证书。
客户端安装自身的客户端证书，并在连接服务器时提供该证书，供服务器验证。
在Android中实现双向认证与SSL Pinning
在Android中，可以使用OkHttp库来配置SSL Pinning和双向认证。以下是实现示例：

准备客户端证书和服务器证书
   客户端证书：与服务器通信的应用程序需要一个客户端证书，该证书由服务器或信任的CA签发。
   服务器证书：客户端需将服务器的证书哈希值嵌入应用程序中。

说明
SSL Pinning：SSL 固定：

CertificatePinner配置用于验证服务器的证书是否匹配预期的哈希值。请用实际的服务器域名和证书哈希值替换代码中的示例。
双向认证：

通过加载客户端证书（client_cert.p12）和服务器的CA证书（server_cert.pem）来进行身份验证。
KeyManagerFactory加载客户端的私钥和证书，以便客户端可以向服务器证明身份。
TrustManagerFactory加载服务器的CA证书，用于信任服务器证书。
SSLContext配置：

使用SSLContext初始化OkHttpClient，实现双向TLS认证。sslSocketFactory将启用SSL连接的双向认证和SSL Pinning。
注意事项
证书更新：SSL Pinning需要定期更新证书哈希值，以避免证书过期导致连接失败。
证书安全性：确保客户端证书的私钥安全存储，不应将其暴露在不安全的存储中。
性能：SSL Pinning和双向认证会增加通信开销和连接延迟。

```java
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SecureClient {

   public static OkHttpClient createSecureClient() throws Exception {

      // SSL Pinning: 配置服务器证书哈希
      CertificatePinner certificatePinner = new CertificatePinner.Builder()
              .add("yourdomain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // 替换成服务器的SHA-256哈希
              .build();

      // 双向认证: 加载客户端证书
      KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
      FileInputStream clientCert = new FileInputStream("path/to/client_cert.p12"); // 客户端证书路径
      clientKeyStore.load(clientCert, "client_password".toCharArray()); // 客户端证书密码
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(clientKeyStore, "client_password".toCharArray());

      // 双向认证: 加载服务器CA证书，信任服务器的证书
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      FileInputStream serverCert = new FileInputStream("path/to/server_cert.pem"); // 服务器证书路径
      X509Certificate caCert = (X509Certificate) cf.generateCertificate(serverCert);
      KeyStore trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustKeyStore.load(null, null);
      trustKeyStore.setCertificateEntry("ca", caCert);
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustKeyStore);

      // 配置SSLContext以实现双向认证
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

      // 创建OkHttpClient并启用SSL Pinning和双向认证
      return new OkHttpClient.Builder()
              .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
              .certificatePinner(certificatePinner)
              .build();
   }

   public static void main(String[] args) {
      try {
         OkHttpClient client = createSecureClient();

         // 发送请求
         Request request = new Request.Builder()
                 .url("https://yourdomain.com/secure-endpoint")
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

```

## openssl生成客户端和服务器的证书
### 使用OpenSSL生成客户端和服务器证书

#### 生成根证书（CA证书）

```shell
# 创建根证书的私钥
openssl genrsa -out rootCA.key 2048
#openssl genpkey -algorithm RSA -out rootCA.key -aes256

# 创建根证书
openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 3650 -out rootCA.pem \
    -subj "/C=US/ST=State/L=City/O=Company/OU=IT/CN=RootCA"
```

#### 为服务器生成证书

```sh
# 创建服务器的私钥
openssl genrsa -out server.key 2048

# 生成服务器的CSR（证书签名请求）
openssl req -new -key server.key -out server.csr \
    -subj "/C=US/ST=State/L=City/O=Company/OU=IT/CN=yourserver.com"

# 使用根证书签署服务器证书
openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial \
    -out server.crt -days 365 -sha256
```

#### 为客户端生成证书

```sh
# 创建客户端的私钥
openssl genrsa -out client.key 2048

# 生成客户端的CSR
openssl req -new -key client.key -out client.csr \
    -subj "/C=US/ST=State/L=City/O=Company/OU=IT/CN=client"

# 使用根证书签署客户端证书
openssl x509 -req -in client.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial \
    -out client.crt -days 365 -sha256
```

#### 将证书转换为PKCS12格式（Java Keystore格式）

```sh
# 生成客户端的PKCS12文件
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name client -CAfile rootCA.pem -caname root -password pass:12345678

# 生成服务器的PKCS12文件
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name server -CAfile rootCA.pem -caname root -password pass:12345678

keytool -importkeystore -srckeystore server.p12 -srcstoretype PKCS12 -destkeystore server.jks -deststoretype JKS
```

如果您的 rootCA.pem 文件仅包含证书而不包含私有密钥，则需要单独使用实际的私有密钥文件。例如：

rootCA.key – the private key.
rootCA.key – 私钥。
rootCA.pem – the certificate.
rootCA.pem – 证书。

```sh
# 将 PEM 转换为 PKCS12 格式
#openssl pkcs12 -export -in rootCA.pem -out rootCA.p12 -name rootCA -password pass:12345678
openssl pkcs12 -export -in rootCA.pem -inkey rootCA.key -out rootCA.p12 -name rootCA -password pass:12345678
# 将证书作为证书条目而不是密钥条目添加到密钥库中，涉及证书的导入，不涉及私钥
keytool -import -alias rootCA -file rootCA.pem -keystore rootCA2.jks
# 将 PKCS12 信任库转换为 JKS 格式，涉及到证书和私钥的转移。
keytool -importkeystore -srckeystore rootCA.p12 -srcstoretype PKCS12 -destkeystore rootCA.jks -deststoretype JKS

keytool -list -keystore rootCA.jks -storepass 12345678
```

### 将自定义 CA 证书导入 Java 信任库
Java 维护一个默认的信任存储区（通常位于 $JAVA_HOME/lib/security/cacerts ），其中包含受信任证书颁发机构的根证书。
您需要将自定义 CA 证书 （rootCA.pem） 导入到此信任存储中，以便 Java 信任此 CA 签名的任何证书。

在 macOS 上，Java 密钥库文件 （cacerts） 的默认密码通常为 changeit，除非已更改。这是 Java 用于保护存储可信根证书 （CA 证书） 的信任存储的密码。

要将自定义 CA 证书导入信任存储区，请使用 Java 附带的 keytool 实用程序：
```sh
export JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-17.0.9/Contents/Home
keytool -import -file rootCA.pem -keystore $JAVA_HOME/lib/security/cacerts -alias rootCA -storepass changeit
# 验证 CA 证书是否已导入：
sudo keytool -list -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit

#rootca, 2024年11月13日, trustedCertEntry, 
#证书指纹 (SHA-256): 14:76:62:96:51:91:AE:7F:5F:DD:D5:E6:E5:50:97:E6:AE:80:97:6D:F5:9C:19:1A:4D:74:03:91:DC:3D:55:F0


```


## java spring服务端实现ssl-pinnig + 双向认证

在Spring Boot应用中配置双向认证时，使用application.properties或application.yml进行配置。

在application.yml配置SSL与双向认证
```yaml
server:
  ssl:
    key-store: classpath:server.p12
    key-store-password: password
    key-store-type: PKCS12
    trust-store: classpath:rootCA.pem # 信任客户端的CA根证书
    trust-store-password: password
    trust-store-type: PEM
    client-auth: need # 强制要求客户端提供证书
```
3. 配置Spring Security以支持SSL客户端认证（可选）
   在Spring Security配置中，可以启用HTTPS请求并限制未经认证的请求。

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .requiresChannel()
                .anyRequest().requiresSecure() // 强制HTTPS
                .and()
                .authorizeRequests()
                .anyRequest().authenticated() // 需要认证
                .and()
                .csrf().disable(); // 根据需求启用或禁用CSRF保护
    }
}

```
4. 在客户端实现SSL Pinning与双向认证
   在客户端中可以使用OkHttp（Android）或其他HTTP库来实现SSL Pinning与双向认证。

OkHttp 客户端双向认证与SSL Pinning
```java
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SecureClient {
public static OkHttpClient createSecureClient() throws Exception {
// 配置SSL Pinning
CertificatePinner certificatePinner = new CertificatePinner.Builder()
.add("yourserver.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=") // 替换为实际哈希值
.build();

        // 加载客户端证书
        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        FileInputStream clientCert = new FileInputStream("path/to/client.p12"); // 客户端证书路径
        clientKeyStore.load(clientCert, "password".toCharArray());
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(clientKeyStore, "password".toCharArray());

        // 加载服务器的根证书
        KeyStore trustKeyStore = KeyStore.getInstance("PKCS12");
        FileInputStream serverCA = new FileInputStream("path/to/rootCA.pem");
        trustKeyStore.load(serverCA, "password".toCharArray());
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);

        // 配置SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        // 创建OkHttpClient
        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustManagerFactory.getTrustManagers()[0])
                .certificatePinner(certificatePinner)
                .build();
    }

    public static void main(String[] args) {
        try {
            OkHttpClient client = createSecureClient();

            // 发送请求
            Request request = new Request.Builder()
                    .url("https://yourserver.com/secure-endpoint")
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
```
注意事项
证书有效期：证书应定期更新，并将最新的证书在客户端与服务器端同步。
证书存储安全：客户端证书和服务器证书应妥善保管，防止泄漏。
调试和验证：可以使用Wireshark等工具验证双向认证和SSL Pinning是否正确配置。
