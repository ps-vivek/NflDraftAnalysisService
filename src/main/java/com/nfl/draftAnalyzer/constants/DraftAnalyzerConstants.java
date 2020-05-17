package com.nfl.draftAnalyzer.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public interface DraftAnalyzerConstants {
	public static final Double DEFAULT_PROSPECT_GRADE = 0d;
	public static final String COMMA_DELIMITER = ",";
	public static final int ROUNDING_PRECISION = 2;
	public static final String ALL_TEAMS = "all";
	public static final int HEADER_ROW = 0;
	public static final Map<String, String> AVERAGE_PROSPECT_GRADE_INFO_COLUMN_MAPPING = initAvgProspecGradeMap();
	static Map<String, String> initAvgProspecGradeMap() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("TEAM_NAME", "teamName");
		map.put("NO_OF_PLAYERS_DRAFTED", "noOfPlayersDrafted");
		map.put("AVERAGE_GRADE", "averageGrade");
		map.put("PLAYERS_DRAFTED", "playersDrafted");
		return Collections.unmodifiableMap(map);
	}
	public static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=Average_Draft_Grade_Results_%s.xlsx";
	public static final String EXCEL_MEDIA_TYPE = "application/vnd.ms-excel";
}
