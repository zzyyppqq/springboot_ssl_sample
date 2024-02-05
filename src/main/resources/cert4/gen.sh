#[基于springboot实现的https单向认证和双向认证（java生成证书）](https://blog.csdn.net/yybdeshijie/article/details/109261837)

#java生成HTTPS证书：
#既然是双向验证，就需要双方的密钥，我们服务端称为localhost，而客户端称为client。需要生成双方的密钥文件，并把对方的cert导入自己的密钥文件里。整个过程如下：
#注意：密码统一为：123456,这个密码自己可以设置，然后记住就可以了。

#生成服务端密钥文件localhost.jks：
keytool -genkey -alias localhost -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore localhost.jks -dname CN=localhost,OU=Test,O=pkslow,L=Guangzhou,C=CN -validity 731 -storepass 123456 -keypass 123456

#导出服务端的cert文件：
keytool -export -alias localhost -file localhost.cer -keystore localhost.jks -storepass 123456

#生成客户端的密钥文件client.jks：
keytool -genkey -alias client -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -keystore client.jks -dname CN=client,OU=Test,O=pkslow,L=Guangzhou,C=CN -validity 731 -storepass 123456 -keypass 123456
#导出客户端的cert文件：
keytool -export -alias client -file client.cer -keystore client.jks -storepass 123456
#把客户端的cert导入到服务端：
keytool -import -alias client -file client.cer -keystore localhost.jks -storepass 123456
#把服务端的cert导入到客户端：
keytool -import -alias localhost -file localhost.cer -keystore client.jks -storepass 123456

#检验服务端是否具有自己的private key和客户端的cert：
keytool -list -keystore localhost.jks -storepass 123456

#为了建立连接，应该要把客户端的密钥文件给谷歌浏览器使用。因为JKS是Java的密钥文件格式，我们转换成通用的PKCS12格式如下：
#转换JKS格式为P12：
keytool -importkeystore -srckeystore client.jks -destkeystore client.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass 123456 -deststorepass 123456 -srckeypass 123456 -destkeypass 123456 -srcalias client -destalias client -noprompt


openssl x509 -inform der -in localhost.cer -out localhost.pem