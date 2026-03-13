package com.saas.professor.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter 
@Setter 
@NoArgsConstructor
public class CreateBimestreRequest {
	
	@NotNull(message = "classRoomId é obrigatório")
	private Long classRoomId;

	@NotBlank(message = "Nome é obrigatório")
	private String name;

	@NotNull(message = "Número do bimestre é obrigatório")
	@Min(1) @Max(4)
	private Integer number;

	@NotNull(message = "Ano letivo é obrigatório")
	private Integer schoolYear;

	private LocalDate startDate;
	private LocalDate endDate;

	private List<Long> examIds;

}
