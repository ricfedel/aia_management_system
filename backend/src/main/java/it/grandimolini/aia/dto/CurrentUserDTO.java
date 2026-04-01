package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private User.Ruolo ruolo;
    private Set<StabilimentoInfo> stabilimenti;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StabilimentoInfo {
        private Long id;
        private String nome;
        private String citta;
    }

    public static CurrentUserDTO fromEntity(User user) {
        return CurrentUserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .ruolo(user.getRuolo())
                .stabilimenti(user.getStabilimenti().stream()
                        .map(s -> StabilimentoInfo.builder()
                                .id(s.getId())
                                .nome(s.getNome())
                                .citta(s.getCitta())
                                .build())
                        .collect(Collectors.toSet()))
                .build();
    }
}
