package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.professor.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

	List<Question> findByExamIdOrderByOrderIndexAsc(Long examId);

	@Query("SELECT COUNT(q) FROM Question q WHERE q.exam.id = :examId")
	int countByExamId(@Param("examId") Long examId);

	@Query("SELECT COALESCE(SUM(q.points), 0) FROM Question q WHERE q.exam.id = :examId")
	Double sumPointsByExamId(@Param("examId") Long examId);

	Optional<Question> findByIdAndExam_ClassRoom_TeacherId(Long id, Long teacherId);
}
