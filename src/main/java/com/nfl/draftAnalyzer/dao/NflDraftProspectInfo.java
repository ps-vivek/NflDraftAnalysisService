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
	private String Player;

	@Column(name = "COLLEGE")
	private String College;

	@Column(name = "STATUS")
	private String Status;

	@Column(name = "POSITION")
	private String Position;

	@Column(name = "TEAM")
	private String Team;

	@Column(name = "CLASS")
	private String collegeClass;

	@Column(name = "GRADE")
	private Double Grade;

	@Column(name = "CONFERENCE")
	private String Conference;

	@Column(name = "YEAR")
	private int year;
}
