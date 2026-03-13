package com.saas.professor.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class BimestreResponse {
	
	private Long id;
	private Long classRoomId;
	private String name;
	private Integer number;
	private Integer schoolYear;
	private LocalDate startDate;
	private LocalDate endDate;
	private Boolean closed;
	private List<ExamResponse> exams;

	public BimestreResponse(Long id, Long classRoomId, String name, Integer number,
	                         Integer schoolYear, LocalDate startDate, LocalDate endDate,
	                         Boolean closed, List<ExamResponse> exams) {
	    this.id = id;
	    this.classRoomId = classRoomId;
	    this.name = name;
	    this.number = number;
	    this.schoolYear = schoolYear;
	    this.startDate = startDate;
	    this.endDate = endDate;
	    this.closed = closed;
	    this.exams = exams;
	}

}
