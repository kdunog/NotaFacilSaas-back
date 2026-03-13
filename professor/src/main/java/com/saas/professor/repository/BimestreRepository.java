package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.professor.entity.Bimestre;

public interface BimestreRepository extends JpaRepository<Bimestre, Long> {
	
	List<Bimestre> findByClassRoom_IdAndTeacherIdOrderByNumberAsc(
		    Long classRoomId, Long teacherId);

		List<Bimestre> findByClassRoom_IdAndTeacherIdAndSchoolYearOrderByNumberAsc(
		    Long classRoomId, Long teacherId, Integer schoolYear);

		Optional<Bimestre> findByIdAndTeacherId(Long id, Long teacherId);

		boolean existsByClassRoom_IdAndTeacherIdAndNumber(
		    Long classRoomId, Long teacherId, Integer number);
 
}
