package it.grandimolini.aia.security;

import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("stabilimentoAccessChecker")
public class StabilimentoAccessChecker {

    @Autowired
    private UserRepository userRepository;

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

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return false;
        }

        // ADMIN ha sempre accesso a tutto
        if (user.getRuolo() == User.Ruolo.ADMIN) {
            return true;
        }

        // Verifica se lo stabilimento è tra quelli assegnati all'utente
        return user.getStabilimenti().stream()
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

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        return user != null &&
               (user.getRuolo() == User.Ruolo.ADMIN ||
                user.getRuolo() == User.Ruolo.RESPONSABILE ||
                user.getRuolo() == User.Ruolo.OPERATORE);
    }

    /**
     * Ottiene l'utente corrente autenticato.
     *
     * @return l'utente corrente o null se non autenticato
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}
