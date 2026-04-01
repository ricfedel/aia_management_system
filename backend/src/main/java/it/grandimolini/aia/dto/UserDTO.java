package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private User.Ruolo ruolo;
    private Boolean attivo;
    private LocalDateTime createdAt;
    private Set<Long> stabilimentiIds;

    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .ruolo(user.getRuolo())
                .attivo(user.getAttivo())
                .createdAt(user.getCreatedAt())
                .stabilimentiIds(user.getStabilimenti().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toSet()))
                .build();
    }
}
