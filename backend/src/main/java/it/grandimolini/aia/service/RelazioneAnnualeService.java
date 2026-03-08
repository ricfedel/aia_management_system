package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.RelazioneAnnualeDTO;
import it.grandimolini.aia.dto.RelazioneAnnualeDTO.*;
import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelazioneAnnualeService {

    private final StabilimentoRepository      stabilimentoRepo;
    private final PrescrizioneRepository      prescrizioneRepo;
    private final MonitoraggioRepository      monitoraggioRepo;
    private final RilevazioneMisuraRepository  rilevRepo;
    private final RegistroMensileRepository   registroRepo;
    private final MovimentoRifiutoRepository  movimentoRepo;
    private final CodiceRifiutoRepository     cerRepo;
    private final ComunicazioneEnteRepository comunicazioniRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ──────────────────────────────────────────────────────────────────────
    // Metodo principale: costruisce il DTO di preview (dati aggregati JSON)
    // ──────────────────────────────────────────────────────────────────────
    public RelazioneAnnualeDTO buildPreview(Long stabilimentoId, int anno) {

        Stabilimento stab = stabilimentoRepo.findById(stabilimentoId)
                .orElseThrow(() -> new RuntimeException("Stabilimento non trovato: " + stabilimentoId));

        RelazioneAnnualeDTO dto = new RelazioneAnnualeDTO();
        dto.setStabilimentoId(stabilimentoId);
        dto.setStabilimentoNome(stab.getNome());
        dto.setStabilimentoIndirizzo(stab.getIndirizzo() != null
                ? stab.getIndirizzo() + (stab.getCitta() != null ? ", " + stab.getCitta() : "")
                : "");
        dto.setStabilimentoCodiceAIA(stab.getNumeroAIA());
        dto.setAnno(anno);
        dto.setDataGenerazione(LocalDate.now().format(FMT));

        buildPrescrizioni(dto, stabilimentoId);
        buildMonitoraggio(dto, stabilimentoId);
        buildConformita(dto, stabilimentoId, anno);
        buildRifiuti(dto, stabilimentoId, anno);
        buildProduzione(dto, stabilimentoId, anno);
        buildComunicazioni(dto, stabilimentoId, anno);

        return dto;
    }

    // ──────────────────────────────────────────────────────────────────────
    // Genera il file .docx
    // ──────────────────────────────────────────────────────────────────────
    public byte[] generateDocx(Long stabilimentoId, int anno) throws IOException {
        RelazioneAnnualeDTO data = buildPreview(stabilimentoId, anno);
        return renderDocx(data);
    }

    // ══════════════════════════════════════════════════════════════════════
    // SEZIONI DI AGGREGAZIONE DATI
    // ══════════════════════════════════════════════════════════════════════

    private void buildPrescrizioni(RelazioneAnnualeDTO dto, Long stabId) {
        List<Prescrizione> all = prescrizioneRepo.findByStabilimentoId(stabId);
        LocalDate oggi = LocalDate.now();
        LocalDate tra30 = oggi.plusDays(30);

        int scadute = 0, inScadenza = 0;
        List<PrescrRow> rows = new ArrayList<>();

        for (Prescrizione p : all) {
            if (p.getDataScadenza() != null) {
                if (p.getDataScadenza().isBefore(oggi))          scadute++;
                else if (!p.getDataScadenza().isAfter(tra30))    inScadenza++;
            }
            PrescrRow row = new PrescrRow();
            row.setId(p.getId());
            row.setNumero(p.getCodice());
            row.setDescrizione(p.getDescrizione());
            row.setTipo(p.getMatriceAmbientale() != null ? p.getMatriceAmbientale().name() : "");
            row.setStato(p.getStato() != null ? p.getStato().name() : "");
            row.setDataScadenza(p.getDataScadenza() != null ? p.getDataScadenza().format(FMT) : "");
            rows.add(row);
        }

        dto.setTotalePrescrizioni(all.size());
        dto.setPrescrizioniScadute(scadute);
        dto.setPrescrizioniInScadenza(inScadenza);
        dto.setPrescrizioni(rows);
    }

    private void buildMonitoraggio(RelazioneAnnualeDTO dto, Long stabId) {
        List<Monitoraggio> punti = monitoraggioRepo.findByStabilimentoId(stabId);
        List<MonRow> rows = new ArrayList<>();

        for (Monitoraggio m : punti) {
            if (Boolean.FALSE.equals(m.getAttivo())) continue;
            MonRow row = new MonRow();
            row.setId(m.getId());
            row.setCodice(m.getCodice());
            row.setDescrizione(m.getDescrizione());
            row.setTipo(m.getTipoMonitoraggio() != null ? m.getTipoMonitoraggio().name() : "");
            row.setFrequenza(m.getFrequenza() != null ? m.getFrequenza().name() : "");
            row.setNumParametri(m.getParametri() != null ? m.getParametri().size() : 0);
            row.setParametriNomi(m.getParametri() != null
                    ? m.getParametri().stream()
                        .filter(p -> Boolean.TRUE.equals(p.getAttivo()) || p.getAttivo() == null)
                        .map(p -> p.getNome() + (p.getLimiteValore() != null
                                ? " (≤ " + p.getLimiteValore() + " " + nvl(p.getLimiteUnita(), p.getUnitaMisura()) + ")"
                                : ""))
                        .collect(Collectors.toList())
                    : List.of());
            rows.add(row);
        }

        dto.setTotalePuntiMonitoraggio(rows.size());
        dto.setPuntiMonitoraggio(rows);
    }

    private void buildConformita(RelazioneAnnualeDTO dto, Long stabId, int anno) {
        LocalDate from = LocalDate.of(anno, 1, 1);
        LocalDate to   = LocalDate.of(anno, 12, 31);
        List<RilevazioneMisura> rilev = rilevRepo.findByStabilimentoAndPeriodo(stabId, from, to);

        long conformi    = rilev.stream().filter(r -> r.getStatoConformita() == RilevazioneMisura.StatoConformita.CONFORME).count();
        long attenzione  = rilev.stream().filter(r -> r.getStatoConformita() == RilevazioneMisura.StatoConformita.ATTENZIONE).count();
        long nonConformi = rilev.stream().filter(r -> r.getStatoConformita() == RilevazioneMisura.StatoConformita.NON_CONFORME).count();
        long totale      = rilev.size();

        dto.setTotaleRilevazioni(totale);
        dto.setRilevConformi(conformi);
        dto.setRilevAttenzione(attenzione);
        dto.setRilevNonConformi(nonConformi);
        dto.setPercConformita(totale > 0 ? Math.round(conformi * 1000.0 / totale) / 10.0 : 0);

        // Top non conformi ordinati per rapporto valore/limite decrescente
        List<ConformRow> topNc = rilev.stream()
                .filter(r -> r.getStatoConformita() == RilevazioneMisura.StatoConformita.NON_CONFORME
                          || r.getStatoConformita() == RilevazioneMisura.StatoConformita.ATTENZIONE)
                .sorted(Comparator.comparingDouble((RilevazioneMisura r) -> {
                    ParametroMonitoraggio p = r.getParametroMonitoraggio();
                    double lim = (p != null && p.getLimiteValore() != null && p.getLimiteValore() != 0)
                            ? p.getLimiteValore() : 1.0;
                    return r.getValoreMisurato() / lim;
                }).reversed())
                .limit(10)
                .map(r -> {
                    ParametroMonitoraggio p = r.getParametroMonitoraggio();
                    Monitoraggio m          = p != null ? p.getMonitoraggio() : null;
                    ConformRow row = new ConformRow();
                    row.setMonitoraggioCodice(m != null ? m.getCodice() : "");
                    row.setParametroNome(p != null ? p.getNome() : "");
                    row.setUnita(r.getUnitaMisura());
                    row.setValoreMisurato(r.getValoreMisurato());
                    double lim = (p != null && p.getLimiteValore() != null) ? p.getLimiteValore() : 0;
                    row.setLimiteValore(lim != 0 ? lim : null);
                    row.setPercLimite(lim != 0 ? Math.round(r.getValoreMisurato() / lim * 1000) / 10.0 : 0);
                    row.setStato(r.getStatoConformita() != null ? r.getStatoConformita().name() : "");
                    row.setDataUltimaRilev(r.getDataCampionamento() != null
                            ? r.getDataCampionamento().format(FMT) : "");
                    return row;
                })
                .collect(Collectors.toList());

        dto.setTopNonConformi(topNc);
    }

    private void buildRifiuti(RelazioneAnnualeDTO dto, Long stabId, int anno) {
        List<CodiceRifiuto> codici = cerRepo.findByStabilimentoIdAndAttivoTrueOrderByCodiceCerAsc(stabId);
        List<RifiutoRow> rows = new ArrayList<>();

        for (CodiceRifiuto cer : codici) {
            List<MovimentoRifiuto> movAnno = movimentoRepo
                    .findByCodiceRifiutoIdAndAnnoOrderByMeseAscTipoMovimentoAsc(cer.getId(), anno);

            if (movAnno.isEmpty()) continue;

            RifiutoRow row = new RifiutoRow();
            row.setCodiceCer(cer.getCodiceCer());
            row.setDescrizione(cer.getDescrizione());
            row.setPericoloso(Boolean.TRUE.equals(cer.getPericoloso()));
            row.setUnita(cer.getUnitaMisura());
            row.setQProdotta(sum(movAnno, MovimentoRifiuto.TipoMovimento.PRODUZIONE));
            row.setQSmaltita(sum(movAnno, MovimentoRifiuto.TipoMovimento.SMALTIMENTO));
            row.setQRecuperata(sum(movAnno, MovimentoRifiuto.TipoMovimento.RECUPERO));
            row.setQCeduta(sum(movAnno, MovimentoRifiuto.TipoMovimento.CESSIONE_TERZI));
            rows.add(row);
        }

        dto.setRifiutiRiepilogo(rows);
    }

    private void buildProduzione(RelazioneAnnualeDTO dto, Long stabId, int anno) {
        List<RegistroMensile> registri = registroRepo.findByStabilimentoIdAndAnnoOrderByMeseAsc(stabId, anno);

        List<String> mesiConDati = registri.stream()
                .map(r -> monthName(r.getMese()))
                .collect(Collectors.toList());
        dto.setMesiConDati(mesiConDati);

        // Aggrego le voci per descrizione/codice su tutti i mesi
        Map<String, ProdRow> aggreg = new LinkedHashMap<>();

        for (RegistroMensile reg : registri) {
            if (reg.getVoci() == null) continue;
            for (VoceProduzione v : reg.getVoci()) {
                String key = v.getCodice() != null ? v.getCodice() : v.getDescrizione();
                if (!aggreg.containsKey(key)) {
                    ProdRow row = new ProdRow();
                    row.setCategoria(v.getCategoria() != null ? v.getCategoria().name() : "");
                    row.setDescrizione(v.getDescrizione());
                    row.setCodice(v.getCodice());
                    row.setUnita(v.getUnitaMisura());
                    row.setTotaleAnno(0.0);
                    row.setTotaleAnnoPrecedente(0.0);
                    aggreg.put(key, row);
                }
                ProdRow row = aggreg.get(key);
                if (v.getQuantita() != null) row.setTotaleAnno(row.getTotaleAnno() + v.getQuantita());
                if (v.getQuantitaAnnoPrecedente() != null)
                    row.setTotaleAnnoPrecedente(row.getTotaleAnnoPrecedente() + v.getQuantitaAnnoPrecedente());
            }
        }

        // Calcola variazione %
        for (ProdRow row : aggreg.values()) {
            if (row.getTotaleAnnoPrecedente() != null && row.getTotaleAnnoPrecedente() != 0) {
                double var = (row.getTotaleAnno() - row.getTotaleAnnoPrecedente()) / row.getTotaleAnnoPrecedente() * 100;
                row.setVariazione(Math.round(var * 10) / 10.0);
            }
        }

        dto.setProduzioneRiepilogo(new ArrayList<>(aggreg.values()));
    }

    private void buildComunicazioni(RelazioneAnnualeDTO dto, Long stabId, int anno) {
        LocalDate from = LocalDate.of(anno, 1, 1);
        LocalDate to   = LocalDate.of(anno, 12, 31);
        List<ComunicazioneEnte> comms = comunicazioniRepo
                .findFiltered(stabId, null, null, from, to);

        long inviate       = comms.stream().filter(c -> c.getStato() != ComunicazioneEnte.StatoComunicazione.BOZZA).count();
        long conRiscontro  = comms.stream().filter(c -> Boolean.TRUE.equals(c.getHasRiscontro())).count();

        dto.setTotaleComunicazioni(comms.size());
        dto.setComunicazioniInviate((int) inviate);
        dto.setComunicazioniConRiscontro((int) conRiscontro);

        List<ComRow> rows = comms.stream().map(c -> {
            ComRow row = new ComRow();
            row.setId(c.getId());
            row.setTipo(c.getTipo() != null ? c.getTipo().name() : "");
            row.setEnte(c.getEnte() != null ? c.getEnte().name() : "");
            row.setOggetto(c.getOggetto());
            row.setDataInvio(c.getDataInvio() != null ? c.getDataInvio().format(FMT) : "");
            row.setStato(c.getStato() != null ? c.getStato().name() : "");
            row.setHasRiscontro(Boolean.TRUE.equals(c.getHasRiscontro()));
            return row;
        }).collect(Collectors.toList());

        dto.setComunicazioni(rows);
    }

    // ══════════════════════════════════════════════════════════════════════
    // RENDERING DOCX
    // ══════════════════════════════════════════════════════════════════════

    private byte[] renderDocx(RelazioneAnnualeDTO d) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Frontespizio ────────────────────────────────────────────
            addCenteredTitle(doc, "RELAZIONE ANNUALE AIA", 20, true);
            addCenteredTitle(doc, "Grandi Molini Italiani S.p.A.", 14, false);
            addCenteredTitle(doc, "Stabilimento: " + d.getStabilimentoNome(), 14, false);
            addCenteredTitle(doc, "Anno " + d.getAnno(), 14, false);
            addEmptyParagraph(doc);
            addCenteredTitle(doc, "Generata il: " + d.getDataGenerazione(), 10, false);
            addPageBreakParagraph(doc);

            // ── Sezione 1: Stabilimento ─────────────────────────────────
            addH1(doc, "1. INFORMAZIONI STABILIMENTO");
            addTableTwoCol(doc, new String[][] {
                {"Ragione Sociale",       "Grandi Molini Italiani S.p.A."},
                {"Stabilimento",          d.getStabilimentoNome()},
                {"Indirizzo",             nvl(d.getStabilimentoIndirizzo(), "—")},
                {"Numero AIA",            nvl(d.getStabilimentoCodiceAIA(), "—")},
                {"Anno di riferimento",   String.valueOf(d.getAnno())},
            });
            addEmptyParagraph(doc);

            // ── Sezione 2: Prescrizioni ─────────────────────────────────
            addH1(doc, "2. PRESCRIZIONI AIA");
            addTableTwoCol(doc, new String[][] {
                {"Totale prescrizioni",           String.valueOf(d.getTotalePrescrizioni())},
                {"Prescrizioni scadute",          String.valueOf(d.getPrescrizioniScadute())},
                {"In scadenza entro 30 giorni",   String.valueOf(d.getPrescrizioniInScadenza())},
            });
            addEmptyParagraph(doc);

            if (d.getPrescrizioni() != null && !d.getPrescrizioni().isEmpty()) {
                addH2(doc, "2.1 Elenco Prescrizioni");
                String[] pHdr = {"N°", "Codice", "Descrizione", "Matrice", "Stato", "Scadenza"};
                List<String[]> pRows = d.getPrescrizioni().stream()
                        .map(p -> new String[] {
                            String.valueOf(p.getId()),
                            nvl(p.getNumero(), ""),
                            truncate(p.getDescrizione(), 80),
                            nvl(p.getTipo(), ""),
                            nvl(p.getStato(), ""),
                            nvl(p.getDataScadenza(), "")
                        }).collect(Collectors.toList());
                addTable(doc, pHdr, pRows);
                addEmptyParagraph(doc);
            }

            // ── Sezione 3: Piano di Monitoraggio ────────────────────────
            addH1(doc, "3. PIANO DI MONITORAGGIO E CONTROLLO (PMC)");
            addParagraph(doc, "Punti di monitoraggio attivi: " + d.getTotalePuntiMonitoraggio());
            addEmptyParagraph(doc);

            if (d.getPuntiMonitoraggio() != null && !d.getPuntiMonitoraggio().isEmpty()) {
                addH2(doc, "3.1 Punti di Monitoraggio");
                String[] mHdr = {"Codice", "Descrizione", "Tipo", "Frequenza", "N° Parametri"};
                List<String[]> mRows = d.getPuntiMonitoraggio().stream()
                        .map(m -> new String[] {
                            nvl(m.getCodice(), ""),
                            nvl(m.getDescrizione(), ""),
                            nvl(m.getTipo(), ""),
                            nvl(m.getFrequenza(), ""),
                            String.valueOf(m.getNumParametri())
                        }).collect(Collectors.toList());
                addTable(doc, mHdr, mRows);
                addEmptyParagraph(doc);
            }

            // ── Sezione 4: Conformità ────────────────────────────────────
            addH1(doc, "4. CONFORMITÀ PARAMETRI AMBIENTALI");
            addTableTwoCol(doc, new String[][] {
                {"Rilevazioni totali nell'anno",  String.valueOf(d.getTotaleRilevazioni())},
                {"Conformi",                      d.getRilevConformi() + " (" + d.getPercConformita() + "%)"},
                {"In attenzione (80-100% limite)", String.valueOf(d.getRilevAttenzione())},
                {"Non conformi (>100% limite)",   String.valueOf(d.getRilevNonConformi())},
            });
            addEmptyParagraph(doc);

            if (d.getTopNonConformi() != null && !d.getTopNonConformi().isEmpty()) {
                addH2(doc, "4.1 Parametri critici / non conformi");
                String[] cHdr = {"Punto", "Parametro", "Valore", "Limite", "% Limite", "Stato", "Data"};
                List<String[]> cRows = d.getTopNonConformi().stream()
                        .map(c -> new String[] {
                            nvl(c.getMonitoraggioCodice(), ""),
                            nvl(c.getParametroNome(), ""),
                            fmt(c.getValoreMisurato()) + " " + nvl(c.getUnita(), ""),
                            c.getLimiteValore() != null ? String.valueOf(c.getLimiteValore()) : "—",
                            c.getPercLimite() + "%",
                            nvl(c.getStato(), ""),
                            nvl(c.getDataUltimaRilev(), "")
                        }).collect(Collectors.toList());
                addTable(doc, cHdr, cRows);
                addEmptyParagraph(doc);
            }

            // ── Sezione 5: Gestione Rifiuti ──────────────────────────────
            addH1(doc, "5. GESTIONE RIFIUTI");

            if (d.getRifiutiRiepilogo() != null && !d.getRifiutiRiepilogo().isEmpty()) {
                String[] rHdr = {"CER", "Descrizione", "P", "Prodotta", "Smaltita", "Recuperata", "Ceduta", "U.M."};
                List<String[]> rRows = d.getRifiutiRiepilogo().stream()
                        .map(r -> new String[] {
                            nvl(r.getCodiceCer(), ""),
                            truncate(r.getDescrizione(), 40),
                            r.isPericoloso() ? "P" : "",
                            fmt(r.getQProdotta()),
                            fmt(r.getQSmaltita()),
                            fmt(r.getQRecuperata()),
                            fmt(r.getQCeduta()),
                            nvl(r.getUnita(), "")
                        }).collect(Collectors.toList());
                addTable(doc, rHdr, rRows);
            } else {
                addParagraph(doc, "Nessun dato di movimentazione rifiuti registrato per l'anno " + d.getAnno() + ".");
            }
            addEmptyParagraph(doc);

            // ── Sezione 6: Produzione e Consumi ──────────────────────────
            addH1(doc, "6. PRODUZIONE E CONSUMI");

            if (d.getMesiConDati() != null && !d.getMesiConDati().isEmpty()) {
                addParagraph(doc, "Mesi con dati registrati: " + String.join(", ", d.getMesiConDati()));
            }
            addEmptyParagraph(doc);

            if (d.getProduzioneRiepilogo() != null && !d.getProduzioneRiepilogo().isEmpty()) {
                String[] prHdr = {"Categoria", "Voce", "Totale Anno", "Anno Prec.", "Δ%", "U.M."};
                List<String[]> prRows = d.getProduzioneRiepilogo().stream()
                        .map(p -> new String[] {
                            nvl(p.getCategoria(), ""),
                            nvl(p.getDescrizione(), ""),
                            fmt(p.getTotaleAnno()),
                            fmt(p.getTotaleAnnoPrecedente()),
                            p.getVariazione() != null ? p.getVariazione() + "%" : "—",
                            nvl(p.getUnita(), "")
                        }).collect(Collectors.toList());
                addTable(doc, prHdr, prRows);
            } else {
                addParagraph(doc, "Nessun dato di produzione registrato per l'anno " + d.getAnno() + ".");
            }
            addEmptyParagraph(doc);

            // ── Sezione 7: Comunicazioni ─────────────────────────────────
            addH1(doc, "7. COMUNICAZIONI CON ENTI");
            addTableTwoCol(doc, new String[][] {
                {"Totale comunicazioni nell'anno",  String.valueOf(d.getTotaleComunicazioni())},
                {"Inviate",                         String.valueOf(d.getComunicazioniInviate())},
                {"Con riscontro ricevuto",          String.valueOf(d.getComunicazioniConRiscontro())},
            });
            addEmptyParagraph(doc);

            if (d.getComunicazioni() != null && !d.getComunicazioni().isEmpty()) {
                String[] comHdr = {"Tipo", "Ente", "Oggetto", "Data invio", "Stato", "Riscontro"};
                List<String[]> comRows = d.getComunicazioni().stream()
                        .map(c -> new String[] {
                            nvl(c.getTipo(), ""),
                            nvl(c.getEnte(), ""),
                            truncate(c.getOggetto(), 50),
                            nvl(c.getDataInvio(), ""),
                            nvl(c.getStato(), ""),
                            c.isHasRiscontro() ? "Sì" : "No"
                        }).collect(Collectors.toList());
                addTable(doc, comHdr, comRows);
            }
            addEmptyParagraph(doc);

            // ── Sezione 8: Conclusioni ────────────────────────────────────
            addH1(doc, "8. CONCLUSIONI");

            String conclusione;
            if (d.getRilevNonConformi() == 0 && d.getTotaleRilevazioni() > 0) {
                conclusione = "Nel corso dell'anno " + d.getAnno() + ", lo stabilimento " + d.getStabilimentoNome()
                        + " ha operato nel pieno rispetto dei limiti autorizzati dall'AIA"
                        + (d.getStabilimentoCodiceAIA() != null ? " n. " + d.getStabilimentoCodiceAIA() : "") + ". "
                        + "Tutte le " + d.getTotaleRilevazioni() + " rilevazioni effettuate sono risultate conformi"
                        + (d.getRilevAttenzione() > 0
                            ? " (" + d.getRilevAttenzione() + " in zona di attenzione, tra 80% e 100% del limite)."
                            : ".");
            } else if (d.getTotaleRilevazioni() == 0) {
                conclusione = "Per l'anno " + d.getAnno() + " non sono state registrate rilevazioni ambientali nel sistema. "
                        + "I dati potranno essere inseriti e la relazione rigenerata.";
            } else {
                conclusione = "Nel corso dell'anno " + d.getAnno() + " sono state riscontrate " + d.getRilevNonConformi()
                        + " non conformità sui " + d.getTotaleRilevazioni() + " parametri monitorati "
                        + "(" + d.getPercConformita() + "% di conformità). "
                        + "Per ciascuna non conformità sono state avviate le opportune azioni correttive.";
            }
            addParagraph(doc, conclusione);
            addEmptyParagraph(doc);
            addParagraph(doc, "Le attività di monitoraggio e controllo proseguiranno nel rispetto delle prescrizioni AIA "
                    + "e della normativa ambientale vigente (D.Lgs. 152/2006 e s.m.i.).");
            addEmptyParagraph(doc);
            addEmptyParagraph(doc);

            // Firma
            XWPFParagraph firma = doc.createParagraph();
            firma.setAlignment(ParagraphAlignment.RIGHT);
            XWPFRun runFirma = firma.createRun();
            runFirma.setFontSize(11);
            runFirma.setText("Data: " + d.getDataGenerazione());
            runFirma.addBreak();
            runFirma.addBreak();
            runFirma.setText("Il Responsabile Ambientale");
            runFirma.addBreak();
            runFirma.setText("____________________________");

            doc.write(out);
            return out.toByteArray();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // HELPER: costruttori POI
    // ══════════════════════════════════════════════════════════════════════

    private void addCenteredTitle(XWPFDocument doc, String text, int size, boolean bold) {
        XWPFParagraph p = doc.createParagraph();
        p.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(size);
        r.setBold(bold);
    }

    private void addH1(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(200);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(13);
        r.setColor("1e293b");
    }

    private void addH2(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(11);
        r.setColor("475569");
    }

    private void addParagraph(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(11);
    }

    private void addEmptyParagraph(XWPFDocument doc) {
        doc.createParagraph().createRun().setText("");
    }

    private void addPageBreakParagraph(XWPFDocument doc) {
        XWPFParagraph p = doc.createParagraph();
        p.setPageBreak(true);
    }

    /** Tabella a due colonne: etichetta / valore */
    private void addTableTwoCol(XWPFDocument doc, String[][] rows) {
        XWPFTable table = doc.createTable(rows.length, 2);
        setTableWidth(table, 9000);
        for (int i = 0; i < rows.length; i++) {
            XWPFTableCell c0 = table.getRow(i).getCell(0);
            XWPFTableCell c1 = table.getRow(i).getCell(1);
            setCell(c0, rows[i][0], true,  10);
            setCell(c1, rows[i][1], false, 10);
        }
    }

    /** Tabella generica con intestazione */
    private void addTable(XWPFDocument doc, String[] headers, List<String[]> rows) {
        int cols = headers.length;
        XWPFTable table = doc.createTable(rows.size() + 1, cols);
        setTableWidth(table, 9000);

        // intestazione
        for (int j = 0; j < cols; j++) {
            setCell(table.getRow(0).getCell(j), headers[j], true, 10);
        }
        // righe
        for (int i = 0; i < rows.size(); i++) {
            for (int j = 0; j < cols; j++) {
                String val = j < rows.get(i).length ? rows.get(i)[j] : "";
                setCell(table.getRow(i + 1).getCell(j), val, false, 9);
            }
        }
    }

    private void setCell(XWPFTableCell cell, String text, boolean bold, int fontSize) {
        cell.removeParagraph(0);
        XWPFParagraph p = cell.addParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(bold);
        r.setFontSize(fontSize);
    }

    private void setTableWidth(XWPFTable table, int width) {
        CTTblWidth tblWidth = table.getCTTbl().getTblPr().addNewTblW();
        tblWidth.setType(STTblWidth.DXA);
        tblWidth.setW(BigInteger.valueOf(width));
    }

    // ══════════════════════════════════════════════════════════════════════
    // UTILITY
    // ══════════════════════════════════════════════════════════════════════

    private static String nvl(String s, String def) { return s != null ? s : def; }

    private static String fmt(Double v) {
        if (v == null) return "—";
        if (v == Math.floor(v)) return String.valueOf(v.longValue());
        return String.format("%.2f", v);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static Double sum(List<MovimentoRifiuto> list, MovimentoRifiuto.TipoMovimento tipo) {
        return list.stream()
                .filter(m -> m.getTipoMovimento() == tipo && m.getQuantita() != null)
                .mapToDouble(MovimentoRifiuto::getQuantita)
                .sum();
    }

    private static String monthName(Integer mese) {
        if (mese == null) return "";
        String[] names = {"", "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
                           "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};
        return mese >= 1 && mese <= 12 ? names[mese] : mese.toString();
    }
}
