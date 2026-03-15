package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.CreateUserRequest;
import it.grandimolini.aia.dto.CurrentUserDTO;
import it.grandimolini.aia.dto.UpdateUserRequest;
import it.grandimolini.aia.dto.UserVM;
import it.grandimolini.aia.exception.BadRequestException;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ────── WRITE ───────────────────────────────────────────────────────────

    /**
     * Crea un nuovo utente e restituisce il VM corrispondente.
     * La sessione resta aperta per mappare stabilimenti (LAZY).
     */
    @Transactional
    public UserVM create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRuolo(request.getRuolo());
        user.setAttivo(true);

        if (request.getStabilimentiIds() != null && !request.getStabilimentiIds().isEmpty()) {
            Set<Stabilimento> stabilimenti = new HashSet<>(
                    stabilimentoRepository.findAllById(request.getStabilimentiIds())
            );
            user.setStabilimenti(stabilimenti);
        }

        User saved = userRepository.save(user);
        return UserVM.from(saved);
    }

    /**
     * Aggiorna un utente esistente e restituisce il VM.
     */
    @Transactional
    public UserVM update(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdWithStabilimenti(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getRuolo() != null) {
            user.setRuolo(request.getRuolo());
        }
        if (request.getAttivo() != null) {
            user.setAttivo(request.getAttivo());
        }

        if (request.getStabilimentiIds() != null) {
            Set<Stabilimento> stabilimenti = new HashSet<>(
                    stabilimentoRepository.findAllById(request.getStabilimentiIds())
            );
            user.setStabilimenti(stabilimenti);
        }

        User saved = userRepository.save(user);
        return UserVM.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    /**
     * Cambia la password. Non espone l'entità: operazione void.
     */
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void assignStabilimenti(Long userId, Set<Long> stabilimentoIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Set<Stabilimento> stabilimenti = new HashSet<>(
                stabilimentoRepository.findAllById(stabilimentoIds)
        );
        user.setStabilimenti(stabilimenti);
        userRepository.save(user);
    }

    // ────── READ — restituisce sempre VM ────────────────────────────────────

    /**
     * Carica tutti gli utenti con stabilimenti (JOIN FETCH) e li mappa a UserVM.
     */
    @Transactional(readOnly = true)
    public List<UserVM> findAll() {
        return userRepository.findAllWithStabilimenti()
                .stream()
                .map(UserVM::from)
                .collect(Collectors.toList());
    }

    /** Alias semantico per i controller che preferiscono il nome «AsVM». */
    @Transactional(readOnly = true)
    public List<UserVM> findAllAsVM() {
        return findAll();
    }

    @Transactional(readOnly = true)
    public Optional<UserVM> findById(Long id) {
        return userRepository.findByIdWithStabilimenti(id).map(UserVM::from);
    }

    /** Alias semantico usato da UserController. */
    @Transactional(readOnly = true)
    public Optional<UserVM> findByIdAsVM(Long id) {
        return findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserVM> findByUsername(String username) {
        return userRepository.findByUsername(username).map(UserVM::from);
    }

    /**
     * Carica l'utente corrente come CurrentUserDTO (con info complete sugli stabilimenti).
     * Usato dall'endpoint GET /api/auth/me.
     */
    @Transactional(readOnly = true)
    public Optional<CurrentUserDTO> findCurrentUserDTO(String username) {
        return userRepository.findByUsername(username)
                .flatMap(u -> userRepository.findByIdWithStabilimenti(u.getId()))
                .map(CurrentUserDTO::fromEntity);
    }

}

