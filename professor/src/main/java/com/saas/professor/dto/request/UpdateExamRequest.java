package com.saas.professor.dto.request;

import java.time.LocalDate;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExamRequest {
	
	@NotBlank(message = "Título é obrigatório")
	private String title;

	private LocalDate examDate;

	@NotNull(message = "Total de pontos é obrigatório")
	@Min(value = 1, message = "Total de pontos deve ser maior que 0")
	private Double totalPoints;

	@NotNull(message = "Peso é obrigatório")
	@Min(value = 1, message = "Peso deve ser maior que 0")
	private Double weight;
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getExamDate() {
		return examDate;
	}

	public void setExamDate(LocalDate examDate) {
		this.examDate = examDate;
	}

	public Double getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(Double totalPoints) {
		this.totalPoints = totalPoints;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	

}
