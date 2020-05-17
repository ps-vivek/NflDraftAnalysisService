package com.nfl.draftAnalyzer.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nfl.draftAnalyzer.config.DraftAnalyzerConfig;
import com.nfl.draftAnalyzer.constants.DraftAnalyzerConstants;
import com.nfl.draftAnalyzer.dao.NflDraftProspectInfo;
import com.nfl.draftAnalyzer.dto.AverageProspectGradeInfo;
import com.nfl.draftAnalyzer.dto.ProspectInfoColumns;
import com.nfl.draftAnalyzer.exception.DraftDataNotFoundException;
import com.nfl.draftAnalyzer.repo.NflDraftProspectInfoRepo;
import com.nfl.draftAnalyzer.util.FileUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DraftAnalyzerService implements DraftAnalyzerConstants {

	@Autowired
	private DraftAnalyzerConfig draftAnalyzerConfig;

	@Autowired
	private NflDraftProspectInfoRepo nflDraftProspectInfoRepo;

	private static Map<Integer, List<AverageProspectGradeInfo>> draftDataByYear;

	/**
	 * --Read prospects data from config file and load into db if not present. Then,
	 * calculate average prospects grade per team each year
	 * 
	 */
	@PostConstruct
	private void initializeDraftData() {
		List<String> validTeams = draftAnalyzerConfig.getTeams();
		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {
			insertProspectsDataToDb(year, validTeams, fileName);

		});

		draftDataByYear = new HashMap<>();
		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {

			MultiValueMap<String, String> playersDraftedByTeam = new LinkedMultiValueMap<>();
			MultiValueMap<String, Double> totalProspectGradesByTeam = fetchTotalProspectGradeByTeam(year,
					playersDraftedByTeam);

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
	private List<AverageProspectGradeInfo> fetchAvgProspectGradeInfoByAllTeams(
			MultiValueMap<String, String> playersDraftedByTeam,
			MultiValueMap<String, Double> totalProspectGradesByTeam) {
		List<AverageProspectGradeInfo> avgProspectGradeInfoByAllTeams = new ArrayList<>();
		for (Entry<String, List<Double>> prospectGradeInfoPerTeam : totalProspectGradesByTeam.entrySet()) {
			int noOfPlayersDrafted = prospectGradeInfoPerTeam.getValue().size();

			Double totalProspectGradesForTeam = prospectGradeInfoPerTeam.getValue().stream().reduce(0d, Double::sum);

			Double avgProspectGradeForTeam = totalProspectGradesForTeam
					/ Double.valueOf(String.valueOf(noOfPlayersDrafted));

			AverageProspectGradeInfo avgProspectGradeInfoByTeam = new AverageProspectGradeInfo(noOfPlayersDrafted,
					prospectGradeInfoPerTeam.getKey(), Precision.round(avgProspectGradeForTeam, ROUNDING_PRECISION),
					playersDraftedByTeam.get(prospectGradeInfoPerTeam.getKey()).stream()
							.collect(Collectors.joining(COMMA_DELIMITER)));
			avgProspectGradeInfoByAllTeams.add(avgProspectGradeInfoByTeam);
		}
		// Sorting the list based on average grade scored by the team
		avgProspectGradeInfoByAllTeams.sort(Comparator.comparingDouble(AverageProspectGradeInfo::getAverageGrade));
		return avgProspectGradeInfoByAllTeams;
	}

	/**
	 * -- Calculates the total prospect grades for a team
	 * 
	 * Total Prospect grade = Sum of individual prospect grades
	 * 
	 * @param fileName
	 * @param playersDraftedByTeam
	 * @return
	 */
	private MultiValueMap<String, Double> fetchTotalProspectGradeByTeam(int year,
			MultiValueMap<String, String> playersDraftedByTeam) {
		MultiValueMap<String, Double> totalProspectGradesByTeam = new LinkedMultiValueMap<>();

		for (NflDraftProspectInfo prospectInfo : nflDraftProspectInfoRepo.findByYear(year)) {

			playersDraftedByTeam.add(prospectInfo.getTeam(), prospectInfo.getPlayer());
			Double prospectGrade = prospectInfo.getGrade();

			if (prospectGrade != null) {
				totalProspectGradesByTeam.add(prospectInfo.getTeam(), Double.valueOf(prospectGrade));
			} else {
				totalProspectGradesByTeam.add(prospectInfo.getTeam(), DEFAULT_PROSPECT_GRADE);
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
	 * @return
	 * @throws IOException
	 */
	public ByteArrayResource findAverageDraftGradesForAllRounds(int year, String team) throws IOException {
		log.info("Entered DraftAnalyzerService::findAverageDraftGradesForAllRounds()");
		
		if(!draftDataByYear.containsKey(year)) {
			throw new DraftDataNotFoundException("Draft data unavailable for the year:"+year);
		}
		ByteArrayResource resource = null;
		if (ALL_TEAMS.equalsIgnoreCase(team)) {
			resource = FileUtils.writeToExcel(StringUtils.EMPTY + year, AVERAGE_PROSPECT_GRADE_INFO_COLUMN_MAPPING,
					draftDataByYear.get(year), AverageProspectGradeInfo.class);
		} else {
			resource = FileUtils.writeToExcel(StringUtils.EMPTY + year, AVERAGE_PROSPECT_GRADE_INFO_COLUMN_MAPPING,
					draftDataByYear.get(year).stream()
							.filter(avgProspectGradeInfoByTeam -> avgProspectGradeInfoByTeam.getTeamName()
									.equalsIgnoreCase(team))
							.collect(Collectors.toList()),
					AverageProspectGradeInfo.class);
		}
		
		
		
		log.info("Exited DraftAnalyzerService::findAverageDraftGradesForAllRounds()");
		return resource;

	}

	/**
	 * --Load prospects data from excel onto db
	 * 
	 * @param year
	 * @param validTeams
	 * @param fileName
	 */
	private void insertProspectsDataToDb(int year, List<String> validTeams, String fileName) {

		boolean recordsPresent = nflDraftProspectInfoRepo.countByYear(year) > 0 ? true : false;
		if (!recordsPresent) {
			List<NflDraftProspectInfo> nflDraftProspectInfos = new ArrayList<NflDraftProspectInfo>();
			for (List<String> prospectInfo : FileUtils.fetchExcelData(fileName)) {
				if (validTeams.contains(prospectInfo.get(ProspectInfoColumns.TEAM.getValue()))) {
					NflDraftProspectInfo nflDraftProspectInfo = new NflDraftProspectInfo();
					nflDraftProspectInfo
							.setGrade(NumberUtils.isParsable(prospectInfo.get(ProspectInfoColumns.GRADE.getValue()))
									? Double.valueOf(prospectInfo.get(ProspectInfoColumns.GRADE.getValue()))
									: DEFAULT_PROSPECT_GRADE);
					nflDraftProspectInfo.setCollege(prospectInfo.get(ProspectInfoColumns.COLLEGE.getValue()));
					nflDraftProspectInfo.setCollegeClass(prospectInfo.get(ProspectInfoColumns.CLASS.getValue()));
					nflDraftProspectInfo.setConference(prospectInfo.get(ProspectInfoColumns.CONFERENCE.getValue()));
					nflDraftProspectInfo.setPlayer(prospectInfo.get(ProspectInfoColumns.PLAYER.getValue()));
					nflDraftProspectInfo.setPosition(prospectInfo.get(ProspectInfoColumns.POSITION.getValue()));
					nflDraftProspectInfo.setStatus(prospectInfo.get(ProspectInfoColumns.STATUS.getValue()));
					nflDraftProspectInfo.setTeam(prospectInfo.get(ProspectInfoColumns.TEAM.getValue()));
					nflDraftProspectInfo.setYear(year);
					nflDraftProspectInfos.add(nflDraftProspectInfo);
				}

			}
			nflDraftProspectInfoRepo.saveAll(nflDraftProspectInfos);
		}

	}
}
