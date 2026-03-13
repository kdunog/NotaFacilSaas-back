package com.saas.professor.dto.request;

import com.saas.professor.enums.AttendanceStatus;
import com.saas.professor.enums.EducationLevel;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAttendanceRequest {
	
	@NotNull(message = "studentId é obrigatório")
	private Long studentId;

	@NotNull(message = "status é obrigatório")
	private AttendanceStatus status;
	

	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public AttendanceStatus getStatus() {
		return status;
	}

	public void setStatus(AttendanceStatus status) {
		this.status = status;
	}
	
	
}
