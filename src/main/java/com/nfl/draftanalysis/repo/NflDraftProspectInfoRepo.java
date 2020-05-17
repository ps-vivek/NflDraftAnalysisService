package com.nfl.draftanalysis.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nfl.draftanalysis.dao.NflDraftProspectInfo;

@Repository
public interface NflDraftProspectInfoRepo extends JpaRepository<NflDraftProspectInfo, Long> {

	Long countByYear(int year);

	List<NflDraftProspectInfo> findByYear(int year);

}
