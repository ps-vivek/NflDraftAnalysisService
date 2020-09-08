package com.nfl.draftanalysis.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nfl.draftanalysis.dao.NflDraftProspectInfo;

@Repository
public interface NflDraftProspectInfoRepo extends JpaRepository<NflDraftProspectInfo, Long> {

	Long countByYear(int year);

	List<NflDraftProspectInfo> findByYear(int year);

	@Query("select t from NflDraftProspectInfo t where t.year=:year and t.team in (:team) and t.status is not null order by t.team")
	List<NflDraftProspectInfo> findDraftedPlayersByYear(@Param("year") int year, @Param("team") List<String> team);

	Page<NflDraftProspectInfo> findByYear(int year, Pageable pageable);

}
