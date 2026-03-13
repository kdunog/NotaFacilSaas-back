package com.saas.professor.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.saas.professor.entity.StudentAnswer;

public interface StudentAnswerRepository extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findByStudentIdAndQuestion_ExamId(Long studentId, Long examId);

    Optional<StudentAnswer> findByStudentIdAndQuestionId(Long studentId, Long questionId);

    // Busca todas as respostas de uma prova (usado no cascade delete)
    List<StudentAnswer> findByQuestion_ExamId(Long examId);

    List<StudentAnswer> findByStudentId(Long studentId);

    @Query("SELECT COALESCE(SUM(sa.pointsEarned), 0) FROM StudentAnswer sa " +
           "WHERE sa.student.id = :studentId AND sa.question.exam.id = :examId")
    Double sumPointsEarnedByStudentAndExam(
        @Param("studentId") Long studentId,
        @Param("examId") Long examId);
}