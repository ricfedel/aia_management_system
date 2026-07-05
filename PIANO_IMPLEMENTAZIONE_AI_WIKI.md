# Piano di Implementazione — Modulo AI Wiki (AI Knowledge Assistant)

> Porting del modulo `ai-module` di PerformPlus-ESG in AIA Management System, adattato al dominio AIA.
> Data: luglio 2026 — Stato: **pianificato** (implementazione non ancora avviata)

## 1. Obiettivo

Aggiungere ad AIA Management System un modulo di **AI Wiki / Assistente di conoscenza**:

- **Wiki della conoscenza a grafo**: pagine Markdown generate automaticamente dai documenti AIA e dai dati strutturati (stabilimenti, prescrizioni, scadenze, conformità, camini, dati ambientali, produzione, rifiuti), collegate da archi tipizzati e navigabili.
- **Chat RAG sui propri dati**: domande in linguaggio naturale con risposte basate esclusivamente sulla base di conoscenza aziendale e fonti citate (`db:…`, `doc:…`).
- **Chat agentica**: generazione di grafici, previsioni (forecast deterministico), bozze di report Word e presentazioni PowerPoint tramite function-calling.
- **Ingestione automatica**: ogni documento caricato/modificato/eliminato nel modulo Documenti viene indicizzato in modo asincrono (estrazione testo Tika → chunking → embeddings locali → pgvector).

Il modulo sorgente è `performplus-esg/backend/ai-module` (package `com.finconsgroup.ai`): Spring Boot 4, LangChain4j (Claude di default, multi-provider OpenAI/on-prem), embeddings ONNX in-process gratuiti (all-MiniLM-L6-v2, 384 dim), vector store pgvector con SQL nativo, guardrail di budget token.

## 2. Sintesi delle decisioni architetturali

| Tema | Decisione |
|---|---|
| Packaging | **Copia dei sorgenti** nel modulo singolo, package `it.grandimolini.aia.ai` (NO multi-modulo Maven) |
| Tenancy | `organizzazione_id` → `stabilimento_id` (FK su `stabilimenti`); **`anno` rimosso dallo scoping** (resta metadato nullable sui chunk, da `Documento.anno`) |
| Ingest | Adapter diretto su `Documento` + `FileStorageService` di AIA; eventi Spring nuovi pubblicati da `DocumentoService` |
| Dati strutturati | `AiaStructuredKnowledgeProviderImpl`: stabilimento → prescrizioni → scadenze/conformità, camini → parametri/limiti, produzione, rifiuti |
| Sicurezza | `@Secured({"AMMINISTRATORE","SUPER_ADMIN"})` → `@PreAuthorize` con ruoli AIA + `@stabilimentoAccessChecker` |
| Infra | Immagine DB `pgvector/pgvector:pg16`; DDL applicato all'avvio da un runner (pattern `SqlLoaderService`, niente Flyway); riuso `ANTHROPIC_API_KEY`; flag `aia.feature.ai-assistant` |
| Frontend | Port in **`frontend_perform/`** (è quello buildato da `nginx/Dockerfile`), stesso stack Bootstrap 5 + ng-bootstrap dell'ESG |

## 3. Packaging: copia nel modulo singolo

**Decisione: copiare i sorgenti in `backend/src/main/java/it/grandimolini/aia/ai/`**, non convertire il backend a multi-modulo Maven. Motivazioni verificate sul codice:

1. `ai-module` **non è portabile as-is**: il suo `pom.xml` dipende da `com.finconsgroup:esg-model` (entità `Organizzazione`) e `com.finconsgroup:workflow-module` (`WfDocumento`, `WfDocumentoRepository`, `WfDocumentoStorageService`, `WfDocumentStore`, eventi documento). ~10 file su 60 importano quei package e altri 15 usano `organizzazioneId`: il codice va comunque modificato.
2. Il `backend/Dockerfile` fa `COPY pom.xml .` + `COPY src ./src` e copia `target/aia-management-*.jar`: con la copia dei sorgenti non cambia nulla; il multi-modulo richiederebbe di riscrivere Dockerfile e path del jar.
3. Java: ai-module compila a 17, AIA a 21 — nessuna incompatibilità.

