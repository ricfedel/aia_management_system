package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO di risposta per User.
 * - Esclude il campo password
 * - Espone nome e cognome separati (split di fullName)
 * - Espone stabilimenti come lista di ID (non oggetti completi)
 */
@Data
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String nome;
    private String cognome;
    private User.Ruolo ruolo;
    private Boolean attivo;
    private List<Long> stabilimenti;

    /**
     * Costruisce un UserResponseDTO dall'entità User.
     * Split di fullName: prima parola = nome, resto = cognome.
     */
    public static UserResponseDTO from(User user) {
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        int spaceIdx = fullName.indexOf(' ');
        String nome    = spaceIdx > 0 ? fullName.substring(0, spaceIdx) : fullName;
        String cognome = spaceIdx > 0 ? fullName.substring(spaceIdx + 1).trim() : "";

        List<Long> stabilimentiIds = user.getStabilimenti() == null
                ? List.of()
                : user.getStabilimenti().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nome(nome)
                .cognome(cognome)
                .ruolo(user.getRuolo())
                .attivo(user.getAttivo())
                .stabilimenti(stabilimentiIds)
                .build();
    }
}
