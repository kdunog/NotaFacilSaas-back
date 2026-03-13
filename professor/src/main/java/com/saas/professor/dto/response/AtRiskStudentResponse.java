package com.saas.professor.dto.response;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtRiskStudentResponse {
	
	private Long studentId;
	private String studentName;
	private Double gradeAverage;
	private Double attendancePercentage;
	private String riskReason;


	public Long getStudentId() {
		return studentId;
	}

	public void setStudentId(Long studentId) {
		this.studentId = studentId;
	}

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public Double getGradeAverage() {
		return gradeAverage;
	}

	public void setGradeAverage(Double gradeAverage) {
		this.gradeAverage = gradeAverage;
	}

	public Double getAttendancePercentage() {
		return attendancePercentage;
	}

	public void setAttendancePercentage(Double attendancePercentage) {
		this.attendancePercentage = attendancePercentage;
	}

	public String getRiskReason() {
		return riskReason;
	}

	public void setRiskReason(String riskReason) {
		this.riskReason = riskReason;
	}
	
	

}
