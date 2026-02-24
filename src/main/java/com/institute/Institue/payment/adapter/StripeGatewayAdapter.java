package com.institute.Institue.payment.adapter;

import com.institute.Institue.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Stripe payment gateway adapter.
 *
 * In production, this would use the official Stripe Java SDK
 * (com.stripe:stripe-java). For now, this implements the contract
 * with simulated behavior.
 *
 * To integrate the real SDK:
 * 1. Add dependency: com.stripe:stripe-java:24.x.x
 * 2. Replace the simulated methods with actual Stripe API calls
 * 3. The rest of the application remains unchanged (Adapter Pattern benefit)
 */
@Component
@Slf4j
public class StripeGatewayAdapter implements PaymentGateway {

    @Value("${payment.stripe.secret-key:sk_test_placeholder}")
    private String secretKey;

    @Value("${payment.stripe.webhook-secret:whsec_placeholder}")
    private String webhookSecret;

    @Override
    public PaymentGatewayOrder createOrder(PaymentGatewayRequest request) {
        log.info("Creating Stripe checkout session for amount: {} {}", request.getAmount(), request.getCurrency());

        // --- PRODUCTION CODE (uncomment when Stripe SDK is added) ---
        // Stripe.apiKey = secretKey;
        // SessionCreateParams params = SessionCreateParams.builder()
        // .setMode(SessionCreateParams.Mode.PAYMENT)
        // .addLineItem(SessionCreateParams.LineItem.builder()
        // .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
        // .setCurrency(request.getCurrency().toLowerCase())
        // .setUnitAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
        // .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
        // .setName(request.getDescription())
        // .build())
        // .build())
        // .setQuantity(1L)
        // .build())
        // .setSuccessUrl("https://yourdomain.com/success")
        // .setCancelUrl("https://yourdomain.com/cancel")
        // .build();
        // Session session = Session.create(params);

        // --- SIMULATED ---
        String simulatedSessionId = "cs_" + UUID.randomUUID().toString().substring(0, 24);

        return PaymentGatewayOrder.builder()
                .providerOrderId(simulatedSessionId)
                .paymentLink("https://checkout.stripe.com/pay/" + simulatedSessionId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("created")
                .build();
    }

    @Override
    public PaymentGatewayStatus verifyPayment(String providerOrderId) {
        log.info("Verifying Stripe payment for session: {}", providerOrderId);

        // --- PRODUCTION: Retrieve session from Stripe ---
        // Session session = Session.retrieve(providerOrderId);
        // String status = session.getPaymentStatus();

        // --- SIMULATED ---
        return PaymentGatewayStatus.builder()
                .providerOrderId(providerOrderId)
                .providerPaymentId("pi_" + UUID.randomUUID().toString().substring(0, 24))
                .status("captured")
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, String> headers, String body) {
        log.info("Verifying Stripe webhook signature");

        // --- PRODUCTION CODE ---
        // String sigHeader = headers.get("stripe-signature");
        // Webhook.constructEvent(body, sigHeader, webhookSecret);

        // --- SIMULATED ---
        return true;
    }

    @Override
    public WebhookResult parseWebhook(String body) {
        log.info("Parsing Stripe webhook");

        // --- PRODUCTION: Parse the Stripe Event ---
        // Event event = Webhook.constructEvent(body, sigHeader, webhookSecret);
        // Session session = (Session)
        // event.getDataObjectDeserializer().getObject().get();

        // --- SIMULATED ---
        return WebhookResult.builder()
                .event("checkout.session.completed")
                .providerOrderId("cs_simulated")
                .providerPaymentId("pi_simulated")
                .status("captured")
                .build();
    }

    @Override
    public String getProviderName() {
        return "STRIPE";
    }
}
