-- ============================================================
-- AIA Management System – SEED DATA (H2 dev)
-- stabilimento_1: GMI S.p.A. – Livorno  (AIA Regione Toscana – numero da acquisire)
-- stabilimento_2: Promolog S.r.l. – Coriano Veronese  (AIA vigente: Decreto n.19082/2024, Regione Veneto)
--
-- Eseguito da Spring Boot DOPO la creazione dello schema Hibernate
-- (spring.jpa.defer-datasource-initialization=true).
-- DataInitializer.java è idempotente: salta stabilimenti/prescrizioni/
-- monitoraggi se già presenti; crea solo utenti e workflow definitions.
-- ============================================================

SET REFERENTIAL_INTEGRITY FALSE;

-- ═══════════════════════════════════════════════════════════════
-- STABILIMENTI
-- ═══════════════════════════════════════════════════════════════
-- stabilimento_1 = GMI Livorno (Regione Toscana – AIA non ancora acquisita fra i documenti)
-- stabilimento_2 = Promolog Srl Coriano Veronese (AIA vigente: Decreto n.19082/2024, Regione Veneto)
--   Il Decreto 19082/2024 (Regione Veneto) ha sostituito la Det. 2635/2023 (Provincia VR).
--   Il numero AIA di Livorno non è disponibile fra i documenti caricati; da aggiornare.
INSERT INTO stabilimenti (id, nome, citta, indirizzo, numero_aia, ente_competente, data_rilascio_aia, data_scadenza_aia, attivo)
VALUES
  (1, 'Grandi Molini Italiani S.p.A. – Livorno',
      'Livorno', 'Via L. Da Vinci, 19',
      NULL, 'Regione Toscana – Settore AIA',
      NULL, NULL, TRUE),
  (2, 'Promolog S.r.l. – Albaredo d''Adige',
      'Albaredo d''Adige', 'Via Zurlare, 21 – Coriano Veronese',
      'Decreto n.19082/2024', 'Regione Veneto – Direzione Ambiente',
      '2024-08-20', '2034-08-20', TRUE);

-- ═══════════════════════════════════════════════════════════════
-- PRESCRIZIONI – GMI LIVORNO
-- colonna "codice" (era "numero"), "matrice_ambientale" (era "tipo_matrice")
-- ENERGIA/GENERALE→ARIA (non nel enum MatriceAmbientale)
-- la tabella "prescrizioni" non ha colonna "attivo"
-- ═══════════════════════════════════════════════════════════════
INSERT INTO prescrizioni (id, stabilimento_id, codice, descrizione, matrice_ambientale, stato, data_scadenza, riferimento_normativo)
VALUES
  (101,1,'ATM-01','Rispettare il quadro emissivo della Tabella A (Allegato A) con frequenza di monitoraggio della Tabella 2 (Allegato B)','ARIA','APERTA',NULL,'D.Lgs. 152/2006 art.269'),
  (102,1,'ATM-02','Emissioni da macinatura e pulitura cereali: monitoraggio annuale polveri con VLE <= 5 mg/Nm3 (BAT-AEL BAT 28 FDM)','ARIA','APERTA',NULL,'BAT 5 / BAT 28 BATc FDM 2019'),
  (103,1,'ATM-03','Emissioni da trasporto/movimentazione: monitoraggio annuale polveri VLE <= 5 mg/Nm3 (24h/giorno, 365 gg/anno)','ARIA','APERTA',NULL,'D.Lgs. 152/2006'),
  (104,1,'ATM-04','Emissioni E66, E91-E96 (movimentazione): controllo mensile differenziale pressorio (deprimometri). VLE <= 3 mg/Nm3','ARIA','APERTA',NULL,'Prescrizione par. 5.2.4'),
  (105,1,'ATM-05','E97 (saldatura): unico controllo analitico entro 31/12/2024. Parametri: polveri 5 mg/Nm3, Cr/Ni/Cd/Co/Pb 0,1 mg/Nm3, Sn 2 mg/Nm3','ARIA','APERTA','2024-12-31','Piano Regionale Qualita Aria All.2'),
  (106,1,'ATM-06','Emissioni con portata >15.000 Nm3/h autorizzate 24h/365gg: installare sistemi allarme differenziale pressorio entro 30/03/2025','ARIA','APERTA','2025-03-30','Prescrizione par. 5.2.6'),
  (107,1,'ATM-07','Manutenzione almeno annuale di tutti i sistemi di abbattimento (filtri a maniche): verifica integrita tessuti filtranti','ARIA','APERTA',NULL,'Tabella 3 Allegato B'),
  (108,1,'ATM-08','Sistemi abbattimento senza allarme pressione: controllo semestrale con misuratore mobile. E66, E91-E96: mensile','ARIA','APERTA',NULL,'Prescrizione par. 5.2.7'),
  (109,1,'ATM-09','E91, E92: comunicare entro 31/10/2024 il range di buon funzionamento del sistema di abbattimento','ARIA','APERTA','2024-10-31','Prescrizione par. 5.2.8'),
  (110,1,'ATM-10','Comunicare data messa in esercizio con preavviso >=10 gg per impianti futura attivazione (incl. E50, E51 sostituzione filtri entro fine 2024)','ARIA','APERTA','2024-12-31','Prescrizione par. 5.2.9'),
  (111,1,'ATM-11','Entro 31/12/2024: relazione valutazione costi-benefici riunificazione camini (art. 270 c.4 D.Lgs. 152/2006)','ARIA','APERTA','2024-12-31','D.Lgs. 152/2006 art.270 c.4'),
  (112,1,'ATM-12','Risultati analisi camini (PMC Allegato B): conservare certificati analitici ordinati cronologicamente presso azienda','ARIA','APERTA',NULL,'App. 1 All. VI Parte V D.Lgs. 152/2006'),
  (113,1,'ATM-13','Modifiche previste entro 2025 (E99-E102, E109-E111): comunicare messa in esercizio con preavviso >=15 gg a RT-AIA, ARPAT, Comune','ARIA','APERTA','2025-12-31','Prescrizione par. 5.2.12'),
  (114,1,'ATM-14','Messa a regime e controlli analitici art. 269 c.3: entro 2024 per E50/E51/E98/E112/E113/E114; entro 2025 per E99-E102/E109-E111','ARIA','APERTA','2025-12-31','D.Lgs. 152/2006 art.269 c.3'),
  (115,1,'ATM-15','Entro 30/06/2026: aggiornare studio diffusionale a seguito attivazione nuove sorgenti emissive previste entro 2025','ARIA','APERTA','2026-06-30','Prescrizione par. 5.2.16'),
  (116,1,'IDR-01','Rispettare limiti allo scarico S1 e S2 in acque superficiali (Tab. 3, All. 5, Parte III D.Lgs. 152/2006)','ACQUA','APERTA',NULL,'D.Lgs. 152/2006 All.5 Tab.3'),
  (117,1,'IDR-02','Monitorare parametri inquinanti caratteristici scarichi idrici (Tabella 4 Allegato B) con frequenza indicata','ACQUA','APERTA',NULL,'Tabella 4 Allegato B'),
  (118,1,'IDR-03','Entro 15/09/2024: trasmettere planimetria aggiornata rete idrica con indicazione pozzetti ispezione uscita impianti depurazione','ACQUA','CHIUSA','2024-09-15','Prescrizione par. 5.3.3'),
  (119,1,'RIF-01','Rifiuti classificati e gestiti nel rispetto della Parte IV D.Lgs. 152/2006','RIFIUTI','APERTA',NULL,'D.Lgs. 152/2006 Parte IV'),
  (120,1,'RIF-02','Rifiuti gestiti con criteri deposito temporaneo (art. 185-bis D.Lgs. 152/2006)','RIFIUTI','APERTA',NULL,'D.Lgs. 152/2006 art.185-bis'),
  (121,1,'RIF-03','Relazione annuale: riportare quantita totale rifiuti prodotti (pericolosi/non pericolosi), destinazione recupero/smaltimento','RIFIUTI','APERTA',NULL,'Prescrizione par. 5.4.6'),
  (122,1,'RIF-04','Entro 15/09/2024: trasmettere planimetria aggiornata depositi rifiuti','RIFIUTI','CHIUSA','2024-09-15','Prescrizione par. 5.4.7'),
  (123,1,'ENE-01','PMC: riportare consumo energia elettrica (MWh) e produzione fotovoltaico','ARIA','APERTA',NULL,'Prescrizione par. 5.5.1'),
  (124,1,'ENE-02','PMC: riportare bilancio energetico complessivo e confronto con valori BREF','ARIA','APERTA',NULL,'Prescrizione par. 5.5.2'),
  (125,1,'ENE-03','Consumo specifico energia atteso < 0,13 MWh/t come media annua (BAT Tab. 14 FDM)','ARIA','APERTA',NULL,'BAT 5 Tab. 14 BATc FDM 2019'),
  (126,1,'ACU-01','Rispettare valutazione impatto acustico presentata. Valutazione periodica ogni 3 anni','RUMORE','APERTA',NULL,'DGR 788/99, DM 16/3/98, DPCM 14/11/97'),
  (127,1,'PMC-01','Entro 30 aprile di ogni anno: trasmettere PEC a RT-AIA, Comune Livorno, ARPAT Livorno, USL Toscana Nord-Ovest sintesi risultati PMC anno precedente','ARIA','APERTA',NULL,'Allegato B par. 1.1.2'),
  (128,1,'PMC-02','Tutte le registrazioni PMC conservate presso sede impianto per intera durata autorizzazione','ARIA','APERTA',NULL,'Allegato B par. 1.1.1');

-- PRESCRIZIONI – PROMOLOG
INSERT INTO prescrizioni (id, stabilimento_id, codice, descrizione, matrice_ambientale, stato, data_scadenza, riferimento_normativo)
VALUES
  (201,2,'ATM-01','Rispettare i limiti emissivi per le emissioni in atmosfera del PMC approvato','ARIA','APERTA',NULL,'D.Lgs. 152/2006 Parte V'),
  (202,2,'ATM-02','Polveri totali: VLE 5 mg/Nm3 per le emissioni da macinazione e pulitura (BAT 28 FDM)','ARIA','APERTA',NULL,'BAT 28 BATc FDM 2019'),
  (203,2,'IDR-01','Rispettare limiti scarichi idrici civili - recapito in fognatura comunale','ACQUA','APERTA',NULL,'D.Lgs. 152/2006 All.5'),
  (204,2,'RIF-01','Gestione rifiuti nel rispetto D.Lgs. 152/2006 Parte IV','RIFIUTI','APERTA',NULL,'D.Lgs. 152/2006 Parte IV'),
  (205,2,'PMC-01','Entro 30 aprile di ogni anno: trasmettere report PMC a Provincia di Verona, ARPAV e Comune di Albaredo','ARIA','APERTA',NULL,'Det. n.2635/2023 par. gestione PMC'),
  (206,2,'PMC-02','Piano di monitoraggio come da Allegato E11 (PMC Rev.04 del 20/04/2023)','ARIA','APERTA',NULL,'Allegato E11 Det. n.2635/2023'),
  (207,2,'GES-01','Sistema di gestione ambientale certificato ISO 14001 - mantenere certificazione','ARIA','APERTA',NULL,'UNI EN ISO 14001');

