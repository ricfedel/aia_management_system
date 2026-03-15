-- =============================================================================
-- SEED: registri_mensili + voci_produzione
-- =============================================================================
-- Stabilimento 1 = GMI S.p.A. – Livorno  (id=1, mulino grande)
-- Stabilimento 2 = Promolog S.r.l. – Coriano Veronese (id=2, mulino medio)
--
-- Anni coperti:
--   2025 – tutti e 12 i mesi, stato APPROVATO
--   2026 – gen/feb INVIATO, mar BOZZA
--
-- Idempotente: usa ON CONFLICT DO NOTHING su (stabilimento_id, anno, mese).
-- Le voci usano subquery per trovare il registro_mensile_id corretto.
-- =============================================================================

BEGIN;

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. REGISTRI MENSILI
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO registri_mensili (stabilimento_id, anno, mese, stato, compilato_da, created_at, updated_at)
VALUES
  -- GMI Livorno – 2025
  (1, 2025,  1, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  2, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  3, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  4, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  5, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  6, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  7, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  8, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025,  9, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025, 10, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025, 11, 'APPROVATO', 'sistema', NOW(), NOW()),
  (1, 2025, 12, 'APPROVATO', 'sistema', NOW(), NOW()),
  -- GMI Livorno – 2026
  (1, 2026,  1, 'INVIATO',   'sistema', NOW(), NOW()),
  (1, 2026,  2, 'INVIATO',   'sistema', NOW(), NOW()),
  (1, 2026,  3, 'BOZZA',     'sistema', NOW(), NOW()),
  -- Promolog Coriano – 2025
  (2, 2025,  1, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  2, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  3, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  4, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  5, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  6, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  7, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  8, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025,  9, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025, 10, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025, 11, 'APPROVATO', 'sistema', NOW(), NOW()),
  (2, 2025, 12, 'APPROVATO', 'sistema', NOW(), NOW()),
  -- Promolog Coriano – 2026
  (2, 2026,  1, 'INVIATO',   'sistema', NOW(), NOW()),
  (2, 2026,  2, 'INVIATO',   'sistema', NOW(), NOW()),
  (2, 2026,  3, 'BOZZA',     'sistema', NOW(), NOW())
ON CONFLICT ON CONSTRAINT uq_registro_stabilimento_anno_mese DO NOTHING;

-- ─────────────────────────────────────────────────────────────────────────────
-- Helper: inserisce le voci di un singolo registro.
-- Ogni blocco identifica il registro con (stabilimento_id, anno, mese).
-- ON CONFLICT su (registro_mensile_id, categoria, descrizione) è gestito
-- con NOT EXISTS per evitare duplicati su re-run.
-- ─────────────────────────────────────────────────────────────────────────────

-- =============================================================================
-- GMI LIVORNO (stabilimento_id = 1)
-- Valori mensili con variazione stagionale:
--   inverno (gen/feb/nov/dic): +10-12%  estate (lug/ago): -8-10%
-- =============================================================================

