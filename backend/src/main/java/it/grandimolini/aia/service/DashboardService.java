package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.ConformitaTrendDTO;
import it.grandimolini.aia.dto.DashboardStatsDTO;
import it.grandimolini.aia.dto.ScadenzaImminenteDTO;
import it.grandimolini.aia.dto.StabilimentoStatsDTO;
import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.DatiAmbientaliRepository;
import it.grandimolini.aia.repository.MonitoraggioRepository;
import it.grandimolini.aia.repository.PrescrizioneRepository;
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

    // Helper methods

    private List<Stabilimento> getAccessibleStabilimenti() {
        if (stabilimentoAccessChecker.isAdmin()) {
            return stabilimentoRepository.findAll();
        } else {
            User currentUser = stabilimentoAccessChecker.getCurrentUser();
            return new ArrayList<>(currentUser.getStabilimenti());
        }
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
