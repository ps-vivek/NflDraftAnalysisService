package com.nfl.draftanalysis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nfl.draftanalysis.constants.DraftAnalyzerConstants;
import com.nfl.draftanalysis.service.DraftAnalyzerService;

import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/draft")
@Controller
@Log4j2
public class DraftAnalyzerController {
	private DraftAnalyzerService draftAnalyzerService;

	@Autowired
	private DraftAnalyzerController(DraftAnalyzerService dService) {
		this.draftAnalyzerService = dService;
	}

	@GetMapping
	@RequestMapping(value = "/teamgrades")
	public ResponseEntity<ByteArrayResource> findAverageDraftGradesForAllRounds(@RequestParam(required = true) int year,
			@RequestParam(defaultValue = DraftAnalyzerConstants.ALL_TEAMS) String team) {
		log.info("Entered DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		ByteArrayResource resource = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION,
				String.format(DraftAnalyzerConstants.CONTENT_DISPOSITION_VALUE, year));

		resource = draftAnalyzerService.findAverageDraftGradesForAllRounds(year, team);

		log.info("Exited DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType(DraftAnalyzerConstants.EXCEL_MEDIA_TYPE)).body(resource);

	}
	
	@GetMapping
	@RequestMapping(value = "/teamgradeswithsteal")
	public ResponseEntity<ByteArrayResource> findAverageDraftGradesForAllRoundsWithSteal(@RequestParam(required = true) int year,
			@RequestParam(defaultValue = DraftAnalyzerConstants.ALL_TEAMS) String team) {
		log.info("Entered DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		ByteArrayResource resource = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION,
				String.format(DraftAnalyzerConstants.CONTENT_DISPOSITION_VALUE, year));

		resource = draftAnalyzerService.findAverageDraftGradesForAllRoundsWithStealValue(year, team);

		log.info("Exited DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType(DraftAnalyzerConstants.EXCEL_MEDIA_TYPE)).body(resource);

	}

}
