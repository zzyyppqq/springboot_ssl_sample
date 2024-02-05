# [【分享】自签名证书 和 SpringBoot单双向认证配置及测试-云社区-华为云](https://bbs.huaweicloud.com/blogs/288569)

#生成CA证书
openssl req -new -newkey rsa:2048 -days 365 -nodes -x509 -keyout ca.key -out ca.crt -subj "/C=CN/ST=GD/L=SZ/O=HW/CN=HW"
# 生成服务端和客户端证书
#1.先生成私钥：
openssl genrsa -out server.key 2048

#2.生成证书请求文件，这一步的CN=后面的127.0.0.1要修改为自己的服务端（即使用者）地址（IP或域名）：
openssl req -new -sha256 -out server.csr -key server.key -subj "/C=CN/ST=GD/L=SZ/O=HW/OU=RDC/CN=127.0.0.1"

#3.添加扩展信息（可选），这一步骤如果是在windows上直接自己创建一个cert_extensions文本文件，里面的内容为subjectAltName=DNS:www.test.com,IP:192.168.1.2，这里是设置使用者可选名称，可以设置多个，用逗号相隔开即可。如果是在linux上，则使用下面的命令：
echo "subjectAltName=DNS:www.test.com,IP:192.168.9.65,IP:192.168.1.3" > cert_extensions

#4.用ca证书进行签发，后面的-extfile cert_extensions是扩展信息可选的
openssl x509 -req -days 365 -CA ca.crt -CAkey ca.key -CAcreateserial -in server.csr -out server.crt -extfile cert_extensions

#使用同样的方法生成客户端证书
openssl genrsa -out client.key 2048
openssl req -new -sha256 -out client.csr -key client.key -subj "/C=CN/ST=GD/L=SZ/O=HW/OU=RDC/CN=127.0.0.1"
openssl x509 -req -days 365 -CA ca.crt -CAkey ca.key -CAcreateserial -in client.csr -out client.crt -extfile cert_extensions
#本质上讲客户端证书和服务端证书是一样的，我们把服务端使用的证书叫服务端证书，客户端使用的证书叫客户端证书

#证书格式转化
#我们现在有了ca证书、ca签名的服务端和客户端证书（都有公私钥），但实际使用时不同的语言或不同的部署方法可能需要不同格式的证书，需要根据不同的情况进行格式转化。

#1、将服务端、客户端证书转为p12格式，p12格式的证书包含公私钥，这时候会让我们设置p12证书的密码，要记住密码
openssl pkcs12 -export -in server.crt -inkey server.key -out server.p12 -name "server"
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name "client"

#2、服务端证书转为jks格式，这里用到keytool，会要求输入p12证书的密码，同时设置jks证书密码
keytool -importkeystore -srckeystore server.p12 -srcstoretype PKCS12 -deststoretype JKS -destkeystore server.jks

#把ca证书（指ca公钥，ca私钥请自行保存，不能给服务端或客户端）导入server.jks，如果服务端有单独的信任链证书，也可以导入另外的信任链证书中，这里我测试的时候直接用的server.jks，其中123456是server.jks证书密码
keytool -importcert -alias ca -keystore server.jks -storepass 123456 -file ca.crt

#3、把client证书转为pem格式
openssl pkcs12 -clcerts -out clientWithKey.pem -in client.p12

#上一步生成的clientWithKey.pem中的私钥是加密的，如果要使用不加密的私钥，则需要解密，其中123456是生成client.p12证书的密码
openssl pkey -in clientWithKey.pem -outform pem -out clientKeyPem.pem -passin pass:123456

#最后把clientWithKey.pem中的公钥和解密后的私钥clientKeyPem.pem内容放到同一个文件中，得到client.pem（可自定义命名）：


#最后补充一下keytool自签名证书方法：

#用相同的方法生成ca/server/client的keypair（放在各自的keystore文件中）：
keytool -genkeypair -alias ca -keystore ca.keystore -storepass 123456
keytool -genkeypair -alias server -keystore server.keystore -storepass 123456
keytool -genkeypair -alias client -keystore client.keystore -storepass 123456
#查看内容：
keytool -list -keystore client.keystore -storepass 123456 -v
#生成证书请求文件：
keytool -certreq -alias client -keystore client.keystore -storepass 123456 -file client.csr
#用ca进行证书签发:
keytool -gencert -alias ca -keystore ca.keystore -storepass 123456 -infile client.csr -outfile client.cer    
#导出ca证书（包含ca公钥），如果有需要可以用此方法导出其他证书，这里我们只导出ca证书
keytool -exportcert -alias ca -keystore ca.keystore -storepass 123456 -file ca.cer

#把ca证书和ca签名的client证书都导入到client的keystore中，导入的client.cer将会替换原来client.keystore中相同别名的证书
keytool -importcert -alias ca -keystore client.keystore -storepass 123456 -file ca.cer
keytool -importcert -alias client -keystore client.keystore -storepass 123456 -file client.cer

#转为jks格式
keytool -importkeystore -srckeystore client.keystore -destkeystore client1.jks -deststoretype JKS
