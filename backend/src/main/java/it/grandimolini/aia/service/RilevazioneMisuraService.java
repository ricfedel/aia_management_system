package it.grandimolini.aia.service;

import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.MonitoraggioRepository;
import it.grandimolini.aia.repository.RilevazioneMisuraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione delle RilevazioneMisura.
 *
 * Prima di ogni salvataggio:
 * - chiama {@link RilevazioneMisura#calcolaConformita()} (già gestito via @PrePersist/@PreUpdate)
 * - chiama {@link RilevazioneMisura#calcolaFlussoMassa(Double, Integer, Integer)}
 *   se il punto di monitoraggio è EMISSIONI_ATMOSFERA e ha AnagraficaCamino collegata
 * - chiama {@link RilevazioneMisura#calcolaCarico(Double)}
 *   se il punto di monitoraggio è SCARICHI_IDRICI o ACQUE_METEORICHE e ha portata definita
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RilevazioneMisuraService {

    private final RilevazioneMisuraRepository repository;
    private final MonitoraggioRepository monitoraggioRepository;

    // ── Query ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RilevazioneMisura> findByParametro(Long parametroId) {
        return repository.findByParametroMonitoraggioIdOrderByDataCampionamentoDesc(parametroId);
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisura> findByMonitoraggio(Long monitoraggioId) {
        return repository.findByMonitoraggioId(monitoraggioId);
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisura> findByStabilimentoAndPeriodo(
            Long stabilimentoId, LocalDate dal, LocalDate al) {
        return repository.findByStabilimentoAndPeriodo(stabilimentoId, dal, al);
    }

    @Transactional(readOnly = true)
    public List<RilevazioneMisura> findNonConformi(Long stabilimentoId) {
        return repository.findNonConformiByStabilimento(stabilimentoId);
    }

    @Transactional(readOnly = true)
    public Optional<RilevazioneMisura> findById(Long id) {
        return repository.findById(id);
    }

    // ── Salvataggio ────────────────────────────────────────────────────────────

    /**
     * Salva una RilevazioneMisura applicando tutti i calcoli automatici.
     *
     * Ordine di elaborazione:
     * 1. Risolve il Monitoraggio dalla catena parametroMonitoraggio → monitoraggio
     * 2. Per EMISSIONI_ATMOSFERA: calcola flusso_massa se AnagraficaCamino disponibile
     * 3. Per SCARICHI_IDRICI / ACQUE_METEORICHE: calcola carico_inquinante_kg_d
     * 4. Salva (il @PrePersist/@PreUpdate gestisce calcolaConformita())
     */
    public RilevazioneMisura save(RilevazioneMisura rilevazione) {
        applicaCalcoliAutomatici(rilevazione);
        RilevazioneMisura saved = repository.save(rilevazione);
        log.debug("RilevazioneMisura salvata: id={}, parametro={}, valore={}, conformita={}",
                saved.getId(),
                saved.getParametroMonitoraggio() != null
                        ? saved.getParametroMonitoraggio().getNome() : "n/a",
                saved.getValoreMisurato(),
                saved.getStatoConformita());
        return saved;
    }

    public List<RilevazioneMisura> saveAll(List<RilevazioneMisura> rilevazioni) {
        rilevazioni.forEach(this::applicaCalcoliAutomatici);
        return repository.saveAll(rilevazioni);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // ── Calcoli automatici ─────────────────────────────────────────────────────

    /**
     * Applica i calcoli automatici di flusso_massa e carico_inquinante prima del salvataggio.
     * Naviga la catena: rilevazione → parametroMonitoraggio → monitoraggio → anagrafica/portata
     */
    private void applicaCalcoliAutomatici(RilevazioneMisura r) {
        if (r.getParametroMonitoraggio() == null) return;

        Monitoraggio mon = r.getParametroMonitoraggio().getMonitoraggio();
        if (mon == null) return;

        // Ricarica il monitoraggio con le relazioni necessarie (portata, anagrafica camino)
        Monitoraggio monitoraggio = monitoraggioRepository.findById(mon.getId()).orElse(mon);

        Monitoraggio.TipoMonitoraggio tipo = monitoraggio.getTipoMonitoraggio();

        if (tipo == Monitoraggio.TipoMonitoraggio.EMISSIONI_ATMOSFERA) {
            // flusso_massa solo se non già valorizzato manualmente
            if (r.getFlussoMassa() == null) {
                AnagraficaCamino camino = monitoraggio.getAnagraficaCamino();
                if (camino != null) {
                    // Preferisce i valori di oreGiorno/giorniAnno dal Monitoraggio (PMC-specifici)
                    // e fallback a quelli dell'AnagraficaCamino (AIA generali)
                    Integer ore = monitoraggio.getOreGiorno() != null
                            ? monitoraggio.getOreGiorno()
                            : camino.getDurataHGiorno();
                    Integer giorni = monitoraggio.getGiorniAnno() != null
                            ? monitoraggio.getGiorniAnno()
                            : camino.getDurataGAnno();

                    r.calcolaFlussoMassa(camino.getPortataNomc3h(), ore, giorni);

                    if (r.getFlussoMassa() != null) {
                        log.debug("Flusso massa calcolato per rilevazione parametro={}: {} kg/anno",
                                r.getParametroMonitoraggio().getNome(), r.getFlussoMassa());
                    }
                }
            }

        } else if (tipo == Monitoraggio.TipoMonitoraggio.SCARICHI_IDRICI
                || tipo == Monitoraggio.TipoMonitoraggio.ACQUE_METEORICHE) {
            // carico_inquinante solo se non già valorizzato manualmente
            if (r.getCaricoInquinanteKgd() == null) {
                r.calcolaCarico(monitoraggio.getPortataScaricoM3d());

                if (r.getCaricoInquinanteKgd() != null) {
                    log.debug("Carico inquinante calcolato per rilevazione parametro={}: {} kg/g",
                            r.getParametroMonitoraggio().getNome(), r.getCaricoInquinanteKgd());
                }
            }
        }
    }
}
