package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Exam;

import io.lettuce.core.dynamic.annotation.Param;

public interface ExamRepository extends JpaRepository<Exam, Long> {

	 List<Exam> findByClassRoom(ClassRoom classRoom);

	    Optional<Exam> findByIdAndClassRoom_TeacherId(Long id, Long teacherId);
}