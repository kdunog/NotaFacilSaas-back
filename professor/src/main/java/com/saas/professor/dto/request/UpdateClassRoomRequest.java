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
public class UpdateClassRoomRequest {
	
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
	

	public Long getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Long subjectId) {
		this.subjectId = subjectId;
	}

	public EducationLevel getEducationLevel() {
		return educationLevel;
	}

	public void setEducationLevel(EducationLevel educationLevel) {
		this.educationLevel = educationLevel;
	}

	public Integer getGradeNumber() {
		return gradeNumber;
	}

	public void setGradeNumber(Integer gradeNumber) {
		this.gradeNumber = gradeNumber;
	}

	public String getSectionLetter() {
		return sectionLetter;
	}

	public void setSectionLetter(String sectionLetter) {
		this.sectionLetter = sectionLetter;
	}

	public Integer getSchoolYear() {
		return schoolYear;
	}

	public void setSchoolYear(Integer schoolYear) {
		this.schoolYear = schoolYear;
	}
	
	

}
