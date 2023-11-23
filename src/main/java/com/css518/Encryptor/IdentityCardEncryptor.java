package com.css518.Encryptor;

public class IdentityCardEncryptor extends Encryptor {

    @Override
    public String encrypt(String data) {
        // 这里的加密算法非常简单，仅用于示例。
        // 实际情况下应当采用更加安全的加密算法。
        if (data != null && !data.isEmpty()) {
            // 假设身份证号码长度为18位，我们加密中间10位
            if (data.length() == 19) {
                String visiblePart = data.substring(0, 3) + data.substring(13);
                String encryptedPart = "*****"; // 使用5个星号替代中间10位
                return visiblePart + encryptedPart;
            }
        }
        return data;
    }
}
