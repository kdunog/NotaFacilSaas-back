package com.saas.professor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.saas.professor.entity.Subscription;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
	
	// SubscriptionRepository.java
	Optional<Subscription> findByTeacherAndStatusIn(Teacher teacher, List<SubscriptionStatus> status);
	Optional<Subscription> findFirstByTeacherOrderByCreatedAtDesc(Teacher teacher);
	
	Optional<Subscription> findByTeacherAndStatus(Teacher teacher, SubscriptionStatus status);

	Optional<Subscription> findByMercadoPagoPaymentId(String paymentId);
	
	@Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s " +
			"WHERE s.status = 'ACTIVE' " +
			"AND MONTH(s.startsAt) = :month AND YEAR(s.startsAt) = :year")
			Double sumMonthlyRevenue(@Param("month") int month, @Param("year") int year);
	
			@Query("SELECT COALESCE(SUM(s.amountPaid), 0) FROM Subscription s " +
			"WHERE s.status = 'ACTIVE' AND YEAR(s.startsAt) = :year")
			Double sumAnnualRevenue(@Param("year") int year);

}
