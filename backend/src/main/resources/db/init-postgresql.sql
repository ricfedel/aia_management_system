-- ===============================================
-- AIA MANAGEMENT SYSTEM - PostgreSQL Init Script
-- ===============================================
-- Database: aia_management
-- Version: 1.0.0
-- Date: 2025-02-08
-- ===============================================

-- Creazione database (eseguire come superuser)
-- CREATE DATABASE aia_management;
-- CREATE USER aia_user WITH ENCRYPTED PASSWORD 'your_secure_password';
-- GRANT ALL PRIVILEGES ON DATABASE aia_management TO aia_user;

-- Connetti al database aia_management prima di eseguire questo script
-- \c aia_management

-- ===============================================
-- SCHEMA CREATION
-- ===============================================

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    ruolo VARCHAR(20) NOT NULL CHECK (ruolo IN ('ADMIN', 'RESPONSABILE', 'OPERATORE')),
    attivo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_accesso TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Table: stabilimenti
CREATE TABLE IF NOT EXISTS stabilimenti (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    citta VARCHAR(100) NOT NULL,
    indirizzo VARCHAR(255),
    numero_aia VARCHAR(100),
    data_rilascio_aia DATE,
    data_scadenza_aia DATE,
    ente_competente VARCHAR(255),
    responsabile_ambientale VARCHAR(255),
    email VARCHAR(255),
    telefono VARCHAR(20),
    attivo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Table: user_stabilimenti (Many-to-Many)
CREATE TABLE IF NOT EXISTS user_stabilimenti (
    user_id BIGINT NOT NULL,
    stabilimento_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, stabilimento_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (stabilimento_id) REFERENCES stabilimenti(id) ON DELETE CASCADE
);

-- Table: prescrizioni
CREATE TABLE IF NOT EXISTS prescrizioni (
    id BIGSERIAL PRIMARY KEY,
    stabilimento_id BIGINT NOT NULL,
    codice VARCHAR(100) NOT NULL,
    descrizione VARCHAR(1000) NOT NULL,
    matrice_ambientale VARCHAR(50) CHECK (matrice_ambientale IN ('ARIA', 'ACQUA', 'FALDA', 'RUMORE', 'SUOLO', 'RIFIUTI', 'ILLUMINAZIONE')),
    stato VARCHAR(50) CHECK (stato IN ('APERTA', 'IN_LAVORAZIONE', 'IN_ATTESA_INTEGRAZIONE', 'CHIUSA', 'SOSPESA')),
    data_emissione DATE,
    data_scadenza DATE,
    ente_emittente VARCHAR(255),
    riferimento_normativo VARCHAR(500),
    priorita VARCHAR(20) CHECK (priorita IN ('BASSA', 'MEDIA', 'ALTA', 'URGENTE')),
    note VARCHAR(2000),
    data_chiusura DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (stabilimento_id) REFERENCES stabilimenti(id) ON DELETE CASCADE
);

-- Table: monitoraggi
CREATE TABLE IF NOT EXISTS monitoraggi (
    id BIGSERIAL PRIMARY KEY,
    stabilimento_id BIGINT NOT NULL,
    codice VARCHAR(100) NOT NULL,
    descrizione VARCHAR(255) NOT NULL,
    tipo_monitoraggio VARCHAR(50) CHECK (tipo_monitoraggio IN ('EMISSIONI_ATMOSFERA', 'SCARICHI_IDRICI', 'FALDA', 'RUMORE', 'SUOLO', 'ODORI')),
    punto_emissione VARCHAR(255),
    frequenza VARCHAR(50) CHECK (frequenza IN ('GIORNALIERA', 'SETTIMANALE', 'MENSILE', 'BIMESTRALE', 'TRIMESTRALE', 'SEMESTRALE', 'ANNUALE', 'BIENNALE', 'TRIENNALE')),
    prossima_scadenza DATE,
    laboratorio VARCHAR(255),
    metodica VARCHAR(255),
    attivo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stabilimento_id) REFERENCES stabilimenti(id) ON DELETE CASCADE
);

-- Table: dati_ambientali
CREATE TABLE IF NOT EXISTS dati_ambientali (
    id BIGSERIAL PRIMARY KEY,
    monitoraggio_id BIGINT NOT NULL,
    data_campionamento DATE NOT NULL,
    parametro VARCHAR(255) NOT NULL,
    valore_misurato DOUBLE PRECISION,
    unita_misura VARCHAR(50),
    limite_autorizzato DOUBLE PRECISION,
    stato_conformita VARCHAR(20) CHECK (stato_conformita IN ('CONFORME', 'ATTENZIONE', 'NON_CONFORME')),
    rapporto_prova VARCHAR(255),
    laboratorio VARCHAR(255),
    note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (monitoraggio_id) REFERENCES monitoraggi(id) ON DELETE CASCADE
);

