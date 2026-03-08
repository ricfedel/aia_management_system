package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.IndicatorePerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicatorePerformanceRepository extends JpaRepository<IndicatorePerformance, Long> {

    List<IndicatorePerformance> findByStabilimentoIdAndAnnoOrderByIndicatore(
            Long stabilimentoId, int anno);

    List<IndicatorePerformance> findByStabilimentoIdOrderByAnnoDescIndicatore(Long stabilimentoId);

    Optional<IndicatorePerformance> findByStabilimentoIdAndAnnoAndIndicatore(
            Long stabilimentoId, int anno, IndicatorePerformance.TipoIndicatore indicatore);
}
