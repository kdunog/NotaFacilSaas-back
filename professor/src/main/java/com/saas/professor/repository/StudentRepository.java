package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.professor.entity.Student;
import com.saas.professor.entity.Teacher;

public interface StudentRepository extends JpaRepository<Student, Long> {
	
	List<Student> findByTeacherIdAndActive(Long teacherId, Boolean active);

	List<Student> findByTeacherId(Long teacherId);

	Optional<Student> findByIdAndTeacherId(Long id, Long teacherId);

}
