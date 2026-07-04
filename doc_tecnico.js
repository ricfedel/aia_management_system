const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign, PageNumber, PageBreak, LevelFormat,
  ExternalHyperlink, TableOfContents
} = require('docx');
const fs = require('fs');

// Colors
const BLUE_DARK = "1F3864";
const BLUE_MID  = "2E75B6";
const BLUE_LIGHT = "D5E8F0";
const GRAY_LIGHT = "F5F5F5";
const GRAY_MID  = "CCCCCC";
const GREEN     = "375623";
const GREEN_LIGHT = "E2EFDA";
const ORANGE    = "833C00";
const ORANGE_LIGHT = "FCE4D6";
const YELLOW_LIGHT = "FFF2CC";

// Helpers
const border = (color = GRAY_MID) => ({ style: BorderStyle.SINGLE, size: 1, color });
const borders = (color) => ({ top: border(color), bottom: border(color), left: border(color), right: border(color) });
const noBorders = () => ({ top: { style: BorderStyle.NONE }, bottom: { style: BorderStyle.NONE }, left: { style: BorderStyle.NONE }, right: { style: BorderStyle.NONE } });
const cellMargins = { top: 100, bottom: 100, left: 150, right: 150 };

function h(level, text, opts = {}) {
  return new Paragraph({
    heading: level,
    children: [new TextRun({ text, font: "Arial", bold: true, color: opts.color || BLUE_DARK, size: opts.size || (level === HeadingLevel.HEADING_1 ? 36 : level === HeadingLevel.HEADING_2 ? 28 : 24) })],
    spacing: { before: 300, after: 120 },
    ...opts
  });
}

function p(text, opts = {}) {
  return new Paragraph({
    children: [new TextRun({ text, font: "Arial", size: opts.size || 22, color: opts.color || "000000", bold: opts.bold || false, italics: opts.italics || false })],
    spacing: { before: opts.before || 60, after: opts.after || 100 },
    alignment: opts.alignment || AlignmentType.JUSTIFIED,
    ...opts
  });
}

function bullet(text, opts = {}) {
  return new Paragraph({
    numbering: { reference: "bullets", level: opts.level || 0 },
    children: [new TextRun({ text, font: "Arial", size: opts.size || 22, color: opts.color || "000000", bold: opts.bold || false })],
    spacing: { before: 40, after: 40 }
  });
}

function numbered(text, opts = {}) {
  return new Paragraph({
    numbering: { reference: "numbers", level: 0 },
    children: [new TextRun({ text, font: "Arial", size: opts.size || 22 })],
    spacing: { before: 40, after: 40 }
  });
}

function space(n = 1) {
  return new Paragraph({ children: [new TextRun("")], spacing: { before: 0, after: n * 80 } });
}

function divider(color = BLUE_MID) {
  return new Paragraph({
    children: [new TextRun("")],
    border: { bottom: { style: BorderStyle.SINGLE, size: 4, color } },
    spacing: { before: 100, after: 100 }
  });
}

function titleBox(text, subtitle) {
  const rows = [
    new TableRow({
      children: [new TableCell({
        borders: noBorders(),
        shading: { fill: BLUE_DARK, type: ShadingType.CLEAR },
        margins: { top: 400, bottom: 200, left: 400, right: 400 },
        width: { size: 9360, type: WidthType.DXA },
        children: [
          new Paragraph({
            children: [new TextRun({ text, font: "Arial", size: 52, bold: true, color: "FFFFFF" })],
            alignment: AlignmentType.CENTER,
            spacing: { before: 0, after: 120 }
          }),
          new Paragraph({
            children: [new TextRun({ text: subtitle, font: "Arial", size: 26, color: "BDD7EE" })],
            alignment: AlignmentType.CENTER,
            spacing: { before: 0, after: 0 }
          })
        ]
      })]
    })
  ];
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360], rows });
}

function infoBox(label, text, fillColor = BLUE_LIGHT, labelColor = BLUE_MID) {
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [1800, 7560],
    rows: [new TableRow({
      children: [
        new TableCell({
          borders: borders(labelColor),
          shading: { fill: labelColor, type: ShadingType.CLEAR },
          margins: cellMargins,
          width: { size: 1800, type: WidthType.DXA },
          verticalAlign: VerticalAlign.CENTER,
          children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: label, font: "Arial", size: 20, bold: true, color: "FFFFFF" })] })]
        }),
        new TableCell({
          borders: borders(labelColor),
          shading: { fill: fillColor, type: ShadingType.CLEAR },
          margins: cellMargins,
          width: { size: 7560, type: WidthType.DXA },
          children: [new Paragraph({ children: [new TextRun({ text, font: "Arial", size: 20 })] })]
        })
      ]
    })]
  });
}

function sectionBox(title, items, fillColor = BLUE_LIGHT, titleColor = BLUE_MID) {
  const rows = [
    new TableRow({
      children: [new TableCell({
        borders: borders(titleColor),
        shading: { fill: titleColor, type: ShadingType.CLEAR },
        margins: { top: 80, bottom: 80, left: 150, right: 150 },
        width: { size: 9360, type: WidthType.DXA },
        children: [new Paragraph({ children: [new TextRun({ text: title, font: "Arial", size: 22, bold: true, color: "FFFFFF" })] })]
      })]
    }),
    ...items.map(item => new TableRow({
      children: [new TableCell({
        borders: borders(GRAY_MID),
        shading: { fill: fillColor, type: ShadingType.CLEAR },
        margins: { top: 60, bottom: 60, left: 150, right: 150 },
        width: { size: 9360, type: WidthType.DXA },
        children: [new Paragraph({ children: [new TextRun({ text: "\u2022  " + item, font: "Arial", size: 21 })] })]
      })]
    }))
  ];
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360], rows });
}

