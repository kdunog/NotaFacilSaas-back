package com.saas.professor.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterAnswersRequest {
	
	@NotNull
	private Long studentId;

	@NotNull
	private Long examId;

	@NotNull
	private List<AnswerItemRequest> answers;

}
