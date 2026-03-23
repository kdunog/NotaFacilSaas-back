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
            String externalRef = (String) preapproval.get("external_reference");
            System.out.println("WEBHOOK - preapprovalId: " + preapprovalId);
            System.out.println("WEBHOOK - status: " + status);
            System.out.println("WEBHOOK - external_reference: " + externalRef);
            System.out.println("WEBHOOK - full payload: " + preapproval);

            // Aceita authorized ou active (MP usa ambos dependendo do plano)
            if (!"authorized".equals(status) && !"active".equals(status)) {
                System.out.println("WEBHOOK - status nao aceito, ignorando: " + status);
                return;
            }

            if (externalRef == null || externalRef.isBlank()) {
                System.out.println("WEBHOOK - external_reference vazio, ignorando");
                return;
            }

            Long teacherId = Long.parseLong(externalRef.trim());
            PlanType plan = PlanType.PRO_PROFESSOR;

            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new BusinessException("Professor não encontrado: " + teacherId));

            System.out.println("WEBHOOK - ativando plano para professor: " + teacher.getEmail());

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

            System.out.println("WEBHOOK - plano ativado com sucesso para: " + teacher.getEmail());

        } catch (Exception e) {
            System.err.println("WEBHOOK ERROR: " + e.getMessage());
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

    /**
     * Chamado quando o IPN envia topic=payment.
     * Busca o pagamento, extrai o preapproval_id e processa.
     */
    @Transactional
    public void processPayment(String paymentId) {
        try {
            System.out.println("PAYMENT - processando pagamento: " + paymentId);
            Map<String, Object> payment = mercadoPagoService.getPayment(paymentId);
            System.out.println("PAYMENT - payload: " + payment);

            String status = (String) payment.get("status");
            System.out.println("PAYMENT - status: " + status);

            if (!"approved".equals(status)) {
                System.out.println("PAYMENT - nao aprovado, ignorando: " + status);
                return;
            }

            // Tenta pegar preapproval_id do pagamento
            Object preapprovalIdObj = payment.get("preapproval_id");
            if (preapprovalIdObj != null) {
                System.out.println("PAYMENT - preapproval_id encontrado: " + preapprovalIdObj);
                processPreapproval(preapprovalIdObj.toString());
                return;
            }

            // Tenta pegar external_reference direto do pagamento
            String externalRef = (String) payment.get("external_reference");
            System.out.println("PAYMENT - external_reference: " + externalRef);
            if (externalRef != null && !externalRef.isBlank()) {
                Long teacherId = Long.parseLong(externalRef.trim());
                activatePlan(teacherId, paymentId);
            } else {
                System.out.println("PAYMENT - sem external_reference nem preapproval_id, ignorando");
            }

        } catch (Exception e) {
            System.err.println("PAYMENT ERROR: " + e.getMessage());
            throw new BusinessException("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    private void activatePlan(Long teacherId, String paymentId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("Professor nao encontrado: " + teacherId));

        System.out.println("PAYMENT - ativando plano para: " + teacher.getEmail());

        subscriptionRepository.findByTeacherAndStatus(teacher, SubscriptionStatus.ACTIVE)
                .ifPresent(old -> {
                    old.setStatus(SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(old);
                });

        PlanType plan = PlanType.PRO_PROFESSOR;
        Subscription subscription = new Subscription(
                teacher, plan, paymentId,
                mercadoPagoService.getPlanPrice(plan).doubleValue()
        );
        subscriptionRepository.save(subscription);

        teacher.setPlan(plan);
        teacher.setPlanExpiresAt(LocalDateTime.now().plusMonths(1));
        teacher.setActive(true);
        teacherRepository.save(teacher);

        System.out.println("PAYMENT - plano ativado com sucesso para: " + teacher.getEmail());
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getPlan(), s.getStatus(),
                s.getAmountPaid(), s.getStartsAt(), s.getExpiresAt());
    }
}