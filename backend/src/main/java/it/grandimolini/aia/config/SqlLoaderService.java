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

/**
 * Esegue data.sql usando la connessione della sessione Hibernate corrente,
 * garantendo che le tabelle create da Hibernate DDL siano visibili.
 */
@Service
public class SqlLoaderService {

    private static final Logger log = LoggerFactory.getLogger(SqlLoaderService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void loadDataSql() {
        log.info("SqlLoaderService: esecuzione data.sql via sessione Hibernate...");
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("data.sql"));
        });
        log.info("SqlLoaderService: data.sql completato.");
    }
}
