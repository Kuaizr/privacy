package com.css518;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.*;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.css518.Encryptor.Encryptor;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class Encipher {
    private volatile Map<String, Class<? extends Encryptor>> encryptorMap = new HashMap<>();
    private String configPath = "src/main/resources/config.json";

    public Encipher() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            JSONParser parser = new JSONParser();
            JSONObject config = (JSONObject) parser.parse(new FileReader(configPath));
            JSONObject sensitiveConfig = (JSONObject) config.get("sensitiveWords");
            Map<String, Class<? extends Encryptor>> tempEncryptorMap = new HashMap<>();
            for (Object key : sensitiveConfig.keySet()) {
                String sensitiveWord = (String) key;
                String encryptorClassName = (String) sensitiveConfig.get(key);
                try {
                    Class<? extends Encryptor> encryptorClass = Class.forName(encryptorClassName).asSubclass(Encryptor.class);
                    tempEncryptorMap.put(sensitiveWord, encryptorClass);
                } catch (ClassNotFoundException e) {
                    logError("Encryption class " + encryptorClassName + " not found", e);
                }
            }
            // Update encryptorMap in an atomic operation to ensure thread safety
            encryptorMap = tempEncryptorMap;
        } catch (Exception e) {
            logError("Failed to load configuration", e);
        }
    }


    public void startWatchingConfig() {
        Path configPath = Paths.get("src/main/resources").toAbsolutePath();
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            configPath.register(watchService, ENTRY_MODIFY);

            while (true) {
                WatchKey watchKey;
                try {
                    watchKey = watchService.take(); // This call is blocking until events are present
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logError("Watch Service interrupted", e);
                    return;
                }

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    if (fileName.toString().equals("config.json")) {
                        try {
                            Thread.sleep(100); // Add a small delay to ensure the file has been fully written
                            loadConfig();
                        } catch (Exception e) {
                            logError("Error reloading configuration", e);
                        }
                    }
                }

                boolean valid = watchKey.reset();
                if (!valid) {
                    break;
                }
            }
        } catch (IOException e) {
            logError("Unable to start watch service for config file", e);
        }
    }

    private void logError(String message, Exception e) {
        // Implement your logging logic here
        // For example, you could log to console or use a logging framework
        System.err.println(message);
        e.printStackTrace();
    }

    public void saveConfig() {
        JSONObject config = new JSONObject();
        JSONObject sensitiveConfig = new JSONObject();

        // 构造新的配置
        for (Map.Entry<String, Class<? extends Encryptor>> entry : encryptorMap.entrySet()) {
            sensitiveConfig.put(entry.getKey(), entry.getValue().getName());
        }
        config.put("sensitiveWords", sensitiveConfig);

        try (FileWriter file = new FileWriter("src/main/resources/config.json")) {
            file.write(config.toJSONString());
            file.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error while writing configuration to file", e);
        }
    }

    public void setEncryptorMap(String sensitiveWord, String encryptorClassName) {
        try {
            // 检查是否需要更新encryptorMap
            if (encryptorMap.containsKey(sensitiveWord) && 
                !encryptorMap.get(sensitiveWord).getName().equals(encryptorClassName)) {
                // 反射查找并加载新类
                Class<? extends Encryptor> encryptorClass = Class.forName(encryptorClassName).asSubclass(Encryptor.class);
                encryptorMap.put(sensitiveWord, encryptorClass);
                // 保存配置到文件中
                saveConfig();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Encryption class " + encryptorClassName + " not found", e);
        }
    }

    public EncryptedData encrypt(List<List<String>> columnsData) throws Exception {
        // 生成密钥
        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
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
                                encryptedColumn.add(encryptor.encrypt(column.get(i), secretKey));
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
        return new EncryptedData(columnsData, secretKey);
    }

    public List<List<String>> decrypt(List<List<String>> columnsData, SecretKey secretKey) throws Exception {
        for (List<String> column : columnsData) {
            if (!column.isEmpty()) {
                String firstString = column.get(0);
                for (String sensitive : encryptorMap.keySet()) {
                    if (firstString.contains(sensitive)) {
                        try {
                            Encryptor encryptor = encryptorMap.get(sensitive).getDeclaredConstructor().newInstance();
                            List<String> decryptedColumn = new ArrayList<>();
                            decryptedColumn.add(column.get(0)); // Add the unencrypted column name
                            for (int i = 1; i < column.size(); i++) { // Start from index 1, not 0
                                decryptedColumn.add(encryptor.decrypt(column.get(i), secretKey));
                            }
                            column.clear();
                            column.addAll(decryptedColumn);
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

    public static class EncryptedData {
        private final List<List<String>> data;
        private final SecretKey key;

        public EncryptedData(List<List<String>> data, SecretKey key) {
            this.data = data;
            this.key = key;
        }

        public List<List<String>> getData() {
            return data;
        }

        public SecretKey getKey() {
            return key;
        }
    }
    
}
