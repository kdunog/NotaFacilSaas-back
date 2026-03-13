package com.saas.professor.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.RegisterAnswersRequest;
import com.saas.professor.dto.response.StudentAnswerResponse;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Question;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.StudentAnswer;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.QuestionRepository;
import com.saas.professor.repository.StudentAnswerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentAnswerService {
	
	private final StudentAnswerRepository studentAnswerRepository;
	private final QuestionRepository questionRepository;
	private final StudentService studentService;
	private final ExamService examService;
	private final GradeService gradeService;

	@Transactional
	public StudentAnswerResponse registerAnswers(RegisterAnswersRequest req, Long teacherId) {
	    Exam exam = examService.findEntityById(req.getExamId(), teacherId);
	    Student student = studentService.getEntityOrThrow(req.getStudentId(), teacherId);

	    double totalEarned = 0.0;

	    for (var item : req.getAnswers()) {
	        Question question = questionRepository.findById(item.getQuestionId())
	            .orElseThrow(() -> new BusinessException("Questão não encontrada"));

	        if (item.getPointsEarned() > question.getPoints())
	            throw new BusinessException("Pontos ganhos não podem exceder pontos da questão: "
	                + question.getStatement());

	        StudentAnswer answer = studentAnswerRepository
	            .findByStudentIdAndQuestionId(student.getId(), question.getId())
	            .orElse(new StudentAnswer(student, question, item.getPointsEarned()));

	        answer.setPointsEarned(item.getPointsEarned());
	        studentAnswerRepository.save(answer);
	        totalEarned += item.getPointsEarned();
	    }

	    // Registra/atualiza nota automaticamente
	    double totalPoints = exam.getTotalPoints();
	    double finalScore = totalPoints > 0
	        ? Math.round((totalEarned / totalPoints) * 1000.0) / 100.0 : 0.0;

	    gradeService.registerOrUpdate(student.getId(), exam.getId(), finalScore, teacherId);

	    return new StudentAnswerResponse(student.getId(), student.getName(),
	            exam.getId(), exam.getTitle(), totalPoints, totalEarned);
	}

}
