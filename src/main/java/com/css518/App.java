package com.css518;
import java.util.List;


public class App {

    public static void main(String[] args) throws Exception{
        List<List<String>> c = ExcelUtils.readExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\评价信息.xlsx");
        Encipher encipher = new Encipher();
        List<List<String>> b =encipher.judgment(c);
        ExcelUtils.writeExcel("E:\\workspace\\Code\\隐私保护\\privacy\\src\\m\\sss.xlsx", b);
        
    }
}
