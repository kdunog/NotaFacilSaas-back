package com.saas.professor.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class BimestreReportResponse {
	
	private Long bimestreId;
	private String bimestreName;
	private Integer number;
	private Integer schoolYear;
	private String classRoomDescription;
	private double classAverage;
	private int totalStudents;
	private List<StudentBimestreReportResponse> students;

	public BimestreReportResponse(Long bimestreId, String bimestreName, Integer number,
	                               Integer schoolYear, String classRoomDescription,
	                               double classAverage, int totalStudents,
	                               List<StudentBimestreReportResponse> students) {
	    this.bimestreId = bimestreId;
	    this.bimestreName = bimestreName;
	    this.number = number;
	    this.schoolYear = schoolYear;
	    this.classRoomDescription = classRoomDescription;
	    this.classAverage = classAverage;
	    this.totalStudents = totalStudents;
	    this.students = students;
	}

}
