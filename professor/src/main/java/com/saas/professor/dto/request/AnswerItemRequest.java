package com.saas.professor.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswerItemRequest {
	
	@NotNull
	private Long questionId;

	@NotNull
	private Double pointsEarned;

}
