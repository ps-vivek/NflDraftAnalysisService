package com.nfl.draftAnalyzer.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;

import com.nfl.draftAnalyzer.constants.DraftAnalyzerConstants;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class FileUtils implements DraftAnalyzerConstants {

	/**
	 * Read an excel in which data in fixed multiple rows need to be merged as
	 * single row. The excel should have the same output format throughout.
	 * 
	 * 
	 * The header names must be supplied externally
	 * 
	 * @param fileName
	 * @param headerNames
	 * @param noOfRowsToCombine
	 */
	public static List<List<String>> fetchExcelData(String fileName, List<String> headerNames) {
		List<List<String>> mergedData = new ArrayList<>();
		List<String> dataToBeMerged = new ArrayList<String>();
		Workbook workbook = null;
		try {

			FileInputStream excelFile = new FileInputStream(new ClassPathResource(fileName).getFile());
			workbook = new XSSFWorkbook(excelFile);
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = sheet.iterator();

			mergedData.add(headerNames);

			while (iterator.hasNext()) {
				Row currentRow = iterator.next();
				Iterator<Cell> cellIterator = currentRow.iterator();

				while (cellIterator.hasNext()) {
					Cell currentCell = cellIterator.next();
					
					if (currentCell.getCellType() == CellType.STRING) {
						dataToBeMerged.add(currentCell.getStringCellValue());
					} else if (currentCell.getCellType() == CellType.NUMERIC) {
						dataToBeMerged.add(currentCell.getNumericCellValue() + "");
					}
				}

				mergedData.add(dataToBeMerged);
				dataToBeMerged = new ArrayList<String>();

			}

		} catch (Exception e) {
			log.error("Error while reading file data:" + e.getLocalizedMessage());
		} finally {
			try {
				workbook.close();
			} catch (IOException e) {
				log.error("Error while closing workbook:" + e.getLocalizedMessage());
			}
		}

		return mergedData;

	}

}
