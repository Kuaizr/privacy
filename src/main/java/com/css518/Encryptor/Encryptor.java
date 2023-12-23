package com.css518.Encryptor;

import javax.crypto.SecretKey;

public abstract class Encryptor {
    public abstract String encrypt(String data, SecretKey secretKey) throws Exception;
    public abstract String decrypt(String data, SecretKey secretKey) throws Exception;
}