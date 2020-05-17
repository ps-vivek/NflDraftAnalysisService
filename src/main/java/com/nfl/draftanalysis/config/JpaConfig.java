package com.nfl.draftanalysis.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JpaConfig {

	@Value("${spring.datasource.url}")
	private String dataSourceUrl;

	@Value("${spring.datasource.username}")
	private String dbUser;

	@Value("${spring.datasource.password}")
	private String dbPassword;

	@Bean(name = "mySqlDataSource")

	@Primary
	public DataSource mySqlDataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.url(dataSourceUrl);
		dataSourceBuilder.username(dbUser);
		dataSourceBuilder.password(dbPassword);
		return dataSourceBuilder.build();
	}

}
