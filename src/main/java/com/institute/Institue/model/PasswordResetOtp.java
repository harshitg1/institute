package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PasswordResetOtp {
    private String id;
    private String otp;

    public PasswordResetOtp() {}

    public PasswordResetOtp(String id, String otp) { this.id = id; this.otp = otp; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}

