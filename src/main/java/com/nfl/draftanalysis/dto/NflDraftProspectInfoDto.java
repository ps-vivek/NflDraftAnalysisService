package com.nfl.draftanalysis.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NflDraftProspectInfoDto {

	private String player;

	private String college;

	private String status;

	private String position;

	private String team;

	private String collegeClass;

	private Double grade;

	private String conference;

	private int year;
}
