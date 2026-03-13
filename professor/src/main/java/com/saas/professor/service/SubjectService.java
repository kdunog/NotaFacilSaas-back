package com.saas.professor.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.saas.professor.dto.request.CreateSubjectRequest;
import com.saas.professor.dto.response.SubjectResponse;
import com.saas.professor.entity.Subject;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.exceptions.ResourceNotFoundException;
import com.saas.professor.repository.SubjectRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
public class SubjectService {
	private final SubjectRepository subjectRepository;
	
	public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

	@Transactional
	public SubjectResponse create(CreateSubjectRequest request, Long teacherId) {
	    if (subjectRepository.existsByTeacherIdAndName(teacherId, request.getName()))
	        throw new BusinessException("Disciplina '" + request.getName() + "' já existe para este professor");
	    Subject subject = new Subject(teacherId, request.getName());
	    return toResponse(subjectRepository.save(subject));
	}

	@Transactional(readOnly = true)
	public List<SubjectResponse> findAllByTeacher(Long teacherId) {
	    return subjectRepository.findByTeacherId(teacherId)
	            .stream()
	            .map(this::toResponse)
	            .collect(Collectors.toList());
	}

	public Subject findEntityById(Long id, Long teacherId) {
	    return subjectRepository.findByIdAndTeacherId(id, teacherId)
	            .orElseThrow(() -> new ResourceNotFoundException("Disciplina não encontrada"));
	}

	private SubjectResponse toResponse(Subject s) {
	    return new SubjectResponse(
	            s.getId(),
	            s.getTeacherId(),
	            s.getName(),
	            s.getCreatedAt()
	    );
	}
}
