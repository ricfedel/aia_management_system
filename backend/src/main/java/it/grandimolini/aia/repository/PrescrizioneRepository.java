package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Prescrizione.StatoPrescrizione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrescrizioneRepository extends JpaRepository<Prescrizione, Long> {
    List<Prescrizione> findByStabilimentoId(Long stabilimentoId);
    List<Prescrizione> findByStabilimentoIdIn(List<Long> stabilimentiIds);
    List<Prescrizione> findByStato(StatoPrescrizione stato);
    List<Prescrizione> findByStabilimentoIdAndStato(Long stabilimentoId, StatoPrescrizione stato);
}
