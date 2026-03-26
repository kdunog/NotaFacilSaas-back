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
        System.out.println("BODY: " + bodyRaw);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(bodyRaw);
            
            // ✅ NOVO FORMATO MP
            String type = jsonNode.path("type").asText();
            String paymentId = jsonNode.path("data").path("id").asText();
            
            System.out.println("📋 TYPE: " + type);
            System.out.println("💳 ID: " + paymentId);
            
            if (paymentId != null && !paymentId.isEmpty()) {
                if ("payment".equals(type) || "subscription_authorized_payment".equals(type)) {
                    System.out.println("✅ PROCESSANDO: " + paymentId);
                    subscriptionService.processPayment(paymentId);
                } else {
                    System.out.println("⚠️  Type ignorado: " + type);
                }
            }
            
            return ResponseEntity.ok("OK");
            
        } catch (Exception e) {
            System.err.println("💥 WEBHOOK ERROR: " + e.getMessage());
            return ResponseEntity.ok("OK");
        }
    }
}