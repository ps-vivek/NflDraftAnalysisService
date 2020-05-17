package com.nfl.draftAnalyzer.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import com.nfl.draftAnalyzer.constants.DraftAnalyzerConstants;
import com.nfl.draftAnalyzer.exception.ExcelReadException;
import com.nfl.draftAnalyzer.exception.ExcelWriteException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtils implements DraftAnalyzerConstants {

	/**
	 * Reads data from excel file. It is assumed that first row of the excel data
	 * contains the headers. It is also assumed that excel contains only one sheet
	 * 
	 * @param fileName
	 */
	public static List<List<String>> fetchExcelData(String fileName) {
		List<List<String>> mergedData = new ArrayList<>();
		List<String> dataToBeMerged = new ArrayList<String>();

		try (FileInputStream excelFile = new FileInputStream(new ClassPathResource(fileName).getFile())) {

			try (Workbook workbook = new XSSFWorkbook(excelFile)) {

				Sheet sheet = workbook.getSheetAt(0);
				Iterator<Row> iterator = sheet.iterator();

				while (iterator.hasNext()) {
					Row currentRow = iterator.next();
					if (currentRow.getRowNum() == HEADER_ROW) {
						continue;
					}
					Iterator<Cell> cellIterator = currentRow.iterator();

					while (cellIterator.hasNext()) {
						Cell currentCell = cellIterator.next();

						if (currentCell.getCellType() == CellType.STRING) {
							dataToBeMerged.add(currentCell.getStringCellValue());
						} else if (currentCell.getCellType() == CellType.NUMERIC) {
							dataToBeMerged.add(currentCell.getNumericCellValue() + StringUtils.EMPTY);
						}
					}

					mergedData.add(dataToBeMerged);
					dataToBeMerged = new ArrayList<String>();

				}
			}

		} catch (Exception e) {
			log.error(EXCEL_READ_EXCEPTION_MSG + e.getLocalizedMessage());
			throw new ExcelReadException(EXCEL_READ_EXCEPTION_MSG + e.getLocalizedMessage());
		}

		return mergedData;

	}

	/**
	 * -- Generic util method for writing data to excel First row will be header
	 * method. Subsequent rows will contain data corresponding to headers.
	 * 
	 * @param sheetName
	 * @param columnHeaderMapping
	 * @param excelData
	 * @param object
	 * @return ByteArrayResource
	 * @throws IOException
	 */
	public static ByteArrayResource writeToExcel(String sheetName, Map<String, String> columnHeaderMapping,
			List<?> excelData, Object object) {
		ByteArrayResource excelFileContents = null;
		try (Workbook workbook = new XSSFWorkbook()) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			Sheet sheet = workbook.createSheet(sheetName);

			Set<String> columnHeaders = writeHeaderRelatedInfo(columnHeaderMapping, workbook, sheet);
			int rowNum = HEADER_ROW + 1;
			for (Object rowDatas : excelData) {
				rowNum = writeDataAtCellLevel(columnHeaderMapping, sheet, columnHeaders, rowNum, rowDatas);
			}

			resizeAllColumns(sheet, columnHeaders);
			workbook.write(outputStream);
			excelFileContents = new ByteArrayResource(outputStream.toByteArray());

		} catch (IOException e) {
			log.error(EXCEL_WRITE_EXCEPTION_MSG + e.getLocalizedMessage());
			throw new ExcelWriteException(EXCEL_WRITE_EXCEPTION_MSG + e.getLocalizedMessage());
		}

		return excelFileContents;
	}

	private static void resizeAllColumns(Sheet sheet, Set<String> columnHeaders) {
		for (int i = 0; i < columnHeaders.size(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

	/**
	 * -- Write data at cell level based on the column header order
	 * 
	 * @param columnHeaderMapping
	 * @param sheet
	 * @param columnHeaders
	 * @param rowNum
	 * @param rowDatas
	 * @return
	 */
	private static int writeDataAtCellLevel(Map<String, String> columnHeaderMapping, Sheet sheet,
			Set<String> columnHeaders, int rowNum, Object rowDatas) {
		int columnNum = HEADER_ROW;
		Row row = sheet.createRow(rowNum++);
		for (String columnHeader : columnHeaders) {
			row.createCell(columnNum++)
					.setCellValue(fetchDataForCellLevelWriting(columnHeaderMapping, rowDatas, columnHeader));
		}
		return rowNum;
	}

	/**
	 * --Fetch data to write for each cell
	 * 
	 * @param columnHeaderMapping
	 * @param rowDatas
	 * @param columnHeader
	 * @return
	 */
	private static String fetchDataForCellLevelWriting(Map<String, String> columnHeaderMapping, Object rowDatas,
			String columnHeader) {
		Object rowData = StringUtils.EMPTY;
		try {
			Field field = rowDatas.getClass().getDeclaredField(columnHeaderMapping.get(columnHeader));
			field.setAccessible(true);
			rowData = field.get(rowDatas);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error("Unable to read the given field:" + e.getLocalizedMessage());
		}
		return StringUtils.EMPTY + rowData;
	}

	/**
	 * -- Write the header related info into excel
	 * 
	 * @param columnHeaderMapping
	 * @param workbook
	 * @param sheet
	 * @return
	 */
	private static Set<String> writeHeaderRelatedInfo(Map<String, String> columnHeaderMapping, Workbook workbook,
			Sheet sheet) {
		CellStyle headerCellStyle = setCellStyleForHeader(workbook);

		// Create a Row
		Row headerRow = sheet.createRow(HEADER_ROW);

		Set<String> columnHeaders = columnHeaderMapping.keySet();
		int cellCount = HEADER_ROW;
		for (String columnHeader : columnHeaders) {
			Cell cell = headerRow.createCell(cellCount);
			cell.setCellValue(columnHeader);
			cell.setCellStyle(headerCellStyle);
			cellCount++;
		}
		return columnHeaders;
	}

	/**
	 * -Create cell style for work book headers
	 * 
	 * @param workbook
	 * @return
	 */
	private static CellStyle setCellStyleForHeader(Workbook workbook) {
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 12);
		headerFont.setColor(IndexedColors.BLUE.getIndex());

		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		return headerCellStyle;
	}

}
