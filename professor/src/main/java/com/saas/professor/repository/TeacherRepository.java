package com.saas.professor.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.PlanType;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Teacher> findByEmailVerificationToken(String token);
    Optional<Teacher> findByPasswordResetToken(String token);
    Optional<Teacher> findByMercadoPagoPayerId(String mercadoPagoPayerId);
    
    long countByActive(boolean active);
    long countByPlan(PlanType plan);
    
    @Query("SELECT t FROM Teacher t WHERE t.planExpiresAt IS NOT NULL AND t.planExpiresAt < :now AND t.active = true")
    List<Teacher> findExpiredPlans(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Teacher t ORDER BY t.createdAt DESC")
    List<Teacher> findRecentRegistrations(Pageable pageable);
    
    @Query("SELECT MONTH(t.createdAt), COUNT(t) FROM Teacher t WHERE YEAR(t.createdAt) = :year GROUP BY MONTH(t.createdAt) ORDER BY MONTH(t.createdAt)")
    List<Object[]> countRegistrationsByMonth(@Param("year") int year);
}