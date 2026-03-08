-- ============================================================
-- AIA Management System - SEED DATA
-- Estratto da documenti AIA ufficiali:
--   GMI (Grandi Molini Italiani): Decreto RT n.19082 del 20/08/2024
--   Promolog: Determinazione Provincia di Verona n.2635 del 06/09/2023
--
-- Compatibile con: H2 (dev/test) e PostgreSQL (produzione)
-- Utilizzo:
--   H2  → inserire in src/main/resources/data.sql  (Spring esegue automaticamente)
--   PG  → psql -U user -d aia_db -f aia_seed_data.sql
--         oppure come migration Flyway: V2__seed_data.sql
--
-- Gli INSERT sono ordinati per rispettare i vincoli FK senza disabilitarli:
--   stabilimento → prescrizione → punto_monitoraggio → parametro_monitoraggio
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- STABILIMENTI
-- ------------------------------------------------------------
INSERT INTO stabilimento (id, nome, indirizzo, comune, provincia, codice_fiscale, partita_iva, codice_aia, ente_rilascio, data_rilascio_aia, data_scadenza_aia, codice_ippc, attivo)
VALUES
  (1, 'Grandi Molini Italiani S.p.A. – Livorno',
      'Via L. Da Vinci, 19', 'Livorno', 'LI',
      NULL, '00363690298',
      'Decreto n.19082/2024',
      'Regione Toscana – Settore AIA',
      '2024-08-20', '2034-08-20',
      '6.4 b2)', TRUE),
  (2, 'Promolog S.r.l. – Albaredo d''Adige',
      'Via Zurlare, 21 – Coriano Veronese', 'Albaredo d''Adige', 'VR',
      NULL, NULL,
      'Det. n.2635/2023',
      'Provincia di Verona – Settore AIA',
      '2023-09-06', '2033-09-06',
      '6.4 b2)', TRUE);

-- ------------------------------------------------------------
-- PRESCRIZIONI AIA – GMI LIVORNO
-- ------------------------------------------------------------
INSERT INTO prescrizione (id, stabilimento_id, numero, descrizione, tipo_matrice, stato, data_scadenza, riferimento_normativo, attivo)
VALUES
-- 5.2 Emissioni atmosfera
  (101, 1, 'ATM-01', 'Rispettare il quadro emissivo della Tabella A (Allegato A) con frequenza di monitoraggio della Tabella 2 (Allegato B)', 'ARIA', 'APERTA', NULL, 'D.Lgs. 152/2006 art.269', TRUE),
  (102, 1, 'ATM-02', 'Emissioni da macinatura e pulitura cereali: monitoraggio annuale polveri con VLE ≤ 5 mg/Nm³ (BAT-AEL BAT 28 FDM)', 'ARIA', 'APERTA', NULL, 'BAT 5 / BAT 28 BATc FDM 2019', TRUE),
  (103, 1, 'ATM-03', 'Emissioni da trasporto/movimentazione: monitoraggio annuale polveri VLE ≤ 5 mg/Nm³ (24h/giorno, 365 gg/anno)', 'ARIA', 'APERTA', NULL, 'D.Lgs. 152/2006', TRUE),
  (104, 1, 'ATM-04', 'Emissioni E66, E91-E96 (movimentazione): controllo mensile differenziale pressorio (deprimometri). VLE ≤ 3 mg/Nm³', 'ARIA', 'APERTA', NULL, 'Prescrizione par. 5.2.4', TRUE),
  (105, 1, 'ATM-05', 'E97 (saldatura): unico controllo analitico entro 31/12/2024. Parametri: polveri 5 mg/Nm³, Cr/Ni/Cd/Co/Pb 0,1 mg/Nm³, Sn 2 mg/Nm³', 'ARIA', 'APERTA', '2024-12-31', 'Piano Regionale Qualità Aria All.2', TRUE),
  (106, 1, 'ATM-06', 'Emissioni con portata >15.000 Nm³/h autorizzate 24h/365gg: installare sistemi allarme differenziale pressorio entro 30/03/2025', 'ARIA', 'APERTA', '2025-03-30', 'Prescrizione par. 5.2.6', TRUE),
  (107, 1, 'ATM-07', 'Manutenzione almeno annuale di tutti i sistemi di abbattimento (filtri a maniche): verifica integrità tessuti filtranti', 'ARIA', 'APERTA', NULL, 'Tabella 3 Allegato B', TRUE),
  (108, 1, 'ATM-08', 'Sistemi abbattimento senza allarme pressione: controllo semestrale ΔP con misuratore mobile. E66, E91-E96: mensile', 'ARIA', 'APERTA', NULL, 'Prescrizione par. 5.2.7', TRUE),
  (109, 1, 'ATM-09', 'E91, E92: comunicare entro 31/10/2024 il range di buon funzionamento del sistema di abbattimento', 'ARIA', 'APERTA', '2024-10-31', 'Prescrizione par. 5.2.8', TRUE),
  (110, 1, 'ATM-10', 'Comunicare data messa in esercizio con preavviso ≥10 gg per impianti futura attivazione (incl. E50, E51 sostituzione filtri entro fine 2024)', 'ARIA', 'APERTA', '2024-12-31', 'Prescrizione par. 5.2.9', TRUE),
  (111, 1, 'ATM-11', 'Entro 31/12/2024: relazione valutazione costi-benefici riunificazione camini (art. 270 c.4 D.Lgs. 152/2006)', 'ARIA', 'APERTA', '2024-12-31', 'D.Lgs. 152/2006 art.270 c.4', TRUE),
  (112, 1, 'ATM-12', 'Risultati analisi camini (PMC Allegato B): conservare certificati analitici ordinati cronologicamente presso azienda', 'ARIA', 'APERTA', NULL, 'App. 1 All. VI Parte V D.Lgs. 152/2006', TRUE),
  (113, 1, 'ATM-13', 'Modifiche previste entro 2025 (E99-E102, E109-E111): comunicare messa in esercizio con preavviso ≥15 gg a RT-AIA, ARPAT, Comune', 'ARIA', 'APERTA', '2025-12-31', 'Prescrizione par. 5.2.12', TRUE),
  (114, 1, 'ATM-14', 'Messa a regime e controlli analitici art. 269 c.3: entro 2024 per E50/E51/E98/E112/E113/E114; entro 2025 per E99-E102/E109-E111', 'ARIA', 'APERTA', '2025-12-31', 'D.Lgs. 152/2006 art.269 c.3', TRUE),
  (115, 1, 'ATM-15', 'Entro 30/06/2026: aggiornare studio diffusionale a seguito attivazione nuove sorgenti emissive previste entro 2025', 'ARIA', 'APERTA', '2026-06-30', 'Prescrizione par. 5.2.16', TRUE),
