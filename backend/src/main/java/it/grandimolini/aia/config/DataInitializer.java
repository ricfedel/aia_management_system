package it.grandimolini.aia.config;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.grandimolini.aia.model.DefinizioneFlusso;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.DefinizioneFlussoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.repository.UserRepository;

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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SqlLoaderService sqlLoaderService;

    /** Se true, esegue data.sql su PostgreSQL (solo primo deploy). Default: false. */
    @Value("${app.initialize-data:false}")
    private boolean initializeData;

    @Override
    public void run(String... args) throws Exception {
        // ── Seed workflow predefiniti (idempotenti: skippa se già esistono) ───
        seedFlussoEstrazioneOCR();
        seedFlussoRinnovoAia();
        seedFlussoNonConformita();
        seedFlussoIntegrazioneEnte();

        // ── Carica data.sql se il DB è vuoto ─────────────────────────────────
        // Spring Boot 4 non esegue automaticamente data.sql con HikariCP:
        // lo facciamo noi via ResourceDatabasePopulator, idempotente (skip se
        // stabilimenti già presenti) oppure se app.initialize-data=true.
        if (stabilimentoRepository.count() == 0 || initializeData) {
            if (initializeData && stabilimentoRepository.count() > 0) {
                log.info("DataInitializer: app.initialize-data=true ma dati già presenti, skip data.sql.");
            } else {
                log.info("DataInitializer: DB vuoto — eseguo data.sql...");
                // SqlLoaderService usa Session.doWork(): stessa connessione JDBC
                // che Hibernate usa per il DDL → le tabelle sono garantite visibili.
                sqlLoaderService.loadDataSql();
                log.info("DataInitializer: data.sql eseguito ({} stabilimenti caricati).",
                        stabilimentoRepository.count());
            }
        } else {
            log.info("DataInitializer: {} stabilimenti già presenti, skip data.sql.",
                    stabilimentoRepository.count());
        }

        // ── Crea utenti di accesso se non presenti ────────────────────────────
        // Le password devono essere hashate con BCrypt a runtime, non possono
        // stare in data.sql in chiaro.
        if (userRepository.count() > 0) {
            log.info("DataInitializer: utenti già presenti, skip.");
            return;
        }

        java.util.List<Stabilimento> tutti = stabilimentoRepository.findAll();
        if (tutti.isEmpty()) {
            log.warn("DataInitializer: nessuno stabilimento trovato — utenti non creati.");
            return;
        }

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

        System.out.println("✓ Utenti creati:");
        System.out.println("  - admin / Admin@123456  (tutti gli stabilimenti)");
        System.out.println("  - mauro.pasetto / Resp@123456  (" + primo.getNome() + ")");
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

