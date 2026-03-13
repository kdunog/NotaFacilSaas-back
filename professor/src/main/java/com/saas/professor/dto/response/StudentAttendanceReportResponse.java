package com.saas.professor.dto.response;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAttendanceReportResponse {
	
	private Long studentId;
	private String studentName;
	private long totalDays;
	private long presences;
	private long absences;
	private long lates;
	private double attendancePercentage;

	public StudentAttendanceReportResponse(Long studentId, String studentName,
	                                        long totalDays, long presences,
	                                        long absences, long lates) {
	    this.studentId = studentId;
	    this.studentName = studentName;
	    this.totalDays = totalDays;
	    this.presences = presences;
	    this.absences = absences;
	    this.lates = lates;
	    this.attendancePercentage = totalDays > 0
	        ? Math.round(((double)(presences + lates) / totalDays) * 10000.0) / 100.0
	        : 0.0;
	}

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

	public long getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(long totalDays) {
		this.totalDays = totalDays;
	}

	public long getPresences() {
		return presences;
	}

	public void setPresences(long presences) {
		this.presences = presences;
	}

	public long getAbsences() {
		return absences;
	}

	public void setAbsences(long absences) {
		this.absences = absences;
	}

	public long getLates() {
		return lates;
	}

	public void setLates(long lates) {
		this.lates = lates;
	}

	public double getAttendancePercentage() {
		return attendancePercentage;
	}

	public void setAttendancePercentage(double attendancePercentage) {
		this.attendancePercentage = attendancePercentage;
	}
	
	

}