-- ── GMI 2025 gen ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',  10190.0, 't',   9680.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4375.0, 't',   4156.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9533.0, 't',   9056.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  4082.0, 't',   3878.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1110.0, 'm³',  1055.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 789000.0,'kWh',749550.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  80080.0,'Nm³', 76076.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3270.0, 'l',   3107.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 1
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 feb ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9936.0, 't',   9439.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4264.0, 't',   4051.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9297.0, 't',   8832.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3983.0, 't',   3784.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1082.0, 'm³',  1028.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 769050.0,'kWh',730598.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  74750.0,'Nm³', 71013.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3190.0, 'l',   3031.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 2
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 mar ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9384.0, 't',   8915.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4141.0, 't',   3934.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8781.0, 't',   8342.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3865.0, 't',   3672.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1022.0, 'm³',   971.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 726750.0,'kWh',690413.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  60320.0,'Nm³', 57304.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3016.0, 'l',   2865.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 3
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 apr ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9016.0, 't',   8565.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4018.0, 't',   3817.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8438.0, 't',   8016.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3744.0, 't',   3557.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   981.0, 'm³',   932.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 698250.0,'kWh',663338.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  43160.0,'Nm³', 41002.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2900.0, 'l',   2755.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 4
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 mag ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   8832.0, 't',   8390.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   3977.0, 't',   3778.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8266.0, 't',   7853.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3706.0, 't',   3521.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   960.0, 'm³',   912.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 684750.0,'kWh',650513.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  34060.0,'Nm³', 32357.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2842.0, 'l',   2700.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 5
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 giu ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   8648.0, 't',   8216.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   3936.0, 't',   3739.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8094.0, 't',   7689.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3668.0, 't',   3485.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   940.0, 'm³',   893.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 671250.0,'kWh',637688.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  27820.0,'Nm³', 26429.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2784.0, 'l',   2645.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 6
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 lug ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   8464.0, 't',   8041.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   3895.0, 't',   3700.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  7922.0, 't',   7526.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3630.0, 't',   3449.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   919.0, 'm³',   873.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 657750.0,'kWh',624863.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  24700.0,'Nm³', 23465.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2726.0, 'l',   2590.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 7
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 ago ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   8740.0, 't',   8303.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   3977.0, 't',   3778.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8180.0, 't',   7771.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3706.0, 't',   3521.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   950.0, 'm³',   903.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 678375.0,'kWh',644456.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  28080.0,'Nm³', 26676.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2813.0, 'l',   2672.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 8
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 set ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9200.0, 't',   8740.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4100.0, 't',   3895.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8600.0, 't',   8170.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3850.0, 't',   3658.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',   980.0, 'm³',   931.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 715000.0,'kWh',679250.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  37700.0,'Nm³', 35815.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2900.0, 'l',   2755.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 9
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 ott ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9476.0, 't',   9002.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4182.0, 't',   3973.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  8858.0, 't',   8415.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  3965.0, 't',   3767.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1009.0, 'm³',   959.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 734125.0,'kWh',697419.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  50050.0,'Nm³', 47548.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   2987.0, 'l',   2838.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 10
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 nov ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9936.0, 't',   9439.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4305.0, 't',   4090.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9298.0, 't',   8833.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  4043.0, 't',   3841.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1058.0, 'm³',  1005.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 769050.0,'kWh',730598.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  71760.0,'Nm³', 68172.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3161.0, 'l',   3003.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 11
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2025 dic ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',  10304.0, 't',   9789.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4428.0, 't',   4207.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9641.0, 't',   9159.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  4159.0, 't',   3951.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1108.0, 'm³',  1053.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 798750.0,'kWh',758813.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  82680.0,'Nm³', 78546.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3306.0, 'l',   3141.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2025 AND r.mese = 12
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2026 gen ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',  10465.0, 't',  10190.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4469.0, 't',   4375.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9790.0, 't',   9533.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  4159.0, 't',   4082.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1132.0, 'm³',  1110.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 805000.0,'kWh',789000.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  83200.0,'Nm³', 80080.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3306.0, 'l',   3270.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2026 AND r.mese = 1
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2026 feb ─────────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',  10212.0, 't',   9936.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4346.0, 't',   4264.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  9545.0, 't',   9297.0,  30),
  ('PRODUZIONE_OUTPUT','Semola di Grano Duro',   'SGD',  4062.0, 't',   3983.0,  40),
  ('ACQUA',            'M1 – Ingresso principale','M1',  1104.0, 'm³',  1082.0,  50),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE', 785250.0,'kWh',769050.0, 60),
  ('GAS_NATURALE',     'Metano – riscaldamento', 'GN',  77740.0,'Nm³', 74750.0,  70),
  ('GASOLIO',          'Gasolio – automezzi',    'GO',   3248.0, 'l',   3190.0,  80)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2026 AND r.mese = 2
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── GMI 2026 mar (BOZZA – solo alcune voci parzialmente compilate) ────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',           'GT',   9568.0, 't',   9384.0,  10),
  ('MATERIA_PRIMA',    'Grano Duro',             'GD',   4223.0, 't',   4141.0,  20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento T00', 'F00',  NULL,   't',   8781.0,  30),
  ('ENERGIA_ELETTRICA','Energia elettrica totale','EE',  NULL,  'kWh',726750.0,  60)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 1 AND r.anno = 2026 AND r.mese = 3
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);


-- =============================================================================
-- PROMOLOG CORIANO VERONESE (stabilimento_id = 2)
-- =============================================================================

