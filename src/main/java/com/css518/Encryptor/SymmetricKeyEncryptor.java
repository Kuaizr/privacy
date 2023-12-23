package com.css518.Encryptor; 
import javax.crypto.Cipher; 
import javax.crypto.SecretKey; 
import java.util.Base64; 

public class SymmetricKeyEncryptor extends Encryptor { 
    // 加密算法 
    private static final String ALGORITHM = "AES";

    // 加密 
    public String encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());

        // 将加密后的数据和密钥编码为Base64，便于存储和传输
        String encryptedData = Base64.getEncoder().encodeToString(encryptedBytes);

        return encryptedData;
    }

    // 解密
    public String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

        return new String(decryptedBytes);
    }
}