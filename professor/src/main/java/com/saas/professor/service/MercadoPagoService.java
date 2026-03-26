package com.saas.professor.service;

import java.math.BigDecimal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.saas.professor.enums.PlanType;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);
    
    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mercadopago.plan-id.pro-professor}")
    private String proProfessorPlanId;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createSubscriptionCheckout(Long teacherId, PlanType plan) {
        String planId = getPlanId(plan);
        
        var headers = createHeaders();
        var body = createSubscriptionBody(teacherId, plan, planId);

        try {
            log.info("🛒 Criando checkout para teacherId: {} | plan: {}", teacherId, plan);
            
            var response = restTemplate.postForEntity(
                "https://api.mercadopago.com/preapproval",
                new HttpEntity<>(body, headers),
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                String initPoint = (String) ((Map<String, Object>) response.getBody()).get("init_point");
                log.info("✅ Checkout criado: {}", initPoint != null ? initPoint.substring(0, 50) + "..." : "null");
                return initPoint;
            }

            throw new RuntimeException("MP não retornou init_point válido");
            
        } catch (Exception e) {
            log.error("❌ Erro createSubscriptionCheckout: {}", e.getMessage(), e);
            return createFallbackUrl(planId, teacherId);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPreapprovalPlan(String preapprovalId) {
        if (preapprovalId == null || preapprovalId.trim().isEmpty()) {
            log.warn("⚠️ Preapproval ID vazio");
            return Map.of();
        }

        var headers = createHeaders();
        
        try {
            log.debug("🔍 GET /preapproval/{}", preapprovalId);
            
            var response = restTemplate.exchange(
                "https://api.mercadopago.com/preapproval/" + preapprovalId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );

            var body = response.getBody();
            if (body != null) {
                String status = (String) body.get("status");
                log.info("✅ Preapproval {} | status: {}", preapprovalId, status);
            }
            return body != null ? body : Map.of();
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("⚠️ Preapproval {} NÃO ENCONTRADO (404)", preapprovalId);
                return null;
            }
            log.error("❌ Erro getPreapproval {}: {}", preapprovalId, e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("❌ Erro getPreapproval {}: {}", preapprovalId, e.getMessage(), e);
            return null;
        }
    }

    public void cancelPreapproval(String preapprovalId) {
        var headers = createHeaders();
        var body = Map.of("status", "cancelled");
        
        try {
            restTemplate.exchange(
                "https://api.mercadopago.com/preapproval/" + preapprovalId,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Void.class
            );
            log.info("✅ Preapproval cancelado: {}", preapprovalId);
        } catch (Exception e) {
            log.error("❌ Erro cancelPreapproval {}: {}", preapprovalId, e.getMessage());
            throw new RuntimeException("Erro ao cancelar preapproval: " + e.getMessage());
        }
    }

    // ✅ UTILITÁRIOS
    private HttpHeaders createHeaders() {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Map<String, Object> createSubscriptionBody(Long teacherId, PlanType plan, String planId) {
        var autoRecurring = Map.of(
            "frequency", 1,
            "frequency_type", "months",
            "transaction_amount", getPlanPrice(plan).doubleValue(),
            "currency_id", "BRL"
        );

        return Map.of(
            "reason", getPlanTitle(plan),
            "preapproval_plan_id", planId,
            "external_reference", teacherId.toString(),
            "back_url", frontendUrl + "/plans/success",
            "success_url", frontendUrl + "/plans/success",
            "failure_url", frontendUrl + "/plans/failure",
            "auto_recurring", autoRecurring
        );
    }

    private String createFallbackUrl(String planId, Long teacherId) {
        return "https://www.mercadopago.com.br/subscriptions/checkout?" +
               "preapproval_plan_id=" + planId +
               "&external_reference=" + teacherId +
               "&notification_url=https://api.notafacil.app.br/webhooks/mercadopago";
    }

    // ✅ PLANOS
    public BigDecimal getPlanPrice(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR -> BigDecimal.valueOf(39.90);
            case ESCOLA -> BigDecimal.valueOf(250.00);
            default -> BigDecimal.ZERO;
        };
    }

    public String getPlanTitle(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR -> "NotaFacil Pro — Mensal";
            case ESCOLA -> "NotaFacil Escola — Mensal";
            default -> "NotaFacil Free";
        };
    }

    private String getPlanId(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR, ESCOLA -> proProfessorPlanId; // Configurar por plano depois
            default -> proProfessorPlanId;
        };
    }
}