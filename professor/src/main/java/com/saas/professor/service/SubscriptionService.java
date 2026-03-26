package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saas.professor.dto.response.CheckoutResponse;
import com.saas.professor.dto.response.SubscriptionResponse;
import com.saas.professor.entity.Subscription;
import com.saas.professor.entity.Teacher;
import com.saas.professor.enums.PlanType;
import com.saas.professor.enums.SubscriptionStatus;
import com.saas.professor.exceptions.BusinessException;
import com.saas.professor.repository.SubscriptionRepository;
import com.saas.professor.repository.TeacherRepository;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final TeacherRepository teacherRepository;
    private final MercadoPagoService mercadoPagoService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               TeacherRepository teacherRepository,
                               MercadoPagoService mercadoPagoService) {
        this.subscriptionRepository = subscriptionRepository;
        this.teacherRepository = teacherRepository;
        this.mercadoPagoService = mercadoPagoService;
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse findCurrent(Teacher teacher) {
        return subscriptionRepository
            .findFirstByTeacherOrderByCreatedAtDesc(teacher)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException("Assine um plano para continuar"));
    }

    public CheckoutResponse createCheckout(PlanType plan, Teacher teacher) {
        String checkoutUrl = mercadoPagoService.createSubscriptionCheckout(teacher.getId(), plan);
        log.info("Checkout criado para {} | plan: {}", teacher.getEmail(), plan);
        return new CheckoutResponse(checkoutUrl);
    }

    /**
     * ✅ WEBHOOK PREAPPROVAL (assinatura)
     */
    @Transactional
    public void processPreapproval(String preapprovalId) {
        log.info("[PREAPPROVAL] Processando: {}", preapprovalId);
        
        var preapproval = mercadoPagoService.getPreapprovalPlan(preapprovalId);
        if (preapproval == null) {
            log.warn("Preapproval {} nao encontrado", preapprovalId);
            return;
        }

        String status = (String) preapproval.get("status");
        String externalRef = (String) preapproval.get("external_reference");

        log.info("Preapproval {} | status: {} | ref: {}", preapprovalId, status, externalRef);

        // ✅ Aceita pending + authorized/active
        if (!List.of("authorized", "active", "pending").contains(status)) {
            log.warn("Status invalido: {} | ignorando", status);
            return;
        }

        if (externalRef == null || externalRef.trim().isEmpty()) {
            log.warn("external_reference vazio");
            return;
        }

        try {
            Long teacherId = Long.parseLong(externalRef.trim());
            activateSubscription(teacherId, preapprovalId, PlanType.PRO_PROFESSOR);
            log.info("Preapproval processado: {}", preapprovalId);
        } catch (NumberFormatException e) {
            log.error("external_reference invalido: {}", externalRef);
        }
    }

    /**
     * ✅ WEBHOOK PAYMENT
     */
    @Transactional
    public void processPayment(String paymentId) {
        log.info("[PAYMENT] Processando: {}", paymentId);
        
        var payment = mercadoPagoService.getPreapprovalPlan(paymentId);
        if (payment == null) {
            log.warn("Payment {} nao encontrado", paymentId);
            return;
        }

        String status = (String) payment.get("status");
        log.info("Payment {} | status: {}", paymentId, status);

        if (!"approved".equals(status)) {
            log.warn("Payment nao aprovado: {}", status);
            return;
        }

        // Extrai preapproval_id
        @SuppressWarnings("unchecked")
        Object preapprovalIdObj = payment.get("preapproval_plan_id");
        if (preapprovalIdObj != null) {
            log.info("Payment {} tem preapproval: {}", paymentId, preapprovalIdObj);
            processPreapproval(preapprovalIdObj.toString());
            return;
        }

        // Fallback external_reference
        String externalRef = (String) payment.get("external_reference");
        if (externalRef != null && !externalRef.trim().isEmpty()) {
            try {
                Long teacherId = Long.parseLong(externalRef.trim());
                activateSubscription(teacherId, paymentId, PlanType.PRO_PROFESSOR);
            } catch (NumberFormatException e) {
                log.error("external_reference invalido no payment: {}", externalRef);
            }
        }
    }

    /**
     * ✅ CANCELAMENTO USUÁRIO
     */
    @Transactional
    public void cancelByTeacher(Teacher teacher) {
        var activeSub = subscriptionRepository.findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new BusinessException("Nenhuma assinatura ativa"));

        try {
            mercadoPagoService.cancelPreapproval(activeSub.getMercadoPagoPaymentId());
        } catch (Exception e) {
            log.error("Erro cancelar MP: {}", e.getMessage());
        }

        activeSub.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(activeSub);
        log.info("Assinatura cancelada: {}", teacher.getEmail());
    }

    /**
     * ✅ ATIVA PLANO (lógica central)
     */
    private void activateSubscription(Long teacherId, String mpId, PlanType plan) {
        var teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new BusinessException("Professor nao encontrado: " + teacherId));

        log.info("Ativando plano {} para {}", plan, teacher.getEmail());

        // ✅ REMOVE TRIAL quando ativa plano pago!
        teacher.setTrialEndsAt(null);  

        // Cancela assinatura anterior
        subscriptionRepository.findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
            .ifPresent(old -> {
                old.setStatus(SubscriptionStatus.CANCELLED);
                subscriptionRepository.save(old);
            });

        // Cria nova assinatura
        double amount = mercadoPagoService.getPlanPrice(plan).doubleValue();
        var subscription = new Subscription(teacher, plan, mpId, amount);
        subscriptionRepository.save(subscription);

        // Ativa professor
        teacher.setPlan(plan);
        teacher.setPlanExpiresAt(LocalDateTime.now().plusMonths(1));
        teacher.setActive(true);
        teacherRepository.save(teacher);

        log.info("✅ Plano ativado SEM trial | expira: {}", teacher.getPlanExpiresAt());
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
            s.getId(), s.getPlan(), s.getStatus(),
            s.getAmountPaid(), s.getStartsAt(), s.getExpiresAt()
        );
    }
}