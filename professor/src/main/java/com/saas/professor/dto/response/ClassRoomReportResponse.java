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
public class ClassRoomReportResponse {

	private Long classRoomId;
	private String classRoomDescription;
	private double classAverage;
	private int totalStudents;
	private int studentsAtRisk;
	private List<StudentReportResponse> students;


	public Long getClassRoomId() {
		return classRoomId;
	}

	public void setClassRoomId(Long classRoomId) {
		this.classRoomId = classRoomId;
	}

	public String getClassRoomDescription() {
		return classRoomDescription;
	}

	public void setClassRoomDescription(String classRoomDescription) {
		this.classRoomDescription = classRoomDescription;
	}

	public double getClassAverage() {
		return classAverage;
	}

	public void setClassAverage(double classAverage) {
		this.classAverage = classAverage;
	}

	public int getTotalStudents() {
		return totalStudents;
	}

	public void setTotalStudents(int totalStudents) {
		this.totalStudents = totalStudents;
	}

	public int getStudentsAtRisk() {
		return studentsAtRisk;
	}

	public void setStudentsAtRisk(int studentsAtRisk) {
		this.studentsAtRisk = studentsAtRisk;
	}

	public List<StudentReportResponse> getStudents() {
		return students;
	}

	public void setStudents(List<StudentReportResponse> students) {
		this.students = students;
	}
	
	
}
