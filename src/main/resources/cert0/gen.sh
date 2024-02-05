#[OpenSSL创建生成CA证书、服务器、客户端证书及密钥\_windows openssl生成ca-CSDN博客](https://blog.csdn.net/qq153471503/article/details/109524764)
# --- 此种方式生成的证书不是给springboot使用的，需要转成jks再导入ca证书 ---

# C-----国家（Country Name）
# ST----省份（State or Province Name）
# L----城市（Locality Name）
# O----公司（Organization Name）
# O----公司（组织名称）
# OU----部门（Organizational Unit Name）
# CN----产品名（Common Name）
# emailAddress----邮箱（Email Address）
# .cer 文件和 .crt 文件在本质上是相同的，们都可以包含X.509证书的公钥部分，并用于证书验证和加密通信。
# .cer 或 .crt 文件通常只包含证书的公钥部分，而私钥通常存储在单独的文件中，例如 .key 文件。在SSL/TLS连接中，客户端和服务器通常需要同时提供证书文件和私钥文件。
# ======  生成CA证书 =====
# 创建CA证书私钥（aes加密）（ca.key）
openssl genrsa -aes256 -out ca.key 2048
# 请求证书（ca.csr）
openssl req -new -sha256 -key ca.key -out ca.csr -config openssl.cnf
# 自签署证书（ca.cer）
openssl x509 -req -days 3650 -sha256 -signkey ca.key -in ca.csr -out ca.cer -extensions v3_req -extfile openssl.cnf
# 生成pem格式CA自签名证书
cat ca.cer >> ca.pem && cat ca.key >> ca.pem

# ======  生成服务器端证书 =====
# 创建服务器私钥（server.key）
openssl genrsa -aes256 -out server.key 2048
# 请求证书（server.csr）
openssl req -new -sha256 -key server.key -out server.csr -config openssl.cnf
# 使用CA证书签署服务器证书（server.cer、ca.srl）
openssl x509 -req -days 3650 -sha256 -extensions v3_req -extfile openssl.cnf -CA ca.cer -CAkey ca.key -CAserial ca.srl -CAcreateserial -in server.csr -out server.cer


# ======  生成客户端证书 =====
# 生成客户端私钥
openssl genrsa -aes256 -out client.key 2048
# 申请证书
openssl req -new -sha256 -key client.key -out client.csr -config openssl.cnf
# 使用CA证书签署客户端证书
openssl x509 -req -days 3650 -sha256 -extensions v3_req -extfile openssl.cnf -CA ca.cer -CAkey ca.key -CAserial ca.srl -CAcreateserial -in client.csr -out client.cer

# 查看证书文件详细信息
openssl x509 -in client.cer -text -noout
openssl x509 -in client.cer -inform PEM -text -noout
openssl x509 -in client.cer -enddate -noout
openssl x509 -in client.cer -pubkey -noout

# ==== 测试 ====
# 单向认证命令行：
# 服务器：
openssl s_server -CAfile ca.cer -cert server.cer -key server.key -accept 22580
# 客户端：
openssl s_client -CAfile ca.cer -cert client.cer -key client.key -connect 127.0.0.1:22580

#双向认证：
#服务器：
openssl s_server -CAfile ca.cer -cert server.cer -key server.key -accept 22580 -Verify 1
#客户端：
openssl s_client -CAfile ca.cer -cert server.cer -key server.key -cert client.cer -key client.key -connect 127.0.0.1:22580


#openssl.cnf
[req]
req_extensions = v3_req
distinguished_name = req_distinguished_name

[v3_req]
#basicConstraints = CA:FALSE
basicConstraints = critical, CA:TRUE, pathlen:1
keyUsage = nonRepudiation, digitalSignature, keyEncipherment
subjectAltName = @alt_names

[alt_names]
DNS.1 = *.aecg.com.cn
IP.1 = 192.168.9.65

[req_distinguished_name]
countryName = Country Name (2 letter code)
countryName_default = CH
stateOrProvinceName = State or Province Name (full name)
stateOrProvinceName_default = JiangSu
localityName = Locality Name (eg, city)
localityName_default = NanJing
organizationName = Organization Name (eg, company)
organizationName_default = NaLong
commonName = Common Name (e.g. server FQDN or YOUR name)
commonName_default = NaLong