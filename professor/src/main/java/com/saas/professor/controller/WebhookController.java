package com.saas.professor.controller;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.saas.professor.service.SubscriptionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final SubscriptionService subscriptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<String> handleMercadoPago(
            @RequestBody String bodyRaw,
            @RequestHeader(value = "x-signature", required = false) String signature) {
        
        log.info("🔥 WEBHOOK RECEBIDO - Signature: {}", signature != null ? signature.substring(0, 20) + "..." : "null");
        log.debug("📄 BODY: {}", bodyRaw);

        try {
            JsonNode payload = objectMapper.readTree(bodyRaw);
            String type = payload.path("type").asText();
            String dataId = payload.path("data").path("id").asText();

            log.info("📋 TYPE: {} | ID: {}", type, dataId);

            switch (type) {
                case "payment" -> {
                    if (!dataId.isEmpty()) {
                        log.info("💳 PROCESSANDO PAYMENT: {}", dataId);
                        subscriptionService.processPayment(dataId);
                    }
                }
                case "subscription_authorized_payment" -> {
                    if (!dataId.isEmpty()) {
                        log.info("🔄 PROCESSANDO SUB_AUTH_PAYMENT: {}", dataId);
                        subscriptionService.processPreapproval(dataId);
                    }
                }
                case "merchant_order" -> {
                    log.info("📦 MERCHANT_ORDER: {} (ignorado)", dataId);
                }
                case "preapproval_plan" -> {
                    log.info("📋 PREAPPROVAL_PLAN: {} (processando)", dataId);
                    subscriptionService.processPreapproval(dataId);
                }
                default -> log.warn("⚠️  TIPO IGNORADO: {}", type);
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("💥 WEBHOOK ERROR: {}", e.getMessage(), e);
            return ResponseEntity.ok("OK"); // ✅ Sempre OK pro MP
        }
    }
}