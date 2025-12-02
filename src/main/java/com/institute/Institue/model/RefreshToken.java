package com.institute.Institue.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshToken {
    private String id;
    private String token;

    public RefreshToken() {}

    public RefreshToken(String id, String token) { this.id = id; this.token = token; }

}

