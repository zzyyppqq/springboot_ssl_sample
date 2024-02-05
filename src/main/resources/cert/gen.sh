#[SpringBoot开启SSL双向验证,以及使用OkHttp作为客户端访问 - 技术交流 - Spring Boot中文社区](https://springboot.io/t/topic/171)

# 1.创建CA证书并且导出公钥
keytool -genkey -deststoretype pkcs12 -alias CA_ROOT -validity 3500 -keystore CA_ROOT.keystore -keyalg RSA -keysize 2048 -storepass 123456 -dname "CN=ca,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
keytool -export -alias CA_ROOT -file CA_ROOT.cer -keystore CA_ROOT.keystore -storepass 123456

#2.创建客户端证书并且创建证书申请
keytool -genkey -deststoretype pkcs12 -alias client -validity 365 -keystore client.keystore -keyalg RSA -keysize 2048 -storepass 123456 -dname "CN=client,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
keytool -certreq -alias client -file client.csr -keystore client.keystore -storepass 123456
#使用CA证书签发客户端证书
keytool -gencert -alias CA_ROOT -infile client.csr -outfile client.cer -keystore CA_ROOT.keystore -storepass 123456

#3.客户端导入CA签发的证书,以及CA的公钥到keystore
#把ca证书和ca签名的client证书都导入到client的keystore中，导入的client.cer将会替换原来client.keystore中相同别名的证书
keytool -import -file CA_ROOT.cer -alias ca -keystore client.keystore -storepass 123456
keytool -import -file client.cer -alias client -keystore client.keystore -storepass 123456


#4.创建服务端证书并且创建证书申请
keytool -genkey -deststoretype pkcs12 -alias server -validity 365 -keystore server.keystore -keyalg RSA -keysize 2048 -storepass 123456 -dname "CN=server,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
keytool -certreq -alias server -file server.csr -keystore server.keystore -storepass 123456
#使用CA证书签发服务端证书
keytool -gencert -alias CA_ROOT -infile server.csr -outfile server.cer -keystore CA_ROOT.keystore -storepass 123456

#5.服务端导入CA签发的证书,以及CA的公钥到keystore
keytool -import -file CA_ROOT.cer -alias ca -keystore server.keystore -storepass 123456
keytool -import -file server.cer -alias server -keystore server.keystore -storepass 123456


#查看内容：
keytool -list -keystore client.keystore -storepass 123456 -v
