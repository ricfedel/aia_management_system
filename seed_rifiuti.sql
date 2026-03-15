-- ============================================================
--  SEED RIFIUTI  –  AIA Management System
--  Caricamento manuale:
--    psql -U aia -d aia_management -f seed_rifiuti.sql
--  oppure via Docker:
--    docker exec -i aia-postgres psql -U aia -d aia_management < seed_rifiuti.sql
-- ============================================================

BEGIN;

-- ─────────────────────────────────────────────────────────────
--  CODICI CER  –  STABILIMENTO 1  (GMI Livorno)
-- ─────────────────────────────────────────────────────────────
INSERT INTO codici_rifiuto
  (stabilimento_id, codice_cer, descrizione, pericoloso, stato_fisico,
   unita_misura, codice_gestione, destinatario_abituale, attivo, created_at)
VALUES
  (1, '02 03 04',
   'Scarti inutilizzabili per il consumo o la trasformazione (farine, crusca, polveri di pulizia)',
   false, 'SOLIDO', 't', 'R3', 'Biogas Energia Srl – Via Industriale 12, Brescia', true, NOW()),

  (1, '02 03 99',
   'Rifiuti non specificati altrimenti – polveri di aspirazione silos e impianti di macinazione',
   false, 'SOLIDO', 't', 'R3', 'Ecologia Service SpA – Via del Lavoro 8, Mantova', true, NOW()),

  (1, '15 01 01',
   'Imballaggi in carta e cartone',
   false, 'SOLIDO', 't', 'R3', 'Cartotecnica Nord Srl – Via Manzoni 44, Milano', true, NOW()),

  (1, '15 01 02',
   'Imballaggi in plastica (sacchi, film, reggette)',
   false, 'SOLIDO', 't', 'R3', 'Plastic Recycling SpA – Via Po 18, Torino', true, NOW()),

  (1, '15 01 06',
   'Imballaggi in materiali misti (sacchi multistrato carta/plastica)',
   false, 'SOLIDO', 't', 'R3', 'Multipack Ecologia Srl – Via Roma 5, Verona', true, NOW()),

  (1, '15 01 10*',
   'Imballaggi contenenti residui di sostanze pericolose o contaminati da tali sostanze (fusti olio lubrificante)',
   true, 'SOLIDO', 't', 'D15', 'Ecotox Srl – Via Industria 33, Bergamo', true, NOW()),

  (1, '13 02 05*',
   'Scarti di olio minerale per motori, ingranaggi e lubrificazione (non clorurati)',
   true, 'LIQUIDO', 'l', 'R9', 'Oleodep SpA – Via Veneto 7, Cremona', true, NOW()),

  (1, '13 01 10*',
   'Oli idraulici minerali non clorurati',
   true, 'LIQUIDO', 'l', 'R9', 'Oleodep SpA – Via Veneto 7, Cremona', true, NOW()),

  (1, '17 04 05',
   'Ferro e acciaio (rottami da manutenzione: viti, bulloni, componenti usurati)',
   false, 'SOLIDO', 't', 'R4', 'Metallurgica Est Srl – Via delle Acciaierie 2, Brescia', true, NOW()),

  (1, '16 06 01*',
   'Batterie al piombo (carrelli elevatori, UPS)',
   true, 'SOLIDO', 'kg', 'R4', 'Batterie Recycling Srl – Via Edison 9, Milano', true, NOW()),

  (1, '20 01 21*',
   'Tubi fluorescenti ed altri rifiuti contenenti mercurio',
   true, 'SOLIDO', 'kg', 'D15', 'Ecolamp – Via della Luce 3, Monza', true, NOW()),

  (1, '08 03 18',
   'Toner per stampa esauriti (non contenenti sostanze pericolose)',
   false, 'SOLIDO', 'kg', 'R4', 'Cartucce & Co Srl – Via Marconi 11, Lodi', true, NOW()),

  (1, '20 03 01',
   'Rifiuti urbani misti – uffici e spogliatoi',
   false, 'SOLIDO', 't', 'D1', 'Amsa SpA – Via Olgettina 25, Milano', true, NOW());


-- ─────────────────────────────────────────────────────────────
--  CODICI CER  –  STABILIMENTO 2  (Promolog)
-- ─────────────────────────────────────────────────────────────
INSERT INTO codici_rifiuto
  (stabilimento_id, codice_cer, descrizione, pericoloso, stato_fisico,
   unita_misura, codice_gestione, destinatario_abituale, attivo, created_at)