function twoColTable(headers, rows, colWidths = [4680, 4680]) {
  const headerRow = new TableRow({
    children: headers.map((h, i) => new TableCell({
      borders: borders(BLUE_MID),
      shading: { fill: BLUE_MID, type: ShadingType.CLEAR },
      margins: cellMargins,
      width: { size: colWidths[i], type: WidthType.DXA },
      children: [new Paragraph({ children: [new TextRun({ text: h, font: "Arial", size: 21, bold: true, color: "FFFFFF" })] })]
    }))
  });
  const dataRows = rows.map((row, ri) => new TableRow({
    children: row.map((cell, ci) => new TableCell({
      borders: borders(GRAY_MID),
      shading: { fill: ri % 2 === 0 ? "FFFFFF" : GRAY_LIGHT, type: ShadingType.CLEAR },
      margins: cellMargins,
      width: { size: colWidths[ci], type: WidthType.DXA },
      children: [new Paragraph({ children: [new TextRun({ text: cell, font: "Arial", size: 20 })] })]
    }))
  }));
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: colWidths, rows: [headerRow, ...dataRows] });
}

function multiColTable(headers, rows, colWidths) {
  const headerRow = new TableRow({
    children: headers.map((h, i) => new TableCell({
      borders: borders(BLUE_MID),
      shading: { fill: BLUE_MID, type: ShadingType.CLEAR },
      margins: cellMargins,
      width: { size: colWidths[i], type: WidthType.DXA },
      children: [new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: h, font: "Arial", size: 20, bold: true, color: "FFFFFF" })] })]
    }))
  });
  const dataRows = rows.map((row, ri) => new TableRow({
    children: row.map((cell, ci) => new TableCell({
      borders: borders(GRAY_MID),
      shading: { fill: ri % 2 === 0 ? "FFFFFF" : GRAY_LIGHT, type: ShadingType.CLEAR },
      margins: cellMargins,
      width: { size: colWidths[ci], type: WidthType.DXA },
      children: [new Paragraph({ children: [new TextRun({ text: cell, font: "Arial", size: 20 })] })]
    }))
  }));
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: colWidths, rows: [headerRow, ...dataRows] });
}

function alertBox(emoji, title, text, fill = YELLOW_LIGHT, borderColor = "FFC000") {
  return new Table({
    width: { size: 9360, type: WidthType.DXA },
    columnWidths: [9360],
    rows: [new TableRow({
      children: [new TableCell({
        borders: borders(borderColor),
        shading: { fill, type: ShadingType.CLEAR },
        margins: { top: 100, bottom: 100, left: 180, right: 180 },
        width: { size: 9360, type: WidthType.DXA },
        children: [
          new Paragraph({ children: [new TextRun({ text: emoji + "  " + title, font: "Arial", size: 22, bold: true })] }),
          new Paragraph({ children: [new TextRun({ text, font: "Arial", size: 20 })], spacing: { before: 60 } })
        ]
      })]
    })]
  });
}

// ===================== DOCUMENT CONTENT =====================

