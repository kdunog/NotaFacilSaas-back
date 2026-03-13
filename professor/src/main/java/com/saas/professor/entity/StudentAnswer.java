package com.saas.professor.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
name = "student_answers",
uniqueConstraints = @UniqueConstraint(
columnNames = {"student_id", "question_id"}
)
)
@Data
@NoArgsConstructor
public class StudentAnswer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(nullable = false)
	private Double pointsEarned;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public StudentAnswer(Student student, Question question, Double pointsEarned) {
	    this.student = student;
	    this.question = question;
	    this.pointsEarned = pointsEarned;
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	}

}
