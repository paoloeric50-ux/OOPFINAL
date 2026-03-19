package com.motorph.payroll.dto;

public class TokenResponseDto {
    private String accessToken;
    private String tokenType = "bearer";
    private UserResponseDto user;

    public TokenResponseDto() {}
    public TokenResponseDto(String accessToken, UserResponseDto user) {
        this.accessToken = accessToken; this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public UserResponseDto getUser() { return user; }
    public void setUser(UserResponseDto user) { this.user = user; }
}