const doc = new Document({
  numbering: {
    config: [
      { reference: "bullets", levels: [{ level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 300 } } } }, { level: 1, format: LevelFormat.BULLET, text: "\u25E6", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 900, hanging: 300 } } } }] },
      { reference: "numbers", levels: [{ level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 600, hanging: 300 } } } }] }
    ]
  },
  styles: {
    default: { document: { run: { font: "Arial", size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 36, bold: true, font: "Arial", color: BLUE_DARK },
        paragraph: { spacing: { before: 360, after: 180 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 28, bold: true, font: "Arial", color: BLUE_MID },
        paragraph: { spacing: { before: 240, after: 120 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true,
        run: { size: 24, bold: true, font: "Arial", color: BLUE_DARK },
        paragraph: { spacing: { before: 180, after: 80 }, outlineLevel: 2 } },
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 11906, height: 16838 },
        margin: { top: 1134, right: 1134, bottom: 1134, left: 1134 }
      }
    },
    headers: {
      default: new Header({
        children: [new Paragraph({
          children: [
            new TextRun({ text: "AIA Management System \u2014 Documento Tecnico Implementativo", font: "Arial", size: 18, color: "888888" }),
            new TextRun({ text: "\t\u2014 pag. ", font: "Arial", size: 18, color: "888888" }),
            new TextRun({ children: [PageNumber.CURRENT], font: "Arial", size: 18, color: "888888" })
          ],
          tabStops: [{ type: "right", position: 9026 }],
          border: { bottom: { style: BorderStyle.SINGLE, size: 2, color: BLUE_MID } },
          spacing: { after: 100 }
        })]
      })
    },
    children: [

      // ===== COPERTINA =====
      space(2),
      titleBox(
        "AIA Management System",
        "Integrazione DMS + BPMN \u2014 Documento Tecnico Implementativo"
      ),
      space(1),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [2260, 6766],
        rows: [
          new TableRow({ children: [
            new TableCell({ borders: noBorders(), width: { size: 2260, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "Versione", font: "Arial", size: 20, bold: true, color: BLUE_MID })] })] }),
            new TableCell({ borders: noBorders(), width: { size: 6766, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "1.0 \u2014 Febbraio 2026", font: "Arial", size: 20 })] })] })
          ]}),
          new TableRow({ children: [
            new TableCell({ borders: noBorders(), width: { size: 2260, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "Progetto", font: "Arial", size: 20, bold: true, color: BLUE_MID })] })] }),
            new TableCell({ borders: noBorders(), width: { size: 6766, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "Grandi Molini Italiani \u2014 Gestione AIA", font: "Arial", size: 20 })] })] })
          ]}),
          new TableRow({ children: [
            new TableCell({ borders: noBorders(), width: { size: 2260, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "Stato", font: "Arial", size: 20, bold: true, color: BLUE_MID })] })] }),
            new TableCell({ borders: noBorders(), width: { size: 6766, type: WidthType.DXA }, children: [new Paragraph({ children: [new TextRun({ text: "Proposta tecnica per approvazione", font: "Arial", size: 20 })] })] })
          ]}),
        ]
      }),
      space(1),
      divider(),
      new Paragraph({ children: [new PageBreak()] }),

      // ===== INDICE =====
      h(HeadingLevel.HEADING_1, "Indice"),
      new TableOfContents("Indice dei contenuti", { hyperlink: true, headingStyleRange: "1-3" }),
      new Paragraph({ children: [new PageBreak()] }),

      // ===== 1. EXECUTIVE SUMMARY =====
      h(HeadingLevel.HEADING_1, "1. Executive Summary"),
      p("Il sistema AIA Management attuale gestisce prescrizioni, scadenze e dati ambientali in modo efficace, ma due processi critici rimangono non coperti: la gestione documentale strutturata dei documenti in ingresso (comunicazioni da enti, provvedimenti AIA, richieste di integrazione) e l\u2019automazione dei workflow autorizzativi che da questi documenti scaturiscono."),
      space(),
      p("Questo documento propone l\u2019integrazione di due componenti open source nel progetto esistente:"),
      space(),
      twoColTable(
        ["Componente", "Scopo"],
        [
          ["Alfresco Community Edition (DMS)", "Archivio documentale strutturato con OCR, metadati, versioning e ricerca full-text"],
          ["Camunda 7 Community + bpmn-js", "Engine BPMN per workflow autorizzativi, embedded nel frontend Angular"],
        ],
        [3500, 5526]
      ),
      space(1),
      p("La soluzione \u00e8 tecnicamente fattibile, si integra con lo stack esistente (Spring Boot + Angular + PostgreSQL) e pu\u00f2 essere implementata in fasi incrementali senza interrompere il sistema in produzione."),
      space(),
      alertBox("\u2705", "Fattibilit\u00e0 complessiva: ALTA",
        "Tutti i componenti proposti hanno API REST mature, buona documentazione e comunity attive. L\u2019integrazione con lo stack Java/Angular esistente \u00e8 diretta e testata in numerosi progetti enterprise.",
        GREEN_LIGHT, "375623"),
      space(2),

      // ===== 2. ANALISI DEL PROBLEMA =====
      h(HeadingLevel.HEADING_1, "2. Analisi del Problema"),
      h(HeadingLevel.HEADING_2, "2.1 Il flusso documentale attuale"),
      p("Attualmente un documento che arriva \u2014 che sia una lettera fisica scansionata, una PEC da ARPA o un provvedimento della Regione \u2014 viene caricato nel modulo Documenti come semplice allegato. Le informazioni operative che contiene (nuove scadenze, prescrizioni modificate, richieste di integrazione) vengono poi inserite manualmente dagli operatori nelle rispettive sezioni del sistema."),
      space(),
      p("Questo genera tre problemi concreti:"),
      bullet("Rischio di perdita di informazioni: se l\u2019operatore non legge attentamente il documento, scadenze critiche possono essere dimenticate"),
      bullet("Duplicazione del lavoro: le stesse informazioni vengono lette dal documento e poi ridigitate nel sistema"),
      bullet("Nessuna tracciabilit\u00e0 del processo: non \u00e8 possibile sapere chi ha preso in carico un documento, in che stato \u00e8 la lavorazione, se \u00e8 stato approvato"),
      space(1),
      h(HeadingLevel.HEADING_2, "2.2 Il flusso documentale target"),
      p("Il flusso ottimale che si vuole realizzare \u00e8 il seguente:"),
      space(),
      numbered("Il documento arriva (upload manuale, scansione, email) e viene salvato in Alfresco con metadati strutturati (tipo, stabilimento, ente emittente, data)"),
      numbered("Il sistema analizza automaticamente il testo del documento tramite OCR e, opzionalmente, AI extraction, e propone le entit\u00e0 da creare (scadenze, prescrizioni)"),
      numbered("Camunda avvia automaticamente un processo BPMN di lavorazione del documento"),
      numbered("I task del processo appaiono nell\u2019interfaccia Angular: il responsabile revisa le bozze estratte, le corregge se necessario e le conferma"),
      numbered("Le entit\u00e0 confermate vengono create nel sistema AIA e collegate al documento originale"),
      numbered("Tutto il percorso \u00e8 tracciato: chi ha fatto cosa, quando, con quale esito"),
      space(2),

      // ===== 3. TECNOLOGIE PROPOSTE =====
      h(HeadingLevel.HEADING_1, "3. Analisi delle Tecnologie Proposte"),
      h(HeadingLevel.HEADING_2, "3.1 Document Management System: Alfresco Community Edition"),
      h(HeadingLevel.HEADING_3, "Perch\u00e9 Alfresco"),
      p("Tra i DMS open source disponibili (Mayan EDMS, OpenKM, Paperless-ngx, Nuxeo, Alfresco), Alfresco Community Edition \u00e8 la scelta pi\u00f9 solida per un contesto di compliance ambientale:"),
      space(),
      multiColTable(
        ["DMS", "Pro", "Contro", "Adatto"],
        [
          ["Alfresco Community", "ECM completo, API REST matura, metadati custom, versioning, Solr full-text, integrazione Flowable nativa", "Pesante (~3-4GB RAM), deployment complesso", "\u2705 Raccomandato"],
          ["Mayan EDMS", "Leggero, OCR integrato, buona API REST, semplice deployment", "Workflow limitato, Python (eterogeneità stack)", "\u26A0\uFE0F Alternativa leggera"],
          ["OpenKM Community", "Java (allineato stack), buona API", "UI datata, community più piccola", "\u26A0\uFE0F Accettabile"],
          ["Paperless-ngx", "Ottimo OCR, UI moderna, facilissimo", "Nessun workflow, pensato per uso personale", "\u274C Non adatto"],
          ["Nuxeo Community", "Molto potente, API eccellente", "Ancora più pesante di Alfresco, licenza ibrida", "\u26A0\uFE0F Eccessivo"],
        ],
        [1800, 3200, 2200, 1826]
      ),
      space(1),
      h(HeadingLevel.HEADING_3, "Funzionalit\u00e0 chiave di Alfresco per questo progetto"),
      bullet("Content Model personalizzabile: schema di metadati specifico per documenti AIA (numeroAIA, stabilimento, enteEmittente, tipoProvvedimento)"),
      bullet("Versioning automatico: ogni revisione di un documento \u00e8 tracciata con autore e timestamp"),
      bullet("Full-text search con Apache Solr: ricerca nel contenuto dei PDF, non solo nei metadati"),
      bullet("API REST/CMIS completa: integrazione nativa con Spring Boot tramite client HTTP"),
      bullet("Thumbnail e preview automatiche: anteprima dei documenti direttamente nel frontend"),
      bullet("Regole e azioni automatiche: quando arriva un documento di tipo X, esegui azione Y (es. avvia un processo Camunda)"),
      space(1),
      h(HeadingLevel.HEADING_3, "Requisiti infrastrutturali Alfresco"),
      twoColTable(
        ["Risorsa", "Minimo raccomandato"],
        [
          ["CPU", "4 core"],
          ["RAM", "6 GB dedicati (Alfresco + Solr + Transform Service)"],
          ["Storage", "SSD, dimensionato sui volumi documentali (es. 100 GB iniziali)"],
          ["Java", "JDK 17 (fornito dal container Docker)"],
          ["Database", "PostgreSQL 14+ (condivisibile con l\u2019applicazione)"],
        ],
        [3000, 6026]
      ),
      space(2),

      h(HeadingLevel.HEADING_2, "3.2 BPMN Engine: Camunda 7 Community"),
      h(HeadingLevel.HEADING_3, "Perch\u00e9 Camunda 7"),
      p("Camunda 7 Community Edition \u00e8 il punto di riferimento open source per i processi BPMN in ambito enterprise Java. La scelta rispetto alle alternative \u00e8 motivata principalmente dalla capacit\u00e0 di embedding nel frontend:"),
      space(),
      twoColTable(
        ["Engine BPMN", "Embedding Angular"],
        [
          ["Camunda 7 Community", "\u2705 bpmn-js (libreria ufficiale npm, 10k+ stelle GitHub)"],
          ["Flowable", "\u26A0\uFE0F API REST buona, ma no libreria Angular dedicata"],
          ["Activiti (standalone)", "\u26A0\uFE0F UI non embeddable facilmente"],
          ["jBPM (Red Hat)", "\u274C UI Workbench non embeddable, deployment complesso"],
        ],
        [4000, 5026]
      ),
      space(1),
      h(HeadingLevel.HEADING_3, "bpmn-js: embedding nel frontend Angular"),
      p("bpmn-js \u00e8 la libreria JavaScript open source di Camunda per renderizzare e interagire con diagrammi BPMN 2.0 nel browser. Pu\u00f2 essere installata come pacchetto npm e integrata in qualsiasi componente Angular:"),
      space(),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [9026],
        rows: [new TableRow({ children: [new TableCell({
          borders: borders("444444"),
          shading: { fill: "2B2B2B", type: ShadingType.CLEAR },
          margins: { top: 100, bottom: 100, left: 200, right: 200 },
          width: { size: 9026, type: WidthType.DXA },
          children: [
            new Paragraph({ children: [new TextRun({ text: "npm install bpmn-js @bpmn-io/form-js camunda-bpmn-moddle", font: "Courier New", size: 20, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "# bpmn-js      → render/edit diagrammi BPMN nel browser", font: "Courier New", size: 18, color: "888888" })], spacing: { before: 40 } }),
            new Paragraph({ children: [new TextRun({ text: "# form-js       → render form task embedded in Angular", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "# camunda-moddle → estensioni Camunda al meta-modello BPMN", font: "Courier New", size: 18, color: "888888" })] }),
          ]
        })]})]}
      ),
      space(1),
      p("Con queste librerie \u00e8 possibile creare in Angular:"),
      bullet("Un viewer del processo in corso (quali task sono completati, quale \u00e8 attivo)"),
      bullet("Un form di task che l\u2019utente compila direttamente nell\u2019interfaccia AIA, senza accedere alla Camunda Tasklist separata"),
      bullet("Una vista amministrativa per disegnare o modificare i processi (BPMN Modeler)"),
      space(1),
      h(HeadingLevel.HEADING_3, "Modalit\u00e0 di deployment Camunda 7"),
      p("Camunda 7 pu\u00f2 essere usato in due modi, entrambi compatibili con Spring Boot:"),
      twoColTable(
        ["Modalit\u00e0", "Descrizione"],
        [
          ["Embedded (raccomandato)", "Camunda gira dentro il processo Spring Boot come libreria. Condivide il datasource PostgreSQL. Deployment pi\u00f9 semplice, nessun servizio aggiuntivo da orchestrare."],
          ["Standalone (REST)", "Camunda gira come servizio Docker separato. Lo Spring Boot lo chiama via REST API. Pi\u00f9 flessibile per future evoluzioni verso Camunda 8."],
        ],
        [2200, 6826]
      ),
      space(),
      alertBox("\uD83D\uDCA1", "Raccomandazione", "Iniziare con Camunda embedded nel backend Spring Boot. Riduce la complessit\u00e0 operativa e permette di condividere le entit\u00e0 JPA esistenti (Stabilimento, Prescrizione, ecc.) direttamente nelle Delegation Java dei task BPMN.", BLUE_LIGHT, BLUE_MID),
      space(2),

      // ===== 4. ARCHITETTURA =====
      h(HeadingLevel.HEADING_1, "4. Architettura della Soluzione"),
      h(HeadingLevel.HEADING_2, "4.1 Schema architetturale"),
      p("L\u2019architettura risultante aggiunge due layer al sistema esistente, mantenendo il backend Spring Boot come unico punto di accesso del frontend:"),
      space(),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [9026],
        rows: [new TableRow({ children: [new TableCell({
          borders: borders(BLUE_MID),
          shading: { fill: BLUE_LIGHT, type: ShadingType.CLEAR },
          margins: { top: 150, bottom: 150, left: 200, right: 200 },
          width: { size: 9026, type: WidthType.DXA },
          children: [
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u250F\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2513", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503        ANGULAR FRONTEND (porta 4200)                  \u2503", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503  [Moduli esistenti] + [DMS UI] + [BPMN Viewer/Task]   \u2503", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2517\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u252B\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u251B", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "                      | REST/HTTP                      ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "                      v                                 ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u250F\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2513", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503  SPRING BOOT BACKEND (porta 8080)                    \u2503", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503  + Camunda Engine (embedded)                         \u2503", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503  + AlfrescoAdapter | + CamundaProcessService         \u2503", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2503  + DocumentoAnalisiService (OCR + AI extraction)     \u2503", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "\u2517\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u252B\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u252B\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u251B", font: "Courier New", size: 20, bold: true })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "           |          |                                 ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "           v          v                                 ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u250F\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2513  \u250F\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2513          ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u2503PostgreSQL\u2503  \u2503  Alfresco Community   \u2503          ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u2503 (AIA DB) \u2503  \u2503  + Apache Solr        \u2503          ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u2503 + Camunda\u2503  \u2503  + Transform Service  \u2503          ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u2503  tables \u2503  \u2503  (porta 8080 interno) \u2503          ", font: "Courier New", size: 20 })] }),
            new Paragraph({ alignment: AlignmentType.CENTER, children: [new TextRun({ text: "  \u2517\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u251B  \u2517\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u2501\u251B          ", font: "Courier New", size: 20 })] }),
          ]
        })]})]}
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "4.2 Flusso dati: documento in ingresso"),
      multiColTable(
        ["Step", "Attore", "Componente", "Output"],
        [
          ["1", "Operatore", "Angular + Spring Boot", "Upload file → salvato in Alfresco con metadati AIA"],
          ["2", "Sistema", "Spring Boot @Async", "Estrazione testo (PDF/OCR), chiamata AI per parsing entit\u00e0"],
          ["3", "Sistema", "Camunda Engine", "Avvio processo BPMN \u201cLavorazione Documento AIA\u201d"],
          ["4", "Responsabile", "Angular + bpmn-js", "Task: revisione bozze estratte, conferma/modifica"],
          ["5", "Sistema", "Spring Boot Service", "Creazione entit\u00e0 confermate (Scadenza, Prescrizione, ecc.)"],
          ["6", "Sistema", "Camunda Engine", "Completamento processo, notifica email al responsabile"],
        ],
        [600, 1500, 2400, 4526]
      ),
      space(2),

      h(HeadingLevel.HEADING_2, "4.3 Processo BPMN: Lavorazione Documento AIA"),
      p("Il processo principale da modellare in BPMN 2.0 \u00e8 il seguente. Viene avviato automaticamente all\u2019arrivo di ogni documento e gestisce la lavorazione fino alla creazione delle entit\u00e0 nel sistema:"),
      space(),
      sectionBox("Lanes del processo BPMN", [
        "Lane SISTEMA: Analisi OCR documento \u2192 Estrazione bozze entit\u00e0 (AI) \u2192 Notifica responsabile",
        "Lane RESPONSABILE: Task \u201cRevisione bozze\u201d \u2192 [Gateway] Dati corretti? \u2192 s\u00ec: Conferma \u2022 no: Modifica manuale",
        "Lane ADMIN (opzionale): Task \u201cApprovazione\u201d per documenti ad alta priorit\u00e0 (es. provvedimenti sanzionatori)",
        "Lane SISTEMA: Creazione entit\u00e0 confermate in AIA \u2192 Aggiornamento stato documento in Alfresco \u2192 Fine"
      ], BLUE_LIGHT, BLUE_MID),
      space(),
      p("Processi secondari aggiuntivi:"),
      bullet("Processo \u201cRinnovo AIA\u201d: scaduto il numero di giorni di preavviso configurato, avvia un workflow di raccolta documentazione per il rinnovo"),
      bullet("Processo \u201cNon Conformit\u00e0\u201d: quando un dato ambientale supera il limite, avvia un workflow di segnalazione e azioni correttive"),
      bullet("Processo \u201cIntegrazione Ente\u201d: gestisce le risposte alle richieste di integrazione da ARPA/Regione con scadenze e reminder"),
      space(2),

      // ===== 5. IMPLEMENTAZIONE BACKEND =====
      h(HeadingLevel.HEADING_1, "5. Implementazione Backend (Spring Boot)"),
      h(HeadingLevel.HEADING_2, "5.1 Dipendenze Maven da aggiungere"),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [9026],
        rows: [new TableRow({ children: [new TableCell({
          borders: borders("444444"),
          shading: { fill: "2B2B2B", type: ShadingType.CLEAR },
          margins: { top: 100, bottom: 100, left: 200, right: 200 },
          width: { size: 9026, type: WidthType.DXA },
          children: [
            new Paragraph({ children: [new TextRun({ text: "<!-- Camunda 7 embedded -->", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "<dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <groupId>org.camunda.bpm.springboot</groupId>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <artifactId>camunda-bpm-spring-boot-starter-webapp</artifactId>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <version>7.21.0</version>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "</dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "", font: "Courier New", size: 18 })] }),
            new Paragraph({ children: [new TextRun({ text: "<!-- Alfresco REST client -->", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "<dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <groupId>org.apache.chemistry.opencmis</groupId>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <artifactId>chemistry-opencmis-client-impl</artifactId>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <version>1.1.0</version>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "</dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "", font: "Courier New", size: 18 })] }),
            new Paragraph({ children: [new TextRun({ text: "<!-- Apache PDFBox per estrazione testo -->", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "<dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <groupId>org.apache.pdfbox</groupId>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "  <artifactId>pdfbox</artifactId>  <version>3.0.1</version>", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "</dependency>", font: "Courier New", size: 18, color: "A8FF60" })] }),
          ]
        })]})]}
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "5.2 Nuovi servizi da implementare"),
      multiColTable(
        ["Classe", "Responsabilit\u00e0"],
        [
          ["AlfrescoService", "Wrapper CMIS/REST per upload, download, ricerca documenti e gestione metadati in Alfresco"],
          ["DocumentoAnalisiService", "Pipeline: estrazione testo PDF (PDFBox) + OCR se necessario + chiamata API AI per parsing strutturato. Restituisce BozzaEntit\u00e0."],
          ["BozzaEntitaService", "CRUD per le bozze proposte dall\u2019analisi. Gestisce conferma, rifiuto e modifica manuale."],
          ["CamundaProcessService", "Avvio e gestione dei processi BPMN. Wrapper attorno al RuntimeService e TaskService di Camunda."],
          ["DocumentoWorkflowListener", "Event listener che reagisce al caricamento di un documento e avvia il processo Camunda corrispondente."],
        ],
        [2800, 6226]
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "5.3 Nuove entit\u00e0 JPA"),
      twoColTable(
        ["Entit\u00e0", "Campi principali"],
        [
          ["BozzaEntita", "id, documentoAlfrescoId, tipoEntita (SCADENZA/PRESCRIZIONE/DATO_AMBIENTALE), datiJson, stato (PROPOSTA/CONFERMATA/RIFIUTATA/MODIFICATA), operatoreId, dataCreazione, dataConferma"],
          ["DocumentoAIA", "id, alfrescoNodeId, alfrescoUrl, stabilimento, tipoDocumento, enteEmittente, dataRicezione, statoLavorazione, processoId (Camunda), note"],
          ["ProcessoInstance", "id, processoId, tipoProcesso, stabilimento, documentoId, stato, dataAvvio, dataChiusura, responsabile"],
        ],
        [2800, 6226]
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "5.4 Nuovi endpoint REST"),
      multiColTable(
        ["Metodo", "Endpoint", "Descrizione"],
        [
          ["POST", "/api/documenti/upload-alfresco", "Upload documento su Alfresco + avvio analisi asincrona"],
          ["GET",  "/api/documenti/{id}/bozze", "Recupera bozze entit\u00e0 estratte da un documento"],
          ["POST", "/api/bozze/{id}/conferma", "Conferma una bozza e crea l\u2019entit\u00e0 definitiva"],
          ["PUT",  "/api/bozze/{id}", "Modifica i dati di una bozza prima della conferma"],
          ["DELETE","/api/bozze/{id}/rifiuta", "Rifiuta una bozza proposta"],
          ["GET",  "/api/workflow/task/miei", "Lista task Camunda assegnati all\u2019utente corrente"],
          ["POST", "/api/workflow/task/{id}/completa", "Completa un task BPMN con le variabili di output"],
          ["GET",  "/api/workflow/processi", "Lista istanze di processo attive"],
          ["GET",  "/api/alfresco/search", "Ricerca full-text documenti in Alfresco tramite Solr"],
        ],
        [900, 3200, 4926]
      ),
      space(2),

      // ===== 6. IMPLEMENTAZIONE FRONTEND =====
      h(HeadingLevel.HEADING_1, "6. Implementazione Frontend (Angular)"),
      h(HeadingLevel.HEADING_2, "6.1 Nuovi moduli Angular"),
      sectionBox("Nuovi componenti da creare", [
        "DocumentaleComponent: ricerca, upload e gestione documenti su Alfresco. Mostra anteprima, metadati e storico versioni.",
        "BozzeRevisoreComponent: elenco bozze da confermare con form di modifica inline. Collegato al documento sorgente.",
        "WorkflowDashboardComponent: vista dei processi attivi con stato, responsabile e task pendenti.",
        "TaskListComponent: elenco task BPMN assegnati all\u2019utente con form di completamento (powered by @bpmn-io/form-js).",
        "BpmnViewerComponent: visualizzazione del diagramma BPMN del processo con evidenziazione del task attivo (powered by bpmn-js).",
        "BpmnModelerComponent (admin): editor visuale BPMN per creare o modificare i processi direttamente dal browser."
      ], BLUE_LIGHT, BLUE_MID),
      space(1),
      h(HeadingLevel.HEADING_2, "6.2 Integrazione bpmn-js in Angular"),
      p("L\u2019integrazione di bpmn-js in un componente Angular standalone \u00e8 diretta: si inizializza il viewer/modeler nel lifecycle hook ngAfterViewInit passando l\u2019elemento DOM come container. Il componente ascolta gli eventi del viewer (element.click, import.done, ecc.) e li trasforma in output Angular con EventEmitter."),
      space(),
      alertBox("\uD83D\uDEE0\uFE0F", "Pattern di integrazione bpmn-js + Angular",
        "1. npm install bpmn-js\n2. Creare un wrapper component con un div#bpmn-container\n3. In ngAfterViewInit: new BpmnViewer({ container: '#bpmn-container' })\n4. Chiamare viewer.importXML(diagramXml) con il BPMN dal backend\n5. Highlight del token attivo via viewer.get('canvas').addMarker(taskId, 'highlight')",
        YELLOW_LIGHT, "FFC000"),
      space(1),
      h(HeadingLevel.HEADING_2, "6.3 Task form embedded con @bpmn-io/form-js"),
      p("Ogni task umano nel processo BPMN ha associato un form schema JSON (definito in Camunda Modeler). Il componente Angular TaskFormComponent usa la libreria @bpmn-io/form-js per renderizzare questo form direttamente nell\u2019interfaccia AIA, senza che l\u2019utente debba accedere alla Camunda Tasklist separata. Al submit, le variabili del form vengono inviate al backend che completa il task Camunda."),
      space(2),

      // ===== 7. INFRASTRUTTURA =====
      h(HeadingLevel.HEADING_1, "7. Infrastruttura e Deployment"),
      h(HeadingLevel.HEADING_2, "7.1 Docker Compose"),
      p("L\u2019intera soluzione pu\u00f2 essere orchestrata con Docker Compose. I servizi aggiuntivi rispetto all\u2019attuale setup:"),
      twoColTable(
        ["Servizio Docker", "Porta / Note"],
        [
          ["alfresco/alfresco-content-repository-community", "8081 interno. Usa il PostgreSQL esistente con un database separato 'alfresco'."],
          ["alfresco/alfresco-search-services (Solr)", "8983 interno. Necessario per la ricerca full-text in Alfresco."],
          ["alfresco/alfresco-transform-core-aio", "8090 interno. Gestisce thumbnail, preview PDF, OCR integrato."],
          ["Camunda (embedded)", "Nessun container aggiuntivo. Gira nel processo Spring Boot."],
        ],
        [4000, 5026]
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "7.2 Variabili di configurazione"),
      new Table({
        width: { size: 9026, type: WidthType.DXA },
        columnWidths: [9026],
        rows: [new TableRow({ children: [new TableCell({
          borders: borders("444444"),
          shading: { fill: "2B2B2B", type: ShadingType.CLEAR },
          margins: { top: 100, bottom: 100, left: 200, right: 200 },
          width: { size: 9026, type: WidthType.DXA },
          children: [
            new Paragraph({ children: [new TextRun({ text: "# application-dev.properties (aggiornamenti)", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "alfresco.base-url=http://localhost:8081/alfresco", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "alfresco.username=admin", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "alfresco.password=${ALFRESCO_PASSWORD}", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "alfresco.folder.aia=/Sites/aia-documents", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "", font: "Courier New", size: 18 })] }),
            new Paragraph({ children: [new TextRun({ text: "camunda.bpm.admin-user.id=admin", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "camunda.bpm.admin-user.password=${CAMUNDA_PASSWORD}", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "camunda.bpm.database.schema-update=true", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "", font: "Courier New", size: 18 })] }),
            new Paragraph({ children: [new TextRun({ text: "# Opzionale: API AI per estrazione entit\u00e0", font: "Courier New", size: 18, color: "888888" })] }),
            new Paragraph({ children: [new TextRun({ text: "ai.extraction.enabled=true", font: "Courier New", size: 18, color: "A8FF60" })] }),
            new Paragraph({ children: [new TextRun({ text: "ai.extraction.api-key=${ANTHROPIC_API_KEY}", font: "Courier New", size: 18, color: "FFFFFF" })] }),
            new Paragraph({ children: [new TextRun({ text: "ai.extraction.model=claude-haiku-4-5-20251001", font: "Courier New", size: 18, color: "FFFFFF" })] }),
          ]
        })]})]}
      ),
      space(2),

      // ===== 8. PIANO IMPLEMENTATIVO =====
      h(HeadingLevel.HEADING_1, "8. Piano Implementativo a Fasi"),
      p("Si propone un approccio incrementale in 4 fasi, ognuna con valore autonomo e testabile:"),
      space(),
      multiColTable(
        ["Fase", "Durata", "Scope", "Valore rilasciato"],
        [
          ["Fase 1\nFoundation", "3-4 sett.", "Setup Alfresco + integrazione upload. Spring Boot \u2194 Alfresco CMIS. Migrazione documenti esistenti.", "Archivio documentale strutturato con metadati, versioning e ricerca."],
          ["Fase 2\nAnalisi", "3-4 sett.", "Pipeline OCR + AI extraction. Entit\u00e0 BozzaEntita. Endpoint revisione bozze. UI Angular di revisione.", "Il sistema propone automaticamente scadenze e prescrizioni dai documenti caricati."],
          ["Fase 3\nWorkflow", "4-5 sett.", "Integrazione Camunda embedded. Modello processo BPMN principale. bpmn-js in Angular. TaskList component.", "Workflow tracciato: ogni documento ha un iter di lavorazione visibile e assegnabile."],
          ["Fase 4\nAvanzato", "3-4 sett.", "Processi secondari (rinnovo AIA, non conformit\u00e0). BPMN Modeler per admin. Notifiche avanzate. Report.", "Sistema completo con automazione dei processi di compliance."],
        ],
        [1000, 900, 3600, 3526]
      ),
      space(2),

      // ===== 9. ANALISI DI FATTIBILITÀ =====
      h(HeadingLevel.HEADING_1, "9. Analisi di Fattibilit\u00e0"),
      h(HeadingLevel.HEADING_2, "9.1 Fattibilit\u00e0 tecnica"),
      multiColTable(
        ["Aspetto", "Valutazione", "Note"],
        [
          ["Integrazione Spring Boot \u2194 Camunda", "\u2705 Alta", "Spring Boot Starter ufficiale Camunda. Documentazione eccellente."],
          ["Integrazione Spring Boot \u2194 Alfresco", "\u2705 Alta", "API REST e CMIS mature. Usate in produzione in migliaia di progetti."],
          ["bpmn-js in Angular", "\u2705 Alta", "Libreria npm standard. Esempi Angular disponibili su GitHub."],
          ["OCR su documenti scansionati", "\u26A0\uFE0F Media", "Qualit\u00e0 dipende dalla scansione. Alfresco Transform Service include Tesseract."],
          ["AI extraction entit\u00e0", "\u2705 Alta", "Claude API restituisce JSON strutturato con alta accuratezza su testi legali italiani."],
          ["Migrazione documenti esistenti", "\u26A0\uFE0F Media", "Script di migrazione da implementare. Nessuna perdita dati prevista."],
          ["Compatibilit\u00e0 Java 21 (progetto)", "\u2705 Alta", "Camunda 7.21 supporta Java 21. Alfresco gira in container proprio."],
        ],
        [3000, 1400, 4626]
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "9.2 Rischi e mitigazioni"),
      multiColTable(
        ["Rischio", "Probabilit\u00e0", "Impatto", "Mitigazione"],
        [
          ["Alfresco pesante da gestire operativamente", "Media", "Alto", "Monitoraggio con Prometheus/Grafana. Health check automatici. Documentare procedure di riavvio."],
          ["OCR di bassa qualit\u00e0 su scansioni vecchie", "Alta", "Medio", "Fallback a inserimento manuale. Indicatore di qualit\u00e0 OCR mostrato all\u2019utente."],
          ["Curva di apprendimento BPMN per il team", "Media", "Medio", "Fornire processi preconfigurati. BPMN Modeler disponibile solo per admin."],
          ["Camunda 7 EOL nel 2025", "Certa", "Basso", "EOL solo per support commerciale. Community Edition rimane disponibile. Path di migrazione a Camunda 8 documentato."],
        ],
        [2600, 1200, 1200, 4026]
      ),
      space(2),

      // ===== 10. STIMA COSTI E TEMPI =====
      h(HeadingLevel.HEADING_1, "10. Stima Costi e Tempi"),
      h(HeadingLevel.HEADING_2, "10.1 Costi di sviluppo"),
      twoColTable(
        ["Voce", "Stima (giorni uomo)"],
        [
          ["Fase 1 \u2014 Foundation (Alfresco + integrazione)", "15-20 gg"],
          ["Fase 2 \u2014 Analisi documenti (OCR + AI + BozzaEntita)", "15-20 gg"],
          ["Fase 3 \u2014 Workflow (Camunda + bpmn-js + Angular)", "20-25 gg"],
          ["Fase 4 \u2014 Processi avanzati + admin", "15-20 gg"],
          ["Testing, documentazione e deploy", "10-15 gg"],
          ["\u2014", "\u2014"],
          ["TOTALE stimato", "75-100 giorni uomo"],
        ],
        [5000, 4026]
      ),
      space(1),
      h(HeadingLevel.HEADING_2, "10.2 Costi infrastrutturali (mensili)"),
      twoColTable(
        ["Componente", "Costo"],
        [
          ["Alfresco Community Edition", "Gratuito (open source)"],
          ["Camunda 7 Community Edition", "Gratuito (open source)"],
          ["bpmn-js + @bpmn-io/form-js", "Gratuito (open source)"],
          ["Server aggiuntivo per Alfresco (cloud)", "~\u20AC 80-150/mese (4 core, 8GB RAM)"],
          ["API AI extraction (opzionale, Claude Haiku)", "~\u20AC 5-15/mese (volumi tipici AIA)"],
          ["Storage documenti (S3 o equivalente)", "~\u20AC 10-30/mese (dipende dai volumi)"],
        ],
        [4000, 5026]
      ),
      space(2),

      // ===== 11. CONCLUSIONI =====
      h(HeadingLevel.HEADING_1, "11. Conclusioni e Raccomandazioni"),
      p("L\u2019integrazione di un DMS e di un engine BPMN nel sistema AIA Management rappresenta un\u2019evoluzione naturale e ad alto valore del progetto esistente. Le tecnologie proposte \u2014 Alfresco Community Edition e Camunda 7 con bpmn-js \u2014 sono mature, open source, e si integrano direttamente con lo stack Java/Angular gi\u00e0 in uso."),
      space(),
      p("Il principale elemento di valore \u00e8 la chiusura del ciclo documentale: un documento che arriva non \u00e8 pi\u00f9 solo un allegato, ma diventa il motore di un processo tracciato che garantisce che nessuna scadenza venga persa e che ogni decisione sia documentata e attribuibile."),
      space(),
      alertBox("\uD83D\uDE80", "Prossimi passi raccomandati",
        "1. Approvazione del documento e definizione delle priorit\u00e0 di fase\n2. Setup ambiente di sviluppo con Docker Compose (Alfresco + Solr)\n3. Avvio Fase 1: integrazione Alfresco con Spring Boot e test upload/download\n4. Disegno del processo BPMN principale con Camunda Modeler (versione desktop, gratuita)\n5. Demo della Fase 1 completata prima di procedere alla Fase 2",
        GREEN_LIGHT, GREEN),
      space(2),
      divider(),
      new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new TextRun({ text: "Documento generato da AIA Management System \u2014 Febbraio 2026", font: "Arial", size: 18, color: "888888", italics: true })],
        spacing: { before: 200 }
      })
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync("/sessions/lucid-trusting-hopper/mnt/aia-management-system 2/DOC_TECNICO_DMS_BPMN.docx", buffer);
  console.log("DONE");
}).catch(e => { console.error(e); process.exit(1); });
