package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.RigaRapportoProva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RigaRapportoProvaRepository extends JpaRepository<RigaRapportoProva, Long> {

    List<RigaRapportoProva> findByRapportoProvaIdOrderByRigaNumeroAsc(Long rapportoProvaId);

    List<RigaRapportoProva> findByStatoConformita(RigaRapportoProva.StatoConformita stato);

    @Query("SELECT r FROM RigaRapportoProva r WHERE r.rapportoProva.monitoraggio.id = :monitoraggioId " +
           "AND r.parametro = :parametro " +
           "ORDER BY r.rapportoProva.dataCampionamento DESC")
    List<RigaRapportoProva> findByMonitoraggioAndParametro(
            @Param("monitoraggioId") Long monitoraggioId,
            @Param("parametro") String parametro);

    @Query("SELECT r FROM RigaRapportoProva r WHERE r.rapportoProva.id = :rapportoId " +
           "AND r.statoConformita = 'NON_CONFORME'")
    List<RigaRapportoProva> findNonConformiByRapporto(@Param("rapportoId") Long rapportoId);
}
