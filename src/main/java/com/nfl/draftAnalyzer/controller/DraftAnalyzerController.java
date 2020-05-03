package com.nfl.draftAnalyzer.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DraftAnalyzerController implements DraftAnalyzerConstants{
	private DraftAnalyzerService service;

	@Autowired
	private DraftAnalyzerController(DraftAnalyzerService dService) {
		this.service = dService;
	}

	@GetMapping(produces = "text/csv")
	public void findAverageDraftGradesForAllRounds(@RequestParam(required = true) int year, @RequestParam(defaultValue = ALL_TEAMS) String team,
			HttpServletResponse response) throws IOException {
		log.info("Entered DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		try {
		service.findAverageDraftGradesForAllRounds(year,team,response);
		}
		catch(Exception e) {
			log.error("Failure in finding average draft grades. Message:"+e.getLocalizedMessage());	
		}
		log.info("Exited DraftAnalyzerController::findAverageDraftGradesForAllRounds()");
		

	}

}
