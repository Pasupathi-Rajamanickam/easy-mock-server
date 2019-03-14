package com.pastech;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtilityFunction {
	public static final String EXCEL_2003_EXTENSION = ".xls";
	public static final String EXCEL_2007_EXTENSION = ".xlsx";
	private static DataFormatter dataformat = new DataFormatter();
	private static Sheet sheet;

	/**
	 * To get sheet from file path
	 * 
	 * @param excelFilePath
	 * @param sheetName
	 * @return Sheet
	 * @throws IOException
	 */
	public static Sheet getSheet(String excelFilePath, String sheetName) throws IOException {
		Workbook workbook = null;
		FileInputStream fileInputStream = new FileInputStream(new File(excelFilePath));
		if (excelFilePath.toLowerCase().endsWith(EXCEL_2003_EXTENSION)) {
			workbook = new HSSFWorkbook(fileInputStream);
		} else if (excelFilePath.toLowerCase().endsWith(EXCEL_2007_EXTENSION)) {
			workbook = new XSSFWorkbook(fileInputStream);
		}
		sheet = workbook.getSheet(sheetName);
		return sheet;
	}

	public static List<Map<String, String>> getRowsURLStartsWith(String filePath, String sheetName, String url)
			throws IOException {
		if(sheet == null) {
			sheet = getSheet(filePath, sheetName);
		}
		
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		int urlIndex = getColumnIndex(sheet, "URL");

		// Comparison starts here
		int rowCount = sheet.getPhysicalNumberOfRows();
		List<String> columnNames = null;
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row != null) {
				Cell cell = row.getCell(urlIndex);
				if (dataformat.formatCellValue(cell).startsWith(url)) {
					if (columnNames == null) {
						columnNames = getColumnNames(sheet);
					}
					Map<String, String> matchedRow = new LinkedHashMap<String, String>();
					for (int columnIterator = 0; columnIterator < columnNames.size(); columnIterator++) {
						Cell cellIter = row.getCell(columnIterator);
						matchedRow.put(columnNames.get(columnIterator), dataformat.formatCellValue(cellIter));
					}
					result.add(matchedRow);
				}
			}
		}
		return result;
	}

	

	/**
	 * To Get column index from column name
	 * 
	 * @param sheet
	 * @param columnName
	 * @return columnIndex
	 */
	public static int getColumnIndex(Sheet sheet, String columnName) {
		int columnIndex = -1;
		Row columnRow = sheet.getRow(0);
		int columnCount = columnRow.getPhysicalNumberOfCells();
		for (int columnIterator = 0; columnIterator < columnCount; columnIterator++) {
			Cell cell = columnRow.getCell(columnIterator);
			String columnNameIterator = dataformat.formatCellValue(cell);
			if (columnName.equalsIgnoreCase(columnNameIterator)) {
				columnIndex = columnIterator;
				break;
			}
		}
		return columnIndex;
	}

	/**
	 * To get all column names
	 * 
	 * @param sheet
	 * @return columnNames
	 */
	public static List<String> getColumnNames(Sheet sheet) {
		List<String> columnNames = new ArrayList<String>();
		Row columnRow = sheet.getRow(0);
		int columnCount = columnRow.getPhysicalNumberOfCells();
		for (int columnIterator = 0; columnIterator < columnCount; columnIterator++) {
			columnNames.add(dataformat.formatCellValue(columnRow.getCell(columnIterator)));
		}
		return columnNames;
	}
}
