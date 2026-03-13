package com.saas.professor.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.professor.entity.Attendance;
import com.saas.professor.enums.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
	
	List<Attendance> findByClassRoom_IdAndAttendanceDate(Long classRoomId, LocalDate date);

	List<Attendance> findByStudent_IdAndClassRoom_Id(Long studentId, Long classRoomId);

	List<Attendance> findByClassRoom_Id(Long classRoomId);

	Optional<Attendance> findByStudent_IdAndClassRoom_IdAndAttendanceDate(
	    Long studentId, Long classRoomId, LocalDate date);

	@Query("SELECT COUNT(a) FROM Attendance a " +
	       "WHERE a.classRoom.id = :classRoomId " +
	       "AND a.student.id = :studentId " +
	       "AND a.status = :status")
	long countByClassRoomIdAndStudentIdAndStatus(
	    @Param("classRoomId") Long classRoomId,
	    @Param("studentId") Long studentId,
	    @Param("status") AttendanceStatus status);

	@Query("SELECT COUNT(DISTINCT a.attendanceDate) FROM Attendance a " +
	       "WHERE a.classRoom.id = :classRoomId")
	long countTotalClassDays(@Param("classRoomId") Long classRoomId);
	
	@Query("SELECT DISTINCT a.attendanceDate FROM Attendance a WHERE a.classRoom.id = :classRoomId ORDER BY a.attendanceDate DESC")
	List<LocalDate> findDistinctDatesByClassRoomId(@Param("classRoomId") Long classRoomId);

}