-- Table: scadenze
CREATE TABLE IF NOT EXISTS scadenze (
    id BIGSERIAL PRIMARY KEY,
    stabilimento_id BIGINT,
    prescrizione_id BIGINT,
    monitoraggio_id BIGINT,
    titolo VARCHAR(255) NOT NULL,
    descrizione VARCHAR(1000),
    tipo_scadenza VARCHAR(50) CHECK (tipo_scadenza IN ('MONITORAGGIO_PMC', 'RELAZIONE_ANNUALE', 'INTEGRAZIONE_ENTE', 'RINNOVO_AIA', 'COMUNICAZIONE', 'ALTRO')),
    data_scadenza DATE NOT NULL,
    stato VARCHAR(20) CHECK (stato IN ('PENDING', 'IN_CORSO', 'COMPLETATA', 'SCADUTA')),
    priorita VARCHAR(20) CHECK (priorita IN ('BASSA', 'MEDIA', 'ALTA', 'URGENTE')),
    responsabile VARCHAR(255),
    email_notifica VARCHAR(255),
    giorni_preavviso INTEGER DEFAULT 20,
    data_completamento DATE,
    note VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (stabilimento_id) REFERENCES stabilimenti(id) ON DELETE SET NULL,
    FOREIGN KEY (prescrizione_id) REFERENCES prescrizioni(id) ON DELETE CASCADE,
    FOREIGN KEY (monitoraggio_id) REFERENCES monitoraggi(id) ON DELETE CASCADE
);

-- Table: documenti
CREATE TABLE IF NOT EXISTS documenti (
    id BIGSERIAL PRIMARY KEY,
    prescrizione_id BIGINT,
    stabilimento_id BIGINT NOT NULL,
    nome VARCHAR(255) NOT NULL,
    nome_file VARCHAR(255) NOT NULL,
    tipo_documento VARCHAR(50) CHECK (tipo_documento IN ('PRESCRIZIONE_AIA', 'RAPPORTO_PROVA', 'RELAZIONE_ANNUALE', 'INTEGRAZIONE', 'COMUNICAZIONE_PEC', 'VERBALE', 'PLANIMETRIA', 'STUDIO_TECNICO', 'ALTRO')),
    descrizione VARCHAR(1000),
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    versione INTEGER DEFAULT 1,
    ente_destinatario VARCHAR(255),
    anno INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    FOREIGN KEY (prescrizione_id) REFERENCES prescrizioni(id) ON DELETE SET NULL,
    FOREIGN KEY (stabilimento_id) REFERENCES stabilimenti(id) ON DELETE CASCADE
);

-- ===============================================
-- INDEXES
-- ===============================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_ruolo ON users(ruolo);

CREATE INDEX idx_stabilimenti_nome ON stabilimenti(nome);
CREATE INDEX idx_stabilimenti_attivo ON stabilimenti(attivo);

CREATE INDEX idx_prescrizioni_stabilimento ON prescrizioni(stabilimento_id);
CREATE INDEX idx_prescrizioni_stato ON prescrizioni(stato);
CREATE INDEX idx_prescrizioni_priorita ON prescrizioni(priorita);

CREATE INDEX idx_monitoraggi_stabilimento ON monitoraggi(stabilimento_id);
CREATE INDEX idx_monitoraggi_attivo ON monitoraggi(attivo);

CREATE INDEX idx_dati_ambientali_monitoraggio ON dati_ambientali(monitoraggio_id);
CREATE INDEX idx_dati_ambientali_data ON dati_ambientali(data_campionamento);
CREATE INDEX idx_dati_ambientali_conformita ON dati_ambientali(stato_conformita);

CREATE INDEX idx_scadenze_stabilimento ON scadenze(stabilimento_id);
CREATE INDEX idx_scadenze_data ON scadenze(data_scadenza);
CREATE INDEX idx_scadenze_stato ON scadenze(stato);

CREATE INDEX idx_documenti_stabilimento ON documenti(stabilimento_id);
CREATE INDEX idx_documenti_prescrizione ON documenti(prescrizione_id);
CREATE INDEX idx_documenti_tipo ON documenti(tipo_documento);
CREATE INDEX idx_documenti_anno ON documenti(anno);

-- ===============================================
-- INITIAL DATA (Opzionale)
-- ===============================================

-- Admin user di default (password: admin123 - DA CAMBIARE!)
-- Password hash BCrypt per 'admin123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO users (username, password, nome, cognome, email, ruolo, attivo)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'Sistema', 'admin@grandimolini.it', 'ADMIN', TRUE)
ON CONFLICT (username) DO NOTHING;

-- ===============================================
-- GRANTS (Opzionale - per utente specifico)
-- ===============================================

-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO aia_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO aia_user;

-- ===============================================
-- FINE SCRIPT
-- ===============================================
