package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ZonaAcustica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaAcusticaRepository extends JpaRepository<ZonaAcustica, Long> {

    List<ZonaAcustica> findByStabilimentoIdOrderByPosizione(Long stabilimentoId);

    Optional<ZonaAcustica> findByStabilimentoIdAndPosizione(Long stabilimentoId, String posizione);

    List<ZonaAcustica> findByStabilimentoIdAndTipoPosizione(
            Long stabilimentoId, ZonaAcustica.TipoPosizione tipoPosizione);
}
