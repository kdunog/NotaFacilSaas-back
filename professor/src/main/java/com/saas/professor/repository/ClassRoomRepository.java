package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.professor.entity.ClassRoom;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.ClassRoomStatus;
import com.saas.professor.enums.EducationLevel;

public interface ClassRoomRepository extends JpaRepository<ClassRoom, Long> {

	List<ClassRoom> findByTeacherIdAndStatus(Long teacherId, ClassRoomStatus status);

	List<ClassRoom> findByTeacherId(Long teacherId);

	Optional<ClassRoom> findByIdAndTeacherId(Long id, Long teacherId);

	boolean existsByTeacherIdAndSubject_IdAndSchoolYearAndGradeNumberAndEducationLevelAndSectionLetterAndStatus(
	    Long teacherId, Long subjectId, Integer schoolYear, Integer gradeNumber,
	    EducationLevel educationLevel, String sectionLetter, ClassRoomStatus status
	);

	long countByTeacherIdAndStatus(Long teacherId, ClassRoomStatus status);

	
}
