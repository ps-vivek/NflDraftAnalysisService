package com.nfl.draftanalysis.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Precision;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.nfl.draftanalysis.config.DraftAnalyzerConfig;
import com.nfl.draftanalysis.constants.DraftAnalyzerConstants;
import com.nfl.draftanalysis.dao.NflDraftProspectInfo;
import com.nfl.draftanalysis.dto.AverageProspectGradeInfo;
import com.nfl.draftanalysis.dto.AverageProspectGradeMapping;
import com.nfl.draftanalysis.dto.DraftRounds;
import com.nfl.draftanalysis.dto.NflDraftProspectInfoDto;
import com.nfl.draftanalysis.dto.NflProspectTiers;
import com.nfl.draftanalysis.dto.PaginatedProspectDataDto;
import com.nfl.draftanalysis.dto.ProspectInfoMapping;
import com.nfl.draftanalysis.exception.DraftDataNotFoundException;
import com.nfl.draftanalysis.exception.ExcelReadException;
import com.nfl.draftanalysis.exception.InvalidNflTeamException;
import com.nfl.draftanalysis.repo.NflDraftProspectInfoRepo;
import com.nfl.draftanalysis.util.FileUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DraftAnalyzerService {

	@Autowired
	private DraftAnalyzerConfig draftAnalyzerConfig;

	@Autowired
	private NflDraftProspectInfoRepo nflDraftProspectInfoRepo;

	private Map<Integer, List<AverageProspectGradeInfo>> draftDataByYear;

	private Map<String, String> averageProspectGradeInfoMapping;

	@Autowired
	private ModelMapper modelMapper;

	/**
	 * --Loads the column mapping required for generating the output excel file
	 * 
	 * @return
	 */
	private Map<String, String> initAverageProspectGradeMapping() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put(AverageProspectGradeMapping.TEAM_NAME.name(), AverageProspectGradeMapping.TEAM_NAME.getValue());
		map.put(AverageProspectGradeMapping.NO_OF_PLAYERS_DRAFTED.name(),
				AverageProspectGradeMapping.NO_OF_PLAYERS_DRAFTED.getValue());
		map.put(AverageProspectGradeMapping.AVERAGE_GRADE.name(), AverageProspectGradeMapping.AVERAGE_GRADE.getValue());
		map.put(AverageProspectGradeMapping.PLAYERS_DRAFTED.name(),
				AverageProspectGradeMapping.PLAYERS_DRAFTED.getValue());
		return Collections.unmodifiableMap(map);
	}

	/**
	 * --Read prospects data from config file and load into db if not present. Then,
	 * calculate average prospects grade per team each year
	 * 
	 */
	@PostConstruct
	private void initializeDraftData() {
		averageProspectGradeInfoMapping = initAverageProspectGradeMapping();
		List<String> validTeams = draftAnalyzerConfig.getTeams();
		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {
			try {
				insertProspectsDataToDb(year, validTeams, fileName);
			} catch (IOException e) {
				throw new ExcelReadException(DraftAnalyzerConstants.EXCEL_READ_EXCEPTION_MSG + e.getLocalizedMessage());
			}

		});

		draftDataByYear = new HashMap<>();

		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {
			initializeDraftDataByYear(year);
		});

	}

	private List<AverageProspectGradeInfo> fetchDraftDataByYearWithStealGrade(Integer year) {
		MultiValueMap<String, String> playersDraftedByTeamWithStealIndex = new LinkedMultiValueMap<>();
		MultiValueMap<String, Double> totalProspectGradesByTeamWithStealValue = fetchTotalProspectGradeByTeam(
				nflDraftProspectInfoRepo.findDraftedPlayersByYear(year), playersDraftedByTeamWithStealIndex, true);
		return fetchAvgProspectGradeInfoByAllTeams(playersDraftedByTeamWithStealIndex,
				totalProspectGradesByTeamWithStealValue);
	}

	private void initializeDraftDataByYear(Integer year) {
		MultiValueMap<String, String> playersDraftedByTeam = new LinkedMultiValueMap<>();
		MultiValueMap<String, Double> totalProspectGradesByTeam = fetchTotalProspectGradeByTeam(
				nflDraftProspectInfoRepo.findByYear(year), playersDraftedByTeam, false);
		draftDataByYear.put(year, fetchAvgProspectGradeInfoByAllTeams(playersDraftedByTeam, totalProspectGradesByTeam));
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
					prospectGradeInfoPerTeam.getKey(),
					Precision.round(avgProspectGradeForTeam, DraftAnalyzerConstants.ROUNDING_PRECISION),
					playersDraftedByTeam.get(prospectGradeInfoPerTeam.getKey()).stream()
							.collect(Collectors.joining(DraftAnalyzerConstants.COMMA_DELIMITER)));
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
	private MultiValueMap<String, Double> fetchTotalProspectGradeByTeam(
			List<NflDraftProspectInfo> nflDraftProspectInfos, MultiValueMap<String, String> playersDraftedByTeam,
			boolean includeStealGrade) {
		MultiValueMap<String, Double> totalProspectGradesByTeam = new LinkedMultiValueMap<>();

		for (NflDraftProspectInfo prospectInfo : nflDraftProspectInfos) {

			playersDraftedByTeam.add(prospectInfo.getTeam(), prospectInfo.getPlayer());
			Double prospectGrade = prospectInfo.getGrade();
			if (includeStealGrade) {
				Double playerStealGrade = fetchStealGradeForPlayer(prospectInfo);
				prospectGrade += playerStealGrade;
			}

			if (prospectGrade != null) {
				totalProspectGradesByTeam.add(prospectInfo.getTeam(), prospectGrade);
			} else {
				totalProspectGradesByTeam.add(prospectInfo.getTeam(), DraftAnalyzerConstants.DEFAULT_PROSPECT_GRADE);
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
	public List<AverageProspectGradeInfo> findAverageDraftGradesForAllRounds(int year, String team) {
		log.info("Entered DraftAnalyzerService::findAverageDraftGradesForAllRounds()");
		List<AverageProspectGradeInfo> avgProspectGradeInfo = null;
		if (!draftDataByYear.containsKey(year)) {
			throw new DraftDataNotFoundException(DraftAnalyzerConstants.DRAFT_DATA_NOT_FOUND_EXCEPTION + year);
		}

		if (!DraftAnalyzerConstants.ALL_TEAMS.equalsIgnoreCase(team)
				&& !draftAnalyzerConfig.getTeams().contains(team)) {
			throw new InvalidNflTeamException(DraftAnalyzerConstants.INVALID_NFL_TEAM_EXCEPTION_MSG + team);
		}

		ByteArrayResource resource = null;
		if (DraftAnalyzerConstants.ALL_TEAMS.equalsIgnoreCase(team)) {
			resource = FileUtils.writeToExcel(StringUtils.EMPTY + year, averageProspectGradeInfoMapping,
					draftDataByYear.get(year));
			avgProspectGradeInfo = draftDataByYear.get(year);
		} else {
			avgProspectGradeInfo = draftDataByYear.get(year).stream().filter(
					avgProspectGradeInfoByTeam -> avgProspectGradeInfoByTeam.getTeamName().equalsIgnoreCase(team))
					.collect(Collectors.toList());
		}

		log.info("Exited DraftAnalyzerService::findAverageDraftGradesForAllRounds()");
		return avgProspectGradeInfo;

	}

	/**
	 * --Load prospects data from excel onto db
	 * 
	 * @param year
	 * @param validTeams
	 * @param fileName
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NumberFormatException
	 */
	private void insertProspectsDataToDb(int year, List<String> validTeams, String fileName) throws IOException {

		boolean recordsPresent = nflDraftProspectInfoRepo.countByYear(year) > 0;
		if (!recordsPresent) {
			List<NflDraftProspectInfo> nflDraftProspectInfos = new ArrayList<>();
			for (List<String> prospectInfo : FileUtils.fetchExcelData(fileName)) {
				if (validTeams.contains(prospectInfo.get(ProspectInfoMapping.TEAM.getValue()))) {
					NflDraftProspectInfo nflDraftProspectInfo = new NflDraftProspectInfo();
					nflDraftProspectInfo
							.setGrade(NumberUtils.isParsable(prospectInfo.get(ProspectInfoMapping.GRADE.getValue()))
									? Double.valueOf(prospectInfo.get(ProspectInfoMapping.GRADE.getValue()))
									: DraftAnalyzerConstants.DEFAULT_PROSPECT_GRADE);
					nflDraftProspectInfo.setCollege(prospectInfo.get(ProspectInfoMapping.COLLEGE.getValue()));
					nflDraftProspectInfo.setCollegeClass(prospectInfo.get(ProspectInfoMapping.CLASS.getValue()));
					nflDraftProspectInfo.setConference(prospectInfo.get(ProspectInfoMapping.CONFERENCE.getValue()));
					nflDraftProspectInfo.setPlayer(prospectInfo.get(ProspectInfoMapping.PLAYER.getValue()));
					nflDraftProspectInfo.setPosition(prospectInfo.get(ProspectInfoMapping.POSITION.getValue()));
					nflDraftProspectInfo.setStatus(prospectInfo.get(ProspectInfoMapping.STATUS.getValue()));
					nflDraftProspectInfo.setTeam(prospectInfo.get(ProspectInfoMapping.TEAM.getValue()));
					nflDraftProspectInfo.setYear(year);
					nflDraftProspectInfos.add(nflDraftProspectInfo);
				}

			}
			nflDraftProspectInfoRepo.saveAll(nflDraftProspectInfos);
		}

	}

	/**
	 * Finds the average prospect grades for a team amongst the players drafted by
	 * them in a given year. Include steal grades too. Look at the design doc in git
	 * to understand how steal grade is calculated
	 * 
	 * @param year
	 * @param team
	 * @return
	 * @throws IOException
	 */
	@Cacheable("stealgrades")
	public List<AverageProspectGradeInfo> findAverageDraftGradesForAllRoundsWithStealValue(int year, String team) {
		log.info("Entered DraftAnalyzerService::findAverageDraftGradesForAllRoundsWithStealValue()");

		if (!draftDataByYear.containsKey(year)) {
			throw new DraftDataNotFoundException(DraftAnalyzerConstants.DRAFT_DATA_NOT_FOUND_EXCEPTION + year);
		}

		if (!DraftAnalyzerConstants.ALL_TEAMS.equalsIgnoreCase(team)
				&& !draftAnalyzerConfig.getTeams().contains(team)) {
			throw new InvalidNflTeamException(DraftAnalyzerConstants.INVALID_NFL_TEAM_EXCEPTION_MSG + team);
		}

		List<AverageProspectGradeInfo> draftDataByYearWithStealGrade = fetchDraftDataByYearWithStealGrade(year);
		if (DraftAnalyzerConstants.ALL_TEAMS.equalsIgnoreCase(team)) {
			draftDataByYearWithStealGrade = fetchDraftDataByYearWithStealGrade(year);
		} else {

			draftDataByYearWithStealGrade = draftDataByYearWithStealGrade.stream().filter(
					avgProspectGradeInfoByTeam -> avgProspectGradeInfoByTeam.getTeamName().equalsIgnoreCase(team))
					.collect(Collectors.toList());
		}

		log.info("Exited DraftAnalyzerService::findAverageDraftGradesForAllRoundsWithStealValue()");
		return draftDataByYearWithStealGrade;

	}

	public Double fetchStealGradeForPlayer(NflDraftProspectInfo playerDraftedByYear) {
		log.info("Entered DraftAnalyzerService::fetchStealGradeForPlayer()");
		Double stealValueByDraftedRound = 0d;
		Double playerGrade = playerDraftedByYear.getGrade();
		String draftedRound = playerDraftedByYear.getStatus().substring(4, 5);
		log.info("Drafted round" + draftedRound + "Player name:" + playerDraftedByYear.getPlayer() + "Round:"
				+ playerDraftedByYear.getStatus());
		String projectedProspectTier = StringUtils.EMPTY;
		if (playerGrade >= NflProspectTiers.TIER_ONE_PROSPECTS.getValue()) {
			projectedProspectTier = NflProspectTiers.TIER_ONE_PROSPECTS.name();
		} else if (playerGrade >= NflProspectTiers.TIER_TWO_PROSPECTS.getValue()) {
			projectedProspectTier = NflProspectTiers.TIER_TWO_PROSPECTS.name();
		} else if (playerGrade >= NflProspectTiers.TIER_THREE_PROSPECTS.getValue()) {
			projectedProspectTier = NflProspectTiers.TIER_THREE_PROSPECTS.name();
		} else if (playerGrade >= NflProspectTiers.TIER_FOUR_PROSPECTS.getValue()) {
			projectedProspectTier = NflProspectTiers.TIER_FOUR_PROSPECTS.name();
		} else if (playerGrade >= NflProspectTiers.TIER_FIVE_PROSPECTS.getValue()) {
			projectedProspectTier = NflProspectTiers.TIER_FIVE_PROSPECTS.name();
		} else {
			return stealValueByDraftedRound;
		}

		EnumMap<DraftRounds, Double> stealGradeInfoEnumMap = DraftAnalyzerConstants.STEAL_GRADE_INFO
				.get(projectedProspectTier);

		stealValueByDraftedRound = stealGradeInfoEnumMap
				.get(DraftRounds.getDraftRoundEnum(Integer.valueOf(draftedRound)));

		String actualProspectTier = DraftAnalyzerConstants.PROSPECT_TIER_AND_DRAFT_RND_MAPPING
				.get(Integer.valueOf(draftedRound));
		log.debug(String.format("Projected Prospect Tier:%s, Drafted Round:%s,Prospect Tier:%s, Steal Value:%s",
				projectedProspectTier, draftedRound, actualProspectTier, stealValueByDraftedRound));
		log.info("Entered DraftAnalyzerService::fetchStealGradeForPlayer()");
		return stealValueByDraftedRound;

	}

	public PaginatedProspectDataDto fetchProspectGradesData(int year, int pageNumber, int size, String sortField) {
		Pageable pageable = PageRequest.of(pageNumber, size, Sort.by(sortField).ascending());
		Page<NflDraftProspectInfo> paginatedProspectsGradeData = nflDraftProspectInfoRepo.findByYear(year, pageable);
		PaginatedProspectDataDto paginatedResults = setPaginatedResponse(pageNumber, size, paginatedProspectsGradeData);
		return paginatedResults;
	}

	private PaginatedProspectDataDto setPaginatedResponse(int pageNumber, int size,
			Page<NflDraftProspectInfo> paginatedProspectsGradeData) {
		PaginatedProspectDataDto paginatedResults = new PaginatedProspectDataDto();
		List<NflDraftProspectInfoDto> paginatedContents = paginatedProspectsGradeData.getContent().stream()
				.map(this::convertToDto).collect(Collectors.toList());
		paginatedResults.setContents(paginatedContents);
		paginatedResults.setNoOfElements(paginatedProspectsGradeData.getNumberOfElements());
		paginatedResults.setTotalElements(paginatedProspectsGradeData.getTotalElements());
		paginatedResults.setPageNumber(pageNumber);
		paginatedResults.setPageSize(size);
		return paginatedResults;
	}

	private NflDraftProspectInfoDto convertToDto(NflDraftProspectInfo nflDraftProspectInfo) {
		NflDraftProspectInfoDto postDto = modelMapper.map(nflDraftProspectInfo, NflDraftProspectInfoDto.class);
		return postDto;
	}
}
