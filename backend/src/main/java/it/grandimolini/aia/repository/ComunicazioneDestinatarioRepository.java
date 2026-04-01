package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ComunicazioneDestinatario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunicazioneDestinatarioRepository extends JpaRepository<ComunicazioneDestinatario, Long> {

    List<ComunicazioneDestinatario> findByComunicazioneId(Long comunicazioneId);

    List<ComunicazioneDestinatario> findByComunicazioneIdAndRuolo(
            Long comunicazioneId, ComunicazioneDestinatario.RuoloDestinatario ruolo);

    Optional<ComunicazioneDestinatario> findByComunicazioneIdAndRecapitoEnteId(
            Long comunicazioneId, Long recapitoEnteId);

    @Query("SELECT d FROM ComunicazioneDestinatario d " +
           "WHERE d.comunicazione.id = :comunicazioneId " +
           "AND d.statoConsegna <> it.grandimolini.aia.model.ComunicazioneDestinatario$StatoConsegna.CONSEGNATA")
    List<ComunicazioneDestinatario> findNonConsegnatiPerComunicazione(
            @Param("comunicazioneId") Long comunicazioneId);

    /** Tutti i destinatari con consegna in sospeso per uno stabilimento */
    @Query("SELECT d FROM ComunicazioneDestinatario d " +
           "WHERE d.comunicazione.stabilimento.id = :stabilimentoId " +
           "AND d.statoConsegna = it.grandimolini.aia.model.ComunicazioneDestinatario$StatoConsegna.IN_ATTESA")
    List<ComunicazioneDestinatario> findInAttesaPerStabilimento(
            @Param("stabilimentoId") Long stabilimentoId);
}
