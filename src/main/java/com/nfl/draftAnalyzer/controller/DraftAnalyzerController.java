package com.nfl.draftAnalyzer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nfl.draftAnalyzer.constants.DraftAnalyzerConstants;
import com.nfl.draftAnalyzer.service.DraftAnalyzerService;

import lombok.extern.log4j.Log4j2;

@RequestMapping(value = "/draft/teamgrades")
@Controller
@Log4j2
public class DraftAnalyzerController implements DraftAnalyzerConstants {
	private DraftAnalyzerService service;

	@Autowired
	private DraftAnalyzerController(DraftAnalyzerService dService) {
		this.service = dService;
	}

	@GetMapping
	public ResponseEntity<ByteArrayResource> findAverageDraftGradesForAllRounds(@RequestParam(required = true) int year,
			@RequestParam(defaultValue = ALL_TEAMS) String team) {
		log.info("Entered DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		ByteArrayResource resource = null;
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, year));

		resource = service.findAverageDraftGradesForAllRounds(year, team);

		log.info("Exited DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		return ResponseEntity.ok().headers(headers).contentLength(resource.contentLength())
				.contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE)).body(resource);

	}

}
