package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ControlloProcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ControlloProcessoRepository extends JpaRepository<ControlloProcesso, Long> {

    List<ControlloProcesso> findByStabilimentoIdOrderByDataControlloDesc(Long stabilimentoId);

    List<ControlloProcesso> findByMonitoraggioIdOrderByDataControlloDesc(Long monitoraggioId);

    List<ControlloProcesso> findByStabilimentoIdAndTipoTabellaOrderByDataControlloDesc(
            Long stabilimentoId, ControlloProcesso.TipoTabella tipoTabella);

    @Query("SELECT c FROM ControlloProcesso c WHERE c.stabilimento.id = :stabilimentoId " +
           "AND c.dataControllo BETWEEN :dal AND :al ORDER BY c.dataControllo DESC")
    List<ControlloProcesso> findByStabilimentoAndPeriodo(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al);

    @Query("SELECT c FROM ControlloProcesso c WHERE c.monitoraggio.id = :monitoraggioId " +
           "AND YEAR(c.dataControllo) = :anno AND MONTH(c.dataControllo) = :mese")
    List<ControlloProcesso> findByMonitoraggioAndMese(
            @Param("monitoraggioId") Long monitoraggioId,
            @Param("anno") int anno,
            @Param("mese") int mese);
}