VALUES
  (2, '02 03 04',
   'Scarti inutilizzabili per il consumo o la trasformazione (farine, semole, polveri di pulizia)',
   false, 'SOLIDO', 't', 'R3', 'Biogas Energia Srl – Via Industriale 12, Brescia', true, NOW()),

  (2, '15 01 01',
   'Imballaggi in carta e cartone',
   false, 'SOLIDO', 't', 'R3', 'Cartotecnica Nord Srl – Via Manzoni 44, Milano', true, NOW()),

  (2, '15 01 02',
   'Imballaggi in plastica',
   false, 'SOLIDO', 't', 'R3', 'Plastic Recycling SpA – Via Po 18, Torino', true, NOW()),

  (2, '15 01 06',
   'Imballaggi in materiali misti',
   false, 'SOLIDO', 't', 'R3', 'Multipack Ecologia Srl – Via Roma 5, Verona', true, NOW()),

  (2, '13 02 05*',
   'Scarti di olio minerale per motori, ingranaggi e lubrificazione (non clorurati)',
   true, 'LIQUIDO', 'l', 'R9', 'Oleodep SpA – Via Veneto 7, Cremona', true, NOW()),

  (2, '17 04 05',
   'Ferro e acciaio (rottami da manutenzione)',
   false, 'SOLIDO', 't', 'R4', 'Metallurgica Est Srl – Via delle Acciaierie 2, Brescia', true, NOW()),

  (2, '20 01 21*',
   'Tubi fluorescenti ed altri rifiuti contenenti mercurio',
   true, 'SOLIDO', 'kg', 'D15', 'Ecolamp – Via della Luce 3, Monza', true, NOW()),

  (2, '20 03 01',
   'Rifiuti urbani misti – uffici e spogliatoi',
   false, 'SOLIDO', 't', 'D1', 'Amsa SpA – Via Olgettina 25, Milano', true, NOW());


