package com.saas.professor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_options")
@Data
@NoArgsConstructor
public class QuestionOption {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(nullable = false)
	private String text;

	@Column(nullable = false)
	private Boolean correct;

	@Column(nullable = false)
	private Integer orderIndex;

	public QuestionOption(Question question, String text,
	                      Boolean correct, Integer orderIndex) {
	    this.question = question;
	    this.text = text;
	    this.correct = correct;
	    this.orderIndex = orderIndex;
	}

}