-- ── Promolog 2025 gen ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4212.0, 't',   4001.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1228.0, 't',   1167.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3942.0, 't',   3745.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   314.0,'m³',    298.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',124150.0,'kWh',117943.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 24570.0,'Nm³', 23342.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   935.0, 'l',    888.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 1
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 feb ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4095.0, 't',   3890.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1196.0, 't',   1136.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3835.0, 't',   3643.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   306.0,'m³',    291.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',120750.0,'kWh',114713.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 22815.0,'Nm³', 21675.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   910.0, 'l',    865.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 2
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 mar ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3861.0, 't',   3668.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1127.0, 't',   1071.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3613.0, 't',   3432.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   289.0,'m³',    275.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',113850.0,'kWh',108158.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 18330.0,'Nm³', 17414.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   860.0, 'l',    817.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 3
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 apr ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3705.0, 't',   3520.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1081.0, 't',   1027.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3468.0, 't',   3295.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   278.0,'m³',    264.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',109250.0,'kWh',103788.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 14625.0,'Nm³', 13894.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   825.0, 'l',    784.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 4
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 mag ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3627.0, 't',   3446.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1058.0, 't',   1005.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3394.0, 't',   3224.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   272.0,'m³',    258.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',106925.0,'kWh',101579.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 11895.0,'Nm³', 11300.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   807.0, 'l',    767.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 5
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 giu ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3549.0, 't',   3372.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1035.0, 't',    983.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3321.0, 't',   3155.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   266.0,'m³',    253.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',104600.0,'kWh', 99370.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN',  9750.0,'Nm³',  9263.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   790.0, 'l',    751.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 6
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 lug ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3471.0, 't',   3297.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1012.0, 't',    961.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3248.0, 't',   3086.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   260.0,'m³',    247.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',102275.0,'kWh', 97161.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN',  8775.0,'Nm³',  8336.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   773.0, 'l',    734.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 7
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 ago ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3588.0, 't',   3409.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1047.0, 't',    995.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3358.0, 't',   3190.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   269.0,'m³',    256.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',105763.0,'kWh',100475.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 10140.0,'Nm³',  9633.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   799.0, 'l',    759.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 8
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 set ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3900.0, 't',   3705.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1150.0, 't',   1093.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3650.0, 't',   3468.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   290.0,'m³',    276.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',115000.0,'kWh',109250.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 13650.0,'Nm³', 12968.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   850.0, 'l',    808.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 9
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 ott ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4017.0, 't',   3816.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1173.0, 't',   1114.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3759.0, 't',   3571.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   299.0,'m³',    284.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',118350.0,'kWh',112433.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 17550.0,'Nm³', 16673.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   876.0, 'l',    832.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 10
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 nov ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4212.0, 't',   4001.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1219.0, 't',   1158.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3942.0, 't',   3745.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   314.0,'m³',    298.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',124150.0,'kWh',117943.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 23010.0,'Nm³', 21860.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   918.0, 'l',    872.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 11
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2025 dic ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4368.0, 't',   4150.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1265.0, 't',   1202.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  4086.0, 't',   3882.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   326.0,'m³',    310.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',128650.0,'kWh',122218.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 25350.0,'Nm³', 24083.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   952.0, 'l',    904.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2025 AND r.mese = 12
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2026 gen ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4296.0, 't',   4212.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1251.0, 't',   1228.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  4018.0, 't',   3942.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   320.0,'m³',    314.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',126475.0,'kWh',124150.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 24960.0,'Nm³', 24570.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   943.0, 'l',    935.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2026 AND r.mese = 1
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2026 feb ─────────────────────────────────────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  4178.0, 't',   4095.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1219.0, 't',   1196.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',  3911.0, 't',   3835.0, 30),
  ('ACQUA',            'M1 – Contatore principale', 'M1',   312.0,'m³',    306.0, 40),
  ('ENERGIA_ELETTRICA','Energia elettrica totale',  'EE',123163.0,'kWh',120750.0, 50),
  ('GAS_NATURALE',     'Gas naturale – processo',   'GN', 23205.0,'Nm³', 22815.0, 60),
  ('GASOLIO',          'Gasolio – carrelli',        'GO',   918.0, 'l',    910.0, 70)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2026 AND r.mese = 2
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ── Promolog 2026 mar (BOZZA – compilazione parziale) ─────────────────────────
INSERT INTO voci_produzione (registro_mensile_id, categoria, descrizione, codice, quantita, unita_misura, quantita_anno_precedente, sort_order, created_at)
SELECT r.id, v.categoria::VARCHAR, v.descrizione, v.codice, v.quantita, v.um, v.qap, v.ord, NOW()
FROM registri_mensili r
CROSS JOIN (VALUES
  ('MATERIA_PRIMA',    'Grano Tenero',              'GT',  3939.0, 't',   3861.0, 10),
  ('MATERIA_PRIMA',    'Mais',                      'MA',  1150.0, 't',   1127.0, 20),
  ('PRODUZIONE_OUTPUT','Farina di Frumento',        'FF',   NULL,  't',   3613.0, 30)
) AS v(categoria, descrizione, codice, quantita, um, qap, ord)
WHERE r.stabilimento_id = 2 AND r.anno = 2026 AND r.mese = 3
  AND NOT EXISTS (SELECT 1 FROM voci_produzione vp WHERE vp.registro_mensile_id = r.id AND vp.descrizione = v.descrizione);

-- ─────────────────────────────────────────────────────────────────────────────
-- Reset sequence (sicurezza dopo insert senza ID esplicito)
-- ─────────────────────────────────────────────────────────────────────────────
SELECT setval(
  pg_get_serial_sequence('registri_mensili', 'id'),
  COALESCE((SELECT MAX(id) FROM registri_mensili), 0) + 1,
  false
);
SELECT setval(
  pg_get_serial_sequence('voci_produzione', 'id'),
  COALESCE((SELECT MAX(id) FROM voci_produzione), 0) + 1,
  false
);

COMMIT;
