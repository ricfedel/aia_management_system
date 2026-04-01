package it.grandimolini.aia.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.grandimolini.aia.model.DefinizioneFlusso;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.model.User;
import it.grandimolini.aia.repository.DefinizioneFlussoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

/**
 * Esegue data.sql usando la connessione della sessione Hibernate corrente,
 * garantendo che le tabelle create da Hibernate DDL siano visibili.
 * Dopo gli INSERT, resetta le sequenze PostgreSQL per le tabelle con ID espliciti.
 */
@Service
public class SqlLoaderService {

    private static final Logger log = LoggerFactory.getLogger(SqlLoaderService.class);

    /** Nome stabile usato da EstrazioneController per trovare il flusso default */
    public static final String FLUSSO_ESTRAZIONE_NOME = "Lavorazione Documento AIA";

    /** Tabelle che hanno INSERT con ID espliciti in data.sql. */
    private static final String[] TABLES_WITH_EXPLICIT_IDS = {
        "stabilimenti", "prescrizioni", "monitoraggi",
        "parametri_monitoraggio", "anagrafica_camini",
        "documenti", "dati_ambientali", "scadenze"
    };

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DefinizioneFlussoRepository definizioneFlussoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;
    
    @Transactional
    public void loadDataSql() {
        log.info("SqlLoaderService: esecuzione data.sql via sessione Hibernate...");
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("data.sql"));
                log.info("SqlLoaderService: data.sql completato, resetto le sequenze...");
                resetSequences(connection);
                log.info("SqlLoaderService: sequenze resettate.");
            } catch (Exception e) {
                log.error("SqlLoaderService: errore durante l'esecuzione di data.sql", e);
                throw e;
            }
        });
        log.info("SqlLoaderService: inizializzazione dati completata.");
    }
    @Transactional
    public void loadRifiutiSql() {
        log.info("SqlLoaderService: esecuzione seed_rifiuti.sql via sessione Hibernate...");
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed_rifiuti.sql"));
                log.info("SqlLoaderService: seed_rifiuti.sql completato, resetto le sequenze...");
                resetSequences(connection);
                log.info("SqlLoaderService seed_rifiuti: sequenze resettate.");
            } catch (Exception e) {
                log.error("SqlLoaderService seed_rifiuti: errore durante l'esecuzione di seed_rifiuti.sql", e);
                throw e;
            }
        });
        log.info("SqlLoaderService seed_rifiuti: inizializzazione dati completata.");
    }
    @Transactional
    public void loadProduzioneSql() {
        log.info("SqlLoaderService: esecuzione seed_produzione.sql via sessione Hibernate...");
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed_produzione.sql"));
                log.info("SqlLoaderService: seed_produzione.sql completato, resetto le sequenze...");
                resetSequences(connection);
                log.info("SqlLoaderService seed_produzione: sequenze resettate.");
            } catch (Exception e) {
                log.error("SqlLoaderService seed_produzione: errore durante l'esecuzione di seed_produzione.sql", e);
                throw e;
            }
        });
        log.info("SqlLoaderService seed_produzione: inizializzazione dati completata.");
    }
    @Transactional
    public void loadConformitaSql() {
        log.info("SqlLoaderService: esecuzione seed_conformita.sql via sessione Hibernate...");
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            try {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("seed_conformita.sql"));
                log.info("SqlLoaderService: seed_conformita.sql completato, resetto le sequenze...");
                resetSequences(connection);
                log.info("SqlLoaderService seed_conformita: sequenze resettate.");
            } catch (Exception e) {
                log.error("SqlLoaderService seed_conformita: errore durante l'esecuzione di seed_conformita.sql", e);
                throw e;
            }
        });
        log.info("SqlLoaderService seed_conformita: inizializzazione dati completata.");
    }
    /**
     * Resetta le sequenze PostgreSQL dopo INSERT con ID espliciti.
     * Usa pg_get_serial_sequence (funziona con SERIAL e IDENTITY BY DEFAULT).
     * Se la sequenza non esiste o non è trovata, logga un warning e continua.
     */
    private void resetSequences(Connection connection) {
        for (String table : TABLES_WITH_EXPLICIT_IDS) {
            try {
                // 1. Trova il nome della sequenza associata alla colonna 'id'
                String seqName = null;
                try (PreparedStatement ps = connection.prepareStatement(
                        "SELECT pg_get_serial_sequence(?, 'id')")) {
                    ps.setString(1, table);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            seqName = rs.getString(1);
                        }
                    }
                }

                if (seqName == null) {
                    // Nessuna sequenza SERIAL/IDENTITY: prova il nome convenzionale Hibernate
                    seqName = tryConventionalSequenceName(connection, table);
                }

                if (seqName == null) {
                    log.warn("SqlLoaderService: nessuna sequenza trovata per {}.id — skip reset.", table);
                    continue;
                }

                // 2. Resetta la sequenza al MAX(id) corrente
                String resetSql = String.format(
                    "SELECT setval('%s', COALESCE((SELECT MAX(id) FROM %s), 1))",
                    seqName, table);
                try (PreparedStatement ps = connection.prepareStatement(resetSql)) {
                    ps.execute();
                    log.debug("SqlLoaderService: sequenza {} resettata per tabella {}.", seqName, table);
                }

            } catch (Exception e) {
                // Non fatale: logga e prosegui con la tabella successiva
                log.warn("SqlLoaderService: impossibile resettare sequenza per {} — {}",
                         table, e.getMessage());
            }
        }
    }

    /**
     * Hibernate 6 può usare una sequenza condivisa o per-tabella con nome
     * convenzionale (es. tablename_seq). Verifica se esiste.
     */
    private String tryConventionalSequenceName(Connection connection, String table) {
        String[] candidates = { table + "_id_seq", table + "_seq" };
        for (String candidate : candidates) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM pg_sequences WHERE sequencename = ?")) {
                ps.setString(1, candidate);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return candidate;
                    }
                }
            } catch (Exception ignored) { }
        }
        return null;
    }
    
    /**
     * Crea la DefinizioneFlusso standard per il flusso di arrivo documento con
     * analisi OCR/AI. Il BPMN è modificabile dall'utente tramite l'editor visuale
     * in /workflow. EstrazioneController la cerca per nome e la usa in avviaProcesso().
     */
    @Transactional
    public void seedFlussoEstrazioneOCR() {
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

    @Transactional
    public void seedFlussoRinnovoAia() {
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

    @Transactional
    public void seedFlussoNonConformita() {
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

    @Transactional
    public void seedFlussoIntegrazioneEnte() {
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
    
@Transactional
public void seedUser() {
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
