package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.RecapitoEnte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecapitoEnteRepository extends JpaRepository<RecapitoEnte, Long> {

    List<RecapitoEnte> findByStabilimentoIdAndAttivoTrue(Long stabilimentoId);

    List<RecapitoEnte> findByStabilimentoIdAndEnteTipoAndAttivoTrue(
            Long stabilimentoId, RecapitoEnte.TipoEnteRecapito enteTipo);

    /**
     * Trova i recapiti da notificare per un determinato tipo di comunicazione.
     * Cerca il tipo comunicazione nella lista CSV del campo tipo_comunicazioni.
     */
    @Query("SELECT r FROM RecapitoEnte r WHERE r.stabilimento.id = :stabilimentoId " +
           "AND r.attivo = true " +
           "AND (r.tipoComunicazioni IS NULL OR r.tipoComunicazioni LIKE %:tipoComunicazione%)")
    List<RecapitoEnte> findPerTipoComunicazione(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("tipoComunicazione") String tipoComunicazione);
}
