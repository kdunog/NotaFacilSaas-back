package com.saas.professor.dto.request;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {
	

	private Long teacherId;

	@NotBlank(message = "Nome da disciplina é obrigatório")
	private String name;
		
}
