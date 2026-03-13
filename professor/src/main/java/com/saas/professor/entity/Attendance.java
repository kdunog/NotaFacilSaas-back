package com.saas.professor.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.saas.professor.enums.AttendanceStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(
name = "attendances",
uniqueConstraints = @UniqueConstraint(
name = "uk_attendance_student_classroom_date",
columnNames = {"student_id", "class_room_id", "attendance_date"}
)
)
@Data
@NoArgsConstructor
public class Attendance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_room_id", nullable = false)
	private ClassRoom classRoom;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private AttendanceStatus status;

	@Column(name = "attendance_date", nullable = false)
	private LocalDate attendanceDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Attendance(Student student, ClassRoom classRoom,
	                  AttendanceStatus status, LocalDate attendanceDate) {
	    this.student = student;
	    this.classRoom = classRoom;
	    this.status = status;
	    this.attendanceDate = attendanceDate;
	}
	

	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}



	public Student getStudent() {
		return student;
	}



	public void setStudent(Student student) {
		this.student = student;
	}



	public ClassRoom getClassRoom() {
		return classRoom;
	}



	public void setClassRoom(ClassRoom classRoom) {
		this.classRoom = classRoom;
	}



	public AttendanceStatus getStatus() {
		return status;
	}



	public void setStatus(AttendanceStatus status) {
		this.status = status;
	}



	public LocalDate getAttendanceDate() {
		return attendanceDate;
	}



	public void setAttendanceDate(LocalDate attendanceDate) {
		this.attendanceDate = attendanceDate;
	}



	public LocalDateTime getCreatedAt() {
		return createdAt;
	}



	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}



	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}



	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}



	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	    this.updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	public void preUpdate() {
	    this.updatedAt = LocalDateTime.now();
	}

}
