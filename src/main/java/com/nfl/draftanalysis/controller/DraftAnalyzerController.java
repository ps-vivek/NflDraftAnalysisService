package com.nfl.draftanalysis.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nfl.draftanalysis.constants.DraftAnalyzerConstants;
import com.nfl.draftanalysis.dto.AverageProspectGradeInfo;
import com.nfl.draftanalysis.dto.PaginatedProspectDataDto;
import com.nfl.draftanalysis.service.DraftAnalyzerService;

import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/draft")
@Controller
@Log4j2
@CrossOrigin(origins = "http://localhost:4200")
public class DraftAnalyzerController {
	private DraftAnalyzerService draftAnalyzerService;

	@Autowired
	private DraftAnalyzerController(DraftAnalyzerService dService) {
		this.draftAnalyzerService = dService;
	}

	@GetMapping
	@RequestMapping(value = "/teamgrades")
	public ResponseEntity<List<AverageProspectGradeInfo>> findAverageDraftGradesForAllRounds(
			@RequestParam(required = true) int year,
			@RequestParam(defaultValue = DraftAnalyzerConstants.ALL_TEAMS) String team,
			@RequestParam(required = true, name = "stealgrade") boolean includeStealGrade) {
		log.info("Entered DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		List<AverageProspectGradeInfo> resource = null;
		if (!includeStealGrade)
			resource = draftAnalyzerService.findAverageDraftGradesForAllRounds(year, team);
		else
			resource = draftAnalyzerService.findAverageDraftGradesForAllRoundsWithStealValue(year, team);
		log.info("Exited DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		return ResponseEntity.ok().body(resource);

	}

	@GetMapping
	@RequestMapping(value = "/prospectgrades")
	public ResponseEntity<PaginatedProspectDataDto> fetchProspectGradesData(@RequestParam(required = true) int year,
			@RequestParam(required = true, name = "pagenum") int pageNumber, @RequestParam(required = true) int size,
			@RequestParam(required = true) String sortField) {
		log.info("Entered DraftAnalyzerController::fetchProspectGradesData()");

		PaginatedProspectDataDto resource = draftAnalyzerService.fetchProspectGradesData(year, pageNumber, size,
				sortField);
		log.info("Exited DraftAnalyzerController::fetchProspectGradesData()");
		return ResponseEntity.ok().body(resource);

	}

}
