package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.CreateUserRequest;
import it.grandimolini.aia.dto.UpdateUserRequest;
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

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User create(CreateUserRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
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

        // Assign stabilimenti if provided
        if (request.getStabilimentiIds() != null && !request.getStabilimentiIds().isEmpty()) {
            Set<Stabilimento> stabilimenti = new HashSet<>(
                    stabilimentoRepository.findAllById(request.getStabilimentiIds())
            );
            user.setStabilimenti(stabilimenti);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Update other fields
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getRuolo() != null) {
            user.setRuolo(request.getRuolo());
        }

        if (request.getAttivo() != null) {
            user.setAttivo(request.getAttivo());
        }

        // Update stabilimenti if provided
        if (request.getStabilimentiIds() != null) {
            Set<Stabilimento> stabilimenti = new HashSet<>(
                    stabilimentoRepository.findAllById(request.getStabilimentiIds())
            );
            user.setStabilimenti(stabilimenti);
        }

        return userRepository.save(user);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
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
}
