package com.saas.professor.service;

import java.time.LocalDateTime;
import java.util.Map;

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
        Subscription subscription = subscriptionRepository
                .findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Nenhuma assinatura ativa encontrada"));
        return toResponse(subscription);
    }

    public CheckoutResponse createCheckout(PlanType plan, Teacher teacher) {
        String url = mercadoPagoService.createSubscriptionCheckout(teacher.getId(), plan);
        return new CheckoutResponse(url);
    }

    /**
     * Chamado pelo WebhookController quando MP notifica nova assinatura.
     */
    @Transactional
    public void processPreapproval(String preapprovalId) {
        try {
            Map<String, Object> preapproval = mercadoPagoService.getPreapproval(preapprovalId);

            String status = (String) preapproval.get("status");
            if (!"authorized".equals(status)) return;

            String externalRef = (String) preapproval.get("external_reference");
            if (externalRef == null || !externalRef.contains("|")) return;

            String[] parts = externalRef.split("\\|");
            Long teacherId = Long.parseLong(parts[0]);
            PlanType plan = PlanType.valueOf(parts[1]);

            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new BusinessException("Professor não encontrado"));

            // Cancela assinatura anterior se existir
            subscriptionRepository.findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
                    .ifPresent(old -> {
                        old.setStatus(SubscriptionStatus.CANCELLED);
                        subscriptionRepository.save(old);
                    });

            // Cria nova assinatura
            Subscription subscription = new Subscription(
                    teacher, plan, preapprovalId,
                    mercadoPagoService.getPlanPrice(plan).doubleValue()
            );
            subscriptionRepository.save(subscription);

            // Ativa plano do professor
            teacher.setPlan(plan);
            teacher.setPlanExpiresAt(LocalDateTime.now().plusMonths(1));
            teacher.setActive(true);
            teacherRepository.save(teacher);

        } catch (Exception e) {
            throw new BusinessException("Erro ao processar assinatura: " + e.getMessage());
        }
    }

    /**
     * Cancelamento pelo próprio usuário via página de perfil.
     * Cancela no MP + marca como cancelada no banco.
     * NÃO zera planExpiresAt — acesso continua até o fim do período pago.
     */
    @Transactional
    public void cancelByTeacher(Teacher teacher) {
        Subscription subscription = subscriptionRepository
                .findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Nenhuma assinatura ativa encontrada"));

        // Cancela no Mercado Pago para parar cobranças futuras
        try {
            mercadoPagoService.cancelPreapproval(subscription.getMercadoPagoPaymentId());
        } catch (Exception e) {
            // Log mas não impede o cancelamento local
            System.err.println("Erro ao cancelar no MP: " + e.getMessage());
        }

        // Marca como cancelada no banco
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        // NÃO zera planExpiresAt — acesso continua até a data já salva
        // Quando planExpiresAt passar, canAccess() retorna false automaticamente
    }

    /**
     * Chamado pelo webhook do MP quando cancelamento vem de lá.
     * Mesmo comportamento — mantém acesso até fim do período.
     */
    @Transactional
    public void cancelSubscription(String preapprovalId) {
        subscriptionRepository.findByMercadoPagoPaymentId(preapprovalId)
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(sub);
                    // NÃO zera planExpiresAt — acesso mantido até fim do período
                });
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getPlan(), s.getStatus(),
                s.getAmountPaid(), s.getStartsAt(), s.getExpiresAt());
    }
}