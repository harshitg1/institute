package com.institute.Institue.payment.adapter;

import com.institute.Institue.payment.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Razorpay payment gateway adapter.
 *
 * In production, this would use the official Razorpay Java SDK
 * (com.razorpay:razorpay-java). For now, this implements the contract
 * with simulated behavior that can be swapped with real SDK calls.
 *
 * To integrate the real SDK:
 * 1. Add dependency: com.razorpay:razorpay-java:1.4.6
 * 2. Replace the simulated methods with actual RazorpayClient calls
 * 3. The rest of the application remains unchanged (Adapter Pattern benefit)
 */
@Component
@Slf4j
public class RazorpayGatewayAdapter implements PaymentGateway {

    @Value("${payment.razorpay.key-id:rzp_test_placeholder}")
    private String keyId;

    @Value("${payment.razorpay.key-secret:rzp_secret_placeholder}")
    private String keySecret;

    @Override
    public PaymentGatewayOrder createOrder(PaymentGatewayRequest request) {
        log.info("Creating Razorpay order for amount: {} {}", request.getAmount(), request.getCurrency());

        // --- PRODUCTION CODE (uncomment when Razorpay SDK is added) ---
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // JSONObject orderRequest = new JSONObject();
        // orderRequest.put("amount",
        // request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
        // orderRequest.put("currency", request.getCurrency());
        // orderRequest.put("receipt", request.getReceiptId());
        // Order order = client.orders.create(orderRequest);
        // return PaymentGatewayOrder.builder()
        // .providerOrderId(order.get("id"))
        // .paymentLink("https://api.razorpay.com/v1/checkout/embedded")
        // .amount(request.getAmount())
        // .currency(request.getCurrency())
        // .status("created")
        // .build();

        // --- SIMULATED FOR DEVELOPMENT ---
        String simulatedOrderId = "order_" + UUID.randomUUID().toString().substring(0, 14);

        return PaymentGatewayOrder.builder()
                .providerOrderId(simulatedOrderId)
                .paymentLink("https://rzp.io/i/" + simulatedOrderId)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status("created")
                .build();
    }

    @Override
    public PaymentGatewayStatus verifyPayment(String providerOrderId) {
        log.info("Verifying Razorpay payment for order: {}", providerOrderId);

        // --- PRODUCTION: Fetch order from Razorpay API and check status ---
        // RazorpayClient client = new RazorpayClient(keyId, keySecret);
        // Order order = client.orders.fetch(providerOrderId);
        // String status = order.get("status");

        // --- SIMULATED ---
        return PaymentGatewayStatus.builder()
                .providerOrderId(providerOrderId)
                .providerPaymentId("pay_" + UUID.randomUUID().toString().substring(0, 14))
                .status("captured")
                .build();
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, String> headers, String body) {
        log.info("Verifying Razorpay webhook signature");

        // --- PRODUCTION CODE ---
        // String webhookSignature = headers.get("x-razorpay-signature");
        // String webhookSecret = this.webhookSecret;
        // return Utils.verifyWebhookSignature(body, webhookSignature, webhookSecret);

        // --- SIMULATED: Always returns true in dev ---
        String signature = headers.getOrDefault("x-razorpay-signature", "");
        if (signature.isEmpty()) {
            log.warn("No Razorpay webhook signature found — accepting in dev mode");
        }
        return true;
    }

    @Override
    public WebhookResult parseWebhook(String body) {
        log.info("Parsing Razorpay webhook");

        // --- PRODUCTION: Parse the JSON body using Jackson/Gson ---
        // JSONObject json = new JSONObject(body);
        // String event = json.getString("event");
        // JSONObject payment =
        // json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");

        // --- SIMULATED ---
        return WebhookResult.builder()
                .event("payment.captured")
                .providerOrderId("order_simulated")
                .providerPaymentId("pay_simulated")
                .status("captured")
                .build();
    }

    @Override
    public String getProviderName() {
        return "RAZORPAY";
    }
}
