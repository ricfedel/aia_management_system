package it.grandimolini.aia.dto;

import it.grandimolini.aia.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    private User.Ruolo ruolo;

    private Boolean attivo;

    private Set<Long> stabilimentiIds;
}
