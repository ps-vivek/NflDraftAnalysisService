package com.nfl.draftanalysis.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import com.nfl.draftanalysis.dao.OverallTeamStandingsByYear;
import com.nfl.draftanalysis.dto.AverageProspectGradeInfo;
import com.nfl.draftanalysis.dto.GradeBreakDown;
import com.nfl.draftanalysis.dto.NflDraftProspectInfoDto;
import com.nfl.draftanalysis.dto.PaginatedProspectDataDto;
import com.nfl.draftanalysis.dto.enumMappings.AverageProspectGradeMapping;
import com.nfl.draftanalysis.dto.enumMappings.DraftRounds;
import com.nfl.draftanalysis.dto.enumMappings.NflProspectTiers;
import com.nfl.draftanalysis.dto.enumMappings.ProspectInfoMapping;
import com.nfl.draftanalysis.exception.DraftDataNotFoundException;
import com.nfl.draftanalysis.exception.ExcelReadException;
import com.nfl.draftanalysis.exception.InvalidNflTeamException;
import com.nfl.draftanalysis.repo.NflDraftProspectInfoRepo;
import com.nfl.draftanalysis.repo.OverallTeamStandingsByYearRepo;
import com.nfl.draftanalysis.util.FileUtils;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class DraftAnalyzerService {

	@Autowired
	private DraftAnalyzerConfig draftAnalyzerConfig;

	@Autowired
	private NflDraftProspectInfoRepo nflDraftProspectInfoRepo;

	@Autowired
	private OverallTeamStandingsByYearRepo overallTeamStandingByYearRepo;

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
		try {
			insertProspectsDataToDb();
			insertOverallTeamStandingsPerYearIntoDb();
		} catch (IOException e) {
			log.error("Error :" + e.getLocalizedMessage());
		}

	}

	private void insertProspectsDataToDb() throws IOException {
		draftAnalyzerConfig.getDraftFilesByYear().forEach((year, fileName) -> {
			try {
				boolean recordsPresent = nflDraftProspectInfoRepo.countByYear(year) > 0;
				if (!recordsPresent) {
					List<NflDraftProspectInfo> nflDraftProspectInfos = new ArrayList<>();
					FileUtils.fetchExcelData(fileName, "Sheet1").forEach(prospectInfo -> {
						if (draftAnalyzerConfig.getTeams()
								.contains(prospectInfo.get(ProspectInfoMapping.TEAM.getValue()))) {
							nflDraftProspectInfos
									.add(NflDraftProspectInfo.prepareNflProspectsDataForInsertion(year, prospectInfo));
						}
					});
					nflDraftProspectInfoRepo.saveAll(nflDraftProspectInfos);
				}
			} catch (IOException e) {
				throw new ExcelReadException(DraftAnalyzerConstants.EXCEL_READ_EXCEPTION_MSG + e.getLocalizedMessage());
			}
		});
	}

	private void insertOverallTeamStandingsPerYearIntoDb() throws IOException {
		List<String> sheetNames = Stream.of("2014", "2015", "2016", "2017", "2018", "2019")
				.collect(Collectors.toList());
		sheetNames.forEach(sheetName -> {
			List<OverallTeamStandingsByYear> overallTeamStandings = new ArrayList<>();
			try {
				FileUtils.fetchExcelData("OverallTeamStandingsByYear.xlsx", sheetName).forEach(prospectInfo -> {

					overallTeamStandings.add(
							OverallTeamStandingsByYear.prepareOverallStandingsByYearDataForInsertion(prospectInfo));
				});
			} catch (Exception e) {
				log.error("Error while inserting overall team standings per year into db:" + e.getLocalizedMessage());
			}
			log.debug("Size:" + overallTeamStandings.size());
			overallTeamStandingByYearRepo.saveAll(overallTeamStandings);
		});

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
	public List<AverageProspectGradeInfo> findAverageDraftGradesForAllRoundsWithStealValue(int year, String team,
			boolean stealGrade) {
		log.info("Entered DraftAnalyzerService::findAverageDraftGradesForAllRoundsWithStealValue()");
		validateInputYearAndTeam(year, team);

		List<String> teamsToQuery = DraftAnalyzerConstants.ALL.equalsIgnoreCase(team) ? draftAnalyzerConfig.getTeams()
				: Arrays.asList(team);

		List<AverageProspectGradeInfo> draftDataByYearWithStealGrade = fetchDraftData(
				nflDraftProspectInfoRepo.findDraftedPlayersByYear(year, teamsToQuery), stealGrade);

		log.info("Exited DraftAnalyzerService::findAverageDraftGradesForAllRoundsWithStealValue()");
		return draftDataByYearWithStealGrade;

	}

	private void validateInputYearAndTeam(int year, String team) {
		Set<Integer> validYears = draftAnalyzerConfig.getDraftFilesByYear().keySet();
		if (!validYears.contains(year)) {
			throw new DraftDataNotFoundException(DraftAnalyzerConstants.DRAFT_DATA_NOT_FOUND_EXCEPTION + year);
		}

		if (!DraftAnalyzerConstants.ALL.equalsIgnoreCase(team) && !draftAnalyzerConfig.getTeams().contains(team)) {
			throw new InvalidNflTeamException(DraftAnalyzerConstants.INVALID_NFL_TEAM_EXCEPTION_MSG + team);
		}
	}

	private List<AverageProspectGradeInfo> fetchDraftData(List<NflDraftProspectInfo> prospectInfo, boolean stealGrade) {
		MultiValueMap<String, String> playersDraftedByTeamWithStealIndex = new LinkedMultiValueMap<>();
		MultiValueMap<String, GradeBreakDown> totalProspectGradesByTeamWithStealValue = fetchTotalProspectGradeByTeam(
				prospectInfo, playersDraftedByTeamWithStealIndex, stealGrade);
		return fetchAvgProspectGradeInfoByAllTeams(playersDraftedByTeamWithStealIndex,
				totalProspectGradesByTeamWithStealValue);
	}

	private List<AverageProspectGradeInfo> fetchAvgProspectGradeInfoByAllTeams(
			MultiValueMap<String, String> playersDraftedByTeam,
			MultiValueMap<String, GradeBreakDown> totalProspectGradesByTeam) {
		List<AverageProspectGradeInfo> avgProspectGradeInfoByAllTeams = new ArrayList<>();

		for (Entry<String, List<GradeBreakDown>> prospectGradeInfoPerTeam : totalProspectGradesByTeam.entrySet()) {
			int noOfPlayersDrafted = prospectGradeInfoPerTeam.getValue().size();

			Double cumulativeStealIndex = 0d;
			Double totalProspectGradesForTeamWithoutStealIndex = 0d;
			Double totalProspectGradesForTeamWithStealIndex = 0d;
			for (GradeBreakDown gradeBreakDown : prospectGradeInfoPerTeam.getValue()) {
				cumulativeStealIndex += gradeBreakDown.getStealIndex();
				totalProspectGradesForTeamWithoutStealIndex += gradeBreakDown.getProspectGradeWithoutStealIndex();
				totalProspectGradesForTeamWithStealIndex += gradeBreakDown.getProspectGradeWithStealIndex();
			}

			Double avgProspectGradeForTeamWithoutStealIndex = totalProspectGradesForTeamWithoutStealIndex
					/ Double.valueOf(String.valueOf(noOfPlayersDrafted));

			Double avgProspectGradeForTeamWithStealIndex = totalProspectGradesForTeamWithStealIndex
					/ Double.valueOf(String.valueOf(noOfPlayersDrafted));

			avgProspectGradeInfoByAllTeams.add(buildAverageProspectGradeInfo(playersDraftedByTeam,
					prospectGradeInfoPerTeam, noOfPlayersDrafted, cumulativeStealIndex,
					avgProspectGradeForTeamWithoutStealIndex, avgProspectGradeForTeamWithStealIndex));
		}
		avgProspectGradeInfoByAllTeams.sort(Comparator.comparingDouble(AverageProspectGradeInfo::getAverageGrade));
		return avgProspectGradeInfoByAllTeams;
	}

	private AverageProspectGradeInfo buildAverageProspectGradeInfo(MultiValueMap<String, String> playersDraftedByTeam,
			Entry<String, List<GradeBreakDown>> prospectGradeInfoPerTeam, int noOfPlayersDrafted,
			Double cumulativeStealIndex, Double avgProspectGradeForTeamWithoutStealIndex,
			Double avgProspectGradeForTeamWithStealIndex) {
		return AverageProspectGradeInfo.builder()
				.averageGrade(Precision.round(avgProspectGradeForTeamWithoutStealIndex,
						DraftAnalyzerConstants.ROUNDING_PRECISION))
				.averageGradeWithStealIndex(Precision.round(avgProspectGradeForTeamWithStealIndex,
						DraftAnalyzerConstants.ROUNDING_PRECISION))
				.noOfPlayersDrafted(noOfPlayersDrafted).teamName(prospectGradeInfoPerTeam.getKey())
				.playersDrafted(playersDraftedByTeam.get(prospectGradeInfoPerTeam.getKey()).stream()
						.collect(Collectors.joining(DraftAnalyzerConstants.COMMA_DELIMITER)))
				.gradeBreakDown(prospectGradeInfoPerTeam.getValue()).cumulativeStealIndex(cumulativeStealIndex).build();
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
	private MultiValueMap<String, GradeBreakDown> fetchTotalProspectGradeByTeam(
			List<NflDraftProspectInfo> nflDraftProspectInfos, MultiValueMap<String, String> playersDraftedByTeam,
			boolean includeStealGrade) {
		MultiValueMap<String, GradeBreakDown> totalProspectGradesByTeam = new LinkedMultiValueMap<>();
		nflDraftProspectInfos.forEach(prospectInfo -> {
			playersDraftedByTeam.add(prospectInfo.getTeam(), prospectInfo.getPlayer());
			Double prospectGradeWithoutStealIndex = prospectInfo.getGrade() != null ? prospectInfo.getGrade()
					: DraftAnalyzerConstants.DEFAULT_PROSPECT_GRADE;
			Double playerStealGrade = includeStealGrade ? fetchStealGradeForPlayer(prospectInfo) : 0d;
			Double prospectGradeWithStealIndex = includeStealGrade ? (prospectGradeWithoutStealIndex + playerStealGrade)
					: prospectGradeWithoutStealIndex;

			totalProspectGradesByTeam.add(prospectInfo.getTeam(), GradeBreakDown.builder()
					.prospectGradeWithoutStealIndex(prospectGradeWithoutStealIndex).stealIndex(playerStealGrade)
					.prospectGradeWithStealIndex(prospectGradeWithStealIndex).playerName(prospectInfo.getPlayer())
					.prospectGradeWithoutStealIndex(prospectGradeWithoutStealIndex)
					.draftedRound(prospectInfo.getDraftedRound()).playerPosition(prospectInfo.getPosition()).build());
		});

		return totalProspectGradesByTeam;
	}

	public Double fetchStealGradeForPlayer(NflDraftProspectInfo playerDraftedByYear) {
		log.debug("Entered DraftAnalyzerService::fetchStealGradeForPlayer()");
		Double stealValueByDraftedRound = 0d;
		Double playerGrade = playerDraftedByYear.getGrade();
		String draftedRound = playerDraftedByYear.getStatus().substring(4, 5);
		log.debug("Drafted round" + draftedRound + "Player name:" + playerDraftedByYear.getPlayer() + "Round:"
				+ playerDraftedByYear.getStatus());
		String projectedProspectTier = fetchProjectedProspectTierForPlayer(playerGrade);

		if (StringUtils.isNotBlank(projectedProspectTier)) {
			EnumMap<DraftRounds, Double> stealGradeInfoEnumMap = DraftAnalyzerConstants.STEAL_GRADE_INFO
					.get(projectedProspectTier);
			stealValueByDraftedRound = stealGradeInfoEnumMap
					.get(DraftRounds.getDraftRoundEnum(Integer.valueOf(draftedRound)));
			String actualProspectTier = DraftAnalyzerConstants.PROSPECT_TIER_AND_DRAFT_RND_MAPPING
					.get(Integer.valueOf(draftedRound));
			log.debug(String.format("Projected Prospect Tier:%s, Drafted Round:%s,Prospect Tier:%s, Steal Value:%s",
					projectedProspectTier, draftedRound, actualProspectTier, stealValueByDraftedRound));
		}

		log.info("Entered DraftAnalyzerService::fetchStealGradeForPlayer()");
		return stealValueByDraftedRound;

	}

	private String fetchProjectedProspectTierForPlayer(Double playerGrade) {
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
		}
		return projectedProspectTier;
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

	public List<AverageProspectGradeInfo> fetchTeamGradesWithOverallStandings(Integer year, String team,
			boolean stealGrade, List<String> draftedRounds, String positionGroupings) {
		MultiValueMap<String, String> playersDraftedByTeamWithStealIndex = new LinkedMultiValueMap<>();
		List<String> teamsToQuery = DraftAnalyzerConstants.ALL.equalsIgnoreCase(team) ? draftAnalyzerConfig.getTeams()
				: Arrays.asList(team);

		List<String> draftRoundsToquery = Objects.nonNull(draftedRounds) && !CollectionUtils.isEmpty(draftedRounds)
				? draftedRounds
				: draftAnalyzerConfig.getDraftRounds();

		List<String> positionsToQuery = Objects.isNull(positionGroupings)
				? draftAnalyzerConfig.getPositionGroupings().get(DraftAnalyzerConstants.ALL)
				: draftAnalyzerConfig.getPositionGroupings().get(positionGroupings);

		log.info("Teams:{},Year:{},DraftRounds:{},Positions:{}", teamsToQuery, year, draftRoundsToquery,
				positionsToQuery);
		List<NflDraftProspectInfo> draftDataByYearWithStealGrade = nflDraftProspectInfoRepo
				.findDraftedPlayersByYearAndTeamAndRound(year, teamsToQuery, draftRoundsToquery, positionsToQuery);

		MultiValueMap<String, GradeBreakDown> totalProspectGradesByTeamWithStealValue = fetchTotalProspectGradeByTeam(
				draftDataByYearWithStealGrade, playersDraftedByTeamWithStealIndex, stealGrade);
		List<AverageProspectGradeInfo> fetchAvgProspectGradeInfoByAllTeams = fetchAvgProspectGradeInfoByAllTeams(
				playersDraftedByTeamWithStealIndex, totalProspectGradesByTeamWithStealValue);
		fetchAvgProspectGradeInfoByAllTeams.forEach(averageProspectInfo -> {
			OverallTeamStandingsByYear overallTeamStandingsByYear = overallTeamStandingByYearRepo
					.findByTeamAndYear(averageProspectInfo.getTeamName(), year);
			averageProspectInfo.setConference(overallTeamStandingsByYear.getConference());
			averageProspectInfo.setSeed(overallTeamStandingsByYear.getSeed());
			averageProspectInfo.setWins(overallTeamStandingsByYear.getWins());
			averageProspectInfo.setLoss(overallTeamStandingsByYear.getLoss());
			averageProspectInfo.setTies(overallTeamStandingsByYear.getTies());
			averageProspectInfo.setReason(overallTeamStandingsByYear.getReason());
			averageProspectInfo.setPosition(overallTeamStandingsByYear.getPosition());
		});
		return fetchAvgProspectGradeInfoByAllTeams;

	}
}
