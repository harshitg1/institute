package com.institute.Institue.service;

public interface OtpService {
    String generateOtp(String userId);
    boolean validateOtp(String userId, String otp);
}

