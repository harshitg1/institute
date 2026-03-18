package com.institute.Institue.payment;
import java.util.Map;

/**
 * Core payment gateway interface — the "Port" in our Adapter Pattern.
 *
 * Each payment provider (Razorpay, Stripe, etc.) implements this interface.
 * The PaymentService only interacts with this contract, never with
 * provider-specific classes — making it trivial to swap or add providers.
 */
public interface PaymentGateway {

    /**
     * Create a payment order with the provider.
     *
     * @param request contains amount, currency, description, metadata
     * @return provider-specific order details
     */
    PaymentGatewayOrder createOrder(PaymentGatewayRequest request);

    /**
     * Verify a payment with the provider using the provider order ID.
     *
     * @param providerOrderId the order ID returned by the provider
     * @return current payment status
     */
    PaymentGatewayStatus verifyPayment(String providerOrderId);

    /**
     * Verify the authenticity of a webhook callback from the provider.
     *
     * @param headers HTTP headers from the webhook request
     * @param body    raw request body
     * @return true if the webhook signature is valid
     */
    boolean verifyWebhookSignature(Map<String, String> headers, String body);

    /**
     * Parse the webhook body to extract order and payment details.
     *
     * @param body raw webhook body
     * @return parsed webhook result
     */
    WebhookResult parseWebhook(String body);

    /**
     * Get the name of this payment provider (e.g., "RAZORPAY", "STRIPE").
     */
    String getProviderName();
}
