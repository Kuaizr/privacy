package com.css518;
import java.util.List;

import javax.crypto.SecretKey;

import com.css518.Encipher.EncryptedData;


public class App {

    public static void main(String[] args) throws Exception{
        List<List<String>> c = ExcelUtils.readExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\评价信息.xlsx");
        Encipher encipher = new Encipher();
        EncryptedData b =encipher.encrypt(c);
        SecretKey key = b.getKey();
        System.out.println(key);
        ExcelUtils.writeExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\sss.xlsx", b.getData());

        // 测试解密
        List<List<String>> d = ExcelUtils.readExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\sss.xlsx");
        List<List<String>> e = encipher.decrypt(d, key);
        ExcelUtils.writeExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\sss1.xlsx", e);
        
    }
}


// public static void main(String[] args) {
//         Encipher encipher = new Encipher();
//         new Thread(() -> encipher.startWatchingConfig()).start();
//     }