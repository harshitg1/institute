package com.institute.Institue.payment;

import com.institute.Institue.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for resolving payment gateway adapters by provider name.
 *
 * Spring auto-wires all PaymentGateway implementations into this factory.
 * Adding a new provider is as simple as:
 *   1. Create a new @Component implementing PaymentGateway
 *   2. It's automatically registered here — zero config changes.
 */
@Component
public class PaymentGatewayFactory {

    private final Map<String, PaymentGateway> gatewayMap = new HashMap<>();

    /**
     * Spring injects ALL beans that implement PaymentGateway.
     * We index them by provider name for O(1) lookup.
     */
    public PaymentGatewayFactory(List<PaymentGateway> gateways) {
        for (PaymentGateway gateway : gateways) {
            gatewayMap.put(gateway.getProviderName().toUpperCase(), gateway);
        }
    }

    /**
     * Get the gateway adapter for the given provider.
     *
     * @param providerName e.g., "RAZORPAY", "STRIPE"
     * @return the matching PaymentGateway implementation
     * @throws BadRequestException if the provider is not supported
     */
    public PaymentGateway getGateway(String providerName) {
        PaymentGateway gateway = gatewayMap.get(providerName.toUpperCase());
        if (gateway == null) {
            throw new BadRequestException(
                    "Unsupported payment provider: " + providerName +
                    ". Supported: " + gatewayMap.keySet(),
                    "UNSUPPORTED_PROVIDER"
            );
        }
        return gateway;
    }
}