**Modifiche a `backend/pom.xml`:**
- `dependencyManagement`: import `dev.langchain4j:langchain4j-bom:1.16.2` (`pom`/`import`).
- Dipendenze nuove: `langchain4j`, `langchain4j-anthropic`, `langchain4j-open-ai`, `langchain4j-embeddings-all-minilm-l6-v2`, `tika-core:2.9.2`, `tika-parsers-standard-package:2.9.2`, `commons-lang3`.
- **NON aggiungere**: `langchain4j-pgvector` (il codice usa SQL nativo), `springdoc` (AIA non lo usa: rimuovere gli import), `jakarta.*-api`/`spring-*` espliciti (forniti dallo starter), `jackson-databind` pinnata, `postgresql` (già presente).
- **POI**: AIA usa `poi-ooxml:5.2.3`, ai-module 5.3.0, Tika 2.9.2 porta POI 5.2.x transitivamente → **allineare tutto a 5.2.3** (DocxWriter/PptxWriter usano solo XWPF/XSLF base, compatibili).

**File da copiare/adattare** (da `ai-module/src/main/java/com/finconsgroup/ai/` a `backend/src/main/java/it/grandimolini/aia/ai/`): package `agent`, `analysis`, `artifact`, `chart`, `config`, `domain`, `graph`, `guardrail`, `ingest`, `llm`, `rag`, `repository`, `rest`, `support`, `wiki`.
**Escludere**: `demo/AiGraphDemo.java`, `documentale/*` (sostituito dall'adapter, §5), `domain/AiDocumentoMeta.java` + repository (AIA ha già `Documento.tags`). Non copiare i file AppleDouble `._*` presenti sul volume exFAT.

**Test da portare** (unit test puri): `ForecasterTest`, `TextChunkerTest`, `ComplexityRouterTest`, `MarkdownRendererTest`, `MediaKindTest` → `backend/src/test/java/it/grandimolini/aia/ai/`.

## 4. Tenancy: mappatura (organizzazione, anno) → stabilimento

Profondità dello scoping verificata nel sorgente: DDL (`kb_md_page`, `kb_edge`, `kb_chunk`, `ai_token_usage` hanno `organizzazione_id NOT NULL` + `anno NOT NULL`, unique `(organizzazione_id, anno, page_key)`), entità JPA con `@ManyToOne Organizzazione`, query native con filtro `organizzazione_id = :orgId OR IS NULL` e `anno = :anno OR IS NULL`, catena servizi (`AiRagService`, `AiAgentService`, `AiTools`, `GraphRetrievalService`, `AiBackfillService`, proiezione grafo, `TokenBudgetGuard`) parametrizzata `(Long orgId, Integer anno)`.

**Mappatura minima:**
- `organizzazione_id` → `stabilimento_id` (FK `public.stabilimenti(id)`). Rinomina meccanica in entità (usare `stabilimentoId Long` semplice, senza `@ManyToOne`, per evitare accoppiamento JPA), repository, query native, DDL.
- **`anno` eliminato dallo scoping** (in AIA prescrizioni/scadenze/camini non hanno la dimensione anno; solo `Documento.anno` è opzionale):
  - `kb_md_page`: unique key → `(stabilimento_id, page_key)`; colonna `anno` rimossa.
  - `kb_chunk`: `anno` **nullable** come metadato ereditato da `Documento.anno`, **rimosso dal filtro** in `findNearest`.
  - `ai_token_usage`: budget **globale per giorno** (installazione mono-cliente; `stabilimento_id` nullable per reporting).
  - Firme dei servizi: `(Long stabilimentoId, String prompt, ...)` senza anno.
- **Risoluzione del contesto**: eliminare `OrgContextResolver`. Lo stabilimento è passato dal client come negli altri controller: `@RequestParam Long stabilimentoId` + `@PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")` (bean esistente in `backend/src/main/java/it/grandimolini/aia/security/StabilimentoAccessChecker.java`).
- Semantica "conoscenza globale" mantenuta: chunk/pagine con `stabilimento_id NULL` = trasversali, inclusi in ogni retrieval (clausola `OR IS NULL` già presente).

## 5. Sorgente documentale: adapter su Documento/FileStorageService

Su AIA: `Documento` ha `stabilimento`, `anno`, `filePath`, `mimeType`, `nomeFile`, `isVersioneCorrente`, `tags` (CSV), `testoEstratto`; i file sono su **filesystem** (`FileStorageService`, dir `file.upload.dir`, volume Docker `aia-uploads:/app/uploads`). Mancano `checksumSha256` ed eventi di dominio.

**Adattamenti in `AiIngestService`:**
- Sostituire `WfDocumentoRepository`/`WfDocumentoStorageService` con `DocumentoRepository` + `FileStorageService` (byte da `loadFileAsResource(doc.getFilePath())`).
- Dedup: SHA-256 calcolato all'ingest (utility in `ai/support/`) e confrontato con `kb_chunk.checksum_sha256` — nessuna colonna nuova su `documenti`.
- Tag: denormalizzare `documento.getTags()` sui chunk; eliminati `AiDocumentoMeta`/`AiDocumentaleService`/`AiDocumentiController`/`DocumentoKbVM` (il CRUD documenti esiste già in `DocumentoController`); catalogo tag via query distinct su `documenti.tags`.
- `WikiPageGenerator.generaPaginaDocumento(Documento doc, String text)`: pageKey `doc-{id}`, scoping per stabilimento (skip se `doc.getStabilimento() == null` → pagina globale).

**Eventi (nuovi):**
- Record `DocumentoModificatoEvent(Long documentoId)` e `DocumentoEliminatoEvent(Long documentoId)`.
- Pubblicati con `ApplicationEventPublisher` da `DocumentoService.uploadDocumento(...)` (dopo il save), `deleteDocumento(...)`, `aggiornaMetadatiDms(...)` (se cambiano i tag); opzionale `EstrazioneDocumentoService` dove archivia documenti.
- `DocumentoIndicizzazioneListener` (`@Async @EventListener`) invariato salvo import; `@EnableAsync` già su `AiModuleConfig`.

**`AiBackfillService`**: `reindicizza(Long stabilimentoId)` — itera i documenti dello stabilimento con `isVersioneCorrente` + i documenti globali; proietta sempre il grafo strutturato.

## 6. AiaStructuredKnowledgeProvider (proiezione zero-token)

Rinominare la porta `EsgStructuredKnowledgeProvider` → `AiaStructuredKnowledgeProvider` (`List<WikiPage> pagesFor(Long stabilimentoId)`); `EsgGraphProjectionService` → `AiaGraphProjectionService`. Implementazione host: `backend/src/main/java/it/grandimolini/aia/config/AiaStructuredKnowledgeProviderImpl.java`, usando i repository esistenti.

| pageKey | type | Contenuto | Archi (`kb_edge`) |
|---|---|---|---|
| `stab-{id}` | `stabilimento` | anagrafica, decreto AIA, sede | radice |
| `presc-{id}` | `prescrizione` | testo, sezione AIA, frequenza, ente | `stab → presc` (parte-di) |
| `scad-{id}` | `scadenza` | descrizione, data, stato, ricorrenza | `presc → scad`; `stab → scad` se orfana |
| `conf-{prescId}` | `conformita` | esiti verifiche della prescrizione | `presc → conf` |
| `camino-{id}` | `camino` | anagrafica punto emissione, parametri autorizzati e limiti | `stab → camino` |
| `param-{caminoId}-{param}` | `parametro` | limite, unità, ultime misure/superamenti (`DatiAmbientali`/`RilevazioneMisura`) | `camino → param` (misura) |
| `produzione-{stabId}-{anno}` | `produzione` | aggregato annuo `VoceProduzione` | `stab → produzione` |
| `rifiuti-{stabId}-{anno}` | `rifiuti` | aggregato per `CodiceRifiuto`/`MovimentoRifiuto` | `stab → rifiuti` |
| `autorizzazioni-{stabId}` | `autorizzazione` | `AltraAutorizzazione` | `stab → autorizzazioni` |
| `enti-{stabId}` | `enti` | `ComunicazioneEnte`/`RecapitoEnte` | `stab → enti` |

Cardinalità: aggregare le scadenze chiuse in una pagina riassuntiva per prescrizione; pagine singole solo per scadenze aperte/future. Le pagine documento (`doc-{id}`) si linkano al grafo per matching lessicale dei titoli (logica esistente, invariata).

## 7. Sicurezza

- `SecurityConfig` AIA usa authority `ROLE_<Ruolo>` (ADMIN/RESPONSABILE/OPERATORE) con `@EnableMethodSecurity`. Le `@Secured({"AMMINISTRATORE","SUPER_ADMIN"})` dell'ESG **non funzionerebbero**. Su `AiController`:
  - `POST /api/ai/chiedi`, `POST /api/ai/agente`, `GET /api/ai/wiki`, `GET /api/ai/wiki/{pageKey}` → `@PreAuthorize("@stabilimentoAccessChecker.hasAccessToStabilimento(#stabilimentoId)")`.
  - `POST /api/ai/reindex` → `@PreAuthorize("@stabilimentoAccessChecker.isAdmin()")`.
- Nessuna modifica a `SecurityConfig.filterChain` (le route `/api/**` sono già `authenticated()` via JWT filter).
- `BudgetExceededException` → HTTP 429 (handler nel controller, invariato).

## 8. Infrastruttura

- **pgvector**: in `docker-compose.yml`, `image: postgres:16-alpine` → **`pgvector/pgvector:pg16`** (volume `aia-db-data` compatibile, stessa major). L'estensione è creata dal DDL (`CREATE EXTENSION IF NOT EXISTS vector`; l'utente del container è superuser).
- **DDL**: AIA non usa Flyway (`ddl-auto=update` + seed via `SqlLoaderService`). Creare `it.grandimolini.aia.ai.config.AiDdlRunner` (bean dentro `AiModuleConfig`, attivo solo a flag on) che all'avvio esegue `classpath:db/ai-module-pgvector.sql` con `ScriptUtils` — DDL già idempotente. Adattare lo script: FK → `public.stabilimenti(id)`, rimozione `anno` da `kb_md_page`/unique key, `ai_token_usage(giorno)`. Guard: eseguire solo su PostgreSQL (test H2 hanno comunque flag off).
- **Config/env**:
  - Copiare `application-ai.properties` in `backend/src/main/resources/` e importarla (`spring.config.import=classpath:application-ai.properties`).
  - Flag rinominato: `tenant.feature.ai-assistant` → **`aia.feature.ai-assistant`** (aggiornare `@ConditionalOnProperty` in `AiModuleConfig`). Default `false` = modulo completamente inerte (nessun bean, nessun listener, nessuna chiamata LLM).
  - Modelli per fascia come nel sorgente (Haiku simple/ingest, Sonnet standard, Opus complex), prompt caching attivo.
  - **`ANTHROPIC_API_KEY` già presente** in AIA per `aia.estrazione.ai.*`: stessa variabile, nessun secret nuovo.
  - `docker-compose.yml` (blocco `backend.environment`): `AIA_AI_ASSISTANT_ENABLED: ${AIA_AI_ASSISTANT_ENABLED:-false}` → `aia.feature.ai-assistant`; `ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY:-}`.
  - `.env.example`: aggiungere `ANTHROPIC_API_KEY=` e `AIA_AI_ASSISTANT_ENABLED=false`.
  - **GitHub Actions** (`deploy.yml`): nessuna modifica — i segreti vivono in `/opt/aia/.env` sul VPS (documentare in `DEPLOY.md` l'aggiunta manuale delle due variabili).

## 9. Frontend

**Frontend attivo: `frontend_perform/`** (è quello buildato da `nginx/Dockerfile` → `dist/frontend_perform/browser`; `frontend/` è una variante non deployata). Stack identico all'ESG (Angular 19 standalone + Bootstrap 5.3 + ng-bootstrap + ngx-bootstrap-icons): SCSS/HTML dei componenti ESG portabili quasi invariati.

Da creare in `frontend_perform/src/app/`:
- `services/ai-assistant.service.ts` — port del service ESG; base URL `environment.apiUrl`; firme con `stabilimentoId` al posto di `anno`: `chiedi(stabilimentoId, prompt, tags)`, `agente(...)`, `wiki(stabilimentoId)`, `wikiPagina(...)`, `reindex(stabilimentoId)`.
- `components/ai-assistant/chat.component.{ts,html,scss}` — port della chat ESG (include navigazione wiki), con **selettore stabilimento** in testa (lista da `ApiService.getStabilimenti()` filtrata per accesso utente, pattern già usato altrove).
- **Non portare** `documenti.component.*`: i documenti si gestiscono in `/documenti`; aggiungere lì un bottone "Reindicizza KB" (solo ADMIN) che chiama `reindex`.
- `ai-assistant-bar.component.*`: opzionale, fase 2.

Da modificare:
- `frontend_perform/src/app/app.routes.ts`: rotta `{ path: 'ai-assistant', component: AiAssistantChatComponent, canActivate: [AuthGuard] }` (componenti eager, stile esistente).
- `components/navbar/navbar.component.html`: voce "Assistente AI" (icona `robot`/`chatDots`); visibile a tutti i ruoli autenticati, enforcement lato backend. Gestire nel componente il caso modulo spento (endpoint assenti → messaggio).

## 10. Breakdown dei task

**Fase 1 — Backend: porting e adattamento (2-4 gg)**
1. `backend/pom.xml`: BOM langchain4j + dipendenze (§3), allineamento POI 5.2.3.
2. Copia sorgenti in `backend/src/main/java/it/grandimolini/aia/ai/` (esclusi `demo`, `documentale`, `AiDocumentoMeta*`); rinomina package; rimozione import springdoc.
3. Tenancy (§4): entità `KbMdPage/KbEdge/KbChunk/AiTokenUsage`, repository, query native, servizi `graph/`, `rag/`, `agent/`, `guardrail/`.
4. Ingest (§5): `AiIngestService`, `WikiPageGenerator`, `AiBackfillService` su `DocumentoRepository`/`FileStorageService`; utility SHA-256; eventi + publish in `DocumentoService`.
5. Porta `AiaStructuredKnowledgeProvider` + `AiaGraphProjectionService`; impl in `it/grandimolini/aia/config/` (§6).
6. `AiController`: parametro `stabilimentoId`, `@PreAuthorize` (§7); rimozione `OrgContextResolver`.
7. `AiModuleConfig`: flag `aia.feature.ai-assistant`. **Attenzione**: il package ai è sotto-package di `it.grandimolini.aia` → component scan ed entity discovery di default già ok; **rimuovere** eventuale `@EnableJpaRepositories` limitato al package ai (romperebbe i repository host) e lasciare l'autoconfigurazione Boot.
8. Resources: `application-ai.properties` (flag rinominato), `db/ai-module-pgvector.sql` adattato, `AiDdlRunner`.
9. Copia test unitari; `mvn test` (profilo H2, flag off).

**Fase 2 — Infra (0.5 gg)**
10. `docker-compose.yml` (immagine pgvector + env backend); `.env.example`; `backend/Dockerfile` runtime → `eclipse-temurin:21-jre` (v. rischio ONNX); `DEPLOY.md` (variabili su `/opt/aia/.env`).

**Fase 3 — Frontend (1-2 gg)**
11. `frontend_perform/src/app/services/ai-assistant.service.ts`.
12. `components/ai-assistant/chat.component.*` con selettore stabilimento + vista wiki.
13. `app.routes.ts`, `navbar.component.html`, bottone reindex in `documenti-list.component`.

**Fase 4 — Verifica end-to-end**
- `docker compose up --build` con `.env`: `AIA_AI_ASSISTANT_ENABLED=true` e `ANTHROPIC_API_KEY` reale.
- Log: `AiDdlRunner` applica il DDL; `psql`: `\dx` → `vector`, `\d kb_md_page` → colonna `embedding vector(384)` e indici HNSW.
- Login admin, upload PDF in `/documenti` → ingest asincrono in log, righe in `kb_chunk`/`kb_md_page` (`doc-{id}`).
- `POST /api/ai/reindex?stabilimentoId=1` → pagine strutturate (`stab-1`, `presc-*`, `scad-*`, …) e archi in `kb_edge`.
- UI `/ai-assistant`: "Quali scadenze AIA ha lo stabilimento X nei prossimi 3 mesi?" → risposta con fonti; `/agente`: "genera un grafico dei rifiuti prodotti" (ChartSpec/Forecaster).
- Budget: abbassare `ai.guardrail.daily-token-budget-per-tenant` → verificare 429.
- Flag off: riavvio con `AIA_AI_ASSISTANT_ENABLED=false` → endpoint `/api/ai/**` assenti, nessun bean AI, app sana.

## 11. Rischi e incompatibilità

1. **ONNX Runtime su Alpine/musl** (rischio più alto): il runtime attuale è `eclipse-temurin:21-jre-alpine`; le native library ONNX sono glibc → passare a `eclipse-temurin:21-jre` (Debian). Jar +~90MB (modello MiniLM embedded), ~200MB RAM aggiuntiva sul VPS (`MaxRAMPercentage=75` da verificare). Runner self-hosted ARM64: onnxruntime pubblica binari linux-aarch64 glibc, ok su base Debian.
2. **Conflitto POI** 5.2.3 (AIA) vs 5.3.0 (ai-module) vs Tika transitiva → allineare a 5.2.3.
3. **`@EnableJpaRepositories` scoping**: importare `AiModuleConfig` così com'è limiterebbe lo scan dei repository, rompendo quelli host → correggere (§10 task 7).
4. **`@Secured` con ruoli ESG** incompatibile → `@PreAuthorize` sui ruoli AIA.
5. **`anno NOT NULL`** nel DDL/unique key ESG incompatibile con il dominio AIA → schema rivisto (§4).
6. **`frontend/` vs `frontend_perform/`**: solo il secondo è deployato; il port va lì (attenzione alle divergenze).
7. **Test H2**: le query native pgvector non girano su H2 → coperte dal flag off di default nei test.
8. **Privacy/DPA**: i documenti AIA transitano verso il provider LLM per riassunti e risposte — verificare l'accordo di trattamento dati col provider o prevedere il profilo on-prem (`ai.llm.provider=openai` + base-url Ollama/vLLM), già supportato dal modulo.

## 12. Verifica ambiente di deploy (eseguita il 04/07/2026 sul VPS di produzione)

VPS Hetzner `ubuntu-4gb-nbg1-1` (ARM64/aarch64, Ubuntu 24.04): 2 vCPU, 3,7 GB RAM, disco 38 GB. Il runner GitHub Actions self-hosted gira **sulla stessa macchina** (`/home/runner/actions-runner`), quindi build e deploy condividono disco e RAM.

**Esito: il modulo è implementabile.** Verifiche puntuali:

| Aspetto | Stato rilevato | Valutazione |
|---|---|---|
| PostgreSQL | 16.13 su `postgres:16-alpine` (musl); estensione `vector` **non disponibile** (verificato: `pg_available_extensions` = 0) | Cambio immagine necessario, come previsto (§8) |
| Immagine pgvector | `pgvector/pgvector:pg16` pubblica manifest **linux/arm64** (verificato) | Compatibile col VPS |
| Dati DB | `aia_management` = **9,8 MB**; volume `aia-db-data` = 50 MB | Migrazione banale |
| Documenti | volume `aia-uploads` = **18,5 MB** | KB vettoriale stimata in decine di MB (vettore 384 dim ≈ 1,5 KB/chunk + indici HNSW) |
| Disco | 21 GB liberi (44% usato); **9,87 GB occupati da 54 immagini Docker accumulate** (il `docker image prune -f` del workflow non rimuove le immagini taggate vecchie) | OK, ma serve pulizia (v. sotto) |
| RAM | 1,0 GB usata (backend 393 MB, db 44 MB, runner ~120 MB); 2,7 GB disponibili; **nessuno swap** | Modulo AI: +200–400 MB stabili (ONNX+modello) + picchi Tika → backend ~0,7–1 GB; margine sufficiente |
| Immagine backend | 621 MB (`eclipse-temurin:21-jre-alpine`, `MaxRAMPercentage=75`, nessun `mem_limit` nel compose) | Con base Debian + modello + Tika → ~1 GB per versione |
| CPU | 2 vCPU ARM | Embeddings MiniLM in-process ok per i volumi AIA (decine di ms/chunk); la chat usa API esterne |

**Interventi aggiuntivi raccomandati (oltre a quelli in §8):**
1. **Cambio immagine Postgres con dump/restore, non con riuso diretto del volume**: il passaggio alpine (musl) → `pgvector/pgvector:pg16` (Debian/glibc) cambia le collation e può invalidare gli indici testuali. Con 9,8 MB di dati: `pg_dumpall` → nuovo volume → restore (pochi minuti). In alternativa `REINDEX DATABASE` subito dopo lo switch.
2. **Pulizia immagini nel workflow di deploy**: sostituire `docker image prune -f` con `docker image prune -af --filter "until=168h"` (o mantenere solo le ultime N tag): con immagini da ~1 GB l'accumulo attuale (~1,4 GB per deploy) esaurirebbe il disco in qualche decina di deploy.
3. **`mem_limit` sul backend** (es. 1,5 GB) nel compose: oggi `MaxRAMPercentage=75` senza limite container significa heap max teorico di 2,8 GB, che affamerebbe DB e runner.
4. **Swapfile di sicurezza da 2 GB** sul VPS (oggi assente): protegge dai picchi transitori di ingest (Tika su PDF grandi) senza OOM-kill.

Nota a margine rilevata durante la verifica: il container `aia-nginx` risultava **unhealthy da 3 mesi** pur funzionando. Causa: nel container `localhost` risolve prima su `::1` (IPv6) dove nginx non ascolta (solo `0.0.0.0:80`). Corretto usando `http://127.0.0.1/nginx-health` nei healthcheck di `docker-compose.yml` e `nginx/Dockerfile` — attivo dal prossimo deploy.

**Opzione Ollama (LLM on-premise) sul VPS attuale: non praticabile.** Ollama supporta linux/arm64 e si installerebbe senza problemi, ma con 2 vCPU ARM senza GPU e ~2,6 GB di RAM disponibili ci starebbe solo un modello 1B quantizzato (qualità insufficiente per RAG grounded in italiano e function-calling) — un 3B q4 richiede ~2,5 GB e andrebbe in OOM col backend, un 7-8B non ci sta proprio. Alternative in ordine di praticità: (a) restare su API Anthropic (setup attuale, DPA col provider); (b) endpoint Ollama/vLLM **su una macchina separata** (server aziendale o VPS dedicato) puntato via `ai.llm.provider=openai` + `ai.openai.base-url` — il modulo lo supporta nativamente e non richiede colocazione; (c) upgrade del VPS ad almeno CAX31 (8 vCPU/16 GB, ~13€/mese) per un 7-8B q4 a ~5-10 token/s, accettabile per un pilota ma lento in chat interattiva. Gli embeddings restano comunque locali (ONNX in-process) in tutti gli scenari.

## 13. Costi di esercizio (indicazioni dal modulo sorgente)

Il costo token è concentrato ~90% nelle risposte dell'agente; embedding, grafo, retrieval, tool e OCR sono a zero token (locali). Il routing per complessità (Haiku/Sonnet/Opus) e il prompt caching riducono il costo medio per domanda. Budget giornaliero configurabile (`daily-token-budget` → HTTP 429 a superamento). Riferimento: `performplus-esg/backend/ai-module/docs/COSTI.md`.

### 13.1 Confronto costi: API Anthropic vs Ollama su VM dedicata (verificato 05/07/2026)

Il modulo è multi-provider: il passaggio a Ollama è solo configurazione (`ai.llm.provider=openai` + `ai.openai.base-url`). Gli embedding restano locali (ONNX) in entrambi gli scenari; il confronto riguarda solo l'LLM di chat/agente.

**Scenario A — API Anthropic** (listini luglio 2026: Haiku 4.5 1$/5$ per Mtoken in/out, Sonnet 3$/15$, Opus 4.8 5$/25$). Domanda RAG tipica ~4K token input / ~500 output:

| Fascia | Costo per domanda |
|---|---|
| Haiku (semplici, ingest) | ~0,007 $ |
| Sonnet (standard) | ~0,02 $ |
| Opus (complesse) | ~0,03–0,05 $ |

Chat agentiche multi-round: 2-4× una domanda singola. Con prompt caching e routing attivi, **stima per la demo (200–500 domande/mese): 3–10 €/mese**; uso intenso (1.000 domande + agente): < 30 €/mese. Tetto rigido garantito dal guardrail `daily-token-budget`. Ingest documenti (18,5 MB attuali): centesimi, una tantum.

**Scenario B — Ollama su VM separata** (prezzi Hetzner post-adeguamento giugno 2026):

| Opzione | Specifiche | Costo | Cosa ci gira |
|---|---|---|---|
| CAX31 | 8 vCPU ARM / 16 GB | ~16 €/mese (+0,50 € IPv4) | 7-8B q4 a ~5-10 token/s — usabile ma lento |
| CAX41 | 16 vCPU ARM / 32 GB | ~31,50 €/mese | 7-8B più fluido, o 14B q4 |
| GEX44 (dedicato GPU, RTX 4000 Ada 20 GB) | — | 184 €/mese + 79 € setup | 7-14B veloci, latenza da prodotto |
| GEX130 (dedicato GPU, RTX 6000 Ada 48 GB) | — | 838 €/mese | 70B quantizzato, qualità paragonabile alle API |

Interventi necessari nello scenario B: provisioning VM nello stesso progetto Hetzner; install Ollama + pull modello 7-8B quantizzato (~5 GB); **rete privata Hetzner Cloud** (gratuita) tra VPS e VM — Ollama non ha autenticazione, la porta 11434 non va mai esposta; config in `/opt/aia/.env` (`ai.llm.provider=openai`, `base-url` sull'IP privato, prompt caching off, routing a fascia unica); validazione del function-calling, che sui 7-8B è inaffidabile (a rischio grafici/report/forecast della chat agentica) e del RAG grounded in italiano.

**Conclusione:** alla scala della demo le API vincono nettamente — 3–10 €/mese contro i 16 €/mese della VM più economica, con qualità superiore (function-calling) e latenza migliore; pareggiare la qualità on-prem richiede il server GPU da 184–838 €/mese. L'unica ragione valida per Ollama non è il costo ma la sovranità del dato (rischio §11.8): in quel caso, pilota su CAX31 accettando i limiti di qualità, con rientro alle API possibile cambiando due variabili d'ambiente. A regime i due scenari possono convivere (provider configurabile per installazione).
