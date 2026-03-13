package com.saas.professor.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentAnswerResponse {
	
	private Long studentId;
	private String studentName;
	private Long examId;
	private String examTitle;
	private Double totalPoints;
	private Double pointsEarned;
	private Double percentage;
	private Double finalScore;

	public StudentAnswerResponse(Long studentId, String studentName, Long examId,
	                              String examTitle, Double totalPoints, Double pointsEarned) {
	    this.studentId = studentId;
	    this.studentName = studentName;
	    this.examId = examId;
	    this.examTitle = examTitle;
	    this.totalPoints = totalPoints;
	    this.pointsEarned = pointsEarned;
	    this.percentage = totalPoints > 0
	        ? Math.round((pointsEarned / totalPoints) * 10000.0) / 100.0 : 0.0;
	    this.finalScore = totalPoints > 0
	        ? Math.round((pointsEarned / totalPoints) * 1000.0) / 100.0 : 0.0;
	}

}
