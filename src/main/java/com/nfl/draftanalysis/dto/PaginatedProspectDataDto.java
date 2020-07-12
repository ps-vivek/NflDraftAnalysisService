package com.nfl.draftanalysis.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PaginatedProspectDataDto {

	private List<NflDraftProspectInfoDto> contents;

	private long totalElements;

	private int noOfElements;

	private int pageNumber;

	private int pageSize;

}
