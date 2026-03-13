package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.professor.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
	
	List<Subject> findByTeacherId(Long teacherId);

	Optional<Subject> findByIdAndTeacherId(Long id, Long teacherId);

	boolean existsByTeacherIdAndName(Long teacherId, String name);

}
