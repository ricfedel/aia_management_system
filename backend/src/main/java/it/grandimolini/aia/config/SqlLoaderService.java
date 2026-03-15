package it.grandimolini.aia.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Esegue data.sql usando la connessione della sessione Hibernate corrente,
 * garantendo che le tabelle create da Hibernate DDL siano visibili.
 * Dopo gli INSERT, resetta le sequenze PostgreSQL per le tabelle con ID espliciti.
 */
@Service
public class SqlLoaderService {

    private static final Logger log = LoggerFactory.getLogger(SqlLoaderService.class);

    /** Tabelle che hanno INSERT con ID espliciti in data.sql. */
    private static final String[] TABLES_WITH_EXPLICIT_IDS = {
        "stabilimenti", "prescrizioni", "monitoraggi",
        "parametri_monitoraggio", "anagrafica_camini",
        "documenti", "dati_ambientali", "scadenze"
    };

    @PersistenceContext
    private EntityManager entityManager;

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
}
