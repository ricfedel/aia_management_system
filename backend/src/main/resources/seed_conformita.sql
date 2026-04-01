-- ============================================================
-- SEED CONFORMITÀ – rilevazioni_misura
-- Campagna PMC 2024 realistica per:
--   stabilimento 1: GMI S.p.A. – Livorno
--   stabilimento 2: Promolog S.r.l. – Albaredo d'Adige
--
-- ★ PREREQUISITO: data.sql già caricato
--   (stabilimenti, monitoraggi, parametri_monitoraggio devono esistere)
--
-- Come caricare:
--   psql -U aia -d aia_management -f seed_conformita.sql
-- oppure via Docker:
--   docker exec -i aia-postgres psql -U aia -d aia_management < seed_conformita.sql
-- ============================================================

BEGIN;

-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 0: MONITORAGGI + PARAMETRI – PROMOLOG (stabilimento_id=2)
-- Non presenti in data.sql; necessari per le rilevazioni Promolog.
-- ══════════════════════════════════════════════════════════════════

INSERT INTO monitoraggi
  (stabilimento_id, codice, descrizione, tipo_monitoraggio,
   punto_emissione, frequenza, laboratorio, attivo)
VALUES
  (2, 'E1',  'Molino principale – aspirazione generale',      'EMISSIONI_ATMOSFERA', 'MACINAZIONE',   'ANNUALE',    'Chelab S.r.l.', TRUE),
  (2, 'E2',  'Aspirazione silos prodotti finiti',             'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE',    'Chelab S.r.l.', TRUE),
  (2, 'E3',  'Prepulitura grano',                            'EMISSIONI_ATMOSFERA', 'PREPULITURA',   'ANNUALE',    'Chelab S.r.l.', TRUE),
  (2, 'E4',  'Movimentazione – nastri trasportatori',         'EMISSIONI_ATMOSFERA', 'MOVIMENTAZIONE','ANNUALE',    'Chelab S.r.l.', TRUE),
  (2, 'SF2', 'Acque meteoriche – punto di scarico SF2',       'SCARICHI_IDRICI',     'SCARICHI',      'SEMESTRALE', 'Chelab S.r.l.', TRUE),
  (2, 'SF1', 'Scarico civile – impianto depurazione',         'SCARICHI_IDRICI',     'SCARICHI',      'ANNUALE',    'Chelab S.r.l.', TRUE)
ON CONFLICT DO NOTHING;

