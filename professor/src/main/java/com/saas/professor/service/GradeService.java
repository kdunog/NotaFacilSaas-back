package com.saas.professor.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.RegisterGradeRequest;
import com.saas.professor.dto.request.UpdateGradeRequest;
import com.saas.professor.dto.response.GradeResponse;
import com.saas.professor.entity.Exam;
import com.saas.professor.entity.Grade;
import com.saas.professor.entity.Student;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.ExamStatus;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.GradeRepository;

@Service
public class GradeService {
	
	private final GradeRepository gradeRepository;
	private final ExamService examService;
	private final StudentService studentService;
	
	public GradeService(GradeRepository gradeRepository, ExamService examService, StudentService studentService) {
		this.gradeRepository = gradeRepository;
		this.examService = examService;
		this.studentService = studentService;
	}

	@Transactional
	public GradeResponse register(RegisterGradeRequest req, Long teacherId) {
	    Exam exam = examService.findEntityById(req.getExamId(), teacherId);

	    if (exam.getStatus() != ExamStatus.APPLIED)
	        throw new BusinessException("Só é possível lançar notas em provas já aplicadas");

	    if (req.getScore() > exam.getTotalPoints())
	        throw new BusinessException("Nota não pode ser maior que o total de pontos da prova");

	    gradeRepository.findByStudentIdAndExamId(req.getStudentId(), req.getExamId())
	            .ifPresent(g -> { throw new BusinessException("Nota já lançada. Use edição."); });

	    Student student = studentService.getEntityOrThrow(req.getStudentId(), teacherId);
	    Grade grade = new Grade(student, exam, req.getScore(), req.getObservation());
	    return toResponse(gradeRepository.save(grade));
	}

	@Transactional
	public GradeResponse update(Long gradeId, UpdateGradeRequest req, Long teacherId) {
	    Grade grade = gradeRepository.findById(gradeId)
	            .orElseThrow(() -> new ResourceNotFoundException("Nota não encontrada"));

	    // Garante que a nota pertence ao professor
	    if (!grade.getExam().getClassRoom().getTeacherId().equals(teacherId))
	        throw new BusinessException("Nota não pertence a este professor");

	    if (req.getScore() > grade.getExam().getTotalPoints())
	        throw new BusinessException("Nota não pode ser maior que o total de pontos da prova");

	    grade.setScore(req.getScore());
	    grade.setObservation(req.getObservation());
	    return toResponse(gradeRepository.save(grade));
	}

	@Transactional(readOnly = true)
	public List<GradeResponse> findByExam(Long examId, Long teacherId) {
	    examService.findEntityById(examId, teacherId); // Verifica se a prova existe e pertence ao professor);
	    return gradeRepository.findByExamId(examId)
	            .stream().map(this::toResponse).collect(Collectors.toList());
	}

	private GradeResponse toResponse(Grade g) {
	    return new GradeResponse(g.getId(), g.getStudent().getId(), g.getStudent().getName(),
	            g.getExam().getId(), g.getExam().getTitle(),
	            g.getScore(), g.getExam().getTotalPoints(), g.getObservation());
	}
	
	@Transactional
	public void registerOrUpdate(Long studentId, Long examId, Double score, Long teacherId) {
	    Exam exam = examService.findEntityById(examId, teacherId);
	    
	    Optional<Grade> existing = gradeRepository.findByStudentIdAndExamId(studentId, examId);
	    
	    if (existing.isPresent()) {
	        Grade grade = existing.get();
	        grade.setScore(score);
	        gradeRepository.save(grade);
	    } else {
	        Student student = studentService.getEntityOrThrow(studentId, teacherId);
	        gradeRepository.save(new Grade(student, exam, score, null));
	    }
	}

}
