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


    

    
    @Autowired
    private SqlLoaderService sqlLoaderService;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;
    
    /** Se true, esegue data.sql su PostgreSQL (solo primo deploy). Default: false. */
    @Value("${app.initialize-data:false}")
    private boolean initializeData;

    @Override
    public void run(String... args) throws Exception {
    	
    	
        // ── Seed workflow predefiniti (idempotenti: skippa se già esistono) ───
    	sqlLoaderService.seedFlussoEstrazioneOCR();
    	sqlLoaderService.seedFlussoRinnovoAia();
    	sqlLoaderService.seedFlussoNonConformita();
    	sqlLoaderService.seedFlussoIntegrazioneEnte();


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
                sqlLoaderService.loadRifiutiSql();
                sqlLoaderService.loadConformitaSql();
                sqlLoaderService.loadProduzioneSql();
                log.info("DataInitializer:  eseguito ({} stabilimenti caricati).",
                        stabilimentoRepository.count());
            }
        } else {
            log.info("DataInitializer: {} stabilimenti già presenti, skip data.sql.",
                    stabilimentoRepository.count());
        }

        sqlLoaderService.seedUser();
    }

 

}

