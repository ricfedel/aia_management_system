package it.grandimolini.aia.security;

import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("stabilimentoAccessChecker")
public class StabilimentoAccessChecker {

    /** Authority assegnata dal {@link LocalhostInternalAuthFilter} alle chiamate BPM interne. */
    private static final String ROLE_INTERNAL = "ROLE_INTERNAL";

    @Autowired
    private UserRepository userRepository;

    // ─── Helper: verifica se l'autenticazione è un principal interno BPM ──────

    /**
     * Ritorna {@code true} se l'autenticazione appartiene al principal interno BPM
     * (es. "bpm-internal"), che non ha una riga nel DB ma ha pieno accesso.
     */
    private boolean isInternalPrincipal(Authentication auth) {
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_INTERNAL.equals(a.getAuthority()));
    }

    /**
     * Verifica se l'utente corrente ha accesso allo stabilimento specificato.
     * Gli ADMIN hanno sempre accesso a tutti gli stabilimenti.
     *
     * @param stabilimentoId ID dello stabilimento da verificare
     * @return true se l'utente ha accesso, false altrimenti
     */
    public boolean hasAccessToStabilimento(Long stabilimentoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        // Il principal interno BPM ha accesso a tutto
        if (isInternalPrincipal(authentication)) return true;

        String username = authentication.getName();
        // findByUsername per ruolo, poi JOIN FETCH per stabilimenti se serve
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return false;
        }

        // ADMIN ha sempre accesso a tutto
        if (user.getRuolo() == User.Ruolo.ADMIN) {
            return true;
        }

        // Usa JOIN FETCH per evitare LazyInitializationException sulla collection stabilimenti
        User userWithStabilimenti = userRepository.findByIdWithStabilimenti(user.getId()).orElse(null);
        if (userWithStabilimenti == null) {
            return false;
        }

        // Verifica se lo stabilimento è tra quelli assegnati all'utente
        return userWithStabilimenti.getStabilimenti().stream()
                .anyMatch(s -> s.getId().equals(stabilimentoId));
    }

    /**
     * Verifica se l'utente corrente è un ADMIN.
     *
     * @return true se l'utente è ADMIN, false altrimenti
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isInternalPrincipal(authentication)) return true;

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null && user.getRuolo() == User.Ruolo.ADMIN;
    }

    /**
     * Verifica se l'utente corrente è un RESPONSABILE o ADMIN.
     *
     * @return true se l'utente è RESPONSABILE o ADMIN, false altrimenti
     */
    public boolean isResponsabileOrAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isInternalPrincipal(authentication)) return true;

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null &&
               (user.getRuolo() == User.Ruolo.ADMIN || user.getRuolo() == User.Ruolo.RESPONSABILE);
    }

    /**
     * Verifica se l'utente può inserire/modificare dati ambientali.
     * ADMIN, RESPONSABILE e OPERATORE possono creare e modificare dati ambientali.
     *
     * @return true se l'utente può editare dati ambientali, false altrimenti
     */
    public boolean canEditDatiAmbientali() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isInternalPrincipal(authentication)) return true;

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null &&
               (user.getRuolo() == User.Ruolo.ADMIN ||
                user.getRuolo() == User.Ruolo.RESPONSABILE ||
                user.getRuolo() == User.Ruolo.OPERATORE);
    }

    /**
     * Verifica se l'utente può caricare documenti.
     * ADMIN, RESPONSABILE e OPERATORE possono caricare documenti.
     *
     * @return true se l'utente può caricare documenti, false altrimenti
     */
    public boolean canUploadDocumenti() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (isInternalPrincipal(authentication)) return true;

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null &&
               (user.getRuolo() == User.Ruolo.ADMIN ||
                user.getRuolo() == User.Ruolo.RESPONSABILE ||
                user.getRuolo() == User.Ruolo.OPERATORE);
    }

    /**
     * Restituisce gli ID degli stabilimenti assegnati all'utente corrente.
     * Usa JOIN FETCH per evitare LazyInitializationException.
     *
     * @return lista di ID stabilimenti, vuota se non autenticato o nessuno assegnato
     */
    public List<Long> getCurrentUserStabilimentoIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        // Il principal interno BPM non ha una riga nel DB: restituisce lista vuota
        // (i suoi chiamanti usano isAdmin() per decidere il percorso admin/non-admin)
        if (isInternalPrincipal(authentication)) return List.of();

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .flatMap(u -> userRepository.findByIdWithStabilimenti(u.getId()))
                .map(u -> u.getStabilimenti().stream()
                        .map(s -> s.getId())
                        .collect(java.util.stream.Collectors.toList()))
                .orElse(List.of());
    }
}
