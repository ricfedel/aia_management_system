-- ============================================================
-- ANAGRAFICA CAMINI – GMI Livorno (stabilimento_id = 1)
-- Fonte: Decreto RT n. 19082 del 20/08/2024 – Allegato A
--        Tabella A "Quadro riassuntivo delle emissioni in atmosfera"
-- Compatibile con H2 (dev) e PostgreSQL (prod)
-- ============================================================

BEGIN;

INSERT INTO anagrafica_camini
  (stabilimento_id, sigla, fase_processo, origine,
   portata_nm3h, sezione_m2, velocita_ms,
   temperatura_c, temperatura_ambiente,
   altezza_m, durata_h_giorno, durata_g_anno,
   impianto_abbattimento, note, attivo)
VALUES

-- ── TRASPORTO ─────────────────────────────────────────────────────────
(1,'E1',  'TRASPORTO',    'trasportatore torre',           4800,  0.125, 10.67, NULL, TRUE,  13,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E2',  'TRASPORTO',    'nastro in banchina',            4800,  0.125, 10.67, NULL, TRUE,  10,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E3',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  12,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E7',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  40,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E8',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  40,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E52', 'TRASPORTO',    'silos sottoprodotti',           7200,  0.20,  10.1,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),

-- ── PREPULITURA ───────────────────────────────────────────────────────
(1,'E4',  'PREPULITURA',  'aspirazione generale',         13200,  0.28,  13.1,  NULL, TRUE,  38,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E5',  'PREPULITURA',  'vibroblok',                   10800,  0.23,  13.04, NULL, TRUE,  38,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E6',  'PREPULITURA',  'vibroblok',                   10800,  0.23,  13.04, NULL, TRUE,  38,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E9',  'PREPULITURA',  'bilancia',                     3600,  0.09,  11.11, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E10', 'PREPULITURA',  'vibroblok',                   10800,  0.28,  10.71, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E11', 'PREPULITURA',  'bilancia',                     3600,  0.09,  11.11, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E12', 'PREPULITURA',  'vibroblok',                   10800,  0.28,  10.71, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E13', 'PREPULITURA',  'aspirazione generale',        20400,  0.44,  12.88, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E28', 'PREPULITURA',  'pompa celle',                  1920,  0.08,   6.67, NULL, TRUE,  53,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E29', 'PREPULITURA',  'pompa macinatrice',            1200,  0.09,   3.7,  NULL, TRUE,  17,   10, 365, 'filtro a maniche', NULL, TRUE),

-- ── PULITURA ──────────────────────────────────────────────────────────
(1,'E30', 'PULITURA',     '1a pulitura grano tenero',    19200,  0.96,   5.6,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E31', 'PULITURA',     '1a-2a pulitura grano tenero', 10800,  0.75,   4.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E32', 'PULITURA',     '1a pulitura grano duro',      19200,  0.96,   5.6,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E33', 'PULITURA',     '1a pulitura grano duro',       9480,  0.57,   4.7,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E34', 'PULITURA',     '1a pulitura grano duro',       9480,  0.57,   4.7,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E35', 'PULITURA',     '1a pulitura grano duro',      10800,  0.75,   4.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E36', 'PULITURA',     '2a pulitura grano duro',       8400,  0.75,   3.1,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E37', 'PULITURA',     'macinazione scarti',          10200,  0.57,   5.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E38', 'PULITURA',     'macinazione scarti',           3600,  0.06,  16.7,  NULL, TRUE,   5,   24, 365, 'filtro a maniche', NULL, TRUE),

-- ── MACINAZIONE ───────────────────────────────────────────────────────
(1,'E39', 'MACINAZIONE',  'molino a duro',               18000,  0.96,   5.2,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E40', 'MACINAZIONE',  'molino a duro',               14400,  0.57,   7.1,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E41', 'MACINAZIONE',  'molino a duro',               33600,  0.96,   9.7,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E42', 'MACINAZIONE',  'molino a duro',               18000,  0.96,   5.2,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E43', 'MACINAZIONE',  'molino a duro',               33600,  0.96,   9.7,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E44', 'MACINAZIONE',  'molino a duro',               37200,  1.14,   9.1,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E45', 'MACINAZIONE',  'molino a duro',               10800,  0.57,   5.3,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E46', 'MACINAZIONE',  'molino a tenero',             18600,  0.57,   9.0,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E47', 'MACINAZIONE',  'molino a tenero',             18600,  0.57,   9.0,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E48', 'MACINAZIONE',  'molino a tenero',             16800,  0.51,   9.2,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E49', 'MACINAZIONE',  'molino a tenero',             16800,  0.51,   9.2,  NULL, TRUE,  33,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E50', 'MACINAZIONE',  'cubettatrici',                25200,  0.45,  15.7,  NULL, TRUE,  33,   24, 365, 'turbodecantatore', 'Prevista sostituzione con filtro a maniche (collaudo entro ottobre 2024)', TRUE),
(1,'E51', 'MACINAZIONE',  'cubettatrici',                25200,  0.45,  15.7,  NULL, TRUE,  33,   24, 365, 'turbodecantatore', 'Prevista sostituzione con filtro a maniche (collaudo entro ottobre 2024)', TRUE),

-- ── MOVIMENTAZIONE ────────────────────────────────────────────────────
(1,'E14', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E15', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E16', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E17', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E18', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E19', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E20', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E21', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E22', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E23', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E24', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E25', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E26', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E27', 'MOVIMENTAZIONE','cappe aspirazione',           15000,  0.56,   7.44, NULL, TRUE,   8,   10, 365, 'filtro a maniche', NULL, TRUE),
(1,'E53', 'MOVIMENTAZIONE','aspirazione silos semola',    2162,  0.075,   8.8,  NULL, TRUE,  35,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E54', 'MOVIMENTAZIONE','aspirazione silos semola',    2162,  0.075,   8.8,  NULL, TRUE,  35,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E55', 'MOVIMENTAZIONE','aspirazione silos semola',    2162,  0.075,   8.8,  NULL, TRUE,  35,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E56', 'MOVIMENTAZIONE','aspirazione staccio semola',  2595,  0.11,    7.0,  NULL, TRUE,  31.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E57', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3784,  0.135,   8.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E58', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3244,  0.135,   7.4,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E59', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3244,  0.135,   7.4,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E60', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3784,  0.135,   8.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E61', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3784,  0.135,   8.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E62', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3244,  0.135,   7.4,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E63', 'MOVIMENTAZIONE','aspirazione silos rinfusa',   3244,  0.135,   7.4,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E64', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E65', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E66', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E67', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E68', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E69', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E70', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E71', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E72', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E73', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3784, 0.135, 9.2,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E74', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3784, 0.135, 9.2,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E75', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E76', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E77', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E78', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E79', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E80', 'MOVIMENTAZIONE','aspirazione silos miscelazione', 3244, 0.135, 7.4,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E81', 'MOVIMENTAZIONE','aspirazione silos miniconfezionamento', 2162, 0.165, 5.0, NULL, TRUE, 36,  24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E82', 'MOVIMENTAZIONE','aspirazione silos miniconfezionamento', 1081, 0.09,  5.4, NULL, TRUE, 36,  24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E83', 'MOVIMENTAZIONE','aspirazione silos insacco',    3784,  0.135,   8.6,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E84', 'MOVIMENTAZIONE','aspirazione silos insacco',    3784,  0.135,   8.6,  NULL, TRUE,  36,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E85', 'MOVIMENTAZIONE','aspirazione insacco',          4866,  0.18,    8.2,  NULL, TRUE,  15,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E86', 'MOVIMENTAZIONE','aspirazione miscelazione',     1081,  0.135,   7.4,  NULL, TRUE,  15,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E87', 'MOVIMENTAZIONE','aspirazione miscelazione',     1081,  0.135,   7.4,  NULL, TRUE,  15,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E88', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E89', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E90', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E91', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E92', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E93', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E94', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E95', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E96', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm³ – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E112','MOVIMENTAZIONE','(in corso di messa in esercizio)', NULL, NULL, NULL, NULL, TRUE, NULL, 24, 365, 'filtro a maniche', 'Messa in esercizio 18/06/2024 (E112)', FALSE),
(1,'E113','MOVIMENTAZIONE','(in corso di messa in esercizio)', NULL, NULL, NULL, NULL, TRUE, NULL, 24, 365, 'filtro a maniche', 'Messa in esercizio 18/06/2024 (E113)', FALSE),
-- Previsti entro 2024/2025 (non ancora in esercizio)
(1,'E98', 'MOVIMENTAZIONE','(realizzazione prevista entro 2024)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro dicembre 2024', FALSE),
(1,'E99', 'MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E100','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E101','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E102','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E109','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E110','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E111','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E114','MOVIMENTAZIONE','(realizzazione prevista entro 2024)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro dicembre 2024', FALSE),

-- ── MANUTENZIONE ──────────────────────────────────────────────────────
(1,'E97', 'MANUTENZIONE', 'aspirazione operazioni di saldatura ad elettrodo',
   6400, 0.071, 25.2, NULL, TRUE, 3, 2, 220, NULL,
   'Emissione sporadica non asservita al ciclo produttivo. VLE: polveri 5 mg/Nm³, Cr/Ni/Cd/Co/Pb 0.1 mg/Nm³, Sn 2 mg/Nm³. Unico controllo analitico entro 31/12/2024', TRUE),

-- ── CONTROLLO QUALITÀ (in deroga – Parte V D.lgs. 152/2006) ──────────
(1,'E115','CONTROLLO_QUALITA','cappe aspirazione laboratorio CQ', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Test fisico-reologici. Dichiarato non soggetto ad autorizzazione: All. IV Part. V D.lgs. 152/2006 lettera jj)', TRUE),
(1,'E116','CONTROLLO_QUALITA','cappe aspirazione laboratorio CQ', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Test fisico-reologici. Dichiarato non soggetto ad autorizzazione: All. IV Part. V D.lgs. 152/2006 lettera jj)', TRUE),
(1,'E117','CONTROLLO_QUALITA','forni cottura sperimentale pane', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Prove di panificazione. Dichiarato non soggetto ad autorizzazione: All. IV lettera f) e jj)', TRUE),
(1,'E118','CONTROLLO_QUALITA','forni cottura sperimentale pane', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Prove di panificazione. Dichiarato non soggetto ad autorizzazione: All. IV lettera f) e jj)', TRUE);

COMMIT;

-- ============================================================
-- NOTE SULL'UTILIZZO
-- ============================================================
-- H2 (sviluppo locale):
--   Includere in src/main/resources/data.sql (dopo aia_seed_data.sql)
--   oppure eseguire manualmente da H2 console.
--
-- PostgreSQL (produzione):
--   psql -U <user> -d <db> -f anagrafica_camini_seed.sql
--   oppure integrare in uno script Flyway (V3__anagrafica_camini.sql).
--
-- Promolog (stabilimento_id = 2):
--   Il PMC Promolog riporta solo provenienza e durata (non portata/sezione/altezza).
--   I dati potranno essere inseriti manualmente dall'interfaccia una volta noti.
-- ============================================================
