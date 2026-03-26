package com.saas.professor.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.saas.professor.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.*;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<String> handleMercadoPago(@RequestBody String bodyRaw) {
        System.out.println("🔥 WEBHOOK RECEBIDO ==================");
        System.out.println("BODY RAW: " + bodyRaw);
        
        try {
            // ✅ CORREÇÃO: Lê JSON do BODY (formato correto do MP)
            JsonNode jsonNode = objectMapper.readTree(bodyRaw);
            
            String topic = jsonNode.path("topic").asText();
            JsonNode dataNode = jsonNode.path("data");
            String paymentId = dataNode.path("id").asText();
            
            System.out.println("📋 TOPIC: " + topic);
            System.out.println("💳 PAYMENT ID: " + paymentId);
            
            // ✅ PROCESSA PAGAMENTO
            if ("payment".equals(topic) && paymentId != null) {
                System.out.println("✅ PROCESSANDO PAGAMENTO: " + paymentId);
                subscriptionService.processPayment(paymentId);
                System.out.println("✅ PAGAMENTO PROCESSADO!");
            } else {
                System.out.println("⚠️  Topic ignorado: " + topic);
            }
            
            return ResponseEntity.ok("OK"); // ← HTTP 200 OBRIGATÓRIO
            
        } catch (Exception e) {
            System.err.println("💥 ERRO WEBHOOK: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("OK"); // ← MESMO COM ERRO, 200!
        }
    }
}