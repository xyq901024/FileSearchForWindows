package com.xyq.fs.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
   
	public String readExcel(Path path)  {
		
		StringBuilder sb = new StringBuilder();
		InputStream is = null;
		Workbook wb = null;
		boolean isXlsx = false;
		if(path.toString().endsWith(".xlsx"))
			isXlsx = true;
		try {
			is = new FileInputStream(path.toString());
			if(isXlsx)
			wb = new XSSFWorkbook(is);
			else 
				wb = new HSSFWorkbook(is);
			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				Sheet sheet = wb.getSheetAt(i);
				if(isXlsx)
					sheet = (XSSFSheet)sheet;
				else sheet = (HSSFSheet)sheet;
				sb.append(readExcelCore(sheet,isXlsx));
				}
		} catch ( Exception e) {
			System.out.println(path+"无法读取");
		}finally{
			try {
				if(wb!=null)
					wb.close();
				if(is!=null)
					is.close();
			} catch (Exception e2) {
			}
		}
		return sb.toString();
	}
	
	
	public  StringBuilder readExcelCore(Sheet sheet, boolean isXlsx){
		
		StringBuilder sb = new StringBuilder();
		Row row = null;
		for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			row = sheet.getRow(rowIndex);
			if(isXlsx){
				row = (XSSFRow)row;
			}else 
				row = (HSSFRow)row;
			if (row != null) {
				for (int i = 0; i < row.getLastCellNum(); i++) { 
					Cell cell = row.getCell(i);
					if(isXlsx)
						cell = (XSSFCell)cell;
					else cell = (HSSFCell)cell;
					if(cell!=null){
						if(cell.getCellType() == Cell.CELL_TYPE_STRING){
							sb.append(cell.getStringCellValue());
						}else if(cell.getCellType() == 2 ||cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
							sb.append(cell.getNumericCellValue());
						}
					}
				}
			}
		}
		return sb;
	}
	
	public static void main(String[] args) {
		
		ExcelUtils eu = new ExcelUtils();
		System.out.println(eu.readExcel(Paths.get("d:\\中盐江西盐化有限公司党委支票用款单.xls")));

	}
}
