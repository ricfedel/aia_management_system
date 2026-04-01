package it.grandimolini.aia.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String ruolo;
    private String tokenType = "Bearer";

    public AuthResponse(String accessToken, String refreshToken, Long userId, String username, String ruolo) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.ruolo = ruolo;
        this.tokenType = "Bearer";
    }
}
