package com.saas.professor.dto.request;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterGradeRequest {

	@NotNull(message = "examId é obrigatório")
	private Long examId;

	@NotNull(message = "studentId é obrigatório")
	private Long studentId;

	@NotNull(message = "Nota é obrigatória")
	@Min(value = 0, message = "Nota não pode ser negativa")
	private Double score;

	private String observation;


	public Long getExamId() {
		return examId;
	}

	public void setExamId(Long examId) {
		this.examId = examId;
	}

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}
	
	
}
