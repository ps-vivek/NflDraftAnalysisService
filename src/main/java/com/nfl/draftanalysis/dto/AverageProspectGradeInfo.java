package com.nfl.draftanalysis.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor
public class AverageProspectGradeInfo {

	private int noOfPlayersDrafted;

	private String teamName;

	private Double averageGrade;

	private Double averageGradeWithStealIndex;

	private Double cumulativeStealIndex;

	private String playersDrafted;

	private int seed;

	private int wins;

	private int loss;

	private int ties;

	private String position;

	private String reason;

	private String conference;

	private List<GradeBreakDown> gradeBreakDown;

}
