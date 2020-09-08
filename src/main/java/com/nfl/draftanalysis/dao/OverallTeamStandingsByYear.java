package com.nfl.draftanalysis.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import com.nfl.draftanalysis.dto.OverallTeamStandingsMappings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@Table(name = "overall_team_standings_by_year")
@IdClass(CompositePrimaryKeyForOverallTeamStandingsByYear.class)
public class OverallTeamStandingsByYear implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private int year;

	@Id
	private String team;

	@Column(name = "SEED")
	private int seed;

	@Column(name = "WINS")
	private int wins;

	@Column(name = "LOSS")
	private int loss;

	@Column(name = "TIE")
	private int ties;

	@Column(name = "POSITION")
	private String position;

	@Column(name = "REASON")
	private String reason;

	@Column(name = "CONFERENCE")
	private String conference;

	public OverallTeamStandingsByYear() {
	}

	public OverallTeamStandingsByYear(int seed, String team, int wins, int loss, int ties, String position,
			String reason, String conference, int year) {
		super();
		this.seed = seed;
		this.team = team;
		this.wins = wins;
		this.loss = loss;
		this.ties = ties;
		this.position = position;
		this.reason = reason;
		this.conference = conference;
		this.year = year;
	}

	public static OverallTeamStandingsByYear prepareOverallStandingsByYearDataForInsertion(List<String> prospectInfo) {
		return OverallTeamStandingsByYear.builder()
				.seed(Integer.valueOf(prospectInfo.get(OverallTeamStandingsMappings.SEED.getValue())))
				.conference(prospectInfo.get(OverallTeamStandingsMappings.CONFERENCE.getValue()))
				.wins(Integer.valueOf(prospectInfo.get(OverallTeamStandingsMappings.WINS.getValue())))
				.loss(Integer.valueOf(prospectInfo.get(OverallTeamStandingsMappings.LOSS.getValue())))
				.ties(Integer.valueOf(prospectInfo.get(OverallTeamStandingsMappings.TIES.getValue())))
				.year(Integer.valueOf(prospectInfo.get(OverallTeamStandingsMappings.YEAR.getValue())))
				.team(prospectInfo.get(OverallTeamStandingsMappings.TEAM.getValue()))
				.position(prospectInfo.get(OverallTeamStandingsMappings.POSITION.getValue()))
				.reason(prospectInfo.get(OverallTeamStandingsMappings.REASON.getValue())).build();
	}
}
