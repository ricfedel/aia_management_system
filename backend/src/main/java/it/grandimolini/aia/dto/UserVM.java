package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.User;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel (VM) per User — oggetto di trasporto verso il frontend.
 *
 * Responsabilità:
 *  - Esclude il campo password dall'esposizione REST.
 *  - Separa nome e cognome (split su primo spazio di fullName).
 *  - Espone gli ID degli stabilimenti (non le entità complete).
 *
 * Il mapping entity → VM deve avvenire DENTRO una transazione attiva
 * (es. in un metodo @Transactional del service) per evitare
 * LazyInitializationException sul campo User.stabilimenti (@ManyToMany LAZY).
 */
@Value          // immutabile: tutti i campi final, equals/hashCode/toString inclusi
@Builder
public class UserVM {

    Long id;
    String username;
    String email;
    String nome;
    String cognome;
    User.Ruolo ruolo;
    Boolean attivo;
    List<Long> stabilimenti;

    /**
     * Costruisce UserVM dall'entità User.
     * ATTENZIONE: chiamare solo dentro un contesto @Transactional per evitare
     * LazyInitializationException su user.getStabilimenti().
     */
    public static UserVM from(User user) {
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        int spaceIdx    = fullName.indexOf(' ');
        String nome     = spaceIdx > 0 ? fullName.substring(0, spaceIdx) : fullName;
        String cognome  = spaceIdx > 0 ? fullName.substring(spaceIdx + 1).trim() : "";

        List<Long> stabilimentiIds = user.getStabilimenti() == null
                ? List.of()
                : user.getStabilimenti().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList());

        return UserVM.builder()
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