-- Parametri per i camini Promolog (Polveri totali, VLE 5 mg/Nm3)
INSERT INTO parametri_monitoraggio
  (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT m.id, 'Polveri totali', 'mg/Nm3', 5.0,
       'BAT-AEL BAT 28 BATc FDM 2019', 'UNI EN 13284', TRUE
FROM monitoraggi m
WHERE m.stabilimento_id = 2 AND m.tipo_monitoraggio = 'EMISSIONI_ATMOSFERA'
  AND NOT EXISTS (
    SELECT 1 FROM parametri_monitoraggio p
    WHERE p.monitoraggio_id = m.id AND p.nome = 'Polveri totali');

-- Parametri SF2 (acque meteoriche)
INSERT INTO parametri_monitoraggio
  (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT m.id, param.nome, param.um, param.lim,
       'D.Lgs. 152/2006 All.5 Tab.3', NULL, TRUE
FROM monitoraggi m
CROSS JOIN (VALUES
  ('COD',               'mg/L', 160.0),
  ('Solidi Sospesi',    'mg/L',  80.0),
  ('Idrocarburi totali','mg/L',  10.0),
  ('pH',                NULL,    NULL),
  ('Azoto ammoniacale', 'mg/L',  15.0),
  ('Oli e grassi',      'mg/L',  20.0)
) AS param(nome, um, lim)
WHERE m.stabilimento_id = 2 AND m.codice = 'SF2'
  AND NOT EXISTS (
    SELECT 1 FROM parametri_monitoraggio p
    WHERE p.monitoraggio_id = m.id AND p.nome = param.nome);

-- Parametri SF1 (scarico civile)
INSERT INTO parametri_monitoraggio
  (monitoraggio_id, nome, unita_misura, limite_valore, limite_riferimento, metodo_analisi, attivo)
SELECT m.id, param.nome, param.um, param.lim,
       'D.Lgs. 152/2006 All.5', NULL, TRUE
FROM monitoraggi m
CROSS JOIN (VALUES
  ('COD',              'mg/L', 160.0),
  ('BOD5',             'mg/L',  40.0),
  ('Solidi Sospesi',   'mg/L',  80.0),
  ('pH',               NULL,    NULL),
  ('Azoto totale',     'mg/L',  15.0),
  ('Fosforo totale',   'mg/L',  10.0),
  ('Tensioattivi',     'mg/L',   2.0)
) AS param(nome, um, lim)
WHERE m.stabilimento_id = 2 AND m.codice = 'SF1'
  AND NOT EXISTS (
    SELECT 1 FROM parametri_monitoraggio p
    WHERE p.monitoraggio_id = m.id AND p.nome = param.nome);


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 1: EMISSIONI ATMOSFERA – GMI LIVORNO
-- Campagna annuale PMC 2024 – Chelab S.r.l.
-- Campionamento: 14–16 maggio 2024
-- Verbale campionamento: AM240035
-- Parametro: Polveri totali, VLE 5 mg/Nm3 (oppure 3 mg/Nm3 per E66/E91-E96)
-- ══════════════════════════════════════════════════════════════════

WITH misure_aria_gmi (codice, valore, data_camp, rapporto, n_acc, nota_campo) AS (VALUES
  -- ── TRASPORTO ──────────────────────────────────────────────────────
  ('E1',  1.2::float8, '2024-05-14'::date, '24/302450', '24.268001.0001', NULL::text),
  ('E2',  0.9::float8, '2024-05-14'::date, '24/302451', '24.268002.0001', NULL),
  ('E3',  1.4::float8, '2024-05-14'::date, '24/302452', '24.268003.0001', NULL),
  ('E7',  1.1::float8, '2024-05-14'::date, '24/302453', '24.268004.0001', NULL),
  ('E8',  0.8::float8, '2024-05-14'::date, '24/302454', '24.268005.0001', NULL),
  ('E52', 2.1::float8, '2024-05-14'::date, '24/302455', '24.268006.0001', NULL),
  -- ── PREPULITURA ────────────────────────────────────────────────────
  ('E4',  3.2::float8, '2024-05-14'::date, '24/302456', '24.268007.0001', NULL),
  ('E5',  2.8::float8, '2024-05-14'::date, '24/302457', '24.268008.0001', NULL),
  ('E6',  2.5::float8, '2024-05-14'::date, '24/302458', '24.268009.0001', NULL),
  ('E9',  1.8::float8, '2024-05-14'::date, '24/302459', '24.268010.0001', NULL),
  ('E10', 4.2::float8, '2024-05-14'::date, '24/302460', '24.268011.0001', 'ATTENZIONE – valore in fascia pre-allarme; verificare tenuta tessuti filtranti'),
  ('E11', 1.5::float8, '2024-05-14'::date, '24/302461', '24.268012.0001', NULL),
  ('E12', 2.2::float8, '2024-05-14'::date, '24/302462', '24.268013.0001', NULL),
  ('E13', 3.8::float8, '2024-05-14'::date, '24/302463', '24.268014.0001', NULL),
  ('E28', 0.6::float8, '2024-05-14'::date, '24/302464', '24.268015.0001', NULL),
  ('E29', 0.7::float8, '2024-05-14'::date, '24/302465', '24.268016.0001', NULL),
  -- ── PULITURA ───────────────────────────────────────────────────────
  ('E30', 3.5::float8, '2024-05-15'::date, '24/302466', '24.268017.0001', NULL),
  ('E31', 2.9::float8, '2024-05-15'::date, '24/302467', '24.268018.0001', NULL),
  ('E32', 4.1::float8, '2024-05-15'::date, '24/302468', '24.268019.0001', 'ATTENZIONE – programmata ispezione maniche filtro (scadenza manutenzione set-2024)'),
  ('E33', 2.2::float8, '2024-05-15'::date, '24/302469', '24.268020.0001', NULL),
  ('E34', 2.0::float8, '2024-05-15'::date, '24/302470', '24.268021.0001', NULL),
  ('E35', 2.8::float8, '2024-05-15'::date, '24/302471', '24.268022.0001', NULL),
  ('E36', 3.3::float8, '2024-05-15'::date, '24/302472', '24.268023.0001', NULL),
  ('E37', 1.9::float8, '2024-05-15'::date, '24/302473', '24.268024.0001', NULL),
  ('E38', 1.1::float8, '2024-05-15'::date, '24/302474', '24.268025.0001', NULL),
  -- ── MACINAZIONE ────────────────────────────────────────────────────
  ('E39', 3.7::float8, '2024-05-15'::date, '24/302475', '24.268026.0001', NULL),
  ('E40', 2.6::float8, '2024-05-15'::date, '24/302476', '24.268027.0001', NULL),
  ('E41', 4.3::float8, '2024-05-15'::date, '24/302477', '24.268028.0001', 'ATTENZIONE – monitorare; filtro E41 in programma revisione entro set-2024'),
  ('E42', 3.1::float8, '2024-05-15'::date, '24/302478', '24.268029.0001', NULL),
  ('E43', 2.9::float8, '2024-05-15'::date, '24/302479', '24.268030.0001', NULL),
  ('E44', 5.2::float8, '2024-05-15'::date, '24/302480', '24.268031.0001', 'NON CONFORME – superamento VLE 5 mg/Nm3 (104%); causa: anomalia filtro a maniche; sostituzione maniche eseguita 2024-06-03; misura di verifica pianificata'),
  ('E45', 2.4::float8, '2024-05-15'::date, '24/302481', '24.268032.0001', NULL),
  ('E46', 3.8::float8, '2024-05-15'::date, '24/302482', '24.268033.0001', NULL),
  ('E47', 3.5::float8, '2024-05-15'::date, '24/302483', '24.268034.0001', NULL),
  ('E48', 2.7::float8, '2024-05-15'::date, '24/302484', '24.268035.0001', NULL),
  ('E49', 3.2::float8, '2024-05-15'::date, '24/302485', '24.268036.0001', NULL),
  ('E50', 4.8::float8, '2024-05-15'::date, '24/302486', '24.268037.0001', 'ATTENZIONE – turbodecantatore ancora presente; sostituzione con filtro a maniche prevista entro fine 2024'),
  ('E51', 4.6::float8, '2024-05-15'::date, '24/302487', '24.268038.0001', 'ATTENZIONE – turbodecantatore ancora presente; sostituzione con filtro a maniche prevista entro fine 2024'),
  -- ── MOVIMENTAZIONE – scarico camion/vagoni (E14–E27) ───────────────
  ('E14', 1.5::float8, '2024-05-16'::date, '24/302490', '24.268040.0001', NULL),
  ('E15', 1.3::float8, '2024-05-16'::date, '24/302491', '24.268041.0001', NULL),
  ('E16', 1.8::float8, '2024-05-16'::date, '24/302492', '24.268042.0001', NULL),
  ('E17', 1.2::float8, '2024-05-16'::date, '24/302493', '24.268043.0001', NULL),
  ('E18', 1.6::float8, '2024-05-16'::date, '24/302494', '24.268044.0001', NULL),
  ('E19', 1.4::float8, '2024-05-16'::date, '24/302495', '24.268045.0001', NULL),
  ('E20', 1.7::float8, '2024-05-16'::date, '24/302496', '24.268046.0001', NULL),
  ('E21', 1.1::float8, '2024-05-16'::date, '24/302497', '24.268047.0001', NULL),
  ('E22', 2.0::float8, '2024-05-16'::date, '24/302498', '24.268048.0001', NULL),
  ('E23', 1.8::float8, '2024-05-16'::date, '24/302499', '24.268049.0001', NULL),
  ('E24', 2.2::float8, '2024-05-16'::date, '24/302500', '24.268050.0001', NULL),
  ('E25', 1.5::float8, '2024-05-16'::date, '24/302501', '24.268051.0001', NULL),
  ('E26', 1.9::float8, '2024-05-16'::date, '24/302502', '24.268052.0001', NULL),
  ('E27', 2.1::float8, '2024-05-16'::date, '24/302503', '24.268053.0001', NULL),
  -- ── MOVIMENTAZIONE – silos semola/rinfusa/farina (E53–E90 annuali) ─
  ('E53', 1.8::float8, '2024-05-16'::date, '24/302505', '24.268055.0001', NULL),
  ('E54', 1.6::float8, '2024-05-16'::date, '24/302506', '24.268056.0001', NULL),
  ('E55', 1.7::float8, '2024-05-16'::date, '24/302507', '24.268057.0001', NULL),
  ('E56', 2.0::float8, '2024-05-16'::date, '24/302508', '24.268058.0001', NULL),
  ('E57', 1.4::float8, '2024-05-16'::date, '24/302509', '24.268059.0001', NULL),
  ('E58', 1.3::float8, '2024-05-16'::date, '24/302510', '24.268060.0001', NULL),
  ('E59', 1.5::float8, '2024-05-16'::date, '24/302511', '24.268061.0001', NULL),
  ('E60', 1.2::float8, '2024-05-16'::date, '24/302512', '24.268062.0001', NULL),
  ('E61', 1.6::float8, '2024-05-16'::date, '24/302513', '24.268063.0001', NULL),
  ('E62', 1.4::float8, '2024-05-16'::date, '24/302514', '24.268064.0001', NULL),
  ('E63', 1.5::float8, '2024-05-16'::date, '24/302515', '24.268065.0001', NULL),
  ('E64', 3.2::float8, '2024-05-16'::date, '24/302516', '24.268066.0001', NULL),
  ('E65', 3.0::float8, '2024-05-16'::date, '24/302517', '24.268067.0001', NULL),
  ('E67', 2.8::float8, '2024-05-16'::date, '24/302518', '24.268068.0001', NULL),
  ('E68', 2.5::float8, '2024-05-16'::date, '24/302519', '24.268069.0001', NULL),
  ('E69', 2.7::float8, '2024-05-16'::date, '24/302520', '24.268070.0001', NULL),
  ('E70', 2.6::float8, '2024-05-16'::date, '24/302521', '24.268071.0001', NULL),
  ('E71', 2.9::float8, '2024-05-16'::date, '24/302522', '24.268072.0001', NULL),
  ('E72', 3.1::float8, '2024-05-16'::date, '24/302523', '24.268073.0001', NULL),
  ('E73', 1.5::float8, '2024-05-16'::date, '24/302524', '24.268074.0001', NULL),
  ('E74', 1.7::float8, '2024-05-16'::date, '24/302525', '24.268075.0001', NULL),
  ('E75', 1.6::float8, '2024-05-16'::date, '24/302526', '24.268076.0001', NULL),
  ('E76', 1.8::float8, '2024-05-16'::date, '24/302527', '24.268077.0001', NULL),
  ('E77', 1.4::float8, '2024-05-16'::date, '24/302528', '24.268078.0001', NULL),
  ('E78', 1.5::float8, '2024-05-16'::date, '24/302529', '24.268079.0001', NULL),
  ('E79', 1.6::float8, '2024-05-16'::date, '24/302530', '24.268080.0001', NULL),
  ('E80', 1.3::float8, '2024-05-16'::date, '24/302531', '24.268081.0001', NULL),
  ('E81', 0.9::float8, '2024-05-16'::date, '24/302532', '24.268082.0001', NULL),
  ('E82', 0.8::float8, '2024-05-16'::date, '24/302533', '24.268083.0001', NULL),
  ('E83', 1.4::float8, '2024-05-16'::date, '24/302534', '24.268084.0001', NULL),
  ('E84', 1.5::float8, '2024-05-16'::date, '24/302535', '24.268085.0001', NULL),
  ('E85', 1.2::float8, '2024-05-16'::date, '24/302536', '24.268086.0001', NULL),
  ('E86', 0.8::float8, '2024-05-16'::date, '24/302537', '24.268087.0001', NULL),
  ('E87', 0.9::float8, '2024-05-16'::date, '24/302538', '24.268088.0001', NULL),
  ('E88', 2.8::float8, '2024-05-16'::date, '24/302539', '24.268089.0001', NULL),
  ('E89', 3.0::float8, '2024-05-16'::date, '24/302540', '24.268090.0001', NULL),
  ('E90', 2.9::float8, '2024-05-16'::date, '24/302541', '24.268091.0001', NULL)
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   verbale_campionamento, note, created_at)
SELECT
  pm.id,
  mv.data_camp,
  mv.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN mv.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN mv.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((mv.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  mv.rapporto,
  mv.n_acc,
  'Chelab S.r.l.',
  'TECNICO_LAB',
  'AM240035/01',
  mv.nota_campo,
  CURRENT_TIMESTAMP
FROM misure_aria_gmi mv
JOIN monitoraggi mon ON mon.codice = mv.codice AND mon.stabilimento_id = 1
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = 'Polveri totali' AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 2: E97 SALDATURA – GMI LIVORNO
-- Unico controllo analitico previsto entro 31/12/2024 (prescrizione ATM-05)
-- Data: 2024-11-20 – Lab: Chelab S.r.l.
-- ══════════════════════════════════════════════════════════════════

WITH e97_vals (param_nome, valore, rapporto, n_acc) AS (VALUES
  ('Polveri totali', 1.8::float8,  '24/612340', '24.312001.0001'),
  ('Cromo (Cr)',     0.006::float8, '24/612340', '24.312001.0001'),
  ('Nichel (Ni)',    0.004::float8, '24/612340', '24.312001.0001'),
  ('Cadmio (Cd)',    0.001::float8, '24/612340', '24.312001.0001'),
  ('Cobalto (Co)',   0.001::float8, '24/612340', '24.312001.0001'),
  ('Piombo (Pb)',    0.003::float8, '24/612340', '24.312001.0001'),
  ('Stagno (Sn)',    0.012::float8, '24/612340', '24.312001.0001')
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   verbale_campionamento, note, created_at)
SELECT
  pm.id,
  '2024-11-20'::date,
  ev.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN ev.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN ev.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((ev.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  ev.rapporto,
  ev.n_acc,
  'Chelab S.r.l.',
  'TECNICO_ARPAV',
  'AM240128/01',
  'Unico controllo analitico ex prescrizione ATM-05 (scadenza 31/12/2024) – tutti i parametri conformi',
  CURRENT_TIMESTAMP
FROM e97_vals ev
JOIN monitoraggi mon ON mon.codice = 'E97' AND mon.stabilimento_id = 1
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = ev.param_nome AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 3: MONITORAGGIO MENSILE – E66, E91÷E96 (GMI LIVORNO)
-- Controllo differenziale di pressione (deprimometri) – 12 mesi 2024
-- VLE 3 mg/Nm3; valori tutti entro fascia CONFORME (≤ 2.4 mg/Nm3)
-- ══════════════════════════════════════════════════════════════════

WITH monthly_vals AS (
  SELECT
    mon.id AS mon_id,
    mon.codice,
    gs.mese,
    ('2024-' || LPAD(gs.mese::text, 2, '0') || '-15')::date AS data_camp,
    ROUND((
      -- Valore base stagionale (estate più alto)
      CASE gs.mese
        WHEN 1  THEN 0.80 WHEN 2  THEN 0.75 WHEN 3  THEN 0.90
        WHEN 4  THEN 1.05 WHEN 5  THEN 1.20 WHEN 6  THEN 1.55
        WHEN 7  THEN 1.45 WHEN 8  THEN 1.30 WHEN 9  THEN 1.10
        WHEN 10 THEN 1.15 WHEN 11 THEN 0.95 WHEN 12 THEN 0.80
        ELSE 1.0
      END +
      -- Piccolo offset per differenziare i camini
      CASE mon.codice
        WHEN 'E66' THEN  0.00 WHEN 'E91' THEN  0.10
        WHEN 'E92' THEN  0.05 WHEN 'E93' THEN -0.05
        WHEN 'E94' THEN  0.15 WHEN 'E95' THEN -0.10
        WHEN 'E96' THEN  0.08 ELSE 0.00
      END
    )::numeric, 2)::float8 AS valore
  FROM generate_series(1, 12) AS gs(mese)
  CROSS JOIN monitoraggi mon
  WHERE mon.stabilimento_id = 1
    AND mon.codice IN ('E66','E91','E92','E93','E94','E95','E96')
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, laboratorio, campionato_da, note, created_at)
SELECT
  pm.id,
  mv.data_camp,
  mv.valore,
  pm.unita_misura,
  CASE
    WHEN mv.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN mv.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  ROUND((mv.valore / pm.limite_valore * 100)::numeric, 1)::float8,
  'Autocontrollo-' || TO_CHAR(mv.data_camp, 'YYYY-MM'),
  'Autocontrollo interno GMI',
  'CLIENTE',
  'Misura differenziale di pressione mensile – verifica range buon funzionamento filtro a maniche',
  CURRENT_TIMESTAMP
FROM monthly_vals mv
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mv.mon_id AND pm.nome = 'Polveri totali' AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 4: SCARICHI IDRICI S1 e S2 – GMI LIVORNO
-- 2 campagne 2024: primavera (26/03) e autunno (16/10)
-- Lab: Chelab S.r.l.
-- VLE: D.Lgs. 152/2006 All.5 Tab.3 (scarico in acque superficiali)
-- ══════════════════════════════════════════════════════════════════

WITH scarichi_vals
  (scarico, param, valore, data_camp, rapporto, n_acc) AS (VALUES
  -- ── S1 campagna marzo ──────────────────────────────────────────
  ('S1', 'pH',                 7.4::float8,  '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'COD',                45.0::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'BOD5',               12.0::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Solidi Sospesi',     18.0::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Azoto ammoniacale',   2.1::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Azoto totale',        3.5::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Fosforo totale',      1.2::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Tensioattivi totali', 0.3::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  ('S1', 'Oli e grassi',        2.0::float8, '2024-03-26'::date, '24/213870', '24.233534.0001'),
  -- ── S1 campagna ottobre ─────────────────────────────────────────
  ('S1', 'pH',                 7.2::float8,  '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'COD',                52.0::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'BOD5',               14.0::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Solidi Sospesi',     22.0::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Azoto ammoniacale',   2.4::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Azoto totale',        4.1::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Fosforo totale',      1.5::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Tensioattivi totali', 0.4::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  ('S1', 'Oli e grassi',        2.5::float8, '2024-10-16'::date, '24/655854', '24.291088.0001'),
  -- ── S2 campagna marzo ──────────────────────────────────────────
  ('S2', 'pH',                 7.3::float8,  '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'COD',                38.0::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'BOD5',                9.0::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Solidi Sospesi',     14.0::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Azoto ammoniacale',   1.8::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Azoto totale',        2.9::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Fosforo totale',      0.9::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Tensioattivi totali', 0.2::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  ('S2', 'Oli e grassi',        1.5::float8, '2024-03-26'::date, '24/213871', '24.233535.0001'),
  -- ── S2 campagna ottobre ─────────────────────────────────────────
  ('S2', 'pH',                 7.5::float8,  '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'COD',                44.0::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'BOD5',               11.0::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Solidi Sospesi',     16.0::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Azoto ammoniacale',   2.0::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Azoto totale',        3.2::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Fosforo totale',      1.1::float8, '2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Tensioattivi totali', 0.25::float8,'2024-10-16'::date, '24/655855', '24.291089.0001'),
  ('S2', 'Oli e grassi',        1.8::float8, '2024-10-16'::date, '24/655855', '24.291089.0001')
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   note, created_at)
SELECT
  pm.id,
  sv.data_camp,
  sv.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN sv.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN sv.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((sv.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  sv.rapporto,
  sv.n_acc,
  'Chelab S.r.l.',
  'TECNICO_LAB',
  'Campionamento scarichi idrici PMC 2024 – ' || sv.scarico,
  CURRENT_TIMESTAMP
FROM scarichi_vals sv
JOIN monitoraggi mon ON mon.codice = sv.scarico AND mon.stabilimento_id = 1
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = sv.param AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 5: PIEZOMETRI PZ1÷PZ4 – GMI LIVORNO
-- 2 campagne 2024: aprile (08/04) e ottobre (07/10)
-- Lab: Chelab S.r.l.
-- VLE: D.Lgs. 152/2006 All.3 Tab.2 (acque sotterranee)
-- Manganese in zona ATTENZIONE (origine geologica accertata)
-- ══════════════════════════════════════════════════════════════════

WITH pz_vals
  (piezometro, param, val_apr, val_ott, rapporto_apr, rapporto_ott) AS (VALUES
  ('PZ1', 'pH',                7.8::float8,  7.6::float8,  '24/198710', '24/642200'),
  ('PZ1', 'Conduttivita',    650.0::float8, 680.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Alluminio',        45.0::float8,  52.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Arsenico',          5.2::float8,   5.8::float8, '24/198710', '24/642200'),
  ('PZ1', 'Cadmio',            1.1::float8,   1.2::float8, '24/198710', '24/642200'),
  ('PZ1', 'Cromo totale',      8.5::float8,   9.1::float8, '24/198710', '24/642200'),
  ('PZ1', 'Cromo VI',          0.3::float8,   0.4::float8, '24/198710', '24/642200'),
  ('PZ1', 'Ferro',            95.0::float8, 112.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Mercurio',          0.08::float8,  0.09::float8,'24/198710', '24/642200'),
  ('PZ1', 'Nichel',            6.2::float8,   7.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Piombo',            3.1::float8,   3.5::float8, '24/198710', '24/642200'),
  ('PZ1', 'Rame',             12.0::float8,  14.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Manganese',        42.0::float8,  38.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Zinco',            85.0::float8,  92.0::float8, '24/198710', '24/642200'),
  ('PZ1', 'Benzene',           0.05::float8,  0.05::float8,'24/198710', '24/642200'),
  ('PZ1', 'Tricloroetilene',   0.08::float8,  0.08::float8,'24/198710', '24/642200'),
  ('PZ1', 'Tetracloroetilene', 0.06::float8,  0.06::float8,'24/198710', '24/642200'),
  ('PZ1', 'IPA totali',        0.01::float8,  0.01::float8,'24/198710', '24/642200'),
  -- PZ2
  ('PZ2', 'pH',                7.9::float8,  7.7::float8,  '24/198711', '24/642201'),
  ('PZ2', 'Conduttivita',    620.0::float8, 645.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Alluminio',        38.0::float8,  44.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Arsenico',          4.8::float8,   5.1::float8, '24/198711', '24/642201'),
  ('PZ2', 'Cadmio',            0.9::float8,   1.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Cromo totale',      7.2::float8,   7.8::float8, '24/198711', '24/642201'),
  ('PZ2', 'Cromo VI',          0.2::float8,   0.3::float8, '24/198711', '24/642201'),
  ('PZ2', 'Ferro',            88.0::float8,  98.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Mercurio',          0.07::float8,  0.08::float8,'24/198711', '24/642201'),
  ('PZ2', 'Nichel',            5.8::float8,   6.2::float8, '24/198711', '24/642201'),
  ('PZ2', 'Piombo',            2.8::float8,   3.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Rame',             10.0::float8,  11.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Manganese',        44.0::float8,  40.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Zinco',            78.0::float8,  84.0::float8, '24/198711', '24/642201'),
  ('PZ2', 'Benzene',           0.05::float8,  0.05::float8,'24/198711', '24/642201'),
  ('PZ2', 'Tricloroetilene',   0.08::float8,  0.08::float8,'24/198711', '24/642201'),
  ('PZ2', 'Tetracloroetilene', 0.06::float8,  0.06::float8,'24/198711', '24/642201'),
  ('PZ2', 'IPA totali',        0.01::float8,  0.01::float8,'24/198711', '24/642201'),
  -- PZ3
  ('PZ3', 'pH',                7.7::float8,  7.5::float8,  '24/198712', '24/642202'),
  ('PZ3', 'Conduttivita',    640.0::float8, 660.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Alluminio',        50.0::float8,  58.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Arsenico',          6.1::float8,   6.5::float8, '24/198712', '24/642202'),
  ('PZ3', 'Cadmio',            1.3::float8,   1.4::float8, '24/198712', '24/642202'),
  ('PZ3', 'Cromo totale',      9.8::float8,  10.2::float8, '24/198712', '24/642202'),
  ('PZ3', 'Cromo VI',          0.4::float8,   0.4::float8, '24/198712', '24/642202'),
  ('PZ3', 'Ferro',           105.0::float8, 118.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Mercurio',          0.09::float8,  0.10::float8,'24/198712', '24/642202'),
  ('PZ3', 'Nichel',            7.0::float8,   7.5::float8, '24/198712', '24/642202'),
  ('PZ3', 'Piombo',            3.5::float8,   3.8::float8, '24/198712', '24/642202'),
  ('PZ3', 'Rame',             13.0::float8,  15.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Manganese',        41.0::float8,  36.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Zinco',            90.0::float8,  96.0::float8, '24/198712', '24/642202'),
  ('PZ3', 'Benzene',           0.05::float8,  0.05::float8,'24/198712', '24/642202'),
  ('PZ3', 'Tricloroetilene',   0.08::float8,  0.08::float8,'24/198712', '24/642202'),
  ('PZ3', 'Tetracloroetilene', 0.06::float8,  0.06::float8,'24/198712', '24/642202'),
  ('PZ3', 'IPA totali',        0.01::float8,  0.01::float8,'24/198712', '24/642202'),
  -- PZ4
  ('PZ4', 'pH',                8.0::float8,  7.8::float8,  '24/198713', '24/642203'),
  ('PZ4', 'Conduttivita',    635.0::float8, 658.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Alluminio',        42.0::float8,  48.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Arsenico',          5.5::float8,   5.9::float8, '24/198713', '24/642203'),
  ('PZ4', 'Cadmio',            1.0::float8,   1.1::float8, '24/198713', '24/642203'),
  ('PZ4', 'Cromo totale',      8.0::float8,   8.8::float8, '24/198713', '24/642203'),
  ('PZ4', 'Cromo VI',          0.3::float8,   0.3::float8, '24/198713', '24/642203'),
  ('PZ4', 'Ferro',            90.0::float8, 105.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Mercurio',          0.08::float8,  0.09::float8,'24/198713', '24/642203'),
  ('PZ4', 'Nichel',            6.0::float8,   6.5::float8, '24/198713', '24/642203'),
  ('PZ4', 'Piombo',            3.0::float8,   3.3::float8, '24/198713', '24/642203'),
  ('PZ4', 'Rame',             11.0::float8,  12.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Manganese',        43.0::float8,  39.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Zinco',            82.0::float8,  89.0::float8, '24/198713', '24/642203'),
  ('PZ4', 'Benzene',           0.05::float8,  0.05::float8,'24/198713', '24/642203'),
  ('PZ4', 'Tricloroetilene',   0.08::float8,  0.08::float8,'24/198713', '24/642203'),
  ('PZ4', 'Tetracloroetilene', 0.06::float8,  0.06::float8,'24/198713', '24/642203'),
  ('PZ4', 'IPA totali',        0.01::float8,  0.01::float8,'24/198713', '24/642203')
),
-- Trasformiamo la tabella pivot (val_apr, val_ott) in righe separate
pz_unpivot AS (
  SELECT piezometro, param, val_apr AS valore, '2024-04-08'::date AS data_camp, rapporto_apr AS rapporto
  FROM pz_vals
  UNION ALL
  SELECT piezometro, param, val_ott AS valore, '2024-10-07'::date AS data_camp, rapporto_ott AS rapporto
  FROM pz_vals
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, laboratorio, campionato_da,
   note, created_at)
SELECT
  pm.id,
  pu.data_camp,
  pu.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN pu.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN pu.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((pu.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  pu.rapporto,
  'Chelab S.r.l.',
  'TECNICO_LAB',
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pu.valore > pm.limite_valore * 0.80
      AND pu.valore <= pm.limite_valore
    THEN 'Manganese elevato – confermata origine geologica naturale (falda alluvionale); situazione sotto controllo'
  END,
  CURRENT_TIMESTAMP
FROM pz_unpivot pu
JOIN monitoraggi mon ON mon.codice = pu.piezometro AND mon.stabilimento_id = 1
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = pu.param AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 6: EMISSIONI ATMOSFERA – PROMOLOG (stabilimento 2)
-- Campagna annuale PMC 2024 – Chelab S.r.l.
-- Campionamento: 11 giugno 2024
-- ══════════════════════════════════════════════════════════════════

WITH misure_aria_pro (codice, valore, rapporto, n_acc) AS (VALUES
  ('E1', 2.8::float8, '24/348210', '24.281001.0001'),
  ('E2', 1.9::float8, '24/348211', '24.281002.0001'),
  ('E3', 3.4::float8, '24/348212', '24.281003.0001'),
  ('E4', 2.1::float8, '24/348213', '24.281004.0001')
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   verbale_campionamento, note, created_at)
SELECT
  pm.id,
  '2024-06-11'::date,
  mp.valore,
  pm.unita_misura,
  CASE
    WHEN mp.valore <= pm.limite_valore * 0.80 THEN 'CONFORME'
    WHEN mp.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  ROUND((mp.valore / pm.limite_valore * 100)::numeric, 1)::float8,
  mp.rapporto,
  mp.n_acc,
  'Chelab S.r.l.',
  'TECNICO_LAB',
  'AM240062/01',
  'Campagna PMC 2024 – tutti i valori conformi al VLE BAT 28 FDM 2019',
  CURRENT_TIMESTAMP
FROM misure_aria_pro mp
JOIN monitoraggi mon ON mon.codice = mp.codice AND mon.stabilimento_id = 2
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = 'Polveri totali' AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 7: ACQUE METEORICHE SF2 – PROMOLOG
-- 2 campagne 2024 allineate agli effettivi RdP Chelab archiviati:
--   • 26/03/2024 – RdP n.24/000213869 (n.acc. 24.233533.0001)
--   • 16/10/2024 – RdP n.24/000655853 (n.acc. 24.291087.0001)
-- ══════════════════════════════════════════════════════════════════

WITH sf2_vals (param, val_mar, val_ott) AS (VALUES
  ('COD',               35.0::float8,  28.0::float8),
  ('Solidi Sospesi',    22.0::float8,  16.0::float8),
  ('Idrocarburi totali', 2.1::float8,   1.8::float8),
  ('pH',                 7.6::float8,   7.4::float8),
  ('Azoto ammoniacale',  0.8::float8,   0.6::float8),
  ('Oli e grassi',       1.5::float8,   1.2::float8)
),
sf2_unpivot AS (
  SELECT param, val_mar AS valore, '2024-03-26'::date AS data_camp,
         '24/000213869' AS rapporto, '24.233533.0001' AS n_acc
  FROM sf2_vals
  UNION ALL
  SELECT param, val_ott AS valore, '2024-10-16'::date AS data_camp,
         '24/000655853' AS rapporto, '24.291087.0001' AS n_acc
  FROM sf2_vals
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   note, created_at)
SELECT
  pm.id,
  su.data_camp,
  su.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN su.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN su.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((su.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  su.rapporto,
  su.n_acc,
  'Chelab S.r.l. (LAB N° 0051 L)',
  'TECNICO_LAB',
  'Acque meteoriche SF2 – rapporto accreditato ACCREDIA',
  CURRENT_TIMESTAMP
FROM sf2_unpivot su
JOIN monitoraggi mon ON mon.codice = 'SF2' AND mon.stabilimento_id = 2
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = su.param AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- SEZIONE 8: SCARICO CIVILE SF1 – PROMOLOG
-- 1 campagna 2024: 15 luglio 2024
-- Lab: Chelab S.r.l.
-- ══════════════════════════════════════════════════════════════════

WITH sf1_vals (param, valore) AS (VALUES
  ('COD',            42.0::float8),
  ('BOD5',           11.0::float8),
  ('Solidi Sospesi', 17.0::float8),
  ('pH',              7.5::float8),
  ('Azoto totale',    3.8::float8),
  ('Fosforo totale',  1.1::float8),
  ('Tensioattivi',    0.28::float8)
)
INSERT INTO rilevazioni_misura
  (parametro_monitoraggio_id, data_campionamento, valore_misurato,
   unita_misura, stato_conformita, discostamento_percentuale,
   rapporto_prova, numero_accettazione, laboratorio, campionato_da,
   note, created_at)
SELECT
  pm.id,
  '2024-07-15'::date,
  sv.valore,
  pm.unita_misura,
  CASE
    WHEN pm.limite_valore IS NULL              THEN NULL
    WHEN sv.valore <= pm.limite_valore * 0.80  THEN 'CONFORME'
    WHEN sv.valore <= pm.limite_valore          THEN 'ATTENZIONE'
    ELSE                                             'NON_CONFORME'
  END,
  CASE
    WHEN pm.limite_valore IS NOT NULL AND pm.limite_valore > 0
    THEN ROUND((sv.valore / pm.limite_valore * 100)::numeric, 1)::float8
  END,
  '24/421050',
  '24.258001.0001',
  'Chelab S.r.l.',
  'TECNICO_LAB',
  'Scarico civile SF1 – campagna annuale PMC 2024 – tutti i parametri conformi',
  CURRENT_TIMESTAMP
FROM sf1_vals sv
JOIN monitoraggi mon ON mon.codice = 'SF1' AND mon.stabilimento_id = 2
JOIN parametri_monitoraggio pm
  ON pm.monitoraggio_id = mon.id AND pm.nome = sv.param AND pm.attivo = TRUE;


-- ══════════════════════════════════════════════════════════════════
-- RESET SEQUENZE
-- ══════════════════════════════════════════════════════════════════
SELECT setval(
  pg_get_serial_sequence('rilevazioni_misura', 'id'),
  COALESCE((SELECT MAX(id) FROM rilevazioni_misura), 1)
);
SELECT setval(
  pg_get_serial_sequence('monitoraggi', 'id'),
  COALESCE((SELECT MAX(id) FROM monitoraggi), 1)
);
SELECT setval(
  pg_get_serial_sequence('parametri_monitoraggio', 'id'),
  COALESCE((SELECT MAX(id) FROM parametri_monitoraggio), 1)
);


-- ══════════════════════════════════════════════════════════════════
-- VERIFICA FINALE
-- ══════════════════════════════════════════════════════════════════
SELECT
  s.nome                                            AS stabilimento,
  mon.tipo_monitoraggio                             AS tipo,
  COUNT(r.id)                                       AS n_rilevazioni,
  SUM(CASE WHEN r.stato_conformita = 'CONFORME'     THEN 1 ELSE 0 END) AS conformi,
  SUM(CASE WHEN r.stato_conformita = 'ATTENZIONE'   THEN 1 ELSE 0 END) AS attenzione,
  SUM(CASE WHEN r.stato_conformita = 'NON_CONFORME' THEN 1 ELSE 0 END) AS non_conformi,
  SUM(CASE WHEN r.stato_conformita IS NULL          THEN 1 ELSE 0 END) AS senza_limite
FROM rilevazioni_misura r
JOIN parametri_monitoraggio pm ON pm.id = r.parametro_monitoraggio_id
JOIN monitoraggi mon ON mon.id = pm.monitoraggio_id
JOIN stabilimenti s ON s.id = mon.stabilimento_id
GROUP BY s.nome, mon.tipo_monitoraggio
ORDER BY s.nome, mon.tipo_monitoraggio;

COMMIT;
