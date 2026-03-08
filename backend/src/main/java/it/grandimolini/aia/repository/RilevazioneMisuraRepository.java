package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.RilevazioneMisura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RilevazioneMisuraRepository extends JpaRepository<RilevazioneMisura, Long> {

    List<RilevazioneMisura> findByParametroMonitoraggioIdOrderByDataCampionamentoDesc(Long parametroId);

    List<RilevazioneMisura> findByParametroMonitoraggioIdAndDataCampionamentoBetweenOrderByDataCampionamentoDesc(
            Long parametroId, LocalDate from, LocalDate to);

    @Query("""
        SELECT r FROM RilevazioneMisura r
        WHERE r.parametroMonitoraggio.monitoraggio.id = :monitoraggioId
        ORDER BY r.dataCampionamento DESC
    """)
    List<RilevazioneMisura> findByMonitoraggioId(@Param("monitoraggioId") Long monitoraggioId);

    @Query("""
        SELECT r FROM RilevazioneMisura r
        WHERE r.parametroMonitoraggio.monitoraggio.stabilimento.id = :stabilimentoId
          AND r.dataCampionamento BETWEEN :from AND :to
        ORDER BY r.dataCampionamento DESC
    """)
    List<RilevazioneMisura> findByStabilimentoAndPeriodo(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
        SELECT r FROM RilevazioneMisura r
        WHERE r.parametroMonitoraggio.monitoraggio.stabilimento.id = :stabilimentoId
          AND r.statoConformita = 'NON_CONFORME'
        ORDER BY r.dataCampionamento DESC
    """)
    List<RilevazioneMisura> findNonConformiByStabilimento(@Param("stabilimentoId") Long stabilimentoId);

    /** Ultima rilevazione per ogni parametro di un monitoraggio */
    @Query("""
        SELECT r FROM RilevazioneMisura r
        WHERE r.id IN (
            SELECT MAX(r2.id) FROM RilevazioneMisura r2
            WHERE r2.parametroMonitoraggio.monitoraggio.id = :monitoraggioId
            GROUP BY r2.parametroMonitoraggio.id
        )
    """)
    List<RilevazioneMisura> findUltimeRilevazioniByMonitoraggio(@Param("monitoraggioId") Long monitoraggioId);

    /** Ultima rilevazione per ogni parametro di uno stabilimento */
    @Query("""
        SELECT r FROM RilevazioneMisura r
        WHERE r.id IN (
            SELECT MAX(r2.id) FROM RilevazioneMisura r2
            WHERE r2.parametroMonitoraggio.monitoraggio.stabilimento.id = :stabilimentoId
            GROUP BY r2.parametroMonitoraggio.id
        )
    """)
    List<RilevazioneMisura> findUltimeRilevazioniByStabilimento(@Param("stabilimentoId") Long stabilimentoId);
}
