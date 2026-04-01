package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.Scadenza.StatoScadenza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScadenzaRepository extends JpaRepository<Scadenza, Long> {
    List<Scadenza> findByStabilimentoId(Long stabilimentoId);
    List<Scadenza> findByStabilimentoIdIn(List<Long> stabilimentiIds);
    List<Scadenza> findByStato(StatoScadenza stato);

    @Query("SELECT s FROM Scadenza s WHERE s.dataScadenza BETWEEN ?1 AND ?2 AND s.stato != 'COMPLETATA'")
    List<Scadenza> findScadenzeInPeriodo(LocalDate dataInizio, LocalDate dataFine);

    @Query("SELECT s FROM Scadenza s WHERE s.dataScadenza <= ?1 AND s.stato = 'PENDING'")
    List<Scadenza> findScadenzeImminenti(LocalDate data);
}
