package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.CodiceRifiuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodiceRifiutoRepository extends JpaRepository<CodiceRifiuto, Long> {

    List<CodiceRifiuto> findByStabilimentoIdOrderByCodiceCerAsc(Long stabilimentoId);

    List<CodiceRifiuto> findByStabilimentoIdAndAttivoTrueOrderByCodiceCerAsc(Long stabilimentoId);

    List<CodiceRifiuto> findByStabilimentoIdAndPericolosoOrderByCodiceCerAsc(Long stabilimentoId, Boolean pericoloso);

    boolean existsByStabilimentoIdAndCodiceCer(Long stabilimentoId, String codiceCer);
}
