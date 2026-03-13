package com.saas.professor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.professor.entity.RefreshToken;
import com.saas.professor.entity.Teacher;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String token);

	void deleteAllByTeacherId(Long teacherId);

}
