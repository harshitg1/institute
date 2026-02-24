package com.institute.Institue.payment;

import com.institute.Institue.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PaymentGatewayFactory Tests")
class PaymentGatewayFactoryTest {

    private PaymentGatewayFactory factory;
    private PaymentGateway razorpayGateway;
    private PaymentGateway stripeGateway;

    @BeforeEach
    void setUp() {
        razorpayGateway = mock(PaymentGateway.class);
        stripeGateway = mock(PaymentGateway.class);
        when(razorpayGateway.getProviderName()).thenReturn("RAZORPAY");
        when(stripeGateway.getProviderName()).thenReturn("STRIPE");

        factory = new PaymentGatewayFactory(List.of(razorpayGateway, stripeGateway));
    }

    @Test
    @DisplayName("should resolve Razorpay gateway")
    void resolve_razorpay() {
        PaymentGateway gw = factory.getGateway("RAZORPAY");
        assertSame(razorpayGateway, gw);
    }

    @Test
    @DisplayName("should resolve Stripe gateway (case insensitive)")
    void resolve_stripe_caseInsensitive() {
        PaymentGateway gw = factory.getGateway("stripe");
        assertSame(stripeGateway, gw);
    }

    @Test
    @DisplayName("should throw for unsupported provider")
    void resolve_unsupported() {
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> factory.getGateway("PAYPAL"));
        assertTrue(ex.getMessage().contains("Unsupported payment provider"));
        assertTrue(ex.getMessage().contains("PAYPAL"));
    }
}
