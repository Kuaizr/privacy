package com.css518.Encryptor;

import javax.crypto.SecretKey;

public class DataAnonymizer extends Encryptor {

    @Override
    public String encrypt(String data, SecretKey secretKey) throws Exception {
        // 获取替换起始点
        int start = data.length() / 2;
        
        // 创建StringBuilder用于构建结果
        StringBuilder anonymized = new StringBuilder(data.length());

        // 将前半部分保持不变
        anonymized.append(data.substring(0, start));
        
        // 将后半部分替换为星号(*)
        for (int i = start; i < data.length(); i++) {
            anonymized.append('*');
        }

        return anonymized.toString();
    }

    @Override
    public String decrypt(String data, SecretKey secretKey) throws Exception {
        return data;
    }
}
