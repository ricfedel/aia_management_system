package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ProcessoDocumento;
import it.grandimolini.aia.model.ProcessoDocumento.StatoProcesso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessoDocumentoRepository extends JpaRepository<ProcessoDocumento, Long> {

    Optional<ProcessoDocumento> findByCodiceProcesso(String codiceProcesso);

    List<ProcessoDocumento> findByStabilimentoId(Long stabilimentoId);

    List<ProcessoDocumento> findByStato(StatoProcesso stato);

    List<ProcessoDocumento> findByAssegnatoA(String username);

    List<ProcessoDocumento> findByDocumentoId(Long documentoId);

    @Query("SELECT p FROM ProcessoDocumento p WHERE p.stato NOT IN ('COMPLETATO','ANNULLATO') ORDER BY p.dataAvvio DESC")
    List<ProcessoDocumento> findProcessiAttivi();

    @Query("SELECT p FROM ProcessoDocumento p WHERE p.stato NOT IN ('COMPLETATO','ANNULLATO') ORDER BY p.dataAvvio DESC")
    Page<ProcessoDocumento> findProcessiAttiviPaged(Pageable pageable);

    @Query("SELECT COUNT(p) FROM ProcessoDocumento p WHERE p.stato NOT IN ('COMPLETATO','ANNULLATO')")
    long countProcessiAttivi();

    @Query("SELECT p FROM ProcessoDocumento p WHERE p.stabilimento.id = :stabilimentoId AND p.stato NOT IN ('COMPLETATO','ANNULLATO')")
    List<ProcessoDocumento> findProcessiAttiviByStabilimento(@Param("stabilimentoId") Long stabilimentoId);
}
