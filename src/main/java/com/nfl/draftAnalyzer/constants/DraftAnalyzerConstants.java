package com.nfl.draftAnalyzer.constants;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DraftAnalyzerConstants {
	public static final List<String> DRAFT_PROSPECT_FILE_HEADERS = Stream
			.of("Player", "College", "Status", "Position", "Team", "Class", "Grade", "Conference")
			.collect(Collectors.toList());
	public static final Double DEFAULT_PROSPECT_GRADE = 0d;
	public static final String PIPE_DELIMITER = "||";
	public static final char COMMA_DELIMITER = ',';
	public static final int ROUNDING_PRECISION = 2;
	public static final String ALL_TEAMS = "all";

}
