package com.nfl.draftanalysis.constants;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.nfl.draftanalysis.dto.enumMappings.DraftRounds;
import com.nfl.draftanalysis.dto.enumMappings.NflProspectTiers;
import com.nfl.draftanalysis.dto.enumMappings.StealGrades;

public class DraftAnalyzerConstants {

	private DraftAnalyzerConstants() {

	}

	public static final Double DEFAULT_PROSPECT_GRADE = 0d;
	public static final String COMMA_DELIMITER = ",";
	public static final int ROUNDING_PRECISION = 2;
	public static final String ALL = "all";
	public static final int HEADER_ROW = 0;

	public static final String CONTENT_DISPOSITION_VALUE = "attachment; filename=Average_Draft_Grade_Results_%s.xlsx";
	public static final String CONTENT_DISPOSITION_VALUE_FOR_STEAL_GRADE = "attachment; filename=Average_Draft_Grade_Results_%s_With_Steal_Grades.xlsx";
	public static final String EXCEL_MEDIA_TYPE = "application/vnd.ms-excel";

	public static final String INVALID_NFL_TEAM_EXCEPTION_MSG = "The given nfl team in request doesn't exist. Team:";
	public static final String EXCEL_WRITE_EXCEPTION_MSG = "Encountered issue while generating response file.";
	public static final String EXCEL_READ_EXCEPTION_MSG = "Error while reading input prospect file data.";
	public static final String DRAFT_DATA_NOT_FOUND_EXCEPTION = "Draft data unavailable for the year:";
	public static final String EXCEL_WORKBOOK_CLOSURE_EXCEPTION = "Error while closing workbook";

	public static Map<String, EnumMap<DraftRounds, Double>> STEAL_GRADE_INFO = new HashMap<String, EnumMap<DraftRounds, Double>>() {
		private static final long serialVersionUID = 1L;

		{
			put(NflProspectTiers.TIER_ONE_PROSPECTS.name(), new EnumMap<DraftRounds, Double>(DraftRounds.class) {
				private static final long serialVersionUID = 1L;
				{
					put(DraftRounds.ROUND_ONE, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_TWO, StealGrades.PLUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_THREE, StealGrades.PLUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_FOUR, StealGrades.PLUS_ONE_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_FIVE, StealGrades.PLUS_TWO.getStealMultiplier());
					put(DraftRounds.ROUND_SIX, StealGrades.PLUS_TWO_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_SEVEN, StealGrades.PLUS_THREE.getStealMultiplier());
				}
			});

			put(NflProspectTiers.TIER_TWO_PROSPECTS.name(), new EnumMap<DraftRounds, Double>(DraftRounds.class) {
				private static final long serialVersionUID = 1L;
				{
					put(DraftRounds.ROUND_ONE, StealGrades.MINUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_TWO, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_THREE, StealGrades.PLUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_FOUR, StealGrades.PLUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_FIVE, StealGrades.PLUS_ONE_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_SIX, StealGrades.PLUS_TWO.getStealMultiplier());
					put(DraftRounds.ROUND_SEVEN, StealGrades.PLUS_TWO_POINT_FIVE.getStealMultiplier());
				}
			});

			put(NflProspectTiers.TIER_THREE_PROSPECTS.name(), new EnumMap<DraftRounds, Double>(DraftRounds.class) {
				private static final long serialVersionUID = 1L;
				{
					put(DraftRounds.ROUND_ONE, StealGrades.MINUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_TWO, StealGrades.MINUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_THREE, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_FOUR, StealGrades.PLUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_FIVE, StealGrades.PLUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_SIX, StealGrades.PLUS_ONE_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_SEVEN, StealGrades.PLUS_TWO.getStealMultiplier());

				}
			});

			put(NflProspectTiers.TIER_FOUR_PROSPECTS.name(), new EnumMap<DraftRounds, Double>(DraftRounds.class) {
				private static final long serialVersionUID = 1L;
				{
					put(DraftRounds.ROUND_ONE, StealGrades.MINUS_ONE_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_TWO, StealGrades.MINUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_THREE, StealGrades.MINUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_FOUR, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_FIVE, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_SIX, StealGrades.PLUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_SEVEN, StealGrades.PLUS_ONE.getStealMultiplier());

				}
			});

			put(NflProspectTiers.TIER_FIVE_PROSPECTS.name(), new EnumMap<DraftRounds, Double>(DraftRounds.class) {
				private static final long serialVersionUID = 1L;
				{
					put(DraftRounds.ROUND_ONE, StealGrades.MINUS_TWO_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_TWO, StealGrades.MINUS_TWO.getStealMultiplier());
					put(DraftRounds.ROUND_THREE, StealGrades.MINUS_ONE_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_FOUR, StealGrades.MINUS_ONE.getStealMultiplier());
					put(DraftRounds.ROUND_FIVE, StealGrades.MINUS_POINT_FIVE.getStealMultiplier());
					put(DraftRounds.ROUND_SIX, StealGrades.ZERO.getStealMultiplier());
					put(DraftRounds.ROUND_SEVEN, StealGrades.ZERO.getStealMultiplier());

				}
			});
		}
	};

	public static final Map<Integer, String> PROSPECT_TIER_AND_DRAFT_RND_MAPPING = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
			put(DraftRounds.ROUND_ONE.getRound(), NflProspectTiers.TIER_ONE_PROSPECTS.name());
			put(DraftRounds.ROUND_TWO.getRound(), NflProspectTiers.TIER_TWO_PROSPECTS.name());
			put(DraftRounds.ROUND_THREE.getRound(), NflProspectTiers.TIER_THREE_PROSPECTS.name());
			put(DraftRounds.ROUND_FOUR.getRound(), NflProspectTiers.TIER_FOUR_PROSPECTS.name());
			put(DraftRounds.ROUND_FIVE.getRound(), NflProspectTiers.TIER_FOUR_PROSPECTS.name());
			put(DraftRounds.ROUND_SIX.getRound(), NflProspectTiers.TIER_FIVE_PROSPECTS.name());
			put(DraftRounds.ROUND_SEVEN.getRound(), NflProspectTiers.TIER_FIVE_PROSPECTS.name());
		}
	};

}
