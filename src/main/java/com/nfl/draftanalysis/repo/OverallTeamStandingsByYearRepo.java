package com.nfl.draftanalysis.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nfl.draftanalysis.dao.OverallTeamStandingsByYear;

@Repository
public interface OverallTeamStandingsByYearRepo extends JpaRepository<OverallTeamStandingsByYear, Long> {
	OverallTeamStandingsByYear findByTeamAndYear(String team, int Year);
}
