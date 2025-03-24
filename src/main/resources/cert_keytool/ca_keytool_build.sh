# keystore：密钥库。可以理解为一种数据库，里面存的是受信任的公钥和自己的私钥，可以存多个。服务端和客户端各有一个。常见的文件后缀有keystore，jks，p12等等
# csr：证书签名请求文件。是由keystore文件里的自签名证书（后面会说是什么）生成出来的，一般于给CA机构，对该文件进行签名。签名后会变为cer文件。
# cer：证书文件。就是一般我们所说的证书了，里面保存了公钥和主体信息。常见的文件后缀有cer，crt，rsa等。

#在 Java Spring 中配置 双向认证（mTLS） 需要使用 CA 证书、客户端证书 和 服务器证书。以下是使用 keytool 生成这些证书，并在 Spring 中配置双向认证的完整步骤：

# ====== 1. 生成 CA 证书 ======
#CA 证书用于签署客户端和服务器证书。

#(1) 生成 CA 私钥和证书
# 1.keytool -genkey 和 keytool -genkeypair 是 相同的命令，只是 -genkey 是 -genkeypair 的 旧版别名
#   在 keytool -genkeypair 命令中，-ext bc:c 是一个 扩展参数，用于指定生成的证书的 扩展属性。
# 2.具体来说，bc:c 是 基本约束（Basic Constraints） 扩展，用于标识证书是否为 CA 证书。
# 3.在生成 CA 证书 时，通常 不需要 指定 -ext "SAN=dns:localhost,ip:127.0.0.1"，这是 服务器证书 或 客户端证书 的常见扩展，用于支持多域名或 IP 地址。
#   CA 证书通常用于签署其他证书，而不是直接用于验证服务器或客户端身份。因此，CA 证书 不需要 指定 SAN。
keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -validity 365 -keystore ca.jks -storepass "123456" -keypass "123456" \
  -dname "CN=zyp.com, OU=ZYP, O=ZYP, L=NanJing, ST=JiangSu, C=CN" -ext bc:c
# 默认为PKCS12格式，指定-storetype JKS 则生成jks格式
#keytool -genkeypair -alias ca -keyalg RSA -keysize 2048 -validity 365 -keystore ca_jks.jks -storetype JKS -storepass "123456" -keypass "123456" \
#  -dname "CN=zyp.com, OU=ZYP, O=ZYP, L=NanJing, ST=JiangSu, C=CN" -ext bc:c
#keytool -list -keystore ca_jks.jks -storepass "123456" -v

#(2) 导出 CA 证书
keytool -exportcert -alias ca -keystore ca.jks -file ca.crt -storepass "123456"
# 验证证书
keytool -list -keystore ca.jks -storepass "123456" -v
# ca.jks放入java spring中运行报错the trustAnchors parameter must be non-empty
# 检查 ca.jks 文件
keytool -exportcert -alias ca -keystore ca.jks -file ca.crt -storepass "123456"
# 创建 Truststore 并导入 CA 证书
keytool -importcert -alias ca -file ca.crt -keystore truststore.jks -storepass "123456" -noprompt

# ===== 2. 生成服务器证书 ======
#服务器证书用于验证服务器身份。

#(1) 生成服务器私钥和证书请求**
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -validity 365 -keystore server.jks -storepass "123456" -keypass "123456" \
  -dname "CN=zyp.com, OU=ZYP, O=ZYP, L=NanJing, ST=JiangSu, C=CN"
#(2) 生成服务器证书签名请求（CSR）
keytool -certreq -alias server -keystore server.jks -file server.csr -storepass "123456"
#(3) 使用 CA 签署服务器证书
keytool -gencert -alias ca -infile server.csr -outfile server.crt -keystore ca.jks -storepass "123456" -validity 365 -ext "SAN=dns:zyp.com,ip:127.0.0.1"
#(4) 将 CA 证书和服务器证书导入服务器 Keystore
keytool -importcert -alias ca -file ca.crt -keystore server.jks -storepass "123456" -noprompt
keytool -importcert -alias server -file server.crt -keystore server.jks -storepass "123456" -noprompt

#======3. 生成客户端证书 =====
#客户端证书用于验证客户端身份。

#(1) 生成客户端私钥和证书请求
keytool -genkeypair -alias client -keyalg RSA -keysize 2048 -validity 365 -keystore client.jks -storepass "123456" -keypass "123456" \
  -dname "CN=zyp.com, OU=ZYP, O=ZYP, L=NanJing, ST=JiangSu, C=CN"
#(2) 生成客户端证书签名请求（CSR）
keytool -certreq -alias client -keystore client.jks -file client.csr -storepass "123456"
#(3) 使用 CA 签署客户端证书
keytool -gencert -alias ca -infile client.csr -outfile client.crt -keystore ca.jks -storepass "123456" -validity 365
#(4) 将 CA 证书和客户端证书导入客户端 Keystore
keytool -importcert -alias ca -file ca.crt -keystore client.jks -storepass "123456" -noprompt
keytool -importcert -alias client -file client.crt -keystore client.jks -storepass "123456" -noprompt

# ==== 测试双向认证 ====
#使用 curl 或其他工具测试双向认证。

#(1) 将客户端证书导出为 PKCS12 格式
keytool -importkeystore -srckeystore client.jks -destkeystore client.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass "123456" -deststorepass "123456"
#(2) 使用 curl 测试
curl -v --cacert ca.crt --cert client.p12:123456 --key client.p12:123456 https://localhost:8443


# === 配置 Spring Boot 服务器 ===
#server:
#  port: 8443
#  ssl:
#    key-store: classpath:server.jks
#    key-store-password: "123456"
#    key-alias: server
#    key-password: "123456"
#    trust-store: classpath:server.jks
#    trust-store-password: "123456"
#    client-auth: need