-- ============================================================
-- PUNTI DI MONITORAGGIO (entity: monitoraggi)
-- tipo_monitoraggio: EMISSIONI_ATMOSFERA / SCARICHI_IDRICI / PIEZOMETRO
-- frequenza: ANNUALE / MENSILE  (MENSILE_DELTA_P → MENSILE, UNA_TANTUM → ANNUALE)
-- ============================================================
INSERT INTO monitoraggi (id, stabilimento_id, codice, descrizione, tipo_monitoraggio, punto_emissione, frequenza, laboratorio, attivo)
VALUES
-- CAMINI TRASPORTO (E1-E8, E52)
  (1001, 1, 'E1',  'Scarico nave – trasportatore torre',           'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1002, 1, 'E2',  'Scarico nave – nastro in banchina',            'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1003, 1, 'E3',  'Scarico nave – trasportatore',                 'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1007, 1, 'E7',  'Trasferimento grano – trasportatore',          'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1008, 1, 'E8',  'Trasferimento grano – trasportatore',          'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1052, 1, 'E52', 'Silos sottoprodotti',                          'EMISSIONI_ATMOSFERA', 'TRASPORTO',     'ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI PREPULITURA (E4-E6, E9-E13, E28-E29)
  (1004, 1, 'E4',  'Prepulitura lato mare – aspirazione generale', 'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1005, 1, 'E5',  'Prepulitura lato mare – vibroblok',            'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1006, 1, 'E6',  'Prepulitura lato mare – vibroblok',            'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1009, 1, 'E9',  'Prepulitura lato terra T – bilancia',          'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1010, 1, 'E10', 'Prepulitura lato terra T – vibroblok',         'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1011, 1, 'E11', 'Prepulitura lato terra D – bilancia',          'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1012, 1, 'E12', 'Prepulitura lato terra D – vibroblok',         'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1013, 1, 'E13', 'Prepulitura lato terra D – aspirazione generale','EMISSIONI_ATMOSFERA','PREPULITURA',  'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1028, 1, 'E28', 'Prepulitura – pompa celle',                    'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1029, 1, 'E29', 'Prepulitura – pompa macinatrice',              'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI MOVIMENTAZIONE (E14-E27)
  (1014, 1, 'E14', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1015, 1, 'E15', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1016, 1, 'E16', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1017, 1, 'E17', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1018, 1, 'E18', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1019, 1, 'E19', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1020, 1, 'E20', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1021, 1, 'E21', 'Scarico camion – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1022, 1, 'E22', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1023, 1, 'E23', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1024, 1, 'E24', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1025, 1, 'E25', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1026, 1, 'E26', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1027, 1, 'E27', 'Scarico vagoni – cappe aspirazione',           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI PULITURA (E30-E38)
  (1030, 1, 'E30', '1a pulitura grano tenero',                     'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1031, 1, 'E31', '1a / 2a pulitura grano tenero',                'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1032, 1, 'E32', '1a pulitura grano duro',                       'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1033, 1, 'E33', '1a pulitura grano duro',                       'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1034, 1, 'E34', '1a pulitura grano duro',                       'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1035, 1, 'E35', '1a pulitura grano duro',                       'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1036, 1, 'E36', '2a pulitura grano duro',                       'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1037, 1, 'E37', 'Pulitura – macinazione scarti',                'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1038, 1, 'E38', 'Pulitura – macinazione scarti',                'EMISSIONI_ATMOSFERA', 'PULITURA',      'ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI MACINAZIONE (E39-E51)
  (1039, 1, 'E39', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1040, 1, 'E40', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1041, 1, 'E41', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1042, 1, 'E42', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1043, 1, 'E43', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1044, 1, 'E44', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1045, 1, 'E45', 'Molino a duro',                                'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1046, 1, 'E46', 'Molino a tenero',                              'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1047, 1, 'E47', 'Molino a tenero',                              'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1048, 1, 'E48', 'Molino a tenero',                              'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1049, 1, 'E49', 'Molino a tenero',                              'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1050, 1, 'E50', 'Cubettatrici coprodotti (sostituzione filtro maniche entro ott. 2024)', 'EMISSIONI_ATMOSFERA', 'MACINAZIONE', 'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1051, 1, 'E51', 'Cubettatrici coprodotti (sostituzione filtro maniche entro ott. 2024)', 'EMISSIONI_ATMOSFERA', 'MACINAZIONE', 'ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI MOVIMENTAZIONE SILOS (E53-E90)
  (1053, 1, 'E53', 'Asp. Silos Semola',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1054, 1, 'E54', 'Asp. Silos Semola',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1055, 1, 'E55', 'Asp. Silos Semola',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1056, 1, 'E56', 'Asp. Staccio Semola',                          'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1057, 1, 'E57', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1058, 1, 'E58', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1059, 1, 'E59', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1060, 1, 'E60', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1061, 1, 'E61', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1062, 1, 'E62', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1063, 1, 'E63', 'Asp. Silos Rinfusa',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1064, 1, 'E64', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1065, 1, 'E65', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1066, 1, 'E66', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1067, 1, 'E67', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1068, 1, 'E68', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1069, 1, 'E69', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1070, 1, 'E70', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1071, 1, 'E71', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1072, 1, 'E72', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1073, 1, 'E73', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1074, 1, 'E74', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1075, 1, 'E75', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1076, 1, 'E76', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1077, 1, 'E77', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1078, 1, 'E78', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1079, 1, 'E79', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1080, 1, 'E80', 'Asp. Silos Miscelazione',                      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1081, 1, 'E81', 'Asp. Silos Minicel',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1082, 1, 'E82', 'Asp. Silos Minicel',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1083, 1, 'E83', 'Asp. Silos Insacco',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1084, 1, 'E84', 'Asp. Silos Insacco',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1085, 1, 'E85', 'Asp. Linea Insacco',                           'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1086, 1, 'E86', 'Asp. Miscelazione',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1087, 1, 'E87', 'Asp. Miscelazione',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1088, 1, 'E88', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1089, 1, 'E89', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1090, 1, 'E90', 'Asp. Silos Farina',                            'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', 'Laboratorio accreditato', TRUE),
  (1091, 1, 'E91', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1092, 1, 'E92', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1093, 1, 'E93', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1094, 1, 'E94', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1095, 1, 'E95', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1096, 1, 'E96', 'Asp. Silos Farina (VLE 3 mg/Nm3 – ctrl mensile deprimometri)', 'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE', 'MENSILE', 'Laboratorio accreditato', TRUE),
  (1097, 1, 'E97', 'Asp. Saldatura (unico controllo analitico entro 31/12/2024)',   'EMISSIONI_ATMOSFERA', 'MANUTENZIONE',  'ANNUALE', 'Laboratorio accreditato', TRUE),
-- CAMINI FUTURI (attivo=FALSE)
  (1098, 1, 'E98',  'Movimentazione – in attivazione entro 2024',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1099, 1, 'E99',  'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1100, 1, 'E100', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1101, 1, 'E101', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1102, 1, 'E102', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1103, 1, 'E109', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1104, 1, 'E110', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1105, 1, 'E111', 'Movimentazione – in attivazione entro 2025',  'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1106, 1, 'E112', 'Insacco germe – in messa in esercizio',        'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1107, 1, 'E113', 'Ripasso insacco – in messa in esercizio',      'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE', NULL, FALSE),
  (1108, 1, 'E114', 'Ampliamento officina – in attivazione entro 2024','EMISSIONI_ATMOSFERA','MANUTENZIONE', 'ANNUALE', NULL, FALSE),
-- SCARICHI IDRICI
  (1201, 1, 'S1', 'Scarico acque reflue domestiche – impianto depurazione palazzina uffici (40 A.E.)', 'SCARICHI_IDRICI', 'SCARICHI', 'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1202, 1, 'S2', 'Scarico acque reflue domestiche – impianto depurazione zona insacco (20 A.E.)',     'SCARICHI_IDRICI', 'SCARICHI', 'ANNUALE', 'Laboratorio accreditato', TRUE),
-- PIEZOMETRI
  (1301, 1, 'PZ1', 'Piezometro 1 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1302, 1, 'PZ2', 'Piezometro 2 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1303, 1, 'PZ3', 'Piezometro 3 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', 'Laboratorio accreditato', TRUE),
  (1304, 1, 'PZ4', 'Piezometro 4 – monitoraggio acque sotterranee', 'PIEZOMETRO', 'ACQUE_SOTTERRANEE', 'ANNUALE', 'Laboratorio accreditato', TRUE);

-- ============================================================
-- PARAMETRI DI MONITORAGGIO (entity: parametri_monitoraggio)
-- FK: monitoraggio_id (era punto_monitoraggio_id)
-- campo: nome (era nome_parametro)
-- ============================================================

-- Polveri totali VLE 5 mg/Nm3 per camini standard (tutti tranne E66,E91-E96,E97)
INSERT INTO parametri_monitoraggio (id, monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT
  1000 + m.id,
  m.id,
  'Polveri totali',
  'mg/Nm3',
  5.0,
  'D.Lgs. 152/2006 – BAT-AEL BAT 28 BATc FDM 2019',
  'UNI EN 13284',
  TRUE
FROM monitoraggi m
WHERE m.stabilimento_id = 1
  AND m.tipo_monitoraggio = 'EMISSIONI_ATMOSFERA'
  AND m.codice NOT IN ('E66','E91','E92','E93','E94','E95','E96','E97');

-- Polveri totali VLE 3 mg/Nm3 per E66, E91-E96 (ctrl mensile deprimometri)
INSERT INTO parametri_monitoraggio (id, monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT
  2000 + m.id,
  m.id,
  'Polveri totali',
  'mg/Nm3',
  3.0,
  'VLE con deroga autocontrollo annuale – ctrl mensile deprimometri',
  'UNI EN 13284',
  TRUE
FROM monitoraggi m
WHERE m.stabilimento_id = 1
  AND m.codice IN ('E66','E91','E92','E93','E94','E95','E96');

-- Parametri multipli E97 (saldatura)
INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1097, 'Polveri totali', 'mg/Nm3',  5.0, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Cromo (Cr)',     'mg/Nm3',  0.1, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Nichel (Ni)',    'mg/Nm3',  0.1, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Cadmio (Cd)',    'mg/Nm3',  0.1, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Cobalto (Co)',   'mg/Nm3',  0.1, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Piombo (Pb)',    'mg/Nm3',  0.1, 'Piano Regionale Qualita Aria All.2', NULL, TRUE),
  (1097, 'Stagno (Sn)',    'mg/Nm3',  2.0, 'Piano Regionale Qualita Aria All.2', NULL, TRUE);

-- Parametri piezometri PZ1 (acque sotterranee)
INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1301, 'pH',                NULL,    NULL, 'D.Lgs. 152/2006 All.3 Tab.2', 'APAT CNR IRSA 2030', TRUE),
  (1301, 'Conduttivita',      'uS/cm', NULL, 'D.Lgs. 152/2006 All.3 Tab.2', 'APAT CNR IRSA 2030', TRUE),
  (1301, 'Alluminio',         'ug/L',  200,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Arsenico',          'ug/L',  10,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cadmio',            'ug/L',  5,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cromo totale',      'ug/L',  50,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Cromo VI',          'ug/L',  5,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Ferro',             'ug/L',  200,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Mercurio',          'ug/L',  1,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Nichel',            'ug/L',  20,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Piombo',            'ug/L',  10,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Rame',              'ug/L',  1000, 'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Manganese',         'ug/L',  50,   'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Zinco',             'ug/L',  3000, 'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 6020A',          TRUE),
  (1301, 'Benzene',           'ug/L',  1,    'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'Tricloroetilene',   'ug/L',  1.5,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'Tetracloroetilene', 'ug/L',  1.1,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 524.2',          TRUE),
  (1301, 'IPA totali',        'ug/L',  0.1,  'D.Lgs. 152/2006 All.3 Tab.2', 'EPA 8270D',          TRUE);

-- Replica parametri piezometrici per PZ2, PZ3, PZ4
INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1302, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametri_monitoraggio WHERE monitoraggio_id = 1301;

INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1303, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametri_monitoraggio WHERE monitoraggio_id = 1301;

INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1304, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametri_monitoraggio WHERE monitoraggio_id = 1301;

-- Parametri scarichi idrici S1
INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
VALUES
  (1201, 'pH',                 NULL,    NULL, 'D.Lgs. 152/2006 All.5 Tab.3 – Scarico in acque superficiali', 'APAT CNR IRSA', TRUE),
  (1201, 'COD',                'mg/L',  160,  'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'BOD5',               'mg/L',  40,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Solidi Sospesi',     'mg/L',  80,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Azoto ammoniacale',  'mg/L',  15,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Azoto totale',       'mg/L',  15,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Fosforo totale',     'mg/L',  10,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Tensioattivi totali','mg/L',  2,    'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE),
  (1201, 'Oli e grassi',       'mg/L',  20,   'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE);

-- Replica parametri scarico per S2
INSERT INTO parametri_monitoraggio (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT 1202, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo
  FROM parametri_monitoraggio WHERE monitoraggio_id = 1201;

-- ============================================================
-- ANAGRAFICA CAMINI – GMI Livorno (stabilimento_id = 1)
-- Fonte: Decreto RT n. 19082 del 20/08/2024 – Allegato A
-- ============================================================
INSERT INTO anagrafica_camini
  (stabilimento_id, sigla, fase_processo, origine,
   portata_nm3h, sezione_m2, velocita_ms,
   temperatura_c, temperatura_ambiente,
   altezza_m, durata_h_giorno, durata_g_anno,
   impianto_abbattimento, note, attivo)
VALUES
-- TRASPORTO
(1,'E1',  'TRASPORTO',    'trasportatore torre',           4800,  0.125, 10.67, NULL, TRUE,  13,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E2',  'TRASPORTO',    'nastro in banchina',            4800,  0.125, 10.67, NULL, TRUE,  10,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E3',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  12,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E7',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  40,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E8',  'TRASPORTO',    'trasportatore',                 4200,  0.08,  14.58, NULL, TRUE,  40,   12, 365, 'filtro a maniche', NULL, TRUE),
(1,'E52', 'TRASPORTO',    'silos sottoprodotti',           7200,  0.20,  10.1,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', NULL, TRUE),
-- PREPULITURA
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
-- PULITURA
(1,'E30', 'PULITURA',     '1a pulitura grano tenero',    19200,  0.96,   5.6,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E31', 'PULITURA',     '1a-2a pulitura grano tenero', 10800,  0.75,   4.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E32', 'PULITURA',     '1a pulitura grano duro',      19200,  0.96,   5.6,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E33', 'PULITURA',     '1a pulitura grano duro',       9480,  0.57,   4.7,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E34', 'PULITURA',     '1a pulitura grano duro',       9480,  0.57,   4.7,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E35', 'PULITURA',     '1a pulitura grano duro',      10800,  0.75,   4.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E36', 'PULITURA',     '2a pulitura grano duro',       8400,  0.75,   3.1,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E37', 'PULITURA',     'macinazione scarti',          10200,  0.57,   5.0,  NULL, TRUE,  38,   24, 365, 'filtro a maniche', NULL, TRUE),
(1,'E38', 'PULITURA',     'macinazione scarti',           3600,  0.06,  16.7,  NULL, TRUE,   5,   24, 365, 'filtro a maniche', NULL, TRUE),
-- MACINAZIONE
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
-- MOVIMENTAZIONE
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
(1,'E66', 'MOVIMENTAZIONE','aspirazione silos farina',    9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
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
(1,'E91', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E92', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E93', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E94', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E95', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E96', 'MOVIMENTAZIONE','aspirazione silos farina',     9731,  0.283,  10.6,  NULL, TRUE,  35.5, 24, 365, 'filtro a maniche', 'VLE 3 mg/Nm3 – controllo mensile deprimometri (deroga autocontrollo annuale)', TRUE),
(1,'E112','MOVIMENTAZIONE','(in corso di messa in esercizio)', NULL, NULL, NULL, NULL, TRUE, NULL, 24, 365, 'filtro a maniche', 'Messa in esercizio 18/06/2024 (E112)', FALSE),
(1,'E113','MOVIMENTAZIONE','(in corso di messa in esercizio)', NULL, NULL, NULL, NULL, TRUE, NULL, 24, 365, 'filtro a maniche', 'Messa in esercizio 18/06/2024 (E113)', FALSE),
-- Previsti entro 2024/2025
(1,'E98', 'MOVIMENTAZIONE','(realizzazione prevista entro 2024)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro dicembre 2024', FALSE),
(1,'E99', 'MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E100','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E101','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E102','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E109','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E110','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E111','MOVIMENTAZIONE','(realizzazione prevista entro 2025)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro 2025', FALSE),
(1,'E114','MOVIMENTAZIONE','(realizzazione prevista entro 2024)', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, 'filtro a maniche', 'Attivazione prevista entro dicembre 2024', FALSE),
-- MANUTENZIONE
(1,'E97', 'MANUTENZIONE', 'aspirazione operazioni di saldatura ad elettrodo',
   6400, 0.071, 25.2, NULL, TRUE, 3, 2, 220, NULL,
   'Emissione sporadica non asservita al ciclo produttivo. VLE: polveri 5 mg/Nm3, Cr/Ni/Cd/Co/Pb 0.1 mg/Nm3, Sn 2 mg/Nm3. Unico controllo analitico entro 31/12/2024', TRUE),
-- CONTROLLO QUALITA (in deroga – Parte V D.lgs. 152/2006)
(1,'E115','CONTROLLO_QUALITA','cappe aspirazione laboratorio CQ', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Test fisico-reologici. Dichiarato non soggetto ad autorizzazione: All. IV Part. V D.lgs. 152/2006 lettera jj)', TRUE),
(1,'E116','CONTROLLO_QUALITA','cappe aspirazione laboratorio CQ', NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Test fisico-reologici. Dichiarato non soggetto ad autorizzazione: All. IV Part. V D.lgs. 152/2006 lettera jj)', TRUE),
(1,'E117','CONTROLLO_QUALITA','forni cottura sperimentale pane',  NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Prove di panificazione. Dichiarato non soggetto ad autorizzazione: All. IV lettera f) e jj)', TRUE),
(1,'E118','CONTROLLO_QUALITA','forni cottura sperimentale pane',  NULL, NULL, NULL, NULL, TRUE, NULL, NULL, NULL, NULL,
   'Prove di panificazione. Dichiarato non soggetto ad autorizzazione: All. IV lettera f) e jj)', TRUE);

-- ============================================================
-- FINE SEED DATA
-- ============================================================
SET REFERENTIAL_INTEGRITY TRUE;

-- ════════════════════════════════════════════════════════════════════
-- SEZIONE 4: DOCUMENTI AIA (cartella 'documenti aia' Promolog/GMI)
-- ════════════════════════════════════════════════════════════════════
-- ═══════════════════════════════════════════════════════════════
-- DOCUMENTI AIA – Seed da cartella 'documenti aia' Promolog/GMI
-- 52 file: 47 CO → stabilimento_2 (Promolog Coriano)
--            5 LI/GROUP → stabilimento_1 (GMI Livorno + documenti di gruppo)
-- ═══════════════════════════════════════════════════════════════
INSERT INTO documenti (stabilimento_id, nome, nome_file, tipo_documento, descrizione,
  file_path, file_size, mime_type, versione, anno, stato_documento, ente_emittente,
  oggetto, numero_protocollo, data_ricezione, tags, is_versione_corrente,
  created_at, created_by, updated_at)
VALUES
  (2, 'RdP Chelab 24/000213869 – Acque meteoriche SF2 (26/03/2024)', '24.233533.0001_24_000213869.pdf', 'RAPPORTO_PROVA', 'RdP accreditato ACCREDIA. Campione SF2 del 26/03/2024. N.accettazione: 24.233533.0001.',
   'stabilimento_2/documenti-aia/24.233533.0001_24_000213869.pdf', 224921, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Chelab S.r.l. (LAB N° 0051 L)',
   'Rapporto di Prova n.24/000213869 – Acque meteoriche SF2', '24/000213869', '2024-04-15', 'chelab,rapporto-prova,acque-meteoriche,sf2,2024-03-26,lab-0051-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Chelab 24/000655853 – Acque meteoriche SF2 (16/10/2024)', '24.291087.0001_24_000655853 ACQUE METEORICHE PUNTO SF2.pdf', 'RAPPORTO_PROVA', 'RdP accreditato ACCREDIA. Campione SF2 del 16/10/2024. N.accettazione: 24.291087.0001.',
   'stabilimento_2/documenti-aia/24.291087.0001_24_000655853 ACQUE METEORICHE PUNTO SF2.pdf', 225910, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Chelab S.r.l. (LAB N° 0051 L)',
   'Rapporto di Prova n.24/000655853 – Acque meteoriche SF2', '24/000655853', '2024-11-04', 'chelab,rapporto-prova,acque-meteoriche,sf2,2024-10-16,lab-0051-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'PEC trasmissione sintesi PMC e relazione conformità 2024 (30/06/2025)', '30_06_2025_Grandi Molini Italiani -trasmissione sintesi risultati PMC e relazione di conformità all''esercizio dell''impianto.pdf', 'COMUNICAZIONE_PEC', 'PEC trasmissione GMI con Allegati 1 (relazione conformità) e 2 (sintesi PMC) anno 2024.',
   'stabilimento_2/documenti-aia/30_06_2025_Grandi Molini Italiani -trasmissione sintesi risultati PMC e relazione di conformità all''esercizio dell''impianto.pdf', 722792, 'application/pdf', 1, 2025, 'ARCHIVIATO', 'Grandi Molini Italiani',
   'Trasmissione sintesi PMC e relazione di conformità anno 2024 – giugno 2025', NULL, '2025-06-30', 'pec,trasmissione,pmc,sintesi,relazione-conformita,2025-06-30,gmi', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 1 – Relazione PMC anno 2024 (aprile 2025)', 'ALLEGATO 1 - Relazione del Piano di Monitoraggio e Controllo_anno 2024.pdf', 'RELAZIONE_ANNUALE', 'Relazione descrittiva del PMC 2024. Analisi dei risultati del monitoraggio annuale.',
   'stabilimento_2/documenti-aia/ALLEGATO 1 - Relazione del Piano di Monitoraggio e Controllo_anno 2024.pdf', 753693, 'application/pdf', 1, 2024, 'APPROVATO', 'Promolog Srl',
   'Relazione del Piano di Monitoraggio e Controllo – anno 2024', NULL, '2025-04-30', 'pmc,relazione,2024,allegato-1', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 1 – Relazione di conformità esercizio impianto 2024 (giugno 2025)', 'ALLEGATO 1 - relazione di conformità dell''esercizio dell''impianto - rif. anno 2024.pdf', 'RELAZIONE_ANNUALE', 'Versione aggiornata relazione di conformità, trasmessa con la sintesi PMC del 30/06/2025.',
   'stabilimento_2/documenti-aia/ALLEGATO 1 - relazione di conformità dell''esercizio dell''impianto - rif. anno 2024.pdf', 1147062, 'application/pdf', 1, 2024, 'APPROVATO', 'Promolog Srl',
   'Relazione di conformità esercizio impianto – anno 2024 (revisione giugno 2025)', NULL, '2025-06-30', 'pmc,relazione-conformita,2024,allegato-1,rev-giugno-2025', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 2 – Sintesi risultati PMC anno 2024', 'ALLEGATO 2 - Sintesi dei risultati del Piano di Monitoraggio e Controllo - rif. anno 2024.xlsx', 'PMC_ANNUALE', 'Tabella riassuntiva di tutti i risultati analitici PMC 2024 per la trasmissione agli enti.',
   'stabilimento_2/documenti-aia/ALLEGATO 2 - Sintesi dei risultati del Piano di Monitoraggio e Controllo - rif. anno 2024.xlsx', 89961, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'APPROVATO', 'Promolog Srl',
   'Sintesi dei risultati del Piano di Monitoraggio e Controllo – anno 2024', NULL, '2025-06-30', 'pmc,sintesi,2024,allegato-2,xlsx', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 3 – PMC 2024 (Piano di Monitoraggio e Controllo)', 'ALLEGATO 3 - Piano di Monitoraggio e Controllo_anno 2024.xlsx', 'PMC_ANNUALE', 'PMC annuale obbligatorio (AIA). 12 fogli: emissioni, scarichi, acque meteoriche, rumore, rifiuti, energia, produzione, indicatori di performance.',
   'stabilimento_2/documenti-aia/ALLEGATO 3 - Piano di Monitoraggio e Controllo_anno 2024.xlsx', 140498, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'APPROVATO', 'Promolog Srl',
   'Piano di Monitoraggio e Controllo anno 2024 – Allegato 3', NULL, '2025-04-30', 'pmc,2024,allegato-3,autocontrollo,xlsx', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 3 – PMC 2024 (archivio ZIP)', 'ALLEGATO 3 - Piano di Monitoraggio e Controllo_anno 2024.zip', 'PMC_ANNUALE', 'Archivio ZIP contenente il PMC 2024 con tutti i fogli e allegati.',
   'stabilimento_2/documenti-aia/ALLEGATO 3 - Piano di Monitoraggio e Controllo_anno 2024.zip', 117264, 'application/zip', 1, 2024, 'APPROVATO', 'Promolog Srl',
   'Piano di Monitoraggio e Controllo anno 2024 – pacchetto ZIP', NULL, '2025-04-30', 'pmc,2024,allegato-3,zip', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 3C – Manutenzione impianti abbattimento 2024', 'ALLEGATO 3C - SCARICHI IDRICI_Manutenzione impianti di abbattimento.pdf', 'STUDIO_TECNICO', 'Registro manutenzioni ordinarie e straordinarie degli impianti di abbattimento/depurazione acque.',
   'stabilimento_2/documenti-aia/ALLEGATO 3C - SCARICHI IDRICI_Manutenzione impianti di abbattimento.pdf', 980073, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Promolog Srl',
   'Allegato 3C – Registro manutenzioni impianti abbattimento scarichi idrici', NULL, '2025-06-20', 'scarichi-idrici,manutenzione,allegato-3c,impianti-abbattimento', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 3D – Formulari smaltimento fanghi vasche trattamento 2024', 'ALLEGATO 3D - formulari inerenti lo smaltimento dei fanghi originati dallo svuotamento e dalla pulizia delle vasche di trattamento.pdf', 'FORMULARIO_RIFIUTI', 'Formulari di identificazione rifiuti per i fanghi generati dalla pulizia delle vasche di trattamento acque.',
   'stabilimento_2/documenti-aia/ALLEGATO 3D - formulari inerenti lo smaltimento dei fanghi originati dallo svuotamento e dalla pulizia delle vasche di trattamento.pdf', 455191, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Promolog Srl',
   'Allegato 3D – FIR e formulari smaltimento fanghi pulizia vasche', NULL, '2025-06-27', 'scarichi-idrici,fanghi,rifiuti,allegato-3d,formulari', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 3E – Comunicazione riscontro criticità impianti depurazione civili', 'ALLEGATO 3E - SCARICHI IDRICI_Comunicazione di riscontro criticità impianti depurazione scarichi civili.pdf', 'COMUNICAZIONE_PEC', 'Comunicazione agli enti sulle criticità riscontrate negli impianti di depurazione scarichi civili.',
   'stabilimento_2/documenti-aia/ALLEGATO 3E - SCARICHI IDRICI_Comunicazione di riscontro criticità impianti depurazione scarichi civili.pdf', 121226, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Promolog Srl',
   'Allegato 3E – Comunicazione criticità impianti depurazione scarichi civili', NULL, '2025-06-27', 'scarichi-idrici,criticita,comunicazione,allegato-3e,depurazione-civile', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato 5 – Valutazione impatto acustico anno 2024 (ZIP)', 'ALLEGATO 5 -Valutazione di impatto acustico_anno 2024.zip', 'VALUTAZIONE_ACUSTICA', 'ZIP della valutazione acustica 2024 (4 posizioni confine + 4 ricettori, Leq diurno/notturno).',
   'stabilimento_2/documenti-aia/ALLEGATO 5 -Valutazione di impatto acustico_anno 2024.zip', 1832109, 'application/zip', 1, 2024, 'ARCHIVIATO', 'Professionista incaricato',
   'Allegato 5 – Valutazione impatto acustico stabilimento Coriano Veronese 2024', NULL, '2025-04-30', 'acustica,valutazione-impatto,allegato-5,zip,2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato A – Determina AIA 2635/2023', 'Allegato AA20230906110737436Z.pdf_Marcato.pdf', 'DECRETO_AIA', 'Allegato A della determina AIA 2023: prescrizioni tecniche precedenti al rinnovo 2024.',
   'stabilimento_2/documenti-aia/Allegato AA20230906110737436Z.pdf_Marcato.pdf', 967975, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Provincia di Verona – Settore Ambiente',
   'Allegato A prescrizioni – Determina AIA 2635/2023', '2635/2023-A', '2023-09-07', 'determina,2023,allegato-a,prescrizioni,aia-precedente', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC – Determina 2635/2023 (dest.2)', 'CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p (1).pdf', 'PEC_RICEVUTA', 'Ricevuta di consegna PEC per trasmissione determina AIA 2635/2023 (2° destinatario).',
   'stabilimento_2/documenti-aia/CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p (1).pdf', 69098, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC trasmissione Determina AIA 2635/2023', NULL, '2023-09-07', 'pec-consegna,determina-2635-2023,ricevuta', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC – Determina 2635/2023 (dest.3)', 'CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p (2).pdf', 'PEC_RICEVUTA', 'Ricevuta di consegna PEC per trasmissione determina AIA 2635/2023 (3° destinatario).',
   'stabilimento_2/documenti-aia/CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p (2).pdf', 69095, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC trasmissione Determina AIA 2635/2023', NULL, '2023-09-07', 'pec-consegna,determina-2635-2023,ricevuta', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC – Determina 2635/2023 (dest.1)', 'CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p.pdf', 'PEC_RICEVUTA', 'Ricevuta di consegna PEC per trasmissione determina AIA 2635/2023 (1° destinatario).',
   'stabilimento_2/documenti-aia/CONSEGNA__PROMOLOG_SRL_stabilimento_di_Coriano_Veronese_-_Determinazione_n.2635_11_del_06_09_2023__p.pdf', 68844, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC trasmissione Determina AIA 2635/2023', NULL, '2023-09-07', 'pec-consegna,determina-2635-2023,ricevuta', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato A – Decreto AIA 19082/2024 (Prescrizioni tecniche)', 'Decreto_n.19082_del_20-08-2024-Allegato-A.pdf', 'DECRETO_AIA', 'Allegato A del decreto AIA 2024: dettaglio prescrizioni tecniche per emissioni, scarichi, rifiuti, acqua, suolo.',
   'stabilimento_2/documenti-aia/Decreto_n.19082_del_20-08-2024-Allegato-A.pdf', 582181, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Regione Veneto – Direzione Ambiente',
   'Allegato A – Prescrizioni tecniche e limiti emissivi decreto AIA 2024', '19082/2024-A', '2024-08-22', 'decreto-aia,allegato-a,prescrizioni,limiti-emissivi,2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Allegato B – Decreto AIA 19082/2024 (PMC prescritto)', 'Decreto_n.19082_del_20-08-2024-Allegato-B.pdf', 'DECRETO_AIA', 'Allegato B del decreto AIA 2024: PMC ufficiale con frequenze, parametri e metodi prescritti.',
   'stabilimento_2/documenti-aia/Decreto_n.19082_del_20-08-2024-Allegato-B.pdf', 308796, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Regione Veneto – Direzione Ambiente',
   'Allegato B – Piano di Monitoraggio e Controllo prescritto dal decreto AIA 2024', '19082/2024-B', '2024-08-22', 'decreto-aia,allegato-b,pmc,piano-monitoraggio,2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Decreto AIA n.19082 del 20/08/2024', 'Decreto_n.19082_del_20-08-2024.pdf', 'DECRETO_AIA', 'Decreto di rinnovo AIA vigente. Stabilisce prescrizioni, PMC e scadenze per il periodo autorizzativo.',
   'stabilimento_2/documenti-aia/Decreto_n.19082_del_20-08-2024.pdf', 819036, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Regione Veneto – Direzione Ambiente',
   'Rinnovo AIA – Promolog Srl stabilimento Coriano Veronese', '19082/2024', '2024-08-22', 'decreto-aia,autorizzazione-vigente,2024,rinnovo', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Determina n.2635 del 06/09/2023 – AIA precedente', 'DeterminaSemplice_Originale_2635_2023.pdf_Marcato.pdf', 'DECRETO_AIA', 'Autorizzazione AIA precedente al rinnovo 2024. Sostituita dal Decreto 19082/2024.',
   'stabilimento_2/documenti-aia/DeterminaSemplice_Originale_2635_2023.pdf_Marcato.pdf', 159777, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Provincia di Verona – Settore Ambiente',
   'Determinazione AIA n.2635/2023 – Promolog Srl Coriano Veronese', '2635/2023', '2023-09-07', 'determina,2023,aia-precedente,provincia-verona', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Dati analitici Agrolab ordine 379077 (Excel copia 2)', 'EXCEL_379077 2.xlsx', 'DATI_LABORATORIO', 'Copia del file Excel Agrolab ordine 379077 con dati analitici S1/S2.',
   'stabilimento_2/documenti-aia/EXCEL_379077 2.xlsx', 7869, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia',
   'Risultati analitici Agrolab ordine 379077 – copia 2', '379077', '2025-05-06', 'agrolab,dati-analitici,379077,xlsx,copia', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Dati analitici Agrolab ordine 379077 (Excel)', 'EXCEL_379077.xlsx', 'DATI_LABORATORIO', 'File Excel Agrolab con dati analitici ordine 379077 per S1 e S2.',
   'stabilimento_2/documenti-aia/EXCEL_379077.xlsx', 7869, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia',
   'Risultati analitici Agrolab ordine 379077 in formato Excel', '379077', '2025-05-06', 'agrolab,dati-analitici,379077,xlsx,s1,s2', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Dati analitici Agrolab ordine 379093 (Excel)', 'EXCEL_379093.xlsx', 'DATI_LABORATORIO', 'File Excel Agrolab con dati analitici ordine 379093 per S1 e S2 (campionamento 29/08/2024).',
   'stabilimento_2/documenti-aia/EXCEL_379093.xlsx', 6126, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia',
   'Risultati analitici Agrolab ordine 379093 in formato Excel', '379093', '2025-05-06', 'agrolab,dati-analitici,379093,xlsx,s1,s2,2024-08-29', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Dati analitici Agrolab ordine 381109 (Excel copia 2)', 'EXCEL_381109 2.xlsx', 'DATI_LABORATORIO', 'Copia del file Excel Agrolab ordine 381109 con dati analitici S1/S2.',
   'stabilimento_2/documenti-aia/EXCEL_381109 2.xlsx', 6127, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia',
   'Risultati analitici Agrolab ordine 381109 – copia 2', '381109', '2025-05-06', 'agrolab,dati-analitici,381109,xlsx,copia', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Dati analitici Agrolab ordine 381109 (Excel)', 'EXCEL_381109.xlsx', 'DATI_LABORATORIO', 'File Excel Agrolab con dati analitici ordine 381109 per S1 e S2.',
   'stabilimento_2/documenti-aia/EXCEL_381109.xlsx', 6127, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia',
   'Risultati analitici Agrolab ordine 381109 in formato Excel', '381109', '2025-05-06', 'agrolab,dati-analitici,381109,xlsx,s1,s2', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (1, 'GMI – Proof of Context package AIA', 'GMI -  Proof of Context - package AIA.docx', 'STUDIO_TECNICO', 'Documento GMI con context informativo sul processo AIA per lo sviluppo del sistema di gestione.',
   'stabilimento_1/documenti-aia/GMI -  Proof of Context - package AIA.docx', 259784, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 1, 2025, 'RICEVUTO', 'Grandi Molini Italiani',
   'Proof of Context – pacchetto informativo AIA per sviluppo sistema gestionale', NULL, '2025-03-03', 'gmi,proof-of-context,analisi,package-aia,progetto', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (1, 'GMI – Analisi di Impatto Preliminare package AIA', 'GMI - Analisi di Impatto Preliminare - package AIA.docx', 'STUDIO_TECNICO', 'Analisi preliminare di impatto del sistema di gestione AIA per Grandi Molini Italiani.',
   'stabilimento_1/documenti-aia/GMI - Analisi di Impatto Preliminare - package AIA.docx', 246183, 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 1, 2025, 'RICEVUTO', 'Grandi Molini Italiani',
   'Analisi di impatto preliminare – pacchetto AIA per sviluppo sistema gestionale', NULL, '2025-02-24', 'gmi,analisi-impatto-preliminare,package-aia,progetto', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (1, 'Trasmissione atto riesame AIA – Grandi Molini Italiani Livorno', 'GrandiM_Riesame_TrasmissioneAtto.pdf', 'COMUNICAZIONE_PEC', 'Comunicazione di trasmissione dell''atto di riesame/rinnovo AIA per lo stabilimento GMI di Livorno (Regione Toscana).',
   'stabilimento_1/documenti-aia/GrandiM_Riesame_TrasmissioneAtto.pdf', 417500, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Regione Toscana – Settore AIA',
   'Trasmissione atto riesame AIA – GMI Livorno', NULL, '2024-08-22', 'riesame,trasmissione,atto,gmi,livorno,toscana,2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Registro controllo operativo – Coriano Veronese (PA01CO-R01)', 'PA01CO-R01_CONTROLLO OPERATIVO.xlsm', 'REGISTRO_OPERATIVO', 'Registro Excel con macro per tracciare i controlli operativi AIA dello stabilimento di Coriano. Contiene dati mensili per tutti i monitoraggi.',
   'stabilimento_2/documenti-aia/PA01CO-R01_CONTROLLO OPERATIVO.xlsm', 1697868, 'application/vnd.ms-excel.sheet.macroenabled.12', 1, 2024, 'RICEVUTO', 'Promolog Srl',
   'PA01CO-R01 – Registro di controllo operativo AIA – Coriano Veronese', 'PA01CO-R01', '2025-03-03', 'registro-operativo,coriano,controllo-operativo,xlsm,pa01co', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (1, 'Registro controllo operativo – Livorno (PA01LI-R01)', 'PA01LI-R01_CONTROLLO OPERATIVO.xlsm', 'REGISTRO_OPERATIVO', 'Registro Excel con macro per tracciare i controlli operativi AIA dello stabilimento di Livorno.',
   'stabilimento_1/documenti-aia/PA01LI-R01_CONTROLLO OPERATIVO.xlsm', 766728, 'application/vnd.ms-excel.sheet.macroenabled.12', 1, 2024, 'RICEVUTO', 'Grandi Molini Italiani',
   'PA01LI-R01 – Registro di controllo operativo AIA – Livorno', 'PA01LI-R01', '2025-03-03', 'registro-operativo,livorno,controllo-operativo,xlsm,pa01li,gmi', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'PMC approvato 2023 – Piano di Monitoraggio e Controllo', 'PMC finale approvato_Marcato.pdf', 'PMC_ANNUALE', 'PMC definitivo approvato con la determina AIA 2635/2023.',
   'stabilimento_2/documenti-aia/PMC finale approvato_Marcato.pdf', 217157, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Provincia di Verona',
   'Piano di Monitoraggio e Controllo finale approvato – 2023', NULL, '2023-09-07', 'pmc,approvato,2023,piano-monitoraggio', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Trasmissione Determina 2635/2023 a Promolog', 'Promolog_Trasm_determina 2635_23.pdf_Marcato.pdf', 'COMUNICAZIONE_PEC', 'PEC di trasmissione della determina AIA 2635/2023 a Promolog Srl.',
   'stabilimento_2/documenti-aia/Promolog_Trasm_determina 2635_23.pdf_Marcato.pdf', 86558, 'application/pdf', 1, 2023, 'ARCHIVIATO', 'Provincia di Verona',
   'Trasmissione Determinazione AIA n.2635/2023', '2635/2023', '2023-09-07', 'trasmissione,determina,2023,provincia-verona', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 379077 – Scarico S1', 'S1_rdp 379077-269735.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab accreditato ACCREDIA per scarico S1 – ordine campionamento 379077.',
   'stabilimento_2/documenti-aia/S1_rdp 379077-269735.pdf', 251390, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 379077 – Scarico S1', '379077-269735', '2025-06-23', 'agrolab,rapporto-prova,scarico-s1,379077,lab-0147-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 379093 – Scarico S1 (29/08/2024)', 'S1_rdp 379093-265652.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab per scarico S1 del 29/08/2024 – ordine 379093.',
   'stabilimento_2/documenti-aia/S1_rdp 379093-265652.pdf', 233697, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 379093 – Scarico S1', '379093-265652', '2025-06-23', 'agrolab,rapporto-prova,scarico-s1,379093,lab-0147-l,2024-08-29', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 381109 – Scarico S1', 'S1_rdp 381109-269747.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab accreditato ACCREDIA per scarico S1 – ordine campionamento 381109.',
   'stabilimento_2/documenti-aia/S1_rdp 381109-269747.pdf', 233700, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 381109 – Scarico S1', '381109-269747', '2025-06-23', 'agrolab,rapporto-prova,scarico-s1,381109,lab-0147-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 379077 – Scarico S2', 'S2_rdp 379077-269736.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab accreditato ACCREDIA per scarico S2 – ordine campionamento 379077.',
   'stabilimento_2/documenti-aia/S2_rdp 379077-269736.pdf', 251080, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 379077 – Scarico S2', '379077-269736', '2025-06-23', 'agrolab,rapporto-prova,scarico-s2,379077,lab-0147-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 379093 – Scarico S2 (29/08/2024)', 'S2_rdp 379093-265653.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab per scarico S2 del 29/08/2024 – ordine 379093.',
   'stabilimento_2/documenti-aia/S2_rdp 379093-265653.pdf', 233725, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 379093 – Scarico S2', '379093-265653', '2025-06-23', 'agrolab,rapporto-prova,scarico-s2,379093,lab-0147-l,2024-08-29', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'RdP Agrolab 381109 – Scarico S2', 'S2_rdp 381109-269748.pdf', 'RAPPORTO_PROVA', 'RdP Agrolab accreditato ACCREDIA per scarico S2 – ordine campionamento 381109.',
   'stabilimento_2/documenti-aia/S2_rdp 381109-269748.pdf', 233730, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Agrolab Italia (LAB N° 0147 L)',
   'RdP Agrolab ordine 381109 – Scarico S2', '381109-269748', '2025-06-23', 'agrolab,rapporto-prova,scarico-s2,381109,lab-0147-l', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (1, 'Scadenze e prescrizioni AIA – Grandi Molini Italiani (gruppo)', 'Scadenze e prescrizioni di gruppo.xlsx', 'ALTRO', 'Foglio Excel centralizzato GMI con tutte le scadenze e prescrizioni AIA per Coriano, Livorno e altri stabilimenti.',
   'stabilimento_1/documenti-aia/Scadenze e prescrizioni di gruppo.xlsx', 25118, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 1, 2024, 'RICEVUTO', 'Grandi Molini Italiani',
   'Registro scadenze e prescrizioni AIA per tutti gli stabilimenti GMI', NULL, '2025-03-03', 'scadenze,prescrizioni,gmi,gruppo,tutti-stabilimenti,xlsx', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Valutazione impatto acustico – Coriano Veronese 2024', 'Valutazione impatto acustico Coriano_2024.pdf', 'VALUTAZIONE_ACUSTICA', 'Relazione tecnica 53 pag. Misure in 4 posizioni a confine (E1-E4) e 4 ricettori (R1-R4), diurno/notturno.',
   'stabilimento_2/documenti-aia/Valutazione impatto acustico Coriano_2024.pdf', 1989436, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Tecnico acustico incaricato',
   'Valutazione di impatto acustico stabilimento Coriano Veronese – anno 2024', NULL, '2025-04-30', 'acustica,coriano,2024,valutazione-impatto,leq,classe-ii-iii', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'PEC campionamento SF2 – Notifica Provincia/ARPAV/Comune (16/10/2024)', 'comunicazione Provincia_ARPAV_Comune per autocontrollo acque meteoriche 16_10_24.pdf', 'COMUNICAZIONE_PEC', 'Notifica PEC preventiva a Provincia VR, ARPAV e Comune Albaredo per campionamento SF2.',
   'stabilimento_2/documenti-aia/comunicazione Provincia_ARPAV_Comune per autocontrollo acque meteoriche 16_10_24.pdf', 750865, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Promolog Srl',
   'Comunicazione autocontrollo acque meteoriche SF2 del 16/10/2024', NULL, '2024-10-16', 'pec-campionamento,sf2,acque-meteoriche,16-10-2024,provincia-arpa-comune', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'PEC campionamento SF2 – Notifica Provincia/ARPAV/Comune (26/03/2024)', 'comunicazione Provincia_ARPAV_Comune per autocontrollo acque meteoriche 26_03_24.pdf', 'COMUNICAZIONE_PEC', 'Notifica PEC preventiva a Provincia VR, ARPAV e Comune Albaredo per campionamento SF2.',
   'stabilimento_2/documenti-aia/comunicazione Provincia_ARPAV_Comune per autocontrollo acque meteoriche 26_03_24.pdf', 750356, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Promolog Srl',
   'Comunicazione autocontrollo acque meteoriche SF2 del 26/03/2024', NULL, '2024-03-26', 'pec-campionamento,sf2,acque-meteoriche,26-03-2024,provincia-arpa-comune', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'PEC trasmissione relazione PMC 2024 (24/04/2025)', 'comunicazione del 24_04_2025 per invio relazione.pdf', 'COMUNICAZIONE_PEC', 'PEC di trasmissione del PMC 2024 (relazione e dati) a Provincia, ARPAV e Comune.',
   'stabilimento_2/documenti-aia/comunicazione del 24_04_2025 per invio relazione.pdf', 43925, 'application/pdf', 1, 2025, 'ARCHIVIATO', 'Promolog Srl',
   'Trasmissione relazione PMC e risultati autocontrollo anno 2024 – aprile 2025', NULL, '2025-04-24', 'pec,trasmissione,relazione-annuale,pmc,2025-04-24', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta accettazione PEC campionamento SF2 (26/03/2024)', 'mar-26-2024--15-36-54-accettazione-promolog-srl---.pdf', 'PEC_RICEVUTA', 'Ricevuta di accettazione PEC per notifica campionamento SF2 del 26/03/2024.',
   'stabilimento_2/documenti-aia/mar-26-2024--15-36-54-accettazione-promolog-srl---.pdf', 2467, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta accettazione PEC campionamento SF2 del 26/03/2024', NULL, '2024-03-26', 'pec-accettazione,sf2,acque-meteoriche,26-03-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 26/03/2024 (dest.1)', 'mar-26-2024--15-37-21-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.1 – Provincia VR) notifica campionamento SF2 26/03/2024.',
   'stabilimento_2/documenti-aia/mar-26-2024--15-37-21-consegna-promolog-srl---dete.pdf', 3518, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 26/03/2024 – dest.1', NULL, '2024-03-26', 'pec-consegna,sf2,acque-meteoriche,26-03-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 26/03/2024 (dest.2)', 'mar-26-2024--15-38-02-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.2 – ARPAV) notifica campionamento SF2 26/03/2024.',
   'stabilimento_2/documenti-aia/mar-26-2024--15-38-02-consegna-promolog-srl---dete.pdf', 3519, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 26/03/2024 – dest.2', NULL, '2024-03-26', 'pec-consegna,sf2,acque-meteoriche,26-03-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 26/03/2024 (dest.3)', 'mar-26-2024--15-39-58-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.3 – Comune Albaredo) notifica campionamento SF2 26/03/2024.',
   'stabilimento_2/documenti-aia/mar-26-2024--15-39-58-consegna-promolog-srl---dete.pdf', 3495, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 26/03/2024 – dest.3', NULL, '2024-03-26', 'pec-consegna,sf2,acque-meteoriche,26-03-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 26/03/2024 (dest.4)', 'mar-26-2024--15-40-20-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.4) notifica campionamento SF2 26/03/2024.',
   'stabilimento_2/documenti-aia/mar-26-2024--15-40-20-consegna-promolog-srl---dete.pdf', 3497, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 26/03/2024 – dest.4', NULL, '2024-03-26', 'pec-consegna,sf2,acque-meteoriche,26-03-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta accettazione PEC campionamento SF2 (16/10/2024)', 'ott-16-2024--17-12-41-accettazione-promolog-srl---.pdf', 'PEC_RICEVUTA', 'Ricevuta di accettazione PEC per notifica campionamento SF2 del 16/10/2024.',
   'stabilimento_2/documenti-aia/ott-16-2024--17-12-41-accettazione-promolog-srl---.pdf', 3030, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta accettazione PEC campionamento SF2 del 16/10/2024', NULL, '2024-10-16', 'pec-accettazione,sf2,acque-meteoriche,16-10-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 16/10/2024 (dest.1)', 'ott-16-2024--17-13-05-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.1 – Provincia VR) notifica campionamento SF2 16/10/2024.',
   'stabilimento_2/documenti-aia/ott-16-2024--17-13-05-consegna-promolog-srl---dete.pdf', 3501, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 16/10/2024 – dest.1', NULL, '2024-10-16', 'pec-consegna,sf2,acque-meteoriche,16-10-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 16/10/2024 (dest.2)', 'ott-16-2024--17-13-33-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.2 – ARPAV) notifica campionamento SF2 16/10/2024.',
   'stabilimento_2/documenti-aia/ott-16-2024--17-13-33-consegna-promolog-srl---dete.pdf', 3526, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 16/10/2024 – dest.2', NULL, '2024-10-16', 'pec-consegna,sf2,acque-meteoriche,16-10-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP),
  (2, 'Ricevuta consegna PEC campionamento SF2 16/10/2024 (dest.3)', 'ott-16-2024--17-13-51-consegna-promolog-srl---dete.pdf', 'PEC_RICEVUTA', 'Ricevuta consegna PEC (dest.3 – Comune Albaredo) notifica campionamento SF2 16/10/2024.',
   'stabilimento_2/documenti-aia/ott-16-2024--17-13-51-consegna-promolog-srl---dete.pdf', 3508, 'application/pdf', 1, 2024, 'ARCHIVIATO', 'Gestore PEC',
   'Ricevuta consegna PEC campionamento SF2 16/10/2024 – dest.3', NULL, '2024-10-16', 'pec-consegna,sf2,acque-meteoriche,16-10-2024', TRUE,
   CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP);

-- Totale documenti inseriti: 52  (stabilimento_1=5, stabilimento_2=47)