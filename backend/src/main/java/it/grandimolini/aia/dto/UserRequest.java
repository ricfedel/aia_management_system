package it.grandimolini.aia.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import it.grandimolini.aia.model.User;
import lombok.Data;

import java.util.Set;

/**
 * DTO unificato per creazione e aggiornamento utente.
 * Accetta "stabilimenti" o "stabilimentiIds" (il frontend li usa entrambi
 * a seconda dell'operazione).
 */
@Data
public class UserRequest {

    private String username;
    private String email;
    private String password;

    /** Nome (prima parte del fullName) */
    private String nome;

    /** Cognome (seconda parte del fullName) */
    private String cognome;

    private User.Ruolo ruolo;
    private Boolean attivo;

    @JsonAlias({"stabilimentiIds", "stabilimenti"})
    private Set<Long> stabilimentiIds;

    /** Combina nome e cognome nel formato atteso da UserService */
    public String getFullName() {
        if (nome == null && cognome == null) return null;
        return ((nome != null ? nome : "") + " " + (cognome != null ? cognome : "")).trim();
    }
}