-- 5.3 Scarichi idrici
  (116, 1, 'IDR-01', 'Rispettare limiti allo scarico S1 e S2 in acque superficiali (Tab. 3, All. 5, Parte III D.Lgs. 152/2006)', 'ACQUA', 'APERTA', NULL, 'D.Lgs. 152/2006 All.5 Tab.3', TRUE),
  (117, 1, 'IDR-02', 'Monitorare parametri inquinanti caratteristici scarichi idrici (Tabella 4 Allegato B) con frequenza indicata', 'ACQUA', 'APERTA', NULL, 'Tabella 4 Allegato B', TRUE),
  (118, 1, 'IDR-03', 'Entro 15/09/2024: trasmettere planimetria aggiornata rete idrica con indicazione pozzetti ispezione uscita impianti depurazione', 'ACQUA', 'CHIUSA', '2024-09-15', 'Prescrizione par. 5.3.3', TRUE),
-- 5.4 Rifiuti
  (119, 1, 'RIF-01', 'Rifiuti classificati e gestiti nel rispetto della Parte IV D.Lgs. 152/2006', 'RIFIUTI', 'APERTA', NULL, 'D.Lgs. 152/2006 Parte IV', TRUE),
  (120, 1, 'RIF-02', 'Rifiuti gestiti con criteri deposito temporaneo (art. 185-bis D.Lgs. 152/2006)', 'RIFIUTI', 'APERTA', NULL, 'D.Lgs. 152/2006 art.185-bis', TRUE),
  (121, 1, 'RIF-03', 'Relazione annuale: riportare quantità totale rifiuti prodotti (pericolosi/non pericolosi), destinazione recupero/smaltimento', 'RIFIUTI', 'APERTA', NULL, 'Prescrizione par. 5.4.6', TRUE),
  (122, 1, 'RIF-04', 'Entro 15/09/2024: trasmettere planimetria aggiornata depositi rifiuti', 'RIFIUTI', 'CHIUSA', '2024-09-15', 'Prescrizione par. 5.4.7', TRUE),
