package com.saas.professor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateOptionRequest {

	@NotBlank(message = "Texto da opção é obrigatório")
	private String text;

	@NotNull(message = "Informe se a opção é correta")
	private Boolean correct;
}
