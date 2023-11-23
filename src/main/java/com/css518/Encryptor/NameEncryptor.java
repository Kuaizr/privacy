package com.css518.Encryptor;

public class NameEncryptor extends Encryptor {

    @Override
    public String encrypt(String data) {
        if (data != null && !data.isEmpty()) {
            return "*" + data.substring(1);
        }
        return data;
    }
}
