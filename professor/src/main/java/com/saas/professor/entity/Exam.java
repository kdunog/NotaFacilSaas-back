package com.saas.professor.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.saas.professor.enums.ExamStatus;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_room_id", nullable = false)
	private ClassRoom classRoom;

	@Column(nullable = false)
	private String title;

	@Column(name = "exam_date")
	private LocalDate examDate;

	@Column(name = "total_points", nullable = false)
	private Double totalPoints;

	@Column(name = "weight", nullable = false)
	private Double weight;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ExamStatus status;

	@Column(name = "applied_at")
	private LocalDateTime appliedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Exam(ClassRoom classRoom, String title, LocalDate examDate,
	            Double totalPoints, Double weight) {
	    this.classRoom = classRoom;
	    this.title = title;
	    this.examDate = examDate;
	    this.totalPoints = totalPoints;
	    this.weight = weight;
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	    this.status = ExamStatus.DRAFT;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ClassRoom getClassRoom() {
		return classRoom;
	}

	public void setClassRoom(ClassRoom classRoom) {
		this.classRoom = classRoom;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public LocalDate getExamDate() {
		return examDate;
	}

	public void setExamDate(LocalDate examDate) {
		this.examDate = examDate;
	}

	public Double getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(Double totalPoints) {
		this.totalPoints = totalPoints;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public ExamStatus getStatus() {
		return status;
	}

	public void setStatus(ExamStatus status) {
		this.status = status;
	}

	public LocalDateTime getAppliedAt() {
		return appliedAt;
	}

	public void setAppliedAt(LocalDateTime appliedAt) {
		this.appliedAt = appliedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
	
	

}
