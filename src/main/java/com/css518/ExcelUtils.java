package com.css518;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


public class ExcelUtils {

    public static List<List<String>> readExcel(String path) throws IOException {
        List<List<String>> columnsData = new ArrayList<>();

        FileInputStream inputStream = new FileInputStream(new File(path));
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet firstSheet = workbook.getSheetAt(0);

        // 初始化columnsData数组
        int columns = firstSheet.getRow(0).getPhysicalNumberOfCells();
        for (int i = 0; i < columns; i++) {
            columnsData.add(new ArrayList<String>());
        }

        for (Row row : firstSheet) {
            int index = 0;
            for (Cell cell : row) {
                String text;
                
                switch (cell.getCellType()) {
                case STRING:
                    text = cell.getStringCellValue();
                    break;
                case NUMERIC:
                    text = Double.toString(cell.getNumericCellValue());
                    break;
                case BOOLEAN:
                    text = Boolean.toString(cell.getBooleanCellValue());
                    break;
                default:
                    text = "";
                }
                
                columnsData.get(index).add(text);
                index++;
            }
        }

        workbook.close();
        inputStream.close();

        return columnsData;
    }

    public static void writeExcel(String path, List<List<String>> columnsData) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        int maxRows = 0;
        for (List<String> columnData : columnsData) {
            if (columnData.size() > maxRows) {
                maxRows = columnData.size();
            }
        }

        for (int rowIdx = 0; rowIdx < maxRows; rowIdx++) {
            Row row = sheet.createRow(rowIdx);
            for (int colIdx = 0; colIdx < columnsData.size(); colIdx++) {
                Cell cell = row.createCell(colIdx);
                if (rowIdx < columnsData.get(colIdx).size()) {
                    String value = columnsData.get(colIdx).get(rowIdx);
                    cell.setCellValue(value);
                }
            }
        }

        FileOutputStream outputStream = new FileOutputStream(path);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}
