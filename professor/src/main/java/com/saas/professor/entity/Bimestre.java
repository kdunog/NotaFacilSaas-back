package com.saas.professor.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bimestres")
@Getter
@Setter 
@NoArgsConstructor
public class Bimestre {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "teacher_id", nullable = false)
	private Long teacherId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_room_id", nullable = false)
	private ClassRoom classRoom;

	@Column(nullable = false)
	private String name; // Ex: "1º Bimestre", "2º Bimestre"

	@Column(nullable = false)
	private Integer number; // 1, 2, 3, 4

	@Column(name = "school_year", nullable = false)
	private Integer schoolYear;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(nullable = false)
	private Boolean closed;

	@OneToMany(mappedBy = "bimestre", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BimestreExam> bimestreExams = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Bimestre(Long teacherId, ClassRoom classRoom, String name,
	                Integer number, Integer schoolYear,
	                LocalDate startDate, LocalDate endDate) {
	    this.teacherId = teacherId;
	    this.classRoom = classRoom;
	    this.name = name;
	    this.number = number;
	    this.schoolYear = schoolYear;
	    this.startDate = startDate;
	    this.endDate = endDate;
	    this.closed = false;
	}

	@PrePersist
	public void prePersist() {
	    this.createdAt = LocalDateTime.now();
	}

}
