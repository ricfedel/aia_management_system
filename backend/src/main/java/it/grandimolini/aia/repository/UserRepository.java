package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    /**
     * Carica tutti gli utenti con JOIN FETCH su stabilimenti per evitare
     * LazyInitializationException fuori dalla sessione Hibernate.
     * DISTINCT evita duplicati causati dal JOIN su @ManyToMany.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.stabilimenti ORDER BY u.username ASC")
    List<User> findAllWithStabilimenti();

    /**
     * Carica un singolo utente con JOIN FETCH su stabilimenti.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.stabilimenti WHERE u.id = :id")
    Optional<User> findByIdWithStabilimenti(@Param("id") Long id);
}