-- 5.5 Energia
  (123, 1, 'ENE-01', 'PMC: riportare consumo energia elettrica (MWh) e produzione fotovoltaico', 'ENERGIA', 'APERTA', NULL, 'Prescrizione par. 5.5.1', TRUE),
  (124, 1, 'ENE-02', 'PMC: riportare bilancio energetico complessivo e confronto con valori BREF', 'ENERGIA', 'APERTA', NULL, 'Prescrizione par. 5.5.2', TRUE),
  (125, 1, 'ENE-03', 'Consumo specifico energia atteso < 0,13 MWh/t come media annua (BAT Tab. 14 FDM)', 'ENERGIA', 'APERTA', NULL, 'BAT 5 Tab. 14 BATc FDM 2019', TRUE),
-- 5.6 Agenti fisici (rumore)
  (126, 1, 'ACU-01', 'Rispettare valutazione impatto acustico presentata. Valutazione periodica ogni 3 anni', 'RUMORE', 'APERTA', NULL, 'DGR 788/99, DM 16/3/98, DPCM 14/11/97', TRUE),
-- PMC generale
  (127, 1, 'PMC-01', 'Entro 30 aprile di ogni anno: trasmettere PEC a RT-AIA, Comune Livorno, ARPAT Livorno, USL Toscana Nord-Ovest sintesi risultati PMC anno precedente', 'GENERALE', 'APERTA', NULL, 'Allegato B par. 1.1.2', TRUE),
  (128, 1, 'PMC-02', 'Tutte le registrazioni PMC conservate presso sede impianto per intera durata autorizzazione', 'GENERALE', 'APERTA', NULL, 'Allegato B par. 1.1.1', TRUE);

