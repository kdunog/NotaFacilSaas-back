package com.saas.professor.service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.saas.professor.enums.PlanType;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${mercadopago.plan-id.pro-professor}")
    private String proProfessorPlanId;

    @Value("${app.url}")
    private String appUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Cria um preapproval via API com external_reference=teacherId
     * e retorna o init_point (URL de checkout personalizada).
     */
    @SuppressWarnings("unchecked")
    public String createSubscriptionCheckout(Long teacherId, PlanType plan) {
        String planId = getPlanId(plan);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("preapproval_plan_id", planId);
        body.put("external_reference", String.valueOf(teacherId));
        body.put("back_url", frontendUrl + "/plans/success");

        try {
            Map<String, Object> response = restTemplate.postForObject(
                "https://api.mercadopago.com/preapproval",
                new HttpEntity<>(body, headers),
                Map.class
            );

            System.out.println("MP preapproval criado: " + response);

            if (response != null && response.containsKey("init_point")) {
                return (String) response.get("init_point");
            }
            throw new RuntimeException("MP nao retornou init_point: " + response);

        } catch (Exception e) {
            System.err.println("Erro ao criar preapproval via API: " + e.getMessage());
            // Fallback para URL direta com notification_url
            return "https://www.mercadopago.com.br/subscriptions/checkout?preapproval_plan_id="
                    + planId
                    + "&external_reference=" + teacherId
                    + "&notification_url=https://api.notafacil.app.br/webhooks/mercadopago";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPreapproval(String preapprovalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            return restTemplate.exchange(
                "https://api.mercadopago.com/preapproval/" + preapprovalId,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar preapproval: " + e.getMessage());
        }
    }

    public void cancelPreapproval(String preapprovalId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = new HashMap<>();
        body.put("status", "cancelled");
        try {
            restTemplate.exchange(
                "https://api.mercadopago.com/preapproval/" + preapprovalId,
                org.springframework.http.HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cancelar preapproval no MP: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPayment(String paymentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        try {
            return restTemplate.exchange(
                "https://api.mercadopago.com/v1/payments/" + paymentId,
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            ).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar pagamento: " + e.getMessage());
        }
    }

    public BigDecimal getPlanPrice(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR -> new BigDecimal("39.90");
            case ESCOLA -> new BigDecimal("250.00");
            default -> BigDecimal.ZERO;
        };
    }

    public String getPlanTitle(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR -> "NotaFacil Pro — Assinatura Mensal";
            case ESCOLA -> "NotaFacil Escola — Assinatura Mensal";
            default -> "NotaFacil Free";
        };
    }

    private String getPlanId(PlanType plan) {
        return switch (plan) {
            case PRO_PROFESSOR -> proProfessorPlanId;
            default -> proProfessorPlanId;
        };
    }
}