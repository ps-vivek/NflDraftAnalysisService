package com.nfl.draftAnalyzer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Setter
@Getter
@EnableConfigurationProperties
@ConfigurationProperties("draft")
public class DraftAnalyzerConfig {


	private Map<Integer, String> draftFilesByYear = new HashMap<>();
	
	private List<String> teams = new ArrayList<>();

}