-- ------------------------------------------------------------
-- PUNTI DI MONITORAGGIO – GMI LIVORNO (selezione)
-- ------------------------------------------------------------
INSERT INTO punto_monitoraggio (id, stabilimento_id, codice, descrizione, monitoraggio_tipo, fase_processo, frequenza_monitoraggio, laboratorio_esterno, attivo)
VALUES
-- CAMINI – selezione rappresentativa (E1-E96 tutti ARIA/CAMINO)
  (1001, 1, 'E1',  'Scarico nave – trasportatore torre',        'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1002, 1, 'E2',  'Scarico nave – nastro in banchina',         'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1003, 1, 'E3',  'Scarico nave – trasportatore',              'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1004, 1, 'E4',  'Prepulitura lato mare – aspirazione generale','CAMINO','PREPULITURA',  'ANNUALE', TRUE, TRUE),
  (1005, 1, 'E5',  'Prepulitura lato mare – vibroblok',         'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1006, 1, 'E6',  'Prepulitura lato mare – vibroblok',         'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1007, 1, 'E7',  'Trasferimento grano – trasportatore',       'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1008, 1, 'E8',  'Trasferimento grano – trasportatore',       'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1009, 1, 'E9',  'Prepulitura lato terra T – bilancia',       'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1010, 1, 'E10', 'Prepulitura lato terra T – vibroblok',      'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1011, 1, 'E11', 'Prepulitura lato terra D – bilancia',       'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1012, 1, 'E12', 'Prepulitura lato terra D – vibroblok',      'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1013, 1, 'E13', 'Prepulitura lato terra D – aspirazione generale','CAMINO','PREPULITURA','ANNUALE', TRUE, TRUE),
  (1014, 1, 'E14', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1015, 1, 'E15', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1016, 1, 'E16', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1017, 1, 'E17', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1018, 1, 'E18', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1019, 1, 'E19', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1020, 1, 'E20', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1021, 1, 'E21', 'Scarico camion – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1022, 1, 'E22', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1023, 1, 'E23', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1024, 1, 'E24', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1025, 1, 'E25', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1026, 1, 'E26', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1027, 1, 'E27', 'Scarico vagoni – cappe aspirazione',        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1028, 1, 'E28', 'Prepulitura – pompa celle',                 'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1029, 1, 'E29', 'Prepulitura – pompa macinatrice',           'CAMINO', 'PREPULITURA',   'ANNUALE', TRUE, TRUE),
  (1030, 1, 'E30', '1ᵃ pulitura grano tenero',                  'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1031, 1, 'E31', '1ᵃ / 2ᵃ pulitura grano tenero',            'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1032, 1, 'E32', '1ᵃ pulitura grano duro',                    'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1033, 1, 'E33', '1ᵃ pulitura grano duro',                    'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1034, 1, 'E34', '1ᵃ pulitura grano duro',                    'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1035, 1, 'E35', '1ᵃ pulitura grano duro',                    'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1036, 1, 'E36', '2ᵃ pulitura grano duro',                    'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1037, 1, 'E37', 'Pulitura – macinazione scarti',             'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1038, 1, 'E38', 'Pulitura – macinazione scarti',             'CAMINO', 'PULITURA',      'ANNUALE', TRUE, TRUE),
  (1039, 1, 'E39', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1040, 1, 'E40', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1041, 1, 'E41', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1042, 1, 'E42', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1043, 1, 'E43', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1044, 1, 'E44', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1045, 1, 'E45', 'Molino a duro',                             'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1046, 1, 'E46', 'Molino a tenero',                           'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1047, 1, 'E47', 'Molino a tenero',                           'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1048, 1, 'E48', 'Molino a tenero',                           'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1049, 1, 'E49', 'Molino a tenero',                           'CAMINO', 'MACINAZIONE',   'ANNUALE', TRUE, TRUE),
  (1050, 1, 'E50', 'Cubettatrici coprodotti (sostituzione filtro maniche entro ott. 2024)', 'CAMINO', 'MACINAZIONE', 'ANNUALE', TRUE, TRUE),
  (1051, 1, 'E51', 'Cubettatrici coprodotti (sostituzione filtro maniche entro ott. 2024)', 'CAMINO', 'MACINAZIONE', 'ANNUALE', TRUE, TRUE),
  (1052, 1, 'E52', 'Silos sottoprodotti',                       'CAMINO', 'TRASPORTO',     'ANNUALE', TRUE, TRUE),
  (1053, 1, 'E53', 'Asp. Silos Semola',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1054, 1, 'E54', 'Asp. Silos Semola',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1055, 1, 'E55', 'Asp. Silos Semola',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1056, 1, 'E56', 'Asp. Staccio Semola',                       'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1057, 1, 'E57', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1058, 1, 'E58', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1059, 1, 'E59', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1060, 1, 'E60', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1061, 1, 'E61', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1062, 1, 'E62', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1063, 1, 'E63', 'Asp. Silos Rinfusa',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1064, 1, 'E64', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1065, 1, 'E65', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1066, 1, 'E66', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1067, 1, 'E67', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1068, 1, 'E68', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1069, 1, 'E69', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1070, 1, 'E70', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1071, 1, 'E71', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1072, 1, 'E72', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1073, 1, 'E73', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1074, 1, 'E74', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1075, 1, 'E75', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1076, 1, 'E76', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1077, 1, 'E77', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1078, 1, 'E78', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1079, 1, 'E79', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1080, 1, 'E80', 'Asp. Silos Miscelazione',                   'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1081, 1, 'E81', 'Asp. Silos Minicel',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1082, 1, 'E82', 'Asp. Silos Minicel',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1083, 1, 'E83', 'Asp. Silos Insacco',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1084, 1, 'E84', 'Asp. Silos Insacco',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1085, 1, 'E85', 'Asp. Linea Insacco',                        'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1086, 1, 'E86', 'Asp. Miscelazione',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1087, 1, 'E87', 'Asp. Miscelazione',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1088, 1, 'E88', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1089, 1, 'E89', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1090, 1, 'E90', 'Asp. Silos Farina',                         'CAMINO', 'MOVIMENTAZIONE','ANNUALE', TRUE, TRUE),
  (1091, 1, 'E91', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1092, 1, 'E92', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1093, 1, 'E93', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1094, 1, 'E94', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1095, 1, 'E95', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1096, 1, 'E96', 'Asp. Silos Farina (VLE 3 mg/Nm³ – ctrl mensile ΔP)', 'CAMINO', 'MOVIMENTAZIONE', 'MENSILE_DELTA_P', TRUE, TRUE),
  (1097, 1, 'E97', 'Asp. Saldatura (un unico controllo analitico entro 31/12/2024)', 'CAMINO', 'MANUTENZIONE', 'UNA_TANTUM', TRUE, TRUE),
-- Emissioni future (previste entro 2024/2025)
  (1098, 1, 'E98',  'Movimentazione – in attivazione entro 2024', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1099, 1, 'E99',  'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1100, 1, 'E100', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1101, 1, 'E101', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1102, 1, 'E102', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1103, 1, 'E109', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1104, 1, 'E110', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1105, 1, 'E111', 'Movimentazione – in attivazione entro 2025', 'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1106, 1, 'E112', 'Insacco germe – in messa in esercizio',      'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1107, 1, 'E113', 'Ripasso insacco – in messa in esercizio',    'CAMINO', 'MOVIMENTAZIONE', 'ANNUALE', TRUE, FALSE),
  (1108, 1, 'E114', 'Ampliamento officina – in attivazione entro 2024', 'CAMINO', 'MANUTENZIONE', 'ANNUALE', TRUE, FALSE),
-- SCARICHI IDRICI
  (1201, 1, 'S1', 'Scarico acque reflue domestiche – impianto depurazione palazzina uffici (40 A.E.)', 'SCARICO', 'SCARICHI', 'ANNUALE', TRUE, TRUE),
  (1202, 1, 'S2', 'Scarico acque reflue domestiche – impianto depurazione zona insacco (20 A.E.)',     'SCARICO', 'SCARICHI', 'ANNUALE', TRUE, TRUE),
-- PIEZOMETRI (acque sotterranee)
  (1301, 1, 'PZ1', 'Piezometro 1 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', TRUE, TRUE),
  (1302, 1, 'PZ2', 'Piezometro 2 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', TRUE, TRUE),
  (1303, 1, 'PZ3', 'Piezometro 3 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', TRUE, TRUE),
  (1304, 1, 'PZ4', 'Piezometro 4 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', TRUE, TRUE);

-- ------------------------------------------------------------
-- PARAMETRI DI MONITORAGGIO – GMI (camini attivi E1-E96 standard)
-- Parametro: Polveri totali, VLE 5 mg/Nm³, metodo UNI EN 13284
-- ------------------------------------------------------------
INSERT INTO parametro_monitoraggio (id, punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT
  1000 + pm.id,
  pm.id,
  'Polveri totali',
  'mg/Nm3',
  5.0,
  'D.Lgs. 152/2006 – BAT-AEL BAT 28 BATc FDM 2019',
  'UNI EN 13284',
  TRUE
FROM punto_monitoraggio pm
WHERE pm.stabilimento_id = 1
  AND pm.monitoraggio_tipo = 'CAMINO'
  AND pm.codice NOT IN ('E66','E91','E92','E93','E94','E95','E96','E97');

-- VLE 3 mg/Nm³ per E66, E91-E96 (movimentazione con deroga analisi periodica)
INSERT INTO parametro_monitoraggio (id, punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT
  2000 + pm.id,
  pm.id,
  'Polveri totali',
  'mg/Nm3',
  3.0,
  'VLE con deroga autocontrollo annuale – ctrl mensile ΔP',
  'UNI EN 13284',
  TRUE
FROM punto_monitoraggio pm
WHERE pm.stabilimento_id = 1
  AND pm.codice IN ('E66','E91','E92','E93','E94','E95','E96');

-- Parametri multipli E97 (saldatura)
INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1097, 'Polveri totali', 'mg/Nm3',  5.0, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Cromo (Cr)',     'mg/Nm3',  0.1, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Nichel (Ni)',    'mg/Nm3',  0.1, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Cadmio (Cd)',    'mg/Nm3',  0.1, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Cobalto (Co)',   'mg/Nm3',  0.1, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Piombo (Pb)',    'mg/Nm3',  0.1, 'Piano Regionale Qualità Aria All.2', NULL, TRUE),
  (1097, 'Stagno (Sn)',    'mg/Nm3',  2.0, 'Piano Regionale Qualità Aria All.2', NULL, TRUE);

-- Parametri piezometri (acque sotterranee)
INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1301, 'pH',                    NULL,   NULL, 'D.Lgs. 152/2006 All.3 Tab.2', 'APAT CNR IRSA 2030', TRUE),
  (1301, 'Conduttività',          'µS/cm',NULL, 'D.Lgs. 152/2006 All.3 Tab.2', 'APAT CNR IRSA 2030', TRUE),
  (1301, 'Alluminio',             'µg/L', 200,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Arsenico',              'µg/L', 10,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cadmio',                'µg/L', 5,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cromo totale',          'µg/L', 50,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cromo VI',              'µg/L', 5,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Ferro',                 'µg/L', 200,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Mercurio',              'µg/L', 1,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Nichel',                'µg/L', 20,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Piombo',                'µg/L', 10,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Rame',                  'µg/L', 1000, 'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Manganese',             'µg/L', 50,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Zinco',                 'µg/L', 3000, 'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Benzene',               'µg/L', 1,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'Tricloroetilene',       'µg/L', 1.5,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'Tetracloroetilene',     'µg/L', 1.1,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'IPA totali',            'µg/L', 0.1,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 8270D',          TRUE);

-- Replica parametri piezometrici per PZ2, PZ3, PZ4
INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1302, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametro_monitoraggio WHERE punto_monitoraggio_id = 1301;

INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1303, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametro_monitoraggio WHERE punto_monitoraggio_id = 1301;

INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1304, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametro_monitoraggio WHERE punto_monitoraggio_id = 1301;

-- Parametri scarichi idrici S1/S2
INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1201, 'pH',                    NULL,   NULL, 'D.Lgs. 152/2006 All.5 Tab.3 – Scarico in acque superficiali', 'APAT CNR IRSA', TRUE),
  (1201, 'COD',                   'mg/L', 160,  'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'BOD5',                  'mg/L', 40,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Solidi Sospesi',        'mg/L', 80,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Azoto ammoniacale',     'mg/L', 15,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Azoto totale',          'mg/L', 15,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Fosforo totale',        'mg/L', 10,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Tensioattivi totali',   'mg/L', 2,    'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Oli e grassi',          'mg/L', 20,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE);

INSERT INTO parametro_monitoraggio (punto_monitoraggio_id, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1202, nome_parametro, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametro_monitoraggio WHERE punto_monitoraggio_id = 1201;

-- ------------------------------------------------------------
-- PRESCRIZIONI PROMOLOG (principali)
-- ------------------------------------------------------------
INSERT INTO prescrizione (id, stabilimento_id, numero, descrizione, tipo_matrice, stato, data_scadenza, riferimento_normativo, attivo)
VALUES
  (201, 2, 'ATM-01', 'Rispettare i limiti emissivi per le emissioni in atmosfera del PMC approvato', 'ARIA', 'APERTA', NULL, 'D.Lgs. 152/2006 Parte V', TRUE),
  (202, 2, 'ATM-02', 'Polveri totali: VLE 5 mg/Nm³ per le emissioni da macinazione e pulitura (BAT 28 FDM)', 'ARIA', 'APERTA', NULL, 'BAT 28 BATc FDM 2019', TRUE),
  (203, 2, 'IDR-01', 'Rispettare limiti scarichi idrici civili – recapito in fognatura comunale', 'ACQUA', 'APERTA', NULL, 'D.Lgs. 152/2006 All.5', TRUE),
  (204, 2, 'RIF-01', 'Gestione rifiuti nel rispetto D.Lgs. 152/2006 Parte IV', 'RIFIUTI', 'APERTA', NULL, 'D.Lgs. 152/2006 Parte IV', TRUE),
  (205, 2, 'PMC-01', 'Entro 30 aprile di ogni anno: trasmettere report PMC a Provincia di Verona, ARPAV e Comune di Albaredo', 'GENERALE', 'APERTA', NULL, 'Det. n.2635/2023 par. gestione PMC', TRUE),
  (206, 2, 'PMC-02', 'Piano di monitoraggio come da Allegato E11 (PMC Rev.04 del 20/04/2023)', 'GENERALE', 'APERTA', NULL, 'Allegato E11 Det. n.2635/2023', TRUE),
  (207, 2, 'GES-01', 'Sistema di gestione ambientale certificato ISO 14001 – mantenere certificazione', 'GENERALE', 'APERTA', NULL, 'UNI EN ISO 14001', TRUE);

COMMIT;

-- ============================================================
-- NOTE
-- 1. Dati reali estratti dai documenti AIA ufficiali.
-- 2. Prescrizioni GMI con data_scadenza nel passato (2024): stato
--    CHIUSA/APERTA da aggiornare in base agli adempimenti effettuati.
-- 3. Limiti piezometri: D.Lgs. 152/2006 All.1 Parte IV Tab.2
--    (acque sotterranee, sito industriale a uso commerciale/industriale).
-- 4. Limiti scarichi S1/S2: Tab.3 All.5 Parte III D.Lgs. 152/2006
--    (scarico in acque superficiali).
-- 5. Camini E98-E114: attivo=FALSE, passano a TRUE alla messa in esercizio.
-- ============================================================