-- ─────────────────────────────────────────────────────────────
--  MOVIMENTI RIFIUTO 2025  –  STABILIMENTO 1
--
--  Tecnica corretta:
--    1. CTE "cer" recupera gli id appena inseriti
--    2. CTE "mv" contiene i dati grezzi con codice_cer come stringa
--    3. JOIN finale risolve codice_cer → id
-- ─────────────────────────────────────────────────────────────
WITH cer AS (
  SELECT id, codice_cer
  FROM codici_rifiuto
  WHERE stabilimento_id = 1
),
mv (c_cer, anno, mese, tipo_mov, quantita, um,
    cod_op, destinatario, trasportatore, fir, data_op) AS (
VALUES
  -- 02 03 04 – Scarti molitura (produzione mensile + recupero trimestrale)
  ('02 03 04', 2025, 1, 'PRODUZIONE',   18.40::float8, 't'::text, NULL::text, NULL::text,                  NULL::text,              NULL::text,      NULL::text),
  ('02 03 04', 2025, 2, 'PRODUZIONE',   17.20,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 3, 'PRODUZIONE',   19.50,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 3, 'RECUPERO',     55.10,         't',       'R3',       'Biogas Energia Srl',        'Trasporti Verdi Srl',   'FIR-2025-0031', '2025-03-28'),
  ('02 03 04', 2025, 4, 'PRODUZIONE',   16.80,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 5, 'PRODUZIONE',   18.10,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 6, 'PRODUZIONE',   17.90,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 6, 'RECUPERO',     52.80,         't',       'R3',       'Biogas Energia Srl',        'Trasporti Verdi Srl',   'FIR-2025-0087', '2025-06-27'),
  ('02 03 04', 2025, 7, 'PRODUZIONE',   20.30,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 8, 'PRODUZIONE',   19.70,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 9, 'PRODUZIONE',   18.60,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 04', 2025, 9, 'RECUPERO',     58.60,         't',       'R3',       'Biogas Energia Srl',        'Trasporti Verdi Srl',   'FIR-2025-0142', '2025-09-26'),

  -- 02 03 99 – Polveri aspirazione
  ('02 03 99', 2025, 1, 'PRODUZIONE',    2.10,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 2, 'PRODUZIONE',    1.95,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 3, 'PRODUZIONE',    2.25,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 3, 'RECUPERO',      6.30,         't',       'R3',       'Ecologia Service SpA',      'Ecofleet Srl',          'FIR-2025-0036', '2025-03-31'),
  ('02 03 99', 2025, 4, 'PRODUZIONE',    2.00,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 5, 'PRODUZIONE',    2.15,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 6, 'PRODUZIONE',    2.05,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('02 03 99', 2025, 6, 'RECUPERO',      6.20,         't',       'R3',       'Ecologia Service SpA',      'Ecofleet Srl',          'FIR-2025-0093', '2025-06-30'),

  -- 15 01 01 – Carta/cartone
  ('15 01 01', 2025, 1, 'PRODUZIONE',    0.82,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 2, 'PRODUZIONE',    0.75,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 3, 'PRODUZIONE',    0.90,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 3, 'RECUPERO',      2.47,         't',       'R3',       'Cartotecnica Nord Srl',     'Ecofleet Srl',          'FIR-2025-0032', '2025-03-31'),
  ('15 01 01', 2025, 4, 'PRODUZIONE',    0.78,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 5, 'PRODUZIONE',    0.85,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 6, 'PRODUZIONE',    0.80,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 01', 2025, 6, 'RECUPERO',      2.43,         't',       'R3',       'Cartotecnica Nord Srl',     'Ecofleet Srl',          'FIR-2025-0088', '2025-06-30'),

  -- 15 01 02 – Plastica
  ('15 01 02', 2025, 1, 'PRODUZIONE',    0.34,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 2, 'PRODUZIONE',    0.31,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 3, 'PRODUZIONE',    0.36,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 3, 'RECUPERO',      1.01,         't',       'R3',       'Plastic Recycling SpA',     'Ecofleet Srl',          'FIR-2025-0033', '2025-03-31'),
  ('15 01 02', 2025, 4, 'PRODUZIONE',    0.29,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 5, 'PRODUZIONE',    0.33,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 6, 'PRODUZIONE',    0.30,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 02', 2025, 6, 'RECUPERO',      0.92,         't',       'R3',       'Plastic Recycling SpA',     'Ecofleet Srl',          'FIR-2025-0089', '2025-06-30'),

  -- 15 01 06 – Imballaggi misti
  ('15 01 06', 2025, 1, 'PRODUZIONE',    0.20,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 2, 'PRODUZIONE',    0.18,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 3, 'PRODUZIONE',    0.22,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 3, 'RECUPERO',      0.60,         't',       'R3',       'Multipack Ecologia Srl',    'Ecofleet Srl',          'FIR-2025-0037', '2025-03-31'),
  ('15 01 06', 2025, 4, 'PRODUZIONE',    0.19,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 5, 'PRODUZIONE',    0.21,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 6, 'PRODUZIONE',    0.20,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 06', 2025, 6, 'RECUPERO',      0.60,         't',       'R3',       'Multipack Ecologia Srl',    'Ecofleet Srl',          'FIR-2025-0094', '2025-06-30'),

  -- 15 01 10* – Imballaggi pericolosi
  ('15 01 10*',2025, 3, 'PRODUZIONE',    0.08,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 10*',2025, 3, 'SMALTIMENTO',   0.08,         't',       'D15',      'Ecotox Srl',                'HazMat Transport Srl',  'FIR-2025-0038', '2025-03-20'),
  ('15 01 10*',2025, 6, 'PRODUZIONE',    0.06,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('15 01 10*',2025, 6, 'SMALTIMENTO',   0.06,         't',       'D15',      'Ecotox Srl',                'HazMat Transport Srl',  'FIR-2025-0095', '2025-06-20'),

  -- 13 02 05* – Oli motori esausti
  ('13 02 05*',2025, 1, 'PRODUZIONE',  140.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 2, 'PRODUZIONE',  120.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 3, 'PRODUZIONE',  155.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 4, 'PRODUZIONE',  130.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 5, 'PRODUZIONE',  145.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 6, 'PRODUZIONE',  160.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 02 05*',2025, 6, 'RECUPERO',    850.0,          'l',       'R9',       'Oleodep SpA',               'HazMat Transport Srl',  'FIR-2025-0090', '2025-06-20'),

  -- 13 01 10* – Oli idraulici
  ('13 01 10*',2025, 1, 'PRODUZIONE',   60.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 2, 'PRODUZIONE',   55.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 3, 'PRODUZIONE',   70.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 4, 'PRODUZIONE',   58.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 5, 'PRODUZIONE',   65.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 6, 'PRODUZIONE',   72.0,          'l',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('13 01 10*',2025, 6, 'RECUPERO',    380.0,          'l',       'R9',       'Oleodep SpA',               'HazMat Transport Srl',  'FIR-2025-0096', '2025-06-20'),

  -- 17 04 05 – Rottami ferrosi
  ('17 04 05', 2025, 1, 'PRODUZIONE',    0.18,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 2, 'PRODUZIONE',    0.22,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 3, 'PRODUZIONE',    0.15,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 4, 'PRODUZIONE',    0.20,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 5, 'PRODUZIONE',    0.17,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 6, 'PRODUZIONE',    0.25,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('17 04 05', 2025, 6, 'RECUPERO',      1.17,         't',       'R4',       'Metallurgica Est Srl',      'Ecofleet Srl',          'FIR-2025-0091', '2025-06-25'),

  -- 16 06 01* – Batterie al piombo
  ('16 06 01*',2025, 4, 'PRODUZIONE',   48.0,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('16 06 01*',2025, 4, 'RECUPERO',     48.0,          'kg',      'R4',       'Batterie Recycling Srl',    'HazMat Transport Srl',  'FIR-2025-0055', '2025-04-10'),

  -- 20 01 21* – Lampade fluorescenti
  ('20 01 21*',2025, 3, 'PRODUZIONE',   24.0,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 01 21*',2025, 3, 'SMALTIMENTO',  24.0,          'kg',      'D15',      'Ecolamp',                   'HazMat Transport Srl',  'FIR-2025-0034', '2025-03-15'),

  -- 08 03 18 – Toner
  ('08 03 18', 2025, 1, 'PRODUZIONE',    3.2,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 2, 'PRODUZIONE',    2.8,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 3, 'PRODUZIONE',    3.5,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 3, 'RECUPERO',      9.5,          'kg',      'R4',       'Cartucce & Co Srl',         'Ecofleet Srl',          'FIR-2025-0039', '2025-03-31'),
  ('08 03 18', 2025, 4, 'PRODUZIONE',    2.9,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 5, 'PRODUZIONE',    3.1,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 6, 'PRODUZIONE',    3.0,          'kg',      NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('08 03 18', 2025, 6, 'RECUPERO',      9.0,          'kg',      'R4',       'Cartucce & Co Srl',         'Ecofleet Srl',          'FIR-2025-0097', '2025-06-30'),

  -- 20 03 01 – Rifiuti urbani misti
  ('20 03 01', 2025, 1, 'PRODUZIONE',    0.45,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 2, 'PRODUZIONE',    0.42,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 3, 'PRODUZIONE',    0.48,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 3, 'SMALTIMENTO',   1.35,         't',       'D1',       'Amsa SpA',                  'Amsa SpA',              'FIR-2025-0035', '2025-03-31'),
  ('20 03 01', 2025, 4, 'PRODUZIONE',    0.44,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 5, 'PRODUZIONE',    0.46,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 6, 'PRODUZIONE',    0.43,         't',       NULL,       NULL,                        NULL,                    NULL,            NULL),
  ('20 03 01', 2025, 6, 'SMALTIMENTO',   1.33,         't',       'D1',       'Amsa SpA',                  'Amsa SpA',              'FIR-2025-0092', '2025-06-30')
)
INSERT INTO movimenti_rifiuto
  (codice_rifiuto_id, anno, mese, tipo_movimento, quantita, unita_misura,
   codice_operazione, destinatario, trasportatore, numero_fir, data_operazione, created_at)
SELECT
  cer.id,
  mv.anno,
  mv.mese,
  mv.tipo_mov,
  mv.quantita,
  mv.um,
  mv.cod_op,
  mv.destinatario,
  mv.trasportatore,
  mv.fir,
  mv.data_op::date,
  NOW()
FROM mv
JOIN cer ON cer.codice_cer = mv.c_cer;


-- ─────────────────────────────────────────────────────────────
--  MOVIMENTI RIFIUTO 2025  –  STABILIMENTO 2  (Promolog)
-- ─────────────────────────────────────────────────────────────
WITH cer AS (
  SELECT id, codice_cer
  FROM codici_rifiuto
  WHERE stabilimento_id = 2
),
mv (c_cer, anno, mese, tipo_mov, quantita, um,
    cod_op, destinatario, trasportatore, fir, data_op) AS (
VALUES
  -- 02 03 04 – Scarti molitura
  ('02 03 04', 2025, 1, 'PRODUZIONE',   12.30::float8, 't'::text, NULL::text, NULL::text,               NULL::text,             NULL::text,      NULL::text),
  ('02 03 04', 2025, 2, 'PRODUZIONE',   11.80,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 3, 'PRODUZIONE',   13.10,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 3, 'RECUPERO',     37.20,         't',       'R3',       'Biogas Energia Srl',     'Trasporti Verdi Srl',  'FIR-2025-1031', '2025-03-28'),
  ('02 03 04', 2025, 4, 'PRODUZIONE',   12.00,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 5, 'PRODUZIONE',   12.70,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 6, 'PRODUZIONE',   11.50,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 6, 'RECUPERO',     36.20,         't',       'R3',       'Biogas Energia Srl',     'Trasporti Verdi Srl',  'FIR-2025-1087', '2025-06-27'),
  ('02 03 04', 2025, 7, 'PRODUZIONE',   13.40,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 8, 'PRODUZIONE',   12.90,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 9, 'PRODUZIONE',   12.20,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('02 03 04', 2025, 9, 'RECUPERO',     38.50,         't',       'R3',       'Biogas Energia Srl',     'Trasporti Verdi Srl',  'FIR-2025-1142', '2025-09-26'),

  -- 15 01 01 – Carta/cartone
  ('15 01 01', 2025, 1, 'PRODUZIONE',    0.52,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 2, 'PRODUZIONE',    0.48,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 3, 'PRODUZIONE',    0.55,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 3, 'RECUPERO',      1.55,         't',       'R3',       'Cartotecnica Nord Srl',  'Ecofleet Srl',         'FIR-2025-1032', '2025-03-31'),
  ('15 01 01', 2025, 4, 'PRODUZIONE',    0.50,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 5, 'PRODUZIONE',    0.53,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 6, 'PRODUZIONE',    0.49,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 01', 2025, 6, 'RECUPERO',      1.52,         't',       'R3',       'Cartotecnica Nord Srl',  'Ecofleet Srl',         'FIR-2025-1088', '2025-06-30'),

  -- 15 01 02 – Plastica
  ('15 01 02', 2025, 1, 'PRODUZIONE',    0.22,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 2, 'PRODUZIONE',    0.20,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 3, 'PRODUZIONE',    0.24,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 3, 'RECUPERO',      0.66,         't',       'R3',       'Plastic Recycling SpA',  'Ecofleet Srl',         'FIR-2025-1033', '2025-03-31'),
  ('15 01 02', 2025, 4, 'PRODUZIONE',    0.19,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 5, 'PRODUZIONE',    0.21,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 6, 'PRODUZIONE',    0.23,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 02', 2025, 6, 'RECUPERO',      0.63,         't',       'R3',       'Plastic Recycling SpA',  'Ecofleet Srl',         'FIR-2025-1089', '2025-06-30'),

  -- 15 01 06 – Imballaggi misti
  ('15 01 06', 2025, 1, 'PRODUZIONE',    0.14,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 2, 'PRODUZIONE',    0.12,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 3, 'PRODUZIONE',    0.15,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 3, 'RECUPERO',      0.41,         't',       'R3',       'Multipack Ecologia Srl', 'Ecofleet Srl',         'FIR-2025-1034', '2025-03-31'),
  ('15 01 06', 2025, 4, 'PRODUZIONE',    0.13,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 5, 'PRODUZIONE',    0.14,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 6, 'PRODUZIONE',    0.13,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('15 01 06', 2025, 6, 'RECUPERO',      0.40,         't',       'R3',       'Multipack Ecologia Srl', 'Ecofleet Srl',         'FIR-2025-1093', '2025-06-30'),

  -- 13 02 05* – Oli esausti
  ('13 02 05*',2025, 1, 'PRODUZIONE',   90.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 2, 'PRODUZIONE',   85.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 3, 'PRODUZIONE',   95.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 4, 'PRODUZIONE',   80.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 5, 'PRODUZIONE',   92.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 6, 'PRODUZIONE',  100.0,          'l',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('13 02 05*',2025, 6, 'RECUPERO',    542.0,          'l',       'R9',       'Oleodep SpA',            'HazMat Transport Srl', 'FIR-2025-1090', '2025-06-20'),

  -- 17 04 05 – Rottami ferrosi
  ('17 04 05', 2025, 1, 'PRODUZIONE',    0.12,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 2, 'PRODUZIONE',    0.14,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 3, 'PRODUZIONE',    0.10,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 4, 'PRODUZIONE',    0.13,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 5, 'PRODUZIONE',    0.11,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 6, 'PRODUZIONE',    0.16,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('17 04 05', 2025, 6, 'RECUPERO',      0.76,         't',       'R4',       'Metallurgica Est Srl',   'Ecofleet Srl',         'FIR-2025-1091', '2025-06-25'),

  -- 20 01 21* – Lampade fluorescenti
  ('20 01 21*',2025, 4, 'PRODUZIONE',   18.0,          'kg',      NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 01 21*',2025, 4, 'SMALTIMENTO',  18.0,          'kg',      'D15',      'Ecolamp',                'HazMat Transport Srl', 'FIR-2025-1055', '2025-04-15'),

  -- 20 03 01 – Rifiuti urbani misti
  ('20 03 01', 2025, 1, 'PRODUZIONE',    0.28,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 2, 'PRODUZIONE',    0.26,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 3, 'PRODUZIONE',    0.30,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 3, 'SMALTIMENTO',   0.84,         't',       'D1',       'Amsa SpA',               'Amsa SpA',             'FIR-2025-1035', '2025-03-31'),
  ('20 03 01', 2025, 4, 'PRODUZIONE',    0.27,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 5, 'PRODUZIONE',    0.29,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 6, 'PRODUZIONE',    0.25,         't',       NULL,       NULL,                     NULL,                   NULL,            NULL),
  ('20 03 01', 2025, 6, 'SMALTIMENTO',   0.81,         't',       'D1',       'Amsa SpA',               'Amsa SpA',             'FIR-2025-1092', '2025-06-30')
)
INSERT INTO movimenti_rifiuto
  (codice_rifiuto_id, anno, mese, tipo_movimento, quantita, unita_misura,
   codice_operazione, destinatario, trasportatore, numero_fir, data_operazione, created_at)
SELECT
  cer.id,
  mv.anno,
  mv.mese,
  mv.tipo_mov,
  mv.quantita,
  mv.um,
  mv.cod_op,
  mv.destinatario,
  mv.trasportatore,
  mv.fir,
  mv.data_op::date,
  NOW()
FROM mv
JOIN cer ON cer.codice_cer = mv.c_cer;


-- ─────────────────────────────────────────────────────────────
--  RESET SEQUENZE
-- ─────────────────────────────────────────────────────────────
SELECT setval(
  pg_get_serial_sequence('codici_rifiuto',   'id'),
  COALESCE((SELECT MAX(id) FROM codici_rifiuto),   1));
SELECT setval(
  pg_get_serial_sequence('movimenti_rifiuto', 'id'),
  COALESCE((SELECT MAX(id) FROM movimenti_rifiuto), 1));


-- ─────────────────────────────────────────────────────────────
--  VERIFICA
-- ─────────────────────────────────────────────────────────────
SELECT
  s.nome                                                         AS stabilimento,
  cr.codice_cer,
  cr.pericoloso,
  COUNT(mr.id)                                                   AS n_movimenti,
  SUM(CASE WHEN mr.tipo_movimento = 'PRODUZIONE'  THEN mr.quantita ELSE 0 END) AS tot_prodotto,
  SUM(CASE WHEN mr.tipo_movimento IN ('RECUPERO','SMALTIMENTO') THEN mr.quantita ELSE 0 END) AS tot_smaltito,
  cr.unita_misura                                                AS um
FROM codici_rifiuto cr
JOIN stabilimenti s ON s.id = cr.stabilimento_id
LEFT JOIN movimenti_rifiuto mr ON mr.codice_rifiuto_id = cr.id AND mr.anno = 2025
GROUP BY s.nome, cr.codice_cer, cr.pericoloso, cr.unita_misura
ORDER BY s.nome, cr.codice_cer;

COMMIT;
