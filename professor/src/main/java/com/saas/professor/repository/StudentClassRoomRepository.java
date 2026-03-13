package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.professor.entity.StudentClassRoom;

public interface StudentClassRoomRepository extends JpaRepository<StudentClassRoom, Long> {
	
	
	List<StudentClassRoom> findByClassRoom_Id(Long classRoomId);

	@Query("SELECT sc FROM StudentClassRoom sc " +
		       "JOIN sc.classRoom cr " +
		       "WHERE sc.student.id = :studentId " +
		       "AND sc.leftAt IS NULL " +
		       "AND cr.teacherId = :teacherId")
		Optional<StudentClassRoom> findActiveByStudentIdAndTeacherId(
		    @Param("studentId") Long studentId,
		    @Param("teacherId") Long teacherId
		);

		List<StudentClassRoom> findByStudent_IdOrderByEnteredAtDesc(Long studentId);

		List<StudentClassRoom> findByClassRoom_IdAndLeftAtIsNull(Long classRoomId);
		
		@Query("SELECT sc.student.id FROM StudentClassRoom sc WHERE sc.leftAt IS NULL")
		List<Long> findStudentIdsWithActiveEnrollment();
}
