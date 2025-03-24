# 生成 CA 私钥和证书
#openssl genrsa 生成的私钥文件是PKCS#1 格式
openssl genrsa -out ca.key 2048
openssl req -new -x509 -key ca.key -out ca.crt -days 36500 \
-subj "/C=CN/ST=JiangSu/L=NanJing/O=ZYP/OU=ZYP/CN=zyp.com" -addext "subjectAltName=DNS:zyp.com,IP:127.0.0.1"
openssl x509 -in ca.crt -text -noout

# 生成服务端私钥和证书
openssl genrsa -out server.key 2048
openssl req -new -key server.key -out server.csr \
-subj "/C=CN/ST=JiangSu/L=NanJing/O=ZYP/OU=ZYP/CN=zyp.com" -addext "subjectAltName=DNS:zyp.com,IP:127.0.0.1"

openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt -days 36500 -extfile <(printf "subjectAltName=DNS:zyp.com,IP:127.0.0.1")

# 生成客户端私钥和证书
openssl genrsa -out client.key 2048
openssl req -new -key client.key -out client.csr \
-subj "/C=CN/ST=JiangSu/L=NanJing/O=ZYP/OU=ZYP/CN=zyp.com" -addext "subjectAltName=DNS:zyp.com,IP:127.0.0.1"

openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt -days 36500 -extfile <(printf "subjectAltName=DNS:zyp.com,IP:127.0.0.1")

# client crt转p12
openssl pkcs12 -export -out client.p12 -inkey client.key -in client.crt -certfile ca.crt
# server crt转p12
openssl pkcs12 -export -out server.p12 -inkey server.key -in server.crt -certfile ca.crt
# 查看p12文件
openssl pkcs12 -info -in ca.p12
# 计算server.crt指纹
openssl x509 -noout -fingerprint -sha256 -in server.crt


#Android 客户端仅需要验证服务端证书时使用。
#例如，用于 SSL Pinning 或验证服务端证书链。
openssl pkcs12 -export -out ca_nokeys.p12 -nokeys -in ca.crt
#Android 客户端需要同时使用 CA 证书和私钥时使用。
#例如，用于双向认证（mTLS）或签名操作。
openssl pkcs12 -export -out ca.p12 -inkey ca.key -in ca.crt

# 注意：openssl转换ca.crt到ca.p12 在java中使用存在兼容性问题，在java中需使用keytool将ca.crt转换为jks再将jks转换为p12
# crt转p12
openssl pkcs12 -export -nokeys -in ca.crt -out ca_nokeys.p12 -password pass:123456 -name "ca"
openssl pkcs12 -export -nokeys -in ca.crt -out ca_nokeys.p12 -password pass:123456
# 检查p12是否有效果
openssl pkcs12 -info -in ca_nokeys.p12 -password pass:123456 -noout


# 将 PEM 格式的私钥转换为 PKCS8 DER 格式
openssl pkcs8 -topk8 -inform PEM -outform DER -in client.key -out client.der -nocrypt

# 注意：openssl转换ca.crt到ca.p12 在java中使用存在兼容性问题，在java中需使用keytool将ca.crt转换为jks再将jks转换为p12
# 将 CA 证书导入信任库, java spring使用ca.jks作为信任库
# 如果 ca.jks 文件不存在，keytool 会自动创建一个新的 JKS 文件
# keytool -import 和 keytool -importcert 是 完全相同的命令，可以互换使用。
# 使用 -trustcacerts 参数可以将导入的证书标记为受信任的 CA 证书
# --- crt转jks ---
keytool -import -trustcacerts -alias myca -file ca.crt -keystore ca.jks -storepass "123456"
# 和以上命令相同
keytool -importcert -trustcacerts -alias myca -file ca.crt -keystore ca.jks -storepass "123456" -noprompt
# 查看
keytool -list -keystore ca.jks -storepass "123456"
# jks转p12
keytool -importkeystore -srckeystore ca.jks -destkeystore ca_jks_convert.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass 123456 -deststorepass 123456




# /Library/Java/JavaVirtualMachines/jdk-17.0.8.jdk/Contents/Home/lib/security
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.0.8.jdk/Contents/Home
# 信任到 JDK 的信任库
keytool -import -file ca.crt -keystore $JAVA_HOME/lib/security/cacerts -alias ca_zyp -storepass changeit
# 验证 CA 证书是否已导入：
keytool -list -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit
#查看每个证书的详细信息（如颁发者、有效期等） -v
keytool -list -v -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit | grep -A 15 ca_zyp
# 删除
keytool -delete -alias ca_zyp -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit

