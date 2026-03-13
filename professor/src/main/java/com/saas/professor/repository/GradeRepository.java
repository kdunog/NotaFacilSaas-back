package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.professor.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
	
	List<Grade> findByExamId(Long examId);

	List<Grade> findByStudentId(Long studentId);

	@Query("SELECT g FROM Grade g JOIN g.exam e WHERE g.student.id = :studentId ORDER BY e.examDate ASC")
	List<Grade> findByStudentIdOrderByExamDateAsc(@Param("studentId") Long studentId);

	Optional<Grade> findByStudentIdAndExamId(Long studentId, Long examId);

}
