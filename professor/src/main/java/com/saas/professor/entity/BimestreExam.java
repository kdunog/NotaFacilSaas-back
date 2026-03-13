package com.saas.professor.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
name = "bimestre_exams",
uniqueConstraints = @UniqueConstraint(columnNames = {"bimestre_id", "exam_id"})
)
@Getter 
@Setter 
@NoArgsConstructor
public class BimestreExam {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bimestre_id", nullable = false)
	private Bimestre bimestre;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exam_id", nullable = false)
	private Exam exam;

	public BimestreExam(Bimestre bimestre, Exam exam) {
	    this.bimestre = bimestre;
	    this.exam = exam;
	}

}
