package com.saas.professor.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.saas.professor.service.SubscriptionService;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final SubscriptionService subscriptionService;

    public WebhookController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<Void> handleMercadoPago(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String dataId) {

        try {
            // MP envia tipo via query param ou body
            String eventType = type != null ? type
                    : (body != null ? (String) body.get("type") : null);
            String id = dataId != null ? dataId
                    : (body != null && body.get("data") instanceof Map<?, ?> d
                        ? (String) d.get("id") : null);

            if (eventType == null || id == null) return ResponseEntity.ok().build();

            switch (eventType) {
                case "subscription_preapproval" -> {
                    subscriptionService.processPreapproval(id);
                }
                case "subscription_authorized_payment" -> {
                    // Renovação mensal — renova planExpiresAt
                    subscriptionService.processPreapproval(id);
                }
                default -> { /* ignora outros eventos */ }
            }
        } catch (Exception e) {
            // Retorna 200 para MP não reenviar infinitamente
            System.err.println("Webhook error: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}