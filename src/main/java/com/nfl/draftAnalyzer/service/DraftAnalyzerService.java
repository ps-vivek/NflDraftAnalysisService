package com.nfl.draftAnalyzer.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nfl.draftAnalyzer.config.DraftAnalyzerConfig;
import com.nfl.draftAnalyzer.constants.DraftAnalyzerConstants;
import com.nfl.draftAnalyzer.dto.ProspectInfo;
import com.nfl.draftAnalyzer.dto.ProspectInfoColumns;
import com.nfl.draftAnalyzer.util.FileUtils;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvException;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DraftAnalyzerService implements DraftAnalyzerConstants {

	@Autowired
	private DraftAnalyzerConfig draftAnalyzerConfig;

	private static Map<Integer, List<ProspectInfo>> draftDataByYear;

	@PostConstruct
	private void initializeDraftData() {

		draftDataByYear = new HashMap<>();
		List<String> validTeams = draftAnalyzerConfig.getTeams();
		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {

			MultiValueMap<String, String> playersDraftedByTeam = new LinkedMultiValueMap<>();
			MultiValueMap<String, Double> totalProspectGradesByTeam = fetchTotalProspectGradeByTeam(validTeams,
					fileName, playersDraftedByTeam);

			draftDataByYear.put(year,
					fetchAvgProspectGradeInfoByAllTeams(playersDraftedByTeam, totalProspectGradesByTeam));

		});

	}

	/**
	 * -- Calculates average prospect grade for a team
	 * 
	 * Average prospect grade per team = Total Prospect Grade/Number Of Players
	 * Drafted by Team. The result is rounded to two places
	 * 
	 * @param playersDraftedByTeam
	 * @param totalProspectGradesByTeam
	 * @return
	 */
	private List<ProspectInfo> fetchAvgProspectGradeInfoByAllTeams(MultiValueMap<String, String> playersDraftedByTeam,
			MultiValueMap<String, Double> totalProspectGradesByTeam) {
		List<ProspectInfo> avgProspectGradeInfoByAllTeams = new ArrayList<>();
		for (Entry<String, List<Double>> prospectGradeInfoPerTeam : totalProspectGradesByTeam.entrySet()) {
			int noOfPlayersDrafted = prospectGradeInfoPerTeam.getValue().size();

			Double totalProspectGradesForTeam = prospectGradeInfoPerTeam.getValue().stream().reduce(0d, Double::sum);

			Double avgProspectGradeForTeam = totalProspectGradesForTeam
					/ Double.valueOf(String.valueOf(noOfPlayersDrafted));

			ProspectInfo avgProspectGradeInfoByTeam = new ProspectInfo(noOfPlayersDrafted,
					prospectGradeInfoPerTeam.getKey(), Precision.round(avgProspectGradeForTeam, ROUNDING_PRECISION),
					playersDraftedByTeam.get(prospectGradeInfoPerTeam.getKey()).stream()
							.collect(Collectors.joining(PIPE_DELIMITER)));
			avgProspectGradeInfoByAllTeams.add(avgProspectGradeInfoByTeam);
		}
		// Sorting the list based on average grade scored by the team
		avgProspectGradeInfoByAllTeams.sort(Comparator.comparingDouble(ProspectInfo::getAverageGrade));
		return avgProspectGradeInfoByAllTeams;
	}

	/**
	 * -- Calculates the total prospect grades for a team
	 * 
	 * Total Prospect grade = Sum of individual prospect grades
	 * 
	 * @param validTeams
	 * @param fileName
	 * @param playersDraftedByTeam
	 * @return
	 */
	private MultiValueMap<String, Double> fetchTotalProspectGradeByTeam(List<String> validTeams, String fileName,
			MultiValueMap<String, String> playersDraftedByTeam) {
		MultiValueMap<String, Double> totalProspectGradesByTeam = new LinkedMultiValueMap<>();

		for (List<String> prospectInfo : FileUtils.fetchExcelData(fileName, DRAFT_PROSPECT_FILE_HEADERS)) {
			String team = prospectInfo.get(ProspectInfoColumns.TEAM.getValue());

			if (validTeams.contains(team)) {
				playersDraftedByTeam.add(team, prospectInfo.get(ProspectInfoColumns.PLAYER.getValue()));
				String prospectGrade = prospectInfo.get(ProspectInfoColumns.GRADE.getValue());

				if (NumberUtils.isParsable(prospectGrade)) {
					totalProspectGradesByTeam.add(team, Double.valueOf(prospectGrade));
				} else {
					totalProspectGradesByTeam.add(prospectInfo.get(ProspectInfoColumns.TEAM.getValue()),
							DEFAULT_PROSPECT_GRADE);
				}

			}

			
		}
		
		return totalProspectGradesByTeam;
	}

	/**
	 * Finds the average prospect grades for a team amongst the players drafted by
	 * them in a given year
	 * 
	 * @param year
	 * @param team
	 * @param response
	 * @throws IOException
	 */
	public void findAverageDraftGradesForAllRounds(int year, String team, HttpServletResponse response)
			throws IOException {
		log.info("Entered DraftAnalyzerService::findAverageDraftGradesForAllRounds()");
		if (ALL_TEAMS.equalsIgnoreCase(team)) {
			writeToCsv(response.getWriter(), draftDataByYear.get(year));
		} else {
			writeToCsv(response.getWriter(), draftDataByYear.get(year).stream().filter(
					avgProspectGradeInfoByTeam -> avgProspectGradeInfoByTeam.getTeamName().equalsIgnoreCase(team))
					.collect(Collectors.toList()));
		}
		log.info("Exited DraftAnalyzerService::findAverageDraftGradesForAllRounds()");

	}

	public static void writeToCsv(PrintWriter writer, List<ProspectInfo> prospectInfosWithAvgGrade) {
		try {
			StatefulBeanToCsv<ProspectInfo> csvBuilder = new StatefulBeanToCsvBuilder<ProspectInfo>(writer)
					.withQuotechar(CSVWriter.NO_QUOTE_CHARACTER).withSeparator(COMMA_DELIMITER).build();

			csvBuilder.write(prospectInfosWithAvgGrade);

		} catch (CsvException ex) {
			log.error("Error mapping Bean to CSV" + ex.getLocalizedMessage());
		}
	}
}
