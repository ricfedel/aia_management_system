package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.AnagraficaCamino;
import it.grandimolini.aia.model.AnagraficaCamino.FaseProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnagraficaCaminoRepository extends JpaRepository<AnagraficaCamino, Long> {

    List<AnagraficaCamino> findByStabilimentoIdOrderBySiglaAsc(Long stabilimentoId);

    List<AnagraficaCamino> findByStabilimentoIdAndAttivoOrderBySiglaAsc(Long stabilimentoId, Boolean attivo);

    List<AnagraficaCamino> findByStabilimentoIdAndFaseProcessoOrderBySiglaAsc(Long stabilimentoId, FaseProcesso faseProcesso);

    Optional<AnagraficaCamino> findByStabilimentoIdAndSigla(Long stabilimentoId, String sigla);

    boolean existsByStabilimentoIdAndSigla(Long stabilimentoId, String sigla);
}
