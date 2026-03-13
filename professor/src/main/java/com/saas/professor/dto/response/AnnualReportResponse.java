package com.saas.professor.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class AnnualReportResponse {
	
	private Long classRoomId;
	private String classRoomDescription;
	private Integer schoolYear;
	private double classAnnualAverage;
	private int totalStudents;
	private List<StudentAnnualReportResponse> students;

	public AnnualReportResponse(Long classRoomId, String classRoomDescription,
	                             Integer schoolYear, double classAnnualAverage,
	                             int totalStudents,
	                             List<StudentAnnualReportResponse> students) {
	    this.classRoomId = classRoomId;
	    this.classRoomDescription = classRoomDescription;
	    this.schoolYear = schoolYear;
	    this.classAnnualAverage = classAnnualAverage;
	    this.totalStudents = totalStudents;
	    this.students = students;
	}

}
