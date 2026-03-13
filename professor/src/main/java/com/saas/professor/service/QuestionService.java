package com.saas.professor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.CreateQuestionRequest;
import com.saas.professor.dto.response.OptionResponse;
import com.saas.professor.dto.response.QuestionResponse;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Question;
import com.saas.professor.entity.QuestionOption;
import com.saas.professor.enums.ExamStatus;
import com.saas.professor.enums.QuestionType;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {
	
	private final QuestionRepository questionRepository;
	private final ExamService examService;

	@Transactional
	public QuestionResponse create(CreateQuestionRequest req, Long teacherId) {
	    Exam exam = examService.findEntityById(req.getExamId(), teacherId);

	    if (exam.getStatus() == ExamStatus.APPLIED)
	        throw new BusinessException("Não é possível adicionar questões a uma prova já aplicada");

	    int order = questionRepository.countByExamId(exam.getId()) + 1;
	    Question question = new Question(exam, req.getType(), req.getStatement(),
	            req.getPoints(), order);

	    // Adiciona opções para múltipla escolha e V/F
	    if (req.getOptions() != null && !req.getOptions().isEmpty()) {
	        validateOptions(req);
	        for (int i = 0; i < req.getOptions().size(); i++) {
	            var opt = req.getOptions().get(i);
	            question.getOptions().add(
	                new QuestionOption(question, opt.getText(), opt.getCorrect(), i + 1));
	        }
	    }

	    // Atualiza totalPoints da prova
	    Double newTotal = questionRepository.sumPointsByExamId(exam.getId()) + req.getPoints();
	    exam.setTotalPoints(newTotal);

	    return toResponse(questionRepository.save(question));
	}

	@Transactional
	public void delete(Long questionId, Long teacherId) {
	    Question question = questionRepository
	        .findByIdAndExam_ClassRoom_TeacherId(questionId, teacherId)
	        .orElseThrow(() -> new ResourceNotFoundException("Questão não encontrada"));

	    if (question.getExam().getStatus() == ExamStatus.APPLIED)
	        throw new BusinessException("Não é possível remover questões de uma prova já aplicada");

	    questionRepository.delete(question);
	}

	@Transactional(readOnly = true)
	public List<QuestionResponse> findByExam(Long examId, Long teacherId) {
	    examService.findEntityById(examId, teacherId);
	    return questionRepository.findByExamIdOrderByOrderIndexAsc(examId)
	            .stream().map(this::toResponse).collect(Collectors.toList());
	}

	public Question getEntityOrThrow(Long id, Long teacherId) {
	    return questionRepository.findByIdAndExam_ClassRoom_TeacherId(id, teacherId)
	            .orElseThrow(() -> new ResourceNotFoundException("Questão não encontrada"));
	}

	private void validateOptions(CreateQuestionRequest req) {
	    if (req.getType() == QuestionType.MULTIPLE_CHOICE) {
	        long correctCount = req.getOptions().stream()
	            .filter(o -> Boolean.TRUE.equals(o.getCorrect())).count();
	        if (correctCount != 1)
	            throw new BusinessException("Múltipla escolha deve ter exatamente 1 opção correta");
	        if (req.getOptions().size() < 2)
	            throw new BusinessException("Múltipla escolha deve ter pelo menos 2 opções");
	    }
	    if (req.getType() == QuestionType.TRUE_OR_FALSE) {
	        if (req.getOptions().size() != 2)
	            throw new BusinessException("Verdadeiro/Falso deve ter exatamente 2 opções");
	    }
	}

	private QuestionResponse toResponse(Question q) {
	    List<OptionResponse> options = q.getOptions().stream()
	        .map(o -> new OptionResponse(o.getId(), o.getText(), o.getCorrect(), o.getOrderIndex()))
	        .collect(Collectors.toList());
	    return new QuestionResponse(q.getId(), q.getExam().getId(), q.getType(),
	            q.getStatement(), q.getPoints(), q.getOrderIndex(), options);
	}

}
