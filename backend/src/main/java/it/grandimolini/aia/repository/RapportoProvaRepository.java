package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.RapportoProva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RapportoProvaRepository extends JpaRepository<RapportoProva, Long> {

    List<RapportoProva> findByStabilimentoIdOrderByDataCampionamentoDesc(Long stabilimentoId);

    List<RapportoProva> findByMonitoraggioIdOrderByDataCampionamentoDesc(Long monitoraggioId);

    Optional<RapportoProva> findByNumeroRapporto(String numeroRapporto);

    Optional<RapportoProva> findByNumeroAccettazione(String numeroAccettazione);

    @Query("SELECT r FROM RapportoProva r WHERE r.stabilimento.id = :stabilimentoId " +
           "AND r.dataCampionamento BETWEEN :dal AND :al " +
           "ORDER BY r.dataCampionamento DESC")
    List<RapportoProva> findByStabilimentoAndPeriodo(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al);

    @Query("SELECT r FROM RapportoProva r WHERE r.stabilimento.id = :stabilimentoId " +
           "AND r.conformitaGlobale = 'NON_CONFORME' " +
           "ORDER BY r.dataCampionamento DESC")
    List<RapportoProva> findNonConformiByStabilimento(@Param("stabilimentoId") Long stabilimentoId);

    @Query("SELECT COUNT(r) FROM RapportoProva r WHERE r.stabilimento.id = :stabilimentoId " +
           "AND r.dataCampionamento BETWEEN :dal AND :al")
    long countByStabilimentoAndPeriodo(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("dal") LocalDate dal,
            @Param("al") LocalDate al);

    @Query("SELECT r FROM RapportoProva r WHERE r.monitoraggio.id = :monitoraggioId " +
           "AND YEAR(r.dataCampionamento) = :anno " +
           "ORDER BY r.dataCampionamento")
    List<RapportoProva> findByMonitoraggioAndAnno(
            @Param("monitoraggioId") Long monitoraggioId,
            @Param("anno") int anno);
}
