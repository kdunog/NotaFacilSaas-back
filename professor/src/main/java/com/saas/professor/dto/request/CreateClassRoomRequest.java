package com.saas.professor.dto.request;
import com.saas.professor.enums.EducationLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRoomRequest {

    @NotNull(message = "subjectId é obrigatório")
    private Long subjectId;

    @NotNull(message = "Nível de ensino é obrigatório")
    private EducationLevel educationLevel;

    @NotNull @Min(1) @Max(9)
    private Integer gradeNumber;

    @NotBlank
    @Size(min = 1, max = 1, message = "Seção deve ser uma única letra")
    private String sectionLetter;

    @NotNull @Min(2000)
    private Integer schoolYear;

    @NotNull(message = "Nota mínima é obrigatória")
    @Min(value = 0)
    @Max(value = 10)
    private Double minimumGrade = 5.0;

    private Boolean allowsBimestreRecovery = false;

    @Size(max = 200)
    private String schoolName;
}