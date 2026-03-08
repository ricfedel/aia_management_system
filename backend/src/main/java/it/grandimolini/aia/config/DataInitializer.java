package it.grandimolini.aia.config;

import it.grandimolini.aia.model.*;
import it.grandimolini.aia.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Nome stabile usato da EstrazioneController per trovare il flusso default */
    public static final String FLUSSO_ESTRAZIONE_NOME = "Lavorazione Documento AIA";

    @Autowired
    private DefinizioneFlussoRepository definizioneFlussoRepository;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Autowired
    private PrescrizioneRepository prescrizioneRepository;

    @Autowired
    private MonitoraggioRepository monitoraggioRepository;

    @Autowired
    private DatiAmbientaliRepository datiAmbientaliRepository;

    @Autowired
    private ScadenzaRepository scadenzaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // ── Seed workflow predefiniti (già idempotenti internamente) ──────────
        seedFlussoEstrazioneOCR();
        seedFlussoRinnovoAia();
        seedFlussoNonConformita();
        seedFlussoIntegrazioneEnte();

        // ── Se data.sql ha già popolato il DB (stabilimenti reali presenti),
        //    creare solo gli utenti di accesso e saltare tutti i dati demo ─────
        if (stabilimentoRepository.count() > 0) {
            log.info("DataInitializer: {} stabilimenti già presenti (caricati da data.sql), skip demo data.",
                    stabilimentoRepository.count());
            if (userRepository.count() == 0) {
                // Recupera stabilimenti reali per associarli agli utenti
                java.util.List<Stabilimento> tutti = stabilimentoRepository.findAll();
                Stabilimento primo = tutti.get(0);
                Set<Stabilimento> tuttiSet = new java.util.HashSet<>(tutti);

                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@grandimolini.it");
                admin.setPassword(passwordEncoder.encode("Admin@123456"));
                admin.setFullName("Amministratore Sistema");
                admin.setRuolo(User.Ruolo.ADMIN);
                admin.setAttivo(true);
                admin.setStabilimenti(tuttiSet);
                userRepository.save(admin);

                User responsabile = new User();
                responsabile.setUsername("mauro.pasetto");
                responsabile.setEmail("mauro.pasetto@grandimolini.it");
                responsabile.setPassword(passwordEncoder.encode("Resp@123456"));
                responsabile.setFullName("Mauro Pasetto");
                responsabile.setRuolo(User.Ruolo.RESPONSABILE);
                responsabile.setAttivo(true);
                responsabile.setStabilimenti(Set.of(primo));
                userRepository.save(responsabile);

                System.out.println("✓ Utenti creati su dati reali AIA:");
                System.out.println("  - admin / Admin@123456 (tutti gli stabilimenti)");
                System.out.println("  - mauro.pasetto / Resp@123456 (" + primo.getNome() + ")");
            } else {
                log.info("DataInitializer: utenti già presenti, nessuna azione.");
            }
            return;
        }

        // ── DB vuoto: crea dati demo completi ────────────────────────────────
        log.info("DataInitializer: DB vuoto, carico dati demo.");

        Stabilimento livorno = new Stabilimento();
        livorno.setNome("Grandi Molini Livorno");
        livorno.setCitta("Livorno");
        livorno.setIndirizzo("Via del Porto 123");
        livorno.setNumeroAIA("AIA-LI-2020-001");
        livorno.setDataRilascioAIA(LocalDate.of(2020, 3, 15));
        livorno.setDataScadenzaAIA(LocalDate.of(2030, 3, 15));
        livorno.setEnteCompetente("Regione Toscana");
        livorno.setResponsabileAmbientale("Mauro Pasetto");
        livorno.setEmail("ambiente.livorno@grandimolini.it");
        livorno = stabilimentoRepository.save(livorno);

        Stabilimento venezia = new Stabilimento();
        venezia.setNome("Grandi Molini Venezia");
        venezia.setCitta("Venezia");
        venezia.setIndirizzo("Via della Laguna 456");
        venezia.setNumeroAIA("AIA-VE-2019-002");
        venezia.setDataRilascioAIA(LocalDate.of(2019, 6, 20));
        venezia.setDataScadenzaAIA(LocalDate.of(2029, 6, 20));
        venezia.setEnteCompetente("Regione Veneto");
        venezia.setResponsabileAmbientale("Mauro Pasetto");
        venezia.setEmail("ambiente.venezia@grandimolini.it");
        venezia = stabilimentoRepository.save(venezia);

        // Crea utenti di esempio
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@grandimolini.it");
        admin.setPassword(passwordEncoder.encode("Admin@123456"));
        admin.setFullName("Amministratore Sistema");
        admin.setRuolo(User.Ruolo.ADMIN);
        admin.setAttivo(true);
        admin.setStabilimenti(Set.of(livorno, venezia));
        userRepository.save(admin);

        User responsabile = new User();
        responsabile.setUsername("mauro.pasetto");
        responsabile.setEmail("mauro.pasetto@grandimolini.it");
        responsabile.setPassword(passwordEncoder.encode("Resp@123456"));
        responsabile.setFullName("Mauro Pasetto");
        responsabile.setRuolo(User.Ruolo.RESPONSABILE);
        responsabile.setAttivo(true);
        responsabile.setStabilimenti(Set.of(livorno));
        userRepository.save(responsabile);

        User operatore = new User();
        operatore.setUsername("operatore.venezia");
        operatore.setEmail("operatore.venezia@grandimolini.it");
        operatore.setPassword(passwordEncoder.encode("Oper@123456"));
        operatore.setFullName("Operatore Venezia");
        operatore.setRuolo(User.Ruolo.OPERATORE);
        operatore.setAttivo(true);
        operatore.setStabilimenti(Set.of(venezia));
        userRepository.save(operatore);

        System.out.println("✓ Utenti di test creati:");
        System.out.println("  - admin / Admin@123456 (accesso a tutti gli stabilimenti)");
        System.out.println("  - mauro.pasetto / Resp@123456 (accesso a Livorno)");
        System.out.println("  - operatore.venezia / Oper@123456 (accesso a Venezia)");

        // Crea prescrizioni di esempio
        Prescrizione p1 = new Prescrizione();
        p1.setStabilimento(livorno);
        p1.setCodice("PRES-LI-2024-001");
        p1.setDescrizione("Monitoraggio emissioni atmosferiche camino E1");
        p1.setMatriceAmbientale(Prescrizione.MatriceAmbientale.ARIA);
        p1.setStato(Prescrizione.StatoPrescrizione.IN_LAVORAZIONE);
        p1.setDataEmissione(LocalDate.of(2024, 1, 15));
        p1.setDataScadenza(LocalDate.of(2024, 12, 31));
        p1.setEnteEmittente("Regione Toscana");
        p1.setPriorita(Prescrizione.Priorita.ALTA);
        p1 = prescrizioneRepository.save(p1);

        Prescrizione p2 = new Prescrizione();
        p2.setStabilimento(venezia);
        p2.setCodice("PRES-VE-2024-001");
        p2.setDescrizione("Controllo scarichi idrici");
        p2.setMatriceAmbientale(Prescrizione.MatriceAmbientale.ACQUA);
        p2.setStato(Prescrizione.StatoPrescrizione.APERTA);
        p2.setDataEmissione(LocalDate.of(2024, 2, 1));
        p2.setDataScadenza(LocalDate.of(2024, 6, 30));
        p2.setEnteEmittente("ARPA Veneto");
        p2.setPriorita(Prescrizione.Priorita.MEDIA);
        p2 = prescrizioneRepository.save(p2);

        // Crea monitoraggi
        Monitoraggio m1 = new Monitoraggio();
        m1.setStabilimento(livorno);
        m1.setCodice("MON-LI-CAM-E1");
        m1.setDescrizione("Monitoraggio camino E1");
        m1.setTipoMonitoraggio(Monitoraggio.TipoMonitoraggio.EMISSIONI_ATMOSFERA);
        m1.setPuntoEmissione("Camino E1");
        m1.setFrequenza(Monitoraggio.FrequenzaMonitoraggio.MENSILE);
        m1.setProssimaScadenza(LocalDate.now().plusDays(15));
        m1.setLaboratorio("Lab Analisi SRL");
        m1 = monitoraggioRepository.save(m1);

        Monitoraggio m2 = new Monitoraggio();
        m2.setStabilimento(venezia);
        m2.setCodice("MON-VE-SCR-S1");
        m2.setDescrizione("Scarico S1");
        m2.setTipoMonitoraggio(Monitoraggio.TipoMonitoraggio.SCARICHI_IDRICI);
        m2.setPuntoEmissione("Scarico S1");
        m2.setFrequenza(Monitoraggio.FrequenzaMonitoraggio.SEMESTRALE);
        m2.setProssimaScadenza(LocalDate.now().plusDays(30));
        m2.setLaboratorio("EcoTest Lab");
        m2 = monitoraggioRepository.save(m2);

        // Crea dati ambientali di esempio
        DatiAmbientali d1 = new DatiAmbientali();
        d1.setMonitoraggio(m1);
        d1.setDataCampionamento(LocalDate.now().minusDays(30));
        d1.setParametro("NOx");
        d1.setValoreMisurato(120.0);
        d1.setUnitaMisura("mg/Nm3");
        d1.setLimiteAutorizzato(150.0);
        d1.setLaboratorio("Lab Analisi SRL");
        d1.setNote("Valore conforme");
        datiAmbientaliRepository.save(d1);

        DatiAmbientali d2 = new DatiAmbientali();
        d2.setMonitoraggio(m1);
        d2.setDataCampionamento(LocalDate.now().minusDays(15));
        d2.setParametro("NOx");
        d2.setValoreMisurato(145.0);
        d2.setUnitaMisura("mg/Nm3");
        d2.setLimiteAutorizzato(150.0);
        d2.setLaboratorio("Lab Analisi SRL");
        d2.setNote("Valore in attenzione - vicino al limite");
        datiAmbientaliRepository.save(d2);

        DatiAmbientali d3 = new DatiAmbientali();
        d3.setMonitoraggio(m2);
        d3.setDataCampionamento(LocalDate.now().minusDays(60));
        d3.setParametro("COD");
        d3.setValoreMisurato(180.0);
        d3.setUnitaMisura("mg/l");
        d3.setLimiteAutorizzato(160.0);
        d3.setLaboratorio("EcoTest Lab");
        d3.setNote("Superamento limite - richiesta integrazione");
        datiAmbientaliRepository.save(d3);

        // Crea scadenze
        Scadenza s1 = new Scadenza();
        s1.setStabilimento(livorno);
        s1.setPrescrizione(p1);
        s1.setTitolo("Invio rapporto mensile emissioni");
        s1.setDescrizione("Invio rapporto mensile emissioni camino E1 a Regione Toscana");
        s1.setTipoScadenza(Scadenza.TipoScadenza.MONITORAGGIO_PMC);
        s1.setDataScadenza(LocalDate.now().plusDays(10));
        s1.setStato(Scadenza.StatoScadenza.PENDING);
        s1.setPriorita(Scadenza.Priorita.ALTA);
        s1.setResponsabile("Mauro Pasetto");
        s1.setEmailNotifica("ambiente.livorno@grandimolini.it");
        scadenzaRepository.save(s1);

        Scadenza s2 = new Scadenza();
        s2.setStabilimento(livorno);
        s2.setTitolo("Relazione Annuale AIA 2024");
        s2.setDescrizione("Invio relazione annuale AIA entro il 30 aprile");
        s2.setTipoScadenza(Scadenza.TipoScadenza.RELAZIONE_ANNUALE);
        s2.setDataScadenza(LocalDate.of(2025, 4, 30));
        s2.setStato(Scadenza.StatoScadenza.PENDING);
        s2.setPriorita(Scadenza.Priorita.URGENTE);
        s2.setResponsabile("Mauro Pasetto");
        scadenzaRepository.save(s2);

        Scadenza s3 = new Scadenza();
        s3.setStabilimento(venezia);
        s3.setPrescrizione(p2);
        s3.setTitolo("Integrazione richiesta da ARPA");
        s3.setDescrizione("Fornire documentazione integrativa su scarichi idrici");
        s3.setTipoScadenza(Scadenza.TipoScadenza.INTEGRAZIONE_ENTE);
        s3.setDataScadenza(LocalDate.now().plusDays(5));
        s3.setStato(Scadenza.StatoScadenza.PENDING);
        s3.setPriorita(Scadenza.Priorita.URGENTE);
        s3.setResponsabile("Mauro Pasetto");
        scadenzaRepository.save(s3);

        System.out.println("✓ Dati di esempio inizializzati con successo!");
    }

    /**
     * Crea la DefinizioneFlusso standard per il flusso di arrivo documento con
     * analisi OCR/AI. Il BPMN è modificabile dall'utente tramite l'editor visuale
     * in /workflow. EstrazioneController la cerca per nome e la usa in avviaProcesso().
     */
    private void seedFlussoEstrazioneOCR() {
        // Se esiste già (es. DB persistente tra riavvii) non la ricreiamo
        if (!definizioneFlussoRepository.findByAttivaTrue().isEmpty()
                && definizioneFlussoRepository.findByAttivaTrue().stream()
                        .anyMatch(df -> FLUSSO_ESTRAZIONE_NOME.equals(df.getNome()))) {
            log.info("DataInitializer: flusso '{}' già presente, skip.", FLUSSO_ESTRAZIONE_NOME);
            return;
        }

        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(FLUSSO_ESTRAZIONE_NOME);
        df.setDescrizione(
            "Flusso standard per l'elaborazione di un documento AIA: " +
            "protocollazione → analisi OCR/AI (automatica) → " +
            "revisione operatore → approvazione → archiviazione. " +
            "Modificabile dall'editor BPMN nella sezione Workflow."
        );
        df.setBpmnXml(bpmnFlussoEstrazione());
        df.setVersione(1);
        df.setAttiva(true);
        df.setSistema(true);
        df.setCreatoDa("sistema");
        definizioneFlussoRepository.save(df);
        log.info("DataInitializer: flusso '{}' creato.", FLUSSO_ESTRAZIONE_NOME);
    }

    private void seedFlussoRinnovoAia() {
        String nome = "Rinnovo AIA";
        if (definizioneFlussoRepository.findByAttivaTrue().stream().anyMatch(df -> nome.equals(df.getNome()))) return;
        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(nome);
        df.setDescrizione("Procedura di rinnovo dell'Autorizzazione Integrata Ambientale: avvio → raccolta documentazione → revisione → invio enti → attesa esito istruttoria.");
        df.setBpmnXml(bpmnRinnovoAia());
        df.setVersione(1); df.setAttiva(true); df.setSistema(true); df.setCreatoDa("sistema");
        definizioneFlussoRepository.save(df);
        log.info("DataInitializer: flusso '{}' creato.", nome);
    }

    private void seedFlussoNonConformita() {
        String nome = "Gestione Non Conformità";
        if (definizioneFlussoRepository.findByAttivaTrue().stream().anyMatch(df -> nome.equals(df.getNome()))) return;
        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(nome);
        df.setDescrizione("Flusso di gestione di una non conformità ambientale: rilevazione → analisi cause → piano azioni correttive → verifica efficacia → chiusura.");
        df.setBpmnXml(bpmnNonConformita());
        df.setVersione(1); df.setAttiva(true); df.setSistema(true); df.setCreatoDa("sistema");
        definizioneFlussoRepository.save(df);
        log.info("DataInitializer: flusso '{}' creato.", nome);
    }

    private void seedFlussoIntegrazioneEnte() {
        String nome = "Integrazione Ente";
        if (definizioneFlussoRepository.findByAttivaTrue().stream().anyMatch(df -> nome.equals(df.getNome()))) return;
        DefinizioneFlusso df = new DefinizioneFlusso();
        df.setNome(nome);
        df.setDescrizione("Gestione richiesta di integrazione documentale da parte di un ente competente: ricezione → analisi → preparazione risposta → invio.");
        df.setBpmnXml(bpmnIntegrazioneEnte());
        df.setVersione(1); df.setAttiva(true); df.setSistema(true); df.setCreatoDa("sistema");
        definizioneFlussoRepository.save(df);
        log.info("DataInitializer: flusso '{}' creato.", nome);
    }

    private String bpmnFlussoEstrazione() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="lavorazione-documento-aia"
  targetNamespace="http://aia.grandimolini.it/bpmn">
  <bpmn2:process id="Process_LavorazioneDocumento" name="Lavorazione Documento AIA" isExecutable="false">
    <bpmn2:startEvent id="Start_Ricezione" name="Documento ricevuto">
      <bpmn2:outgoing>Flow_01</bpmn2:outgoing>
    </bpmn2:startEvent>
    <bpmn2:userTask id="Task_Protocollazione" name="Ricezione e protocollazione documento">
      <bpmn2:incoming>Flow_01</bpmn2:incoming>
      <bpmn2:outgoing>Flow_02</bpmn2:outgoing>
    </bpmn2:userTask>
    <bpmn2:serviceTask id="Task_AnalisiOCR" name="Analisi OCR / AI (estrazione automatica)">
      <bpmn2:incoming>Flow_02</bpmn2:incoming>
      <bpmn2:outgoing>Flow_03</bpmn2:outgoing>
    </bpmn2:serviceTask>
    <bpmn2:userTask id="Task_RevisioneEntita" name="Revisione entità estratte (scadenze e prescrizioni)">
      <bpmn2:incoming>Flow_03</bpmn2:incoming>
      <bpmn2:outgoing>Flow_04</bpmn2:outgoing>
    </bpmn2:userTask>
    <bpmn2:userTask id="Task_Approvazione" name="Approvazione e conferma entità">
      <bpmn2:incoming>Flow_04</bpmn2:incoming>
      <bpmn2:outgoing>Flow_05</bpmn2:outgoing>
    </bpmn2:userTask>
    <bpmn2:serviceTask id="Task_Archiviazione" name="Archiviazione documento">
      <bpmn2:incoming>Flow_05</bpmn2:incoming>
      <bpmn2:outgoing>Flow_06</bpmn2:outgoing>
    </bpmn2:serviceTask>
    <bpmn2:endEvent id="End_Completato" name="Documento archiviato">
      <bpmn2:incoming>Flow_06</bpmn2:incoming>
    </bpmn2:endEvent>
    <bpmn2:sequenceFlow id="Flow_01" sourceRef="Start_Ricezione"     targetRef="Task_Protocollazione"/>
    <bpmn2:sequenceFlow id="Flow_02" sourceRef="Task_Protocollazione" targetRef="Task_AnalisiOCR"/>
    <bpmn2:sequenceFlow id="Flow_03" sourceRef="Task_AnalisiOCR"      targetRef="Task_RevisioneEntita"/>
    <bpmn2:sequenceFlow id="Flow_04" sourceRef="Task_RevisioneEntita"  targetRef="Task_Approvazione"/>
    <bpmn2:sequenceFlow id="Flow_05" sourceRef="Task_Approvazione"    targetRef="Task_Archiviazione"/>
    <bpmn2:sequenceFlow id="Flow_06" sourceRef="Task_Archiviazione"   targetRef="End_Completato"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_LavorazioneDocumento">
      <bpmndi:BPMNShape id="S_Start"           bpmnElement="Start_Ricezione">      <dc:Bounds x="152" y="272" width="36"  height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_Protocollazione" bpmnElement="Task_Protocollazione"> <dc:Bounds x="250" y="250" width="130" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_AnalisiOCR"      bpmnElement="Task_AnalisiOCR">      <dc:Bounds x="440" y="250" width="130" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_RevisioneEntita" bpmnElement="Task_RevisioneEntita"> <dc:Bounds x="630" y="250" width="130" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_Approvazione"    bpmnElement="Task_Approvazione">    <dc:Bounds x="820" y="250" width="130" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_Archiviazione"   bpmnElement="Task_Archiviazione">   <dc:Bounds x="1010" y="250" width="130" height="80"/></bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="S_End"             bpmnElement="End_Completato">       <dc:Bounds x="1202" y="272" width="36"  height="36"/></bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="E_01" bpmnElement="Flow_01"><di:waypoint x="188"  y="290"/><di:waypoint x="250"  y="290"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="E_02" bpmnElement="Flow_02"><di:waypoint x="380"  y="290"/><di:waypoint x="440"  y="290"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="E_03" bpmnElement="Flow_03"><di:waypoint x="570"  y="290"/><di:waypoint x="630"  y="290"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="E_04" bpmnElement="Flow_04"><di:waypoint x="760"  y="290"/><di:waypoint x="820"  y="290"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="E_05" bpmnElement="Flow_05"><di:waypoint x="950"  y="290"/><di:waypoint x="1010" y="290"/></bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="E_06" bpmnElement="Flow_06"><di:waypoint x="1140" y="290"/><di:waypoint x="1202" y="290"/></bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>
""";
    }

    private String bpmnRinnovoAia() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="rinnovo-aia" targetNamespace="http://aia.grandimolini.it/bpmn">
  <bpmn2:process id="Process_RinnovoAia" name="Rinnovo AIA" isExecutable="false">
    <bpmn2:startEvent id="Start_RinnovoAia" name="Avvio rinnovo"><bpmn2:outgoing>F1</bpmn2:outgoing></bpmn2:startEvent>
    <bpmn2:userTask id="Task_AvvioRinnovo" name="Avvio procedura rinnovo AIA"><bpmn2:incoming>F1</bpmn2:incoming><bpmn2:outgoing>F2</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_RaccoltaDoc" name="Raccolta documentazione per rinnovo"><bpmn2:incoming>F2</bpmn2:incoming><bpmn2:outgoing>F3</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_RevisioneDoc" name="Revisione documenti raccolti"><bpmn2:incoming>F3</bpmn2:incoming><bpmn2:outgoing>F4</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_InvioEnti" name="Invio istanza agli enti competenti"><bpmn2:incoming>F4</bpmn2:incoming><bpmn2:outgoing>F5</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_AttesaEsito" name="Attesa esito istruttoria"><bpmn2:incoming>F5</bpmn2:incoming><bpmn2:outgoing>F6</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:endEvent id="End_RinnovoAia" name="Rinnovo completato"><bpmn2:incoming>F6</bpmn2:incoming></bpmn2:endEvent>
    <bpmn2:sequenceFlow id="F1" sourceRef="Start_RinnovoAia"  targetRef="Task_AvvioRinnovo"/>
    <bpmn2:sequenceFlow id="F2" sourceRef="Task_AvvioRinnovo" targetRef="Task_RaccoltaDoc"/>
    <bpmn2:sequenceFlow id="F3" sourceRef="Task_RaccoltaDoc"  targetRef="Task_RevisioneDoc"/>
    <bpmn2:sequenceFlow id="F4" sourceRef="Task_RevisioneDoc" targetRef="Task_InvioEnti"/>
    <bpmn2:sequenceFlow id="F5" sourceRef="Task_InvioEnti"    targetRef="Task_AttesaEsito"/>
    <bpmn2:sequenceFlow id="F6" sourceRef="Task_AttesaEsito"  targetRef="End_RinnovoAia"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="Diagram_RinnovoAia"><bpmndi:BPMNPlane bpmnElement="Process_RinnovoAia">
    <bpmndi:BPMNShape id="SS1" bpmnElement="Start_RinnovoAia"><dc:Bounds x="152" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS2" bpmnElement="Task_AvvioRinnovo"><dc:Bounds x="250" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS3" bpmnElement="Task_RaccoltaDoc"><dc:Bounds x="440" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS4" bpmnElement="Task_RevisioneDoc"><dc:Bounds x="630" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS5" bpmnElement="Task_InvioEnti"><dc:Bounds x="820" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS6" bpmnElement="Task_AttesaEsito"><dc:Bounds x="1010" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="SS7" bpmnElement="End_RinnovoAia"><dc:Bounds x="1202" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNEdge id="SE1" bpmnElement="F1"><di:waypoint x="188" y="290"/><di:waypoint x="250" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="SE2" bpmnElement="F2"><di:waypoint x="380" y="290"/><di:waypoint x="440" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="SE3" bpmnElement="F3"><di:waypoint x="570" y="290"/><di:waypoint x="630" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="SE4" bpmnElement="F4"><di:waypoint x="760" y="290"/><di:waypoint x="820" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="SE5" bpmnElement="F5"><di:waypoint x="950" y="290"/><di:waypoint x="1010" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="SE6" bpmnElement="F6"><di:waypoint x="1140" y="290"/><di:waypoint x="1202" y="290"/></bpmndi:BPMNEdge>
  </bpmndi:BPMNPlane></bpmndi:BPMNDiagram>
</bpmn2:definitions>
""";
    }

    private String bpmnNonConformita() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="non-conformita" targetNamespace="http://aia.grandimolini.it/bpmn">
  <bpmn2:process id="Process_NonConformita" name="Gestione Non Conformità" isExecutable="false">
    <bpmn2:startEvent id="Start_NC" name="Rilevazione NC"><bpmn2:outgoing>N1</bpmn2:outgoing></bpmn2:startEvent>
    <bpmn2:userTask id="Task_RilevazioneNC" name="Rilevazione non conformità"><bpmn2:incoming>N1</bpmn2:incoming><bpmn2:outgoing>N2</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_AnalisiCause" name="Analisi cause e responsabilità"><bpmn2:incoming>N2</bpmn2:incoming><bpmn2:outgoing>N3</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_PianoAzioni" name="Definizione piano azioni correttive"><bpmn2:incoming>N3</bpmn2:incoming><bpmn2:outgoing>N4</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_VerificaEfficacia" name="Verifica efficacia azioni"><bpmn2:incoming>N4</bpmn2:incoming><bpmn2:outgoing>N5</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_ChiusuraNC" name="Chiusura non conformità"><bpmn2:incoming>N5</bpmn2:incoming><bpmn2:outgoing>N6</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:endEvent id="End_NC" name="NC chiusa"><bpmn2:incoming>N6</bpmn2:incoming></bpmn2:endEvent>
    <bpmn2:sequenceFlow id="N1" sourceRef="Start_NC"            targetRef="Task_RilevazioneNC"/>
    <bpmn2:sequenceFlow id="N2" sourceRef="Task_RilevazioneNC"  targetRef="Task_AnalisiCause"/>
    <bpmn2:sequenceFlow id="N3" sourceRef="Task_AnalisiCause"   targetRef="Task_PianoAzioni"/>
    <bpmn2:sequenceFlow id="N4" sourceRef="Task_PianoAzioni"    targetRef="Task_VerificaEfficacia"/>
    <bpmn2:sequenceFlow id="N5" sourceRef="Task_VerificaEfficacia" targetRef="Task_ChiusuraNC"/>
    <bpmn2:sequenceFlow id="N6" sourceRef="Task_ChiusuraNC"     targetRef="End_NC"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="Diagram_NC"><bpmndi:BPMNPlane bpmnElement="Process_NonConformita">
    <bpmndi:BPMNShape id="NS1" bpmnElement="Start_NC"><dc:Bounds x="152" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS2" bpmnElement="Task_RilevazioneNC"><dc:Bounds x="250" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS3" bpmnElement="Task_AnalisiCause"><dc:Bounds x="440" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS4" bpmnElement="Task_PianoAzioni"><dc:Bounds x="630" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS5" bpmnElement="Task_VerificaEfficacia"><dc:Bounds x="820" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS6" bpmnElement="Task_ChiusuraNC"><dc:Bounds x="1010" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="NS7" bpmnElement="End_NC"><dc:Bounds x="1202" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNEdge id="NE1" bpmnElement="N1"><di:waypoint x="188" y="290"/><di:waypoint x="250" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="NE2" bpmnElement="N2"><di:waypoint x="380" y="290"/><di:waypoint x="440" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="NE3" bpmnElement="N3"><di:waypoint x="570" y="290"/><di:waypoint x="630" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="NE4" bpmnElement="N4"><di:waypoint x="760" y="290"/><di:waypoint x="820" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="NE5" bpmnElement="N5"><di:waypoint x="950" y="290"/><di:waypoint x="1010" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="NE6" bpmnElement="N6"><di:waypoint x="1140" y="290"/><di:waypoint x="1202" y="290"/></bpmndi:BPMNEdge>
  </bpmndi:BPMNPlane></bpmndi:BPMNDiagram>
