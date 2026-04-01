package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ManutenzioneImpianto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManutenzioneImpiantoRepository extends JpaRepository<ManutenzioneImpianto, Long> {

    List<ManutenzioneImpianto> findByStabilimentoIdOrderByDataInterventoDesc(Long stabilimentoId);

    List<ManutenzioneImpianto> findByMonitoraggioIdOrderByDataInterventoDesc(Long monitoraggioId);

    @Query("SELECT m FROM ManutenzioneImpianto m WHERE m.stabilimento.id = :stabilimentoId " +
           "AND m.dataIntervento BETWEEN :dal AND :al ORDER BY m.dataIntervento DESC")
    List<ManutenzioneImpianto> findByStabilimentoAndPeriodo(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al);

    @Query("SELECT m FROM ManutenzioneImpianto m WHERE m.stabilimento.id = :stabilimentoId " +
           "AND m.prossimaManutenzione <= :scadenza ORDER BY m.prossimaManutenzione")
    List<ManutenzioneImpianto> findScadenzeImminenti(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("scadenza") LocalDate scadenza);
}
