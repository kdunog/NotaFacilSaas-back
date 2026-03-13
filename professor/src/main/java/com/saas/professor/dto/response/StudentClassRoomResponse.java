package com.saas.professor.dto.response;

import java.time.LocalDateTime;

import com.saas.professor.dto.request.CreateClassRoomRequest;
import com.saas.professor.enums.EducationLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentClassRoomResponse {

	private Long id;
	private Long studentId;
	private String studentName;
	private Long classRoomId;
	private String classRoomDescription;
	private LocalDateTime enteredAt;
	private LocalDateTime leftAt;
	

	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
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
	public LocalDateTime getEnteredAt() {
		return enteredAt;
	}
	public void setEnteredAt(LocalDateTime enteredAt) {
		this.enteredAt = enteredAt;
	}
	public LocalDateTime getLeftAt() {
		return leftAt;
	}
	public void setLeftAt(LocalDateTime leftAt) {
		this.leftAt = leftAt;
	}
	
	
	
	
}
