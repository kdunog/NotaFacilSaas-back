package com.saas.professor.dto.request;

import java.util.List;

import com.saas.professor.enums.QuestionType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateQuestionRequest {
	
	@NotNull(message = "examId é obrigatório")
	private Long examId;

	@NotNull(message = "Tipo é obrigatório")
	private QuestionType type;

	@NotBlank(message = "Enunciado é obrigatório")
	private String statement;

	@NotNull(message = "Pontos é obrigatório")
	@Min(value = 0, message = "Pontos não pode ser negativo")
	private Double points;

	private List<CreateOptionRequest> options;

}
