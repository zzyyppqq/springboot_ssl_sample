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
openssl pkcs12 -export -out ca.p12 -nokeys -in ca.crt
#Android 客户端需要同时使用 CA 证书和私钥时使用。
#例如，用于双向认证（mTLS）或签名操作。
openssl pkcs12 -export -out ca.p12 -inkey ca.key -in ca.crt

# 将 PEM 格式的私钥转换为 PKCS8 DER 格式
openssl pkcs8 -topk8 -inform PEM -outform DER -in client.key -out client.der -nocrypt

#将 CA 证书导入信任库, java spring使用ca.jks作为信任库
keytool -import -trustcacerts -alias myca -file ca.crt -keystore ca.jks -storepass "123456"
