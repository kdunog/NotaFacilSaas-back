package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.request.AdminLoginRequest;
import com.saas.professor.dto.response.AdminDashboardResponse;
import com.saas.professor.dto.response.AuthResponse;
import com.saas.professor.dto.response.MonthlyRegistrationResponse;
import com.saas.professor.dto.response.TeacherSummaryResponse;
import com.saas.professor.entity.Admin;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.PlanType;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.AdminRepository;
import com.saas.professor.repository.SubscriptionRepository;
import com.saas.professor.repository.TeacherRepository;
import com.saas.professor.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
	
	private final AdminRepository adminRepository;
	private final TeacherRepository teacherRepository;
	private final SubscriptionRepository subscriptionRepository;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AuthResponse login(AdminLoginRequest req) {
	    Admin admin = adminRepository.findByEmail(req.getEmail())
	        .orElseThrow(() -> new BusinessException("Credenciais inválidas"));

	    if (!passwordEncoder.matches(req.getPassword(), admin.getPassword()))
	        throw new BusinessException("Credenciais inválidas");

	    if (!admin.getActive())
	        throw new BusinessException("Conta desativada");

	    String token = jwtService.generateAdminToken(admin.getId(), admin.getEmail());
	    return new AuthResponse(token, null, admin.getId(), admin.getName(), "ADMIN");
	}

	@Transactional(readOnly = true)
	public AdminDashboardResponse getDashboard() {
	    int currentYear = LocalDateTime.now().getYear();
	    int currentMonth = LocalDateTime.now().getMonthValue();

	    long total = teacherRepository.count();
	    long active = teacherRepository.countByActive(true);
	    long inactive = teacherRepository.countByActive(false);
	    long pro = teacherRepository.countByPlan(PlanType.PRO_PROFESSOR);
	    long escola = teacherRepository.countByPlan(PlanType.ESCOLA);

	    List<Teacher> expired = teacherRepository.findExpiredPlans(LocalDateTime.now());
	    long expiredCount = expired.size();

	    Double monthly = subscriptionRepository.sumMonthlyRevenue(currentMonth, currentYear);
	    Double annual = subscriptionRepository.sumAnnualRevenue(currentYear);

	    List<Teacher> recent = teacherRepository.findRecentRegistrations(
	        PageRequest.of(0, 10));

	    List<Object[]> byMonth = teacherRepository.countRegistrationsByMonth(currentYear);
	    List<MonthlyRegistrationResponse> monthlyReg = byMonth.stream()
	        .map(r -> new MonthlyRegistrationResponse(
	            ((Number) r[0]).intValue(),
	            ((Number) r[1]).longValue()))
	        .collect(Collectors.toList());

	    return new AdminDashboardResponse(
	        total, active, inactive, pro, escola, expiredCount,
	        monthly != null ? monthly : 0.0,
	        annual != null ? annual : 0.0,
	        recent.stream().map(this::toSummary).collect(Collectors.toList()),
	        expired.stream().map(this::toSummary).collect(Collectors.toList()),
	        monthlyReg
	    );
	}

	@Transactional(readOnly = true)
	public List<TeacherSummaryResponse> getAllTeachers() {
	    return teacherRepository.findAll().stream()
	        .map(this::toSummary).collect(Collectors.toList());
	}

	@Transactional
	public void toggleTeacherActive(Long teacherId) {
	    Teacher teacher = teacherRepository.findById(teacherId)
	        .orElseThrow(() -> new BusinessException("Professor não encontrado"));
	    teacher.setActive(!teacher.getActive());
	    teacherRepository.save(teacher);
	}

	private TeacherSummaryResponse toSummary(Teacher t) {
	    return new TeacherSummaryResponse(
	        t.getId(), t.getName(), t.getEmail(),
	        t.getPlan() != null ? t.getPlan().name() : null,
	        t.getActive(), t.getCreatedAt(), t.getPlanExpiresAt());
	}

}
