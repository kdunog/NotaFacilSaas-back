package com.saas.professor.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.saas.professor.enums.QuestionType;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
public class Question {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id", nullable = false)
	private Exam exam;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private QuestionType type;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String statement;

	@Column(nullable = false)
	private Double points;

	@Column(nullable = false)
	private Integer orderIndex;

	@OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("orderIndex ASC")
	private List<QuestionOption> options = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Question(Exam exam, QuestionType type, String statement,
	                Double points, Integer orderIndex) {
	    this.exam = exam;
	    this.type = type;
	    this.statement = statement;
	    this.points = points;
	    this.orderIndex = orderIndex;
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	}
}
