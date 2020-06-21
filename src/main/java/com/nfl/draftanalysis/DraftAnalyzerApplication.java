package com.nfl.draftanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DraftAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DraftAnalyzerApplication.class);
	}

}
