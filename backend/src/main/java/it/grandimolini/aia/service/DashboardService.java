package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.ConformitaTrendDTO;
import it.grandimolini.aia.dto.DashboardStatsDTO;
import it.grandimolini.aia.dto.KpiAmbientaleDTO;
import it.grandimolini.aia.dto.ScadenzaImminenteDTO;
import it.grandimolini.aia.dto.StabilimentoStatsDTO;
import it.grandimolini.aia.dto.StatoCampionamentoDTO;
import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.model.MovimentoRifiuto;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.RapportoProva;
import it.grandimolini.aia.model.RegistroMensile;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.VoceProduzione;
import it.grandimolini.aia.repository.DatiAmbientaliRepository;
import it.grandimolini.aia.repository.MonitoraggioRepository;
import it.grandimolini.aia.repository.MovimentoRifiutoRepository;
import it.grandimolini.aia.repository.PrescrizioneRepository;
import it.grandimolini.aia.repository.RapportoProvaRepository;
import it.grandimolini.aia.repository.RegistroMensileRepository;
import it.grandimolini.aia.repository.ScadenzaRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.security.StabilimentoAccessChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Autowired
    private PrescrizioneRepository prescrizioneRepository;

    @Autowired
    private ScadenzaRepository scadenzaRepository;

    @Autowired
    private DatiAmbientaliRepository datiAmbientaliRepository;

    @Autowired
    private MonitoraggioRepository monitoraggioRepository;

    @Autowired
    private RegistroMensileRepository registroMensileRepository;

    @Autowired
    private MovimentoRifiutoRepository movimentoRifiutoRepository;

    @Autowired
    private RapportoProvaRepository rapportoProvaRepository;

    @Autowired
    private StabilimentoAccessChecker stabilimentoAccessChecker;

    /**
     * Ottiene statistiche generali dashboard
     */
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        List<Stabilimento> stabilimenti = getAccessibleStabilimenti();
        List<Long> stabilimentiIds = stabilimenti.stream()
                .map(Stabilimento::getId)
                .collect(Collectors.toList());

        // Statistiche stabilimenti
        long totalStabilimenti = stabilimenti.size();
        long stabilimentiAttivi = stabilimenti.stream()
                .filter(Stabilimento::getAttivo)
                .count();

        // Statistiche prescrizioni
        List<Prescrizione> prescrizioni = stabilimentiIds.isEmpty()
                ? Collections.emptyList()
                : prescrizioneRepository.findByStabilimentoIdIn(stabilimentiIds);

        long totalPrescrizioni = prescrizioni.size();
        Map<String, Long> prescrizioniPerStato = prescrizioni.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStato() != null ? p.getStato().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        // Statistiche scadenze
        LocalDate oggi = LocalDate.now();
        LocalDate limite30Giorni = oggi.plusDays(30);

        List<Scadenza> scadenze = stabilimentiIds.isEmpty()
                ? Collections.emptyList()
                : scadenzaRepository.findByStabilimentoIdIn(stabilimentiIds);

        long scadenzeImminenti = scadenze.stream()
                .filter(s -> s.getDataScadenza() != null)
                .filter(s -> !s.getDataScadenza().isBefore(oggi))
                .filter(s -> s.getDataScadenza().isBefore(limite30Giorni))
                .filter(s -> s.getStato() != Scadenza.StatoScadenza.COMPLETATA)
                .count();

        long scadenzeScadute = scadenze.stream()
                .filter(s -> s.getDataScadenza() != null)
                .filter(s -> s.getDataScadenza().isBefore(oggi))
                .filter(s -> s.getStato() != Scadenza.StatoScadenza.COMPLETATA)
                .count();

        long scadenzeCompletate = scadenze.stream()
                .filter(s -> s.getStato() == Scadenza.StatoScadenza.COMPLETATA)
                .count();

        // Statistiche dati ambientali
        List<DatiAmbientali> datiAmbientali = stabilimentiIds.isEmpty()
                ? Collections.emptyList()
                : datiAmbientaliRepository.findByMonitoraggioStabilimentoIdIn(stabilimentiIds);

        long totalDatiAmbientali = datiAmbientali.size();
        long datiNonConformi = datiAmbientali.stream()
                .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.NON_CONFORME)
                .count();
        long datiInAttenzione = datiAmbientali.stream()
                .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.ATTENZIONE)
                .count();

        double percentualeConformita = totalDatiAmbientali > 0
                ? ((double) (totalDatiAmbientali - datiNonConformi) / totalDatiAmbientali) * 100
                : 100.0;

        return DashboardStatsDTO.builder()
                .totalStabilimenti(totalStabilimenti)
                .stabilimentiAttivi(stabilimentiAttivi)
                .totalPrescrizioni(totalPrescrizioni)
                .prescrizioniPerStato(prescrizioniPerStato)
                .scadenzeImminenti(scadenzeImminenti)
                .scadenzeScadute(scadenzeScadute)
                .scadenzeCompletate(scadenzeCompletate)
                .totalDatiAmbientali(totalDatiAmbientali)
                .datiNonConformi(datiNonConformi)
                .datiInAttenzione(datiInAttenzione)
                .percentualeConformita(Math.round(percentualeConformita * 100.0) / 100.0)
                .build();
    }

    /**
     * Ottiene statistiche per uno specifico stabilimento
     */
    @Transactional(readOnly = true)
    public StabilimentoStatsDTO getStabilimentoStats(Long stabilimentoId) {
        Stabilimento stabilimento = stabilimentoRepository.findById(stabilimentoId)
                .orElseThrow(() -> new RuntimeException("Stabilimento not found"));

        // Prescrizioni
        List<Prescrizione> prescrizioni = prescrizioneRepository.findByStabilimentoId(stabilimentoId);
        long prescrizioniAperte = prescrizioni.stream()
                .filter(p -> p.getStato() == Prescrizione.StatoPrescrizione.APERTA ||
                            p.getStato() == Prescrizione.StatoPrescrizione.IN_LAVORAZIONE)
                .count();
        long prescrizioniChiuse = prescrizioni.stream()
                .filter(p -> p.getStato() == Prescrizione.StatoPrescrizione.CHIUSA)
                .count();
        long prescrizioniUrgenti = prescrizioni.stream()
                .filter(p -> p.getPriorita() == Prescrizione.Priorita.URGENTE)
                .filter(p -> p.getStato() != Prescrizione.StatoPrescrizione.CHIUSA)
                .count();

        // Scadenze
        LocalDate oggi = LocalDate.now();
        LocalDate limite30Giorni = oggi.plusDays(30);

        List<Scadenza> scadenze = scadenzaRepository.findByStabilimentoId(stabilimentoId);
        long scadenzeImminenti = scadenze.stream()
                .filter(s -> s.getDataScadenza() != null)
                .filter(s -> !s.getDataScadenza().isBefore(oggi))
                .filter(s -> s.getDataScadenza().isBefore(limite30Giorni))
                .filter(s -> s.getStato() != Scadenza.StatoScadenza.COMPLETATA)
                .count();
        long scadenzeScadute = scadenze.stream()
                .filter(s -> s.getDataScadenza() != null)
                .filter(s -> s.getDataScadenza().isBefore(oggi))
                .filter(s -> s.getStato() != Scadenza.StatoScadenza.COMPLETATA)
                .count();

        // Dati ambientali
        List<DatiAmbientali> datiAmbientali = datiAmbientaliRepository
                .findByMonitoraggioStabilimentoId(stabilimentoId);
        long totalDatiAmbientali = datiAmbientali.size();
        long datiNonConformi = datiAmbientali.stream()
                .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.NON_CONFORME)
                .count();
        double percentualeConformita = totalDatiAmbientali > 0
                ? ((double) (totalDatiAmbientali - datiNonConformi) / totalDatiAmbientali) * 100
                : 100.0;

        // Monitoraggi
        long totalMonitoraggi = monitoraggioRepository.findByStabilimentoId(stabilimentoId).size();
        long monitoraggiAttivi = monitoraggioRepository.findByStabilimentoId(stabilimentoId).stream()
                .filter(m -> m.getAttivo())
                .count();

        return StabilimentoStatsDTO.builder()
                .stabilimentoId(stabilimentoId)
                .stabilimentoNome(stabilimento.getNome())
                .prescrizioniAperte(prescrizioniAperte)
                .prescrizioniChiuse(prescrizioniChiuse)
                .prescrizioniUrgenti(prescrizioniUrgenti)
                .scadenzeImminenti(scadenzeImminenti)
                .scadenzeScadute(scadenzeScadute)
                .totalDatiAmbientali(totalDatiAmbientali)
                .datiNonConformi(datiNonConformi)
                .percentualeConformita(Math.round(percentualeConformita * 100.0) / 100.0)
                .totalMonitoraggi(totalMonitoraggi)
                .monitoraggiAttivi(monitoraggiAttivi)
                .build();
    }

    /**
     * Ottiene lista scadenze imminenti
     */
    @Transactional(readOnly = true)
    public List<ScadenzaImminenteDTO> getScadenzeImminenti(int giorni) {
        List<Stabilimento> stabilimenti = getAccessibleStabilimenti();
        List<Long> stabilimentiIds = stabilimenti.stream()
                .map(Stabilimento::getId)
                .collect(Collectors.toList());

        if (stabilimentiIds.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate oggi = LocalDate.now();
        LocalDate limiteData = oggi.plusDays(giorni);

        List<Scadenza> scadenze = scadenzaRepository.findByStabilimentoIdIn(stabilimentiIds);

        return scadenze.stream()
                .filter(s -> s.getDataScadenza() != null)
                .filter(s -> !s.getDataScadenza().isBefore(oggi))
                .filter(s -> s.getDataScadenza().isBefore(limiteData))
                .filter(s -> s.getStato() != Scadenza.StatoScadenza.COMPLETATA)
                .sorted(Comparator.comparing(Scadenza::getDataScadenza))
                .map(this::convertToScadenzaImminenteDTO)
                .collect(Collectors.toList());
    }

    /**
     * Ottiene trend conformità per gli ultimi 12 mesi
     */
    @Transactional(readOnly = true)
    public List<ConformitaTrendDTO> getConformitaTrend(int mesi) {
        List<Stabilimento> stabilimenti = getAccessibleStabilimenti();
        List<Long> stabilimentiIds = stabilimenti.stream()
                .map(Stabilimento::getId)
                .collect(Collectors.toList());

        if (stabilimentiIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<DatiAmbientali> datiAmbientali = datiAmbientaliRepository
                .findByMonitoraggioStabilimentoIdIn(stabilimentiIds);

        LocalDate dataInizio = LocalDate.now().minusMonths(mesi);

        // Filtra dati degli ultimi N mesi
        List<DatiAmbientali> datiRecenti = datiAmbientali.stream()
                .filter(d -> d.getDataCampionamento() != null)
                .filter(d -> !d.getDataCampionamento().isBefore(dataInizio))
                .collect(Collectors.toList());

        // Raggruppa per anno/mese
        Map<String, List<DatiAmbientali>> datiPerMese = datiRecenti.stream()
                .collect(Collectors.groupingBy(d ->
                        d.getDataCampionamento().getYear() + "-" +
                        String.format("%02d", d.getDataCampionamento().getMonthValue())
                ));

        // Crea DTOs ordinati
        return datiPerMese.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int anno = Integer.parseInt(parts[0]);
                    int mese = Integer.parseInt(parts[1]);
                    List<DatiAmbientali> datiMese = entry.getValue();

                    long totale = datiMese.size();
                    long conformi = datiMese.stream()
                            .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.CONFORME)
                            .count();
                    long inAttenzione = datiMese.stream()
                            .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.ATTENZIONE)
                            .count();
                    long nonConformi = datiMese.stream()
                            .filter(d -> d.getStatoConformita() == DatiAmbientali.StatoConformita.NON_CONFORME)
                            .count();

                    double percentuale = totale > 0
                            ? ((double) conformi / totale) * 100
                            : 100.0;

                    return ConformitaTrendDTO.builder()
                            .anno(anno)
                            .mese(mese)
                            .totaleMisurazioni(totale)
                            .conformi(conformi)
                            .inAttenzione(inAttenzione)
                            .nonConformi(nonConformi)
                            .percentualeConformita(Math.round(percentuale * 100.0) / 100.0)
                            .build();
                })
                .sorted(Comparator.comparing(ConformitaTrendDTO::getAnno)
                        .thenComparing(ConformitaTrendDTO::getMese))
                .collect(Collectors.toList());
    }

    /**
     * KPI ambientali per tutti gli stabilimenti accessibili nell'anno indicato
     */
    @Transactional(readOnly = true)
    public List<KpiAmbientaleDTO> getKpiAmbientali(int anno) {
        List<Stabilimento> stabilimenti = getAccessibleStabilimenti();
        return stabilimenti.stream()
                .map(s -> computeKpi(s, anno))
                .collect(Collectors.toList());
    }

    private KpiAmbientaleDTO computeKpi(Stabilimento stabilimento, int anno) {
        // ── Consumi da RegistroMensile ──────────────────────────────────────
        List<RegistroMensile> registri =
                registroMensileRepository.findByStabilimentoIdAndAnnoOrderByMeseAsc(stabilimento.getId(), anno);

        double acqua = 0, elettrico = 0, gas = 0, materiaPrima = 0;

        for (RegistroMensile r : registri) {
            for (VoceProduzione v : r.getVoci()) {
                if (v.getQuantita() == null) continue;
                switch (v.getCategoria()) {
                    case ACQUA               -> acqua        += v.getQuantita();
                    case ENERGIA_ELETTRICA   -> elettrico    += v.getQuantita();
                    case GAS_NATURALE        -> gas          += v.getQuantita();
                    case MATERIA_PRIMA       -> materiaPrima += v.getQuantita();
                    default -> {}
                }
            }
        }

        double idricoSpec   = materiaPrima > 0 ? acqua / materiaPrima : 0;
        double elettricoSpec = materiaPrima > 0 ? elettrico / materiaPrima : 0;
        double gasSpec       = materiaPrima > 0 ? gas / materiaPrima : 0;

        // ── Rifiuti da MovimentoRifiuto ─────────────────────────────────────
        List<MovimentoRifiuto> movimenti =
                movimentoRifiutoRepository.findByStabilimentoAndAnno(stabilimento.getId(), anno);

        double rifiutiTot = movimenti.stream()
                .filter(m -> m.getTipoMovimento() == MovimentoRifiuto.TipoMovimento.PRODUZIONE
                          || m.getTipoMovimento() == MovimentoRifiuto.TipoMovimento.DEPOSITO_TEMPORANEO)
                .filter(m -> m.getQuantita() != null)
                .mapToDouble(MovimentoRifiuto::getQuantita)
                .sum();

        double rifiutiPer = movimenti.stream()
                .filter(m -> m.getTipoMovimento() == MovimentoRifiuto.TipoMovimento.PRODUZIONE
                          || m.getTipoMovimento() == MovimentoRifiuto.TipoMovimento.DEPOSITO_TEMPORANEO)
                .filter(m -> m.getQuantita() != null)
                .filter(m -> m.getCodiceRifiuto() != null
                          && Boolean.TRUE.equals(m.getCodiceRifiuto().getPericoloso()))
                .mapToDouble(MovimentoRifiuto::getQuantita)
                .sum();

        double limTot = 30.0;
        double limPer = 10.0;

        return KpiAmbientaleDTO.builder()
                .stabilimentoId(stabilimento.getId())
                .stabilimentoNome(stabilimento.getNome())
                .annoRiferimento(anno)
                .consumoIdrico(round2(acqua))
                .consumoEletrico(round2(elettrico))
                .consumoGas(round2(gas))
                .produzioneTotale(round2(materiaPrima))
                .consumoIdricoSpecifico(round2(idricoSpec))
                .consumoEletricoSpecifico(round2(elettricoSpec))
                .consumoGasSpecifico(round2(gasSpec))
                .rifiutiTotali(round2(rifiutiTot))
                .rifiutiPericolosi(round2(rifiutiPer))
                .limiteRifiutiTotali(limTot)
                .limiteRifiutiPericolosi(limPer)
                .rifiutiTotaliOk(rifiutiTot <= limTot)
                .rifiutiPericolosiOk(rifiutiPer <= limPer)
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Stato campionamenti per tutti i monitoraggi attivi degli stabilimenti accessibili
     */
    @Transactional(readOnly = true)
    public List<StatoCampionamentoDTO> getStatoCampionamenti() {
        List<Stabilimento> stabilimenti = getAccessibleStabilimenti();
        LocalDate oggi = LocalDate.now();
        int annoCorrente = oggi.getYear();

        return stabilimenti.stream()
                .flatMap(s -> monitoraggioRepository.findByStabilimentoId(s.getId()).stream()
                        .filter(Monitoraggio::getAttivo)
                        .map(m -> buildStatoCampionamento(m, s, oggi, annoCorrente)))
                .sorted(Comparator.comparing(StatoCampionamentoDTO::getStabilimentoNome)
                        .thenComparing(StatoCampionamentoDTO::getCodice))
                .collect(Collectors.toList());
    }

    private StatoCampionamentoDTO buildStatoCampionamento(
            Monitoraggio m, Stabilimento s, LocalDate oggi, int annoCorrente) {

        // Ultimo rapporto di prova per questo monitoraggio nell'anno corrente
        List<RapportoProva> rapporti =
                rapportoProvaRepository.findByMonitoraggioAndAnno(m.getId(), annoCorrente);

        LocalDate dataUltimo = null;
        String numeroUltimo = null;
        RapportoProva.ConformitaGlobale conformita = null;

        if (!rapporti.isEmpty()) {
            RapportoProva ultimo = rapporti.get(rapporti.size() - 1);
            dataUltimo = ultimo.getDataCampionamento();
            numeroUltimo = ultimo.getNumeroRapporto();
            conformita = ultimo.getConformitaGlobale();
        }

        // Giorni alla scadenza
        LocalDate prossimaScadenza = m.getProssimaScadenza();
        // Se non valorizzata nel DB, calcola dinamicamente da frequenza + ultimo campionamento
        if (prossimaScadenza == null && m.getFrequenza() != null) {
            LocalDate base = (dataUltimo != null) ? dataUltimo : oggi;
            prossimaScadenza = calcolaProssimaScadenza(base, m.getFrequenza());
        }
        Integer giorniAllaScadenza = null;
        if (prossimaScadenza != null) {
            giorniAllaScadenza = (int) java.time.temporal.ChronoUnit.DAYS.between(oggi, prossimaScadenza);
        }

        // Logica semaforo
        String colore;
        String descrizione;

        if (dataUltimo == null) {
            // Nessun campionamento nell'anno corrente
            colore = "ROSSO";
            descrizione = "Non campionato nel " + annoCorrente;
        } else if (prossimaScadenza != null && prossimaScadenza.isBefore(oggi)) {
            // Scadenza già superata
            colore = "ROSSO";
            descrizione = "Scadenza superata il " + prossimaScadenza;
        } else if (giorniAllaScadenza != null && giorniAllaScadenza <= 30) {
            // Scadenza entro 30 giorni
            colore = "GIALLO";
            descrizione = "Scadenza tra " + giorniAllaScadenza + " giorni";
        } else {
            // Tutto ok
            colore = "VERDE";
            descrizione = "Ultimo: " + dataUltimo;
        }

        return StatoCampionamentoDTO.builder()
                .monitoraggioId(m.getId())
                .codice(m.getCodice())
                .descrizione(m.getDescrizione())
                .tipoMonitoraggio(m.getTipoMonitoraggio())
                .frequenza(m.getFrequenza())
                .stabilimentoId(s.getId())
                .stabilimentoNome(s.getNome())
                .dataUltimoCampionamento(dataUltimo)
                .numeroUltimoRapporto(numeroUltimo)
                .conformitaUltimo(conformita)
                .prossimaScadenza(prossimaScadenza)
                .giorniAllaScadenza(giorniAllaScadenza)
                .statoColore(colore)
                .statoDescrizione(descrizione)
                .build();
    }

    // Helper methods

    private List<Stabilimento> getAccessibleStabilimenti() {
        if (stabilimentoAccessChecker.isAdmin()) {
            return stabilimentoRepository.findAll();
        } else {
            List<Long> ids = stabilimentoAccessChecker.getCurrentUserStabilimentoIds();
            return stabilimentoRepository.findAllById(ids);
        }
    }

    private LocalDate calcolaProssimaScadenza(LocalDate base, Monitoraggio.FrequenzaMonitoraggio frequenza) {
        return switch (frequenza) {
            case GIORNALIERA -> base.plusDays(1);
            case SETTIMANALE -> base.plusWeeks(1);
            case MENSILE     -> base.plusMonths(1);
            case BIMESTRALE  -> base.plusMonths(2);
            case TRIMESTRALE -> base.plusMonths(3);
            case SEMESTRALE  -> base.plusMonths(6);
            case ANNUALE     -> base.plusYears(1);
            case BIENNALE    -> base.plusYears(2);
            case TRIENNALE   -> base.plusYears(3);
        };
    }

    private ScadenzaImminenteDTO convertToScadenzaImminenteDTO(Scadenza scadenza) {
        LocalDate oggi = LocalDate.now();
        int giorniRimanenti = (int) ChronoUnit.DAYS.between(oggi, scadenza.getDataScadenza());

        return ScadenzaImminenteDTO.builder()
                .id(scadenza.getId())
                .titolo(scadenza.getTitolo())
                .dataScadenza(scadenza.getDataScadenza())
                .giorniRimanenti(giorniRimanenti)
                .tipoScadenza(scadenza.getTipoScadenza())
                .stato(scadenza.getStato())
                .priorita(scadenza.getPriorita())
                .stabilimentoNome(scadenza.getStabilimento() != null
                        ? scadenza.getStabilimento().getNome()
                        : null)
                .responsabile(scadenza.getResponsabile())
                .build();
    }
}
