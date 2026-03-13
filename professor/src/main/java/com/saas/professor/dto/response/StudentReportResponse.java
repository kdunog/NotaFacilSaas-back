package com.saas.professor.dto.response;

import java.util.List;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentReportResponse {
	
	private Long studentId;
	private String studentName;
	private Double average;
	private Integer totalExams;
	private List<GradeResponse> grades;


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

	public Double getAverage() {
		return average;
	}

	public void setAverage(Double average) {
		this.average = average;
	}

	public Integer getTotalExams() {
		return totalExams;
	}

	public void setTotalExams(Integer totalExams) {
		this.totalExams = totalExams;
	}

	public List<GradeResponse> getGrades() {
		return grades;
	}

	public void setGrades(List<GradeResponse> grades) {
		this.grades = grades;
	}
	
	

}
