package com.nfl.draftanalysis.dao;

import java.io.Serializable;

import javax.persistence.Column;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompositePrimaryKeyForOverallTeamStandingsByYear implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(name = "TEAM")
	private String team;

	@Column(name = "YEAR")
	private int year;

	public CompositePrimaryKeyForOverallTeamStandingsByYear() {
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((team == null) ? 0 : team.hashCode());
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompositePrimaryKeyForOverallTeamStandingsByYear other = (CompositePrimaryKeyForOverallTeamStandingsByYear) obj;
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		if (year != other.year)
			return false;
		return true;
	}
}
