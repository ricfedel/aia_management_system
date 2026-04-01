package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.AltraAutorizzazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AltraAutorizzazioneRepository extends JpaRepository<AltraAutorizzazione, Long> {
    List<AltraAutorizzazione> findByStabilimentoId(Long stabilimentoId);
}
