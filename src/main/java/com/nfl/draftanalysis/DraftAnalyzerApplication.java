
package com.nfl.draftanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableAutoConfiguration
@EntityScan(basePackages = { "com.nfl.draftanalysis.*" })
public class DraftAnalyzerApplication {
	public static void main(String[] args) {
		SpringApplication.run(DraftAnalyzerApplication.class);
	}
}
