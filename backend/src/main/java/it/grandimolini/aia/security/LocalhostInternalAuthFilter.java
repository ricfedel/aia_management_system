package it.grandimolini.aia.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Filtro interno BPM: NON è un @Component — viene istanziato come @Bean in SecurityConfig
 * per evitare il check sull'order registry di Spring Security 7.
 *
 * Filtro di sicurezza che concede accesso automatico alle richieste interne
 * senza richiedere un token JWT.
 *
 * <p>Una richiesta è considerata "interna" se soddisfa <em>almeno una</em> delle
 * seguenti condizioni:
 * <ol>
 *   <li><b>IP loopback</b>: RemoteAddr è {@code 127.0.0.1} o {@code ::1}.
 *       Funziona per self-call diretti (Spring Boot senza proxy).</li>
 *   <li><b>Header segreto</b>: la richiesta contiene {@code X-Internal-Token}
 *       con il valore configurato in {@code app.internal.secret}.
 *       Funziona in tutti i setup: Docker, Nginx, proxy inverso.</li>
 * </ol>
 *
 * <h3>Setup con Nginx</h3>
 * Nginx deve strippare l'header dalle richieste in ingresso per evitare
 * che client esterni lo falsifichino:
 * <pre>
 *   proxy_set_header X-Internal-Token "";
 * </pre>
 * Il BPM engine aggiunge l'header automaticamente a tutte le chiamate
 * di tipo {@code API_CALL} verso endpoint interni.
 *
 * <h3>Sicurezza</h3>
 * <ul>
 *   <li>L'header {@code X-Forwarded-For} viene intenzionalmente ignorato
 *       per evitare spoofing.</li>
 *   <li>Il confronto del segreto usa un compare a tempo costante per
 *       prevenire timing attacks.</li>
 *   <li>Se il SecurityContext è già popolato (JWT valido presente), il filtro
 *       non sovrascrive l'autenticazione esistente.</li>
 * </ul>
 */
public class LocalhostInternalAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LocalhostInternalAuthFilter.class);

    /** Nome dell'header HTTP usato per l'autenticazione interna. */
    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    /** Indirizzi loopback riconosciuti (IPv4 e IPv6). */
    private static final Set<String> LOOPBACK_ADDRESSES = Set.of(
        "127.0.0.1",
        "::1",
        "0:0:0:0:0:0:0:1"
    );

    private static final String ROLE_INTERNAL   = "ROLE_INTERNAL";
    private static final String INTERNAL_PRINCIPAL = "bpm-internal";

    private final String internalSecret;

    public LocalhostInternalAuthFilter(String internalSecret) {
        this.internalSecret = internalSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null
                && isInternalRequest(request)) {

            log.trace("Richiesta interna autenticata [{} {}] da {}",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                INTERNAL_PRINCIPAL,
                null,
                List.of(new SimpleGrantedAuthority(ROLE_INTERNAL))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Una richiesta è interna se proviene da loopback OPPURE presenta l'header segreto valido.
     */
    private boolean isInternalRequest(HttpServletRequest request) {
        return isLoopback(request.getRemoteAddr()) || hasValidInternalToken(request);
    }

    private boolean isLoopback(String addr) {
        return addr != null && LOOPBACK_ADDRESSES.contains(addr.trim());
    }

    /**
     * Confronto a tempo costante per prevenire timing attacks sul segreto.
     */
    private boolean hasValidInternalToken(HttpServletRequest request) {
        String token = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (!StringUtils.hasText(token) || !StringUtils.hasText(internalSecret)) {
            return false;
        }
        // MessageDigest.isEqual è constant-time
        return java.security.MessageDigest.isEqual(
            token.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            internalSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
