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
            @RequestParam(required = false) String dataId,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id) {
        try {
            System.out.println("WEBHOOK RECEBIDO ==================");
            System.out.println("query param type: " + type);
            System.out.println("query param dataId: " + dataId);
            System.out.println("query param topic: " + topic);
            System.out.println("query param id: " + id);
            System.out.println("body: " + body);

            // Suporte ao formato IPN antigo (topic + id)
            if (topic != null && id != null) {
                System.out.println("WEBHOOK - formato IPN: topic=" + topic + " id=" + id);
                if ("preapproval".equals(topic) || "merchant_order".equals(topic)) {
                    subscriptionService.processPreapproval(id);
                }
                return ResponseEntity.ok().build();
            }

            // Formato novo (type + data.id)
            String eventType = type != null ? type
                    : (body != null ? (String) body.get("type") : null);
            String eventId = dataId != null ? dataId
                    : (body != null && body.get("data") instanceof Map<?, ?> d
                        ? (String) d.get("id") : null);

            System.out.println("WEBHOOK - eventType: " + eventType + " eventId: " + eventId);

            if (eventType == null || eventId == null) {
                System.out.println("WEBHOOK - eventType ou id nulo, ignorando");
                return ResponseEntity.ok().build();
            }

            switch (eventType) {
                case "subscription_preapproval" -> subscriptionService.processPreapproval(eventId);
                case "subscription_authorized_payment" -> subscriptionService.processPreapproval(eventId);
                case "preapproval" -> subscriptionService.processPreapproval(eventId);
                case "payment" -> System.out.println("WEBHOOK - pagamento avulso ignorado: " + eventId);
                default -> System.out.println("WEBHOOK - tipo nao tratado: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("WEBHOOK ERROR: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}