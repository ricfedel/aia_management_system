package it.grandimolini.aia.controller;

import it.grandimolini.aia.dto.CreateUserRequest;
import it.grandimolini.aia.dto.UpdateUserRequest;
import it.grandimolini.aia.dto.UserRequest;
import it.grandimolini.aia.dto.UserVM;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * GET /api/users
     * Restituisce la lista di tutti gli utenti come UserVM (solo ADMIN).
     * Il mapping entity→VM avviene dentro la transazione del service,
     * evitando LazyInitializationException su User.stabilimenti.
     */
    @GetMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<List<UserVM>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllAsVM());
    }

    /**
     * GET /api/users/{id}
     * Restituisce un singolo utente come UserVM (solo ADMIN).
     */
    @GetMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<UserVM> getUserById(@PathVariable Long id) {
        UserVM vm = userService.findByIdAsVM(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return ResponseEntity.ok(vm);
    }

    /**
     * POST /api/users
     * Crea un nuovo utente (solo ADMIN).
     * Accetta UserRequest con nome/cognome separati e li combina in fullName.
     */
    @PostMapping
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<UserVM> createUser(@RequestBody UserRequest request) {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setUsername(request.getUsername());
        createRequest.setEmail(request.getEmail());
        createRequest.setPassword(request.getPassword());
        createRequest.setFullName(request.getFullName());
        createRequest.setRuolo(request.getRuolo());
        createRequest.setStabilimentiIds(request.getStabilimentiIds());

        UserVM vm = userService.create(createRequest);
        return ResponseEntity.ok(vm);
    }

    /**
     * PUT /api/users/{id}
     * Aggiorna un utente esistente (solo ADMIN).
     */
    @PutMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<UserVM> updateUser(@PathVariable Long id,
                                             @RequestBody UserRequest request) {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmail(request.getEmail());
        updateRequest.setFullName(request.getFullName());
        updateRequest.setRuolo(request.getRuolo());
        updateRequest.setAttivo(request.getAttivo());
        updateRequest.setStabilimentiIds(request.getStabilimentiIds());

        UserVM vm = userService.update(id, updateRequest);
        return ResponseEntity.ok(vm);
    }

    /**
     * DELETE /api/users/{id}
     * Elimina un utente (solo ADMIN).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@stabilimentoAccessChecker.isAdmin()")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
