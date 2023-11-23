package com.css518;

import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.css518.Encryptor.Encryptor;

public class Encipher {
    private Map<String, Class<? extends Encryptor>> encryptorMap = new HashMap<>();

    public Encipher() {
        // 从配置文件中读取敏感词及其对应的加密类名称
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader("src/main/resources/config.json"));
            JSONObject sensitiveConfig = (JSONObject) config.get("sensitiveWords");
            for (Object key : sensitiveConfig.keySet()) {
                String sensitiveWord = (String) key;
                String encryptorClassName = (String) sensitiveConfig.get(key);
                try {
                    // 反射查找并加载类
                    Class<? extends Encryptor> encryptorClass = Class.forName(encryptorClassName).asSubclass(Encryptor.class);
                    encryptorMap.put(sensitiveWord, encryptorClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Encryption class " + encryptorClassName + " not found", e);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public List<List<String>> judgment(List<List<String>> columnsData) {
        for (List<String> column : columnsData) {
            if (!column.isEmpty()) {
                String firstString = column.get(0);
                for (String sensitive : encryptorMap.keySet()) {
                    if (firstString.contains(sensitive)) {
                        try {
                            Encryptor encryptor = encryptorMap.get(sensitive).getDeclaredConstructor().newInstance();
                            List<String> encryptedColumn = new ArrayList<>();
                            encryptedColumn.add(column.get(0)); // Add the unencrypted column name
                            for (int i = 1; i < column.size(); i++) { // Start from index 1, not 0
                                encryptedColumn.add(encryptor.encrypt(column.get(i)));
                            }
                            column.clear();
                            column.addAll(encryptedColumn);
                        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                            throw new RuntimeException("Encryption class instantiation error", e);
                        }
                        break;
                    }
                }
            }
        }
        return columnsData;
    }
}

