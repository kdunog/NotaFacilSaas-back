package com.saas.professor.dto.response;

import java.time.LocalDate;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEvolutionResponse {
	
	private Long examId;
	private String examTitle;
	private LocalDate examDate;
	private Double score;
	private Double totalPoints;
	private Double weight;
	private Double percentage;

	public StudentEvolutionResponse(Long examId, String examTitle, LocalDate examDate,
	                                 Double score, Double totalPoints, Double weight) {
	    this.examId = examId;
	    this.examTitle = examTitle;
	    this.examDate = examDate;
	    this.score = score;
	    this.totalPoints = totalPoints;
	    this.weight = weight;
	    this.percentage = totalPoints > 0
	        ? Math.round((score / totalPoints) * 10000.0) / 100.0
	        : 0.0;
	}
	
	

}
