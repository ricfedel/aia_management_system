package it.grandimolini.aia.service;

import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione dei Rapporti di Prova (RdP) dei laboratori accreditati ACCREDIA.
 *
 * Gestisce:
 * - Creazione e aggiornamento di RapportoProva con le relative RigaRapportoProva
 * - Calcolo automatico della conformità globale del rapporto
 * - Ricerca per stabilimento, punto di monitoraggio, periodo
 * - Aggiornamento automatico delle RilevazioneMisura collegate
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RapportoProvaService {

    private final RapportoProvaRepository rapportoProvaRepository;
    private final RigaRapportoProvaRepository rigaRapportoProvaRepository;
    private final StabilimentoRepository stabilimentoRepository;
    private final MonitoraggioRepository monitoraggioRepository;

    // ── Query ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RapportoProva> findByStabilimento(Long stabilimentoId) {
        return rapportoProvaRepository.findByStabilimentoIdOrderByDataCampionamentoDesc(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public List<RapportoProva> findByMonitoraggio(Long monitoraggioId) {
        return rapportoProvaRepository.findByMonitoraggioIdOrderByDataCampionamentoDesc(monitoraggioId);
    }

    @Transactional(readOnly = true)
    public List<RapportoProva> findByStabilimentoAndPeriodo(Long stabilimentoId, LocalDate dal, LocalDate al) {
        return rapportoProvaRepository.findByStabilimentoAndPeriodo(stabilimentoId, dal, al);
    }

    @Transactional(readOnly = true)
    public List<RapportoProva> findNonConformi(Long stabilimentoId) {
        return rapportoProvaRepository.findNonConformiByStabilimento(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public Optional<RapportoProva> findById(Long id) {
        return rapportoProvaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<RapportoProva> findByNumeroRapporto(String numeroRapporto) {
        return rapportoProvaRepository.findByNumeroRapporto(numeroRapporto);
    }

    // ── Salvataggio ────────────────────────────────────────────────────────────

    /**
     * Crea o aggiorna un RapportoProva.
     * Dopo il salvataggio delle righe, ricalcola la conformità globale.
     */
    public RapportoProva save(RapportoProva rapporto) {
        // Validazione
        if (rapporto.getNumeroRapporto() == null || rapporto.getNumeroRapporto().isBlank()) {
            throw new IllegalArgumentException("Il numero rapporto è obbligatorio");
        }
        if (rapporto.getDataCampionamento() == null) {
            throw new IllegalArgumentException("La data campionamento è obbligatoria");
        }

        // Carica relazioni se presenti solo gli ID
        if (rapporto.getStabilimento() != null && rapporto.getStabilimento().getId() != null
                && rapporto.getStabilimento().getNome() == null) {
            Stabilimento s = stabilimentoRepository.findById(rapporto.getStabilimento().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Stabilimento non trovato"));
            rapporto.setStabilimento(s);
        }

        RapportoProva saved = rapportoProvaRepository.save(rapporto);

        // Ricalcola conformità globale dopo salvataggio delle righe
        aggiornaConformitaGlobale(saved);

        log.info("RapportoProva salvato: id={}, numero={}, monitoraggio={}",
                saved.getId(), saved.getNumeroRapporto(),
                saved.getMonitoraggio() != null ? saved.getMonitoraggio().getCodice() : "n/a");

        return saved;
    }

    /**
     * Aggiunge o aggiorna le righe analitiche di un rapporto.
     * Ricalcola la conformità globale dopo ogni modifica.
     */
    public RapportoProva salvaRighe(Long rapportoId, List<RigaRapportoProva> righe) {
        RapportoProva rapporto = rapportoProvaRepository.findById(rapportoId)
                .orElseThrow(() -> new IllegalArgumentException("RapportoProva non trovato: " + rapportoId));

        // Rimuovi righe esistenti e sostituisci
        rapporto.getRighe().clear();
        for (int i = 0; i < righe.size(); i++) {
            RigaRapportoProva riga = righe.get(i);
            riga.setRapportoProva(rapporto);
            if (riga.getRigaNumero() == null) {
                riga.setRigaNumero(i + 1);
            }
            rapporto.getRighe().add(riga);
        }

        RapportoProva saved = rapportoProvaRepository.save(rapporto);
        aggiornaConformitaGlobale(saved);

        log.info("RapportoProva {}: aggiornate {} righe analitiche", rapportoId, righe.size());
        return saved;
    }

    // ── Conformità globale ─────────────────────────────────────────────────────

    /**
     * Ricalcola la conformità globale del rapporto in base allo stato delle righe.
     * - CONFORME: tutte le righe CONFORME o SOTTO_LOQ o NON_NORMATO
     * - NON_CONFORME: almeno una riga NON_CONFORME
     * - PARZIALE: nessuna riga NON_CONFORME ma almeno una ATTENZIONE
     */
    public void aggiornaConformitaGlobale(RapportoProva rapporto) {
        List<RigaRapportoProva> righe = rapporto.getRighe();
        if (righe == null || righe.isEmpty()) {
            rapporto.setConformitaGlobale(RapportoProva.ConformitaGlobale.IN_VALUTAZIONE);
            rapportoProvaRepository.save(rapporto);
            return;
        }

        boolean hasNonConforme = righe.stream()
                .anyMatch(r -> r.getStatoConformita() == RigaRapportoProva.StatoConformita.NON_CONFORME);
        boolean hasAttenzione = righe.stream()
                .anyMatch(r -> r.getStatoConformita() == RigaRapportoProva.StatoConformita.ATTENZIONE);

        RapportoProva.ConformitaGlobale nuovaConformita;
        if (hasNonConforme) {
            nuovaConformita = RapportoProva.ConformitaGlobale.NON_CONFORME;
        } else if (hasAttenzione) {
            nuovaConformita = RapportoProva.ConformitaGlobale.PARZIALE;
        } else {
            nuovaConformita = RapportoProva.ConformitaGlobale.CONFORME;
        }

        rapporto.setConformitaGlobale(nuovaConformita);
        rapportoProvaRepository.save(rapporto);

        log.info("RapportoProva {}: conformità globale aggiornata a {}",
                rapporto.getId(), nuovaConformita);
    }

    // ── Eliminazione ───────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!rapportoProvaRepository.existsById(id)) {
            throw new IllegalArgumentException("RapportoProva non trovato: " + id);
        }
        rapportoProvaRepository.deleteById(id);
        log.info("RapportoProva {} eliminato", id);
    }

    // ── Statistiche ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public long countByStabilimentoAndAnno(Long stabilimentoId, int anno) {
        LocalDate dal = LocalDate.of(anno, 1, 1);
        LocalDate al = LocalDate.of(anno, 12, 31);
        return rapportoProvaRepository.countByStabilimentoAndPeriodo(stabilimentoId, dal, al);
    }
}
