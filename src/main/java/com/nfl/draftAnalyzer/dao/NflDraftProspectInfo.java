package com.nfl.draftAnalyzer.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "nfl_draft_prospect_info")
public class NflDraftProspectInfo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "PLAYER")
	private String player;

	@Column(name = "COLLEGE")
	private String college;

	@Column(name = "STATUS")
	private String status;

	@Column(name = "POSITION")
	private String position;

	@Column(name = "TEAM")
	private String team;

	@Column(name = "CLASS")
	private String collegeClass;

	@Column(name = "GRADE")
	private Double grade;

	@Column(name = "CONFERENCE")
	private String conference;

	@Column(name = "YEAR")
	private int year;
}
