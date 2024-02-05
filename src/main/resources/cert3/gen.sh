# ---- 单向认证 ----
#生成服务端证书
keytool -genkey -alias server -keypass 123456 -keyalg RSA -keysize 2048 -validity 365 -storepass 123456 -storetype PKCS12 -keystore server.p12  -dname "CN=localhost,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
#导出服务端cer证书
keytool -export -alias server -keystore server.p12 -storetype PKCS12 -keypass 123456 -file server.cer -storepass 123456
# 配置SpringBoot
#将server.p12拷贝到SpringBoot的resources目录
#ssl配置
server.ssl.enabled=true
server.ssl.key-store=classpath:server.p12
server.ssl.key-store-password=123456
server.ssl.key-store-type=JKS
# 证书别名
server.ssl.key-alias=server


# ---- 双向认证 ----
#1.生成客户端证书
keytool -genkey -alias client -keypass 123456 -keyalg RSA -keysize 2048 -validity 365 -storepass 123456 -storetype PKCS12 -keystore client.p12  -dname "CN=localhost,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
#2.导出客户端cer证书
keytool -export -alias client -keystore client.p12 -storetype PKCS12 -keypass 123456 -file client.cer -storepass 123456
#3.生成keystore用来存储springboot信任的证书
keytool -genkey -alias springboot_keystore -keypass 123456 -keyalg RSA -keysize 2048 -validity 365 -storepass 123456 -storetype PKCS12 -keystore springboot_keystore.keystore  -dname "CN=localhost,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
#4.导入客户端的公钥到springboot_keystore.keystore
keytool -import -v -file client.cer -keystore springboot_keystore.keystore -storepass 123456

#配置SpringBoot
#将springboot_keystore.keystore拷贝到SpringBoot项目，并添加如下配置

#双向认证配置
server.ssl.trust-store=classpath:springboot_keystore.keystore
server.ssl.trust-store-password=123456
server.ssl.client-auth=need
server.ssl.trust-store-type=JKS
server.ssl.trust-store-provider=SUN


openssl x509 -inform der -in server.cer -out server.pem
openssl x509 -inform der -in client.cer -out client.crt

openssl x509 -inform der -in server.cer -out server.pem

openssl pkcs12 -in client.p12 -clcerts -nokeys -out client.cer