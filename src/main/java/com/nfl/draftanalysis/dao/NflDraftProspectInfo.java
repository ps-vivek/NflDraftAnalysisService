package com.nfl.draftanalysis.dao;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.math.NumberUtils;

import com.nfl.draftanalysis.constants.DraftAnalyzerConstants;
import com.nfl.draftanalysis.dto.enumMappings.ProspectInfoMapping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "nfl_draft_prospect_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

	@Column(name = "DRAFTEDROUND")
	private String draftedRound;

	public static NflDraftProspectInfo prepareNflProspectsDataForInsertion(Integer year, List<String> prospectInfo) {
		return NflDraftProspectInfo.builder()
				.grade(NumberUtils.isParsable(prospectInfo.get(ProspectInfoMapping.GRADE.getValue()))
						? Double.valueOf(prospectInfo.get(ProspectInfoMapping.GRADE.getValue()))
						: DraftAnalyzerConstants.DEFAULT_PROSPECT_GRADE)
				.college(prospectInfo.get(ProspectInfoMapping.COLLEGE.getValue()))
				.collegeClass(prospectInfo.get(ProspectInfoMapping.CLASS.getValue()))
				.conference(prospectInfo.get(ProspectInfoMapping.CONFERENCE.getValue()))
				.player(prospectInfo.get(ProspectInfoMapping.PLAYER.getValue()))
				.position(prospectInfo.get(ProspectInfoMapping.POSITION.getValue()))
				.status(prospectInfo.get(ProspectInfoMapping.STATUS.getValue()))
				.team(prospectInfo.get(ProspectInfoMapping.TEAM.getValue())).year(year)
				.draftedRound(prospectInfo.get(ProspectInfoMapping.STATUS.getValue()).substring(0, 5)).build();
	}
}
