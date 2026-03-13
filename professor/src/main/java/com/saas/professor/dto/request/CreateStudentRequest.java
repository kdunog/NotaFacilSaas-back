package com.saas.professor.dto.request;

import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRequest {
	

	@NotBlank(message = "Nome é obrigatório")
	private String name;

}