</bpmn2:definitions>
""";
    }

    private String bpmnIntegrazioneEnte() {
        return """
<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="integrazione-ente" targetNamespace="http://aia.grandimolini.it/bpmn">
  <bpmn2:process id="Process_IntegrazioneEnte" name="Integrazione Ente" isExecutable="false">
    <bpmn2:startEvent id="Start_IE" name="Richiesta ricevuta"><bpmn2:outgoing>I1</bpmn2:outgoing></bpmn2:startEvent>
    <bpmn2:userTask id="Task_RicezioneRichiesta" name="Ricezione richiesta integrazione"><bpmn2:incoming>I1</bpmn2:incoming><bpmn2:outgoing>I2</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_AnalisiRichiesta" name="Analisi e valutazione richiesta"><bpmn2:incoming>I2</bpmn2:incoming><bpmn2:outgoing>I3</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_PreparazioneRisposta" name="Preparazione risposta integrativa"><bpmn2:incoming>I3</bpmn2:incoming><bpmn2:outgoing>I4</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:userTask id="Task_InvioRisposta" name="Invio risposta all'ente"><bpmn2:incoming>I4</bpmn2:incoming><bpmn2:outgoing>I5</bpmn2:outgoing></bpmn2:userTask>
    <bpmn2:endEvent id="End_IE" name="Integrazione completata"><bpmn2:incoming>I5</bpmn2:incoming></bpmn2:endEvent>
    <bpmn2:sequenceFlow id="I1" sourceRef="Start_IE"                 targetRef="Task_RicezioneRichiesta"/>
    <bpmn2:sequenceFlow id="I2" sourceRef="Task_RicezioneRichiesta"  targetRef="Task_AnalisiRichiesta"/>
    <bpmn2:sequenceFlow id="I3" sourceRef="Task_AnalisiRichiesta"    targetRef="Task_PreparazioneRisposta"/>
    <bpmn2:sequenceFlow id="I4" sourceRef="Task_PreparazioneRisposta" targetRef="Task_InvioRisposta"/>
    <bpmn2:sequenceFlow id="I5" sourceRef="Task_InvioRisposta"       targetRef="End_IE"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="Diagram_IE"><bpmndi:BPMNPlane bpmnElement="Process_IntegrazioneEnte">
    <bpmndi:BPMNShape id="IS1" bpmnElement="Start_IE"><dc:Bounds x="152" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="IS2" bpmnElement="Task_RicezioneRichiesta"><dc:Bounds x="250" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="IS3" bpmnElement="Task_AnalisiRichiesta"><dc:Bounds x="440" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="IS4" bpmnElement="Task_PreparazioneRisposta"><dc:Bounds x="630" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="IS5" bpmnElement="Task_InvioRisposta"><dc:Bounds x="820" y="250" width="130" height="80"/></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="IS6" bpmnElement="End_IE"><dc:Bounds x="1012" y="272" width="36" height="36"/></bpmndi:BPMNShape>
    <bpmndi:BPMNEdge id="IE1" bpmnElement="I1"><di:waypoint x="188" y="290"/><di:waypoint x="250" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="IE2" bpmnElement="I2"><di:waypoint x="380" y="290"/><di:waypoint x="440" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="IE3" bpmnElement="I3"><di:waypoint x="570" y="290"/><di:waypoint x="630" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="IE4" bpmnElement="I4"><di:waypoint x="760" y="290"/><di:waypoint x="820" y="290"/></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="IE5" bpmnElement="I5"><di:waypoint x="950" y="290"/><di:waypoint x="1012" y="290"/></bpmndi:BPMNEdge>
  </bpmndi:BPMNPlane></bpmndi:BPMNDiagram>
</bpmn2:definitions>
""";
    }
}

