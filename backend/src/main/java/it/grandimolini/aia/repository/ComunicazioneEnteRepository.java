package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ComunicazioneEnte;
import it.grandimolini.aia.model.ComunicazioneEnte.StatoComunicazione;
import it.grandimolini.aia.model.ComunicazioneEnte.EnteEsterno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ComunicazioneEnteRepository extends JpaRepository<ComunicazioneEnte, Long> {

    List<ComunicazioneEnte> findByStabilimentoIdOrderByDataInvioDesc(Long stabilimentoId);

    List<ComunicazioneEnte> findByStabilimentoIdAndStatoOrderByDataInvioDesc(
            Long stabilimentoId, StatoComunicazione stato);

    List<ComunicazioneEnte> findByStabilimentoIdAndEnteOrderByDataInvioDesc(
            Long stabilimentoId, EnteEsterno ente);

    @Query("""
        SELECT c FROM ComunicazioneEnte c
        WHERE c.stabilimento.id = :stabId
          AND (:stato IS NULL OR c.stato = :stato)
          AND (:ente  IS NULL OR c.ente  = :ente)
          AND (:from  IS NULL OR c.dataInvio >= :from)
          AND (:to    IS NULL OR c.dataInvio <= :to)
        ORDER BY c.dataInvio DESC
    """)
    List<ComunicazioneEnte> findFiltered(
            @Param("stabId") Long stabId,
            @Param("stato")  StatoComunicazione stato,
            @Param("ente")   EnteEsterno ente,
            @Param("from")   LocalDate from,
            @Param("to")     LocalDate to);

    // Comunicazioni senza riscontro inviate da più di N giorni
    @Query("""
        SELECT c FROM ComunicazioneEnte c
        WHERE c.stabilimento.id = :stabId
          AND c.hasRiscontro = false
          AND c.stato = 'INVIATA'
          AND c.dataInvio <= :soglia
        ORDER BY c.dataInvio ASC
    """)
    List<ComunicazioneEnte> findInAttesaRiscontro(
            @Param("stabId") Long stabId,
            @Param("soglia") LocalDate soglia);

    @Query("SELECT COUNT(c) FROM ComunicazioneEnte c WHERE c.stabilimento.id = :stabId AND c.stato = :stato")
    long countByStabilimentoIdAndStato(@Param("stabId") Long stabId, @Param("stato") StatoComunicazione stato);
}
