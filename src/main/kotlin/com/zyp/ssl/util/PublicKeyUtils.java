package com.zyp.ssl.util;

import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class PublicKeyUtils {

    public static byte[] sha256Hash(X509Certificate certificate) {
        try {
            PublicKey publicKey = certificate.getPublicKey();
            byte[] publicKeyEncoded = publicKey.getEncoded();
            // 使用 MessageDigest 计算 SHA-256 哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(publicKeyEncoded);

            return hash;
        } catch (Exception e) {
            throw new RuntimeException("Error computing SHA-256 hash for certificate", e);
        }
    }

    public static String sha256HashBase64(X509Certificate certificate) {
        byte[] hash = sha256Hash(certificate);

        return Base64.getEncoder().encodeToString(hash);  // 将结果编码为 Base64 字符串
    }

    // 计算 SHA-256 哈希
    public static byte[] getPublicKeySha256(PublicKey publicKey) {
        try {
            // 获取公钥的字节编码
            byte[] encodedPublicKey = publicKey.getEncoded();

            // 使用 MessageDigest 计算 SHA-256 哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(encodedPublicKey); // 返回计算出的 SHA-256 哈希值
        } catch (Exception e) {
            throw new RuntimeException("Error calculating SHA-256 for public key", e);
        }
    }

    public static byte[] getCertificateSha256(Certificate certificate) {
        try {
            // 获取公钥的字节编码
            byte[] encodedPublicKey = certificate.getEncoded();

            // 使用 MessageDigest 计算 SHA-256 哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(encodedPublicKey); // 返回计算出的 SHA-256 哈希值
        } catch (Exception e) {
            throw new RuntimeException("Error calculating SHA-256 for public key", e);
        }
    }

    // 将 SHA-256 哈希值格式化为指纹（以冒号分隔的十六进制字符串）
    public static String formatSha256Fingerprint(byte[] sha256Hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : sha256Hash) {
            // 将每个字节转换为两位十六进制数，并在前面加上冒号
            hexString.append(String.format("%02X:", b));
        }
        // 去掉最后一个多余的冒号
        return hexString.substring(0, hexString.length() - 1);
    }

    // 将字节数组转换为十六进制字符串（最多 n 个字节）
    public static String toHex(byte[] bytes, int maxBytes) {
        StringBuilder hexString = new StringBuilder();
        int length = Math.min(bytes.length, maxBytes);

        for (int i = 0; i < length; i++) {
            hexString.append(String.format("%02x", bytes[i]));
            if (i < length - 1) {
                hexString.append(":");
            }
        }
        return hexString.toString().toUpperCase();
    }

    public static String getPublicKeySha265(X509Certificate certificate) {
        PublicKey publicKey = certificate.getPublicKey();
        byte[] publicKeyBytes = publicKey.getEncoded();
        return toHex(publicKeyBytes, 256);
    }

    // 计算公钥的 SHA-256 指纹，并以冒号分隔的十六进制格式返回
    public static String getPublicKeySha256Fingerprint(X509Certificate certificate) {
        byte[] sha256Hash = getPublicKeySha256(certificate.getPublicKey());
        return formatSha256Fingerprint(sha256Hash); // 将哈希值格式化为指纹
    }

    // 计算公钥的 SHA-256 指纹，并以冒号分隔的十六进制格式返回
    public static String getCertSha256Fingerprint(X509Certificate certificate) {
        byte[] sha256Hash = getCertificateSha256(certificate);
        return formatSha256Fingerprint(sha256Hash); // 将哈希值格式化为指纹
    }
}
