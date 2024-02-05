#[keytool命令制作CA根证书，签发二级证书 - dijia478 - 博客园](https://www.cnblogs.com/dijia478/p/12103977.html)

# 只能单向认证

# keystore：密钥库。可以理解为一种数据库，里面存的是受信任的公钥和自己的私钥，可以存多个。服务端和客户端各有一个。常见的文件后缀有keystore，jks，p12等等
# csr：证书签名请求文件。是由keystore文件里的自签名证书（后面会说是什么）生成出来的，一般于给CA机构，对该文件进行签名。签名后会变为cer文件。
# cer：证书文件。就是一般我们所说的证书了，里面保存了公钥和主体信息。常见的文件后缀有cer，crt，rsa等。

# 1. 生成客户端，服务端，CA，三者的密钥库
keytool -genkeypair -keyalg RSA -keysize 2048 -alias client -keystore client.keystore -storepass 123456 -dname "CN=client,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
keytool -genkeypair -keyalg RSA -keysize 2048 -alias server -keystore server.keystore -storepass 123456 -dname "CN=server,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
keytool -genkeypair -keyalg RSA -keysize 2048 -alias ca -keystore ca.keystore -storepass 123456 -dname "CN=ca,OU=zyp,O=zyp,L=nanjing,ST=jiangsu,C=CN"
# 密钥库中存储的是密钥对，通过以下命令查看
# 注意此处的证书链长度为1，一会经过CA签发后，会变成2。证书[1]里的所有者和发布者都是自己，这种就叫自签名证书。
keytool -list -keystore server.keystore -storepass 123456 -v

# 2. 服务端生成证书签名请求文件
keytool -certreq -alias server -keystore server.keystore -storepass 123456 -file server.csr
# 3. CA对证书请求文件进行签发
keytool -gencert -alias ca -keystore ca.keystore -storepass 123456 -infile server.csr -outfile server.cer
# 4. 服务端需要将cer证书导入到自己的密钥库中
# CA生成自己的自签名证书文件
keytool -exportcert -alias ca -keystore ca.keystore -storepass 123456 -file ca.cer
# 服务端将ca.cer导入到自己的server.keystore文件中
keytool -importcert -alias ca -keystore server.keystore -storepass 123456 -file ca.cer
keytool -importcert -alias server -keystore server.keystore -storepass 123456 -file server.cer
# 再看服务端密钥库里，server密钥对的详情，就会发现变成了证书链，长度变成了2。现在的证书[1]发布者，也变成了CA。原来的证书[1]，就变成了下面的证书[2]
keytool -list -keystore server.keystore -storepass 123456 -v


# 客户端生成证书链（可不需要）
#客户端端生成证书签名请求文件
keytool -certreq -alias client -keystore client.keystore -storepass 123456 -file client.csr
#CA对证书请求文件进行签发
keytool -gencert -alias ca -keystore ca.keystore -storepass 123456 -infile client.csr -outfile client.cer
keytool -importcert -alias client -keystore client.keystore -storepass 123456 -file client.cer

# 5. 将CA的ca.cer导入到客户端的密钥库中
keytool -importcert -alias ca -keystore client.keystore -storepass 123456 -file ca.cer


#CA根证书制作完毕，也已经签发了二级证书server.cer。
#现在三个密钥库中的情况是：
# - client中有自己的密钥对“client”，信任的ca证书“ca”
# - server中有自己的密钥对“server”，信任的ca证书“ca”，其中，“server”证书是一个证书链
# - ca中有自己的密钥对“ca”
# tls单向认证的时候，客户端加载client.keystore文件即可，服务端加载server.keystore即可

# PKCS12转p12
 keytool -list -keystore server.keystore -storepass 123456 -v

 keytool -importkeystore -srckeystore server.keystore -destkeystore server.jks -deststoretype JKS  -storepass 123456
 keytool -importkeystore -srckeystore client.keystore -destkeystore client.jks -deststoretype JKS  -storepass 123456

 keytool -importkeystore -srckeystore client.keystore -srcstoretype PKCS12 -destkeystore client.p12 -deststoretype PKCS12 -storepass 123456
 keytool -importkeystore -srckeystore client.jks -srcstoretype JKS -destkeystore client.p12 -deststoretype PKCS12 -storepass 123456

