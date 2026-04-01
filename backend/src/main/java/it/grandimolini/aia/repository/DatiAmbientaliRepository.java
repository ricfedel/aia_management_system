package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.DatiAmbientali.StatoConformita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DatiAmbientaliRepository extends JpaRepository<DatiAmbientali, Long> {
    List<DatiAmbientali> findByMonitoraggioId(Long monitoraggioId);
    List<DatiAmbientali> findByMonitoraggioStabilimentoId(Long stabilimentoId);
    List<DatiAmbientali> findByMonitoraggioStabilimentoIdIn(List<Long> stabilimentiIds);
    List<DatiAmbientali> findByStatoConformita(StatoConformita stato);

    @Query("SELECT d FROM DatiAmbientali d WHERE d.monitoraggio.stabilimento.id = ?1 AND YEAR(d.dataCampionamento) = ?2")
    List<DatiAmbientali> findByStabilimentoAndAnno(Long stabilimentoId, int anno);
}
