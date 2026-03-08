package it.grandimolini.aia.service;

import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.Documento;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.DocumentoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import it.grandimolini.aia.specification.DocumentoSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentoService {

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload documento con salvataggio transazionale DB + filesystem
     */
    @Transactional
    public Documento uploadDocumento(
            MultipartFile file,
            Long stabilimentoId,
            Documento.TipoDocumento tipoDocumento,
            String descrizione,
            Long prescrizioneId,
            String enteDestinatario,
            Integer anno,
            String createdBy
    ) {
        // Verifica stabilimento esiste
        Stabilimento stabilimento = stabilimentoRepository.findById(stabilimentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", stabilimentoId));

        // Determina categoria per organizzazione file
        String category = getCategoryFromTipoDocumento(tipoDocumento);

        // Store file fisico
        String filePath = fileStorageService.storeFile(file, stabilimentoId, category);

        // Crea entity documento
        Documento documento = new Documento();
        documento.setStabilimento(stabilimento);
        documento.setNome(file.getOriginalFilename());
        documento.setNomeFile(file.getOriginalFilename());
        documento.setTipoDocumento(tipoDocumento);
        documento.setDescrizione(descrizione);
        documento.setFilePath(filePath);
        documento.setFileSize(file.getSize());
        documento.setMimeType(file.getContentType());
        documento.setEnteDestinatario(enteDestinatario);
        documento.setAnno(anno);
        documento.setCreatedBy(createdBy);

        // Se è associato a una prescrizione, impostala
        if (prescrizioneId != null) {
            // Nota: qui potresti voler validare che la prescrizione esista
            // Ma per ora lasciamo semplice
        }

        return documentoRepository.save(documento);
    }

    /**
     * Download documento
     */
    public Resource downloadDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        return fileStorageService.loadFileAsResource(documento.getFilePath());
    }

    /**
     * Delete documento con rimozione transazionale DB + filesystem
     */
    @Transactional
    public void deleteDocumento(Long documentoId) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        // Elimina prima il record dal DB
        documentoRepository.delete(documento);

        // Poi elimina il file fisico
        try {
            fileStorageService.deleteFile(documento.getFilePath());
        } catch (Exception e) {
            // Log error ma non fare rollback della transazione
            System.err.println("Warning: Could not delete physical file: " + documento.getFilePath());
        }
    }

    /**
     * Get documento by ID
     */
    public Documento getDocumentoById(Long documentoId) {
        return documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));
    }

    /**
     * List documenti per stabilimento
     */
    public List<Documento> getDocumentiByStabilimento(Long stabilimentoId) {
        // Verifica stabilimento esiste
        if (!stabilimentoRepository.existsById(stabilimentoId)) {
            throw new ResourceNotFoundException("Stabilimento", "id", stabilimentoId);
        }
        return documentoRepository.findByStabilimentoId(stabilimentoId);
    }

    /**
     * List documenti per prescrizione
     */
    public List<Documento> getDocumentiByPrescrizione(Long prescrizioneId) {
        return documentoRepository.findByPrescrizioneId(prescrizioneId);
    }

    /**
     * List documenti per anno
     */
    public List<Documento> getDocumentiByAnno(Integer anno) {
        return documentoRepository.findByAnno(anno);
    }

    /**
     * Get all documenti
     */
    public List<Documento> getAllDocumenti() {
        return documentoRepository.findAll();
    }

    /**
     * Update documento metadata (non il file)
     */
    @Transactional
    public Documento updateDocumento(Long documentoId, String descrizione, String enteDestinatario) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        if (descrizione != null) {
            documento.setDescrizione(descrizione);
        }
        if (enteDestinatario != null) {
            documento.setEnteDestinatario(enteDestinatario);
        }

        return documentoRepository.save(documento);
    }

    /**
     * Aggiorna i metadati DMS di un documento (stato, oggetto, ente, protocollazione)
     */
    @Transactional
    public Documento aggiornaMetadatiDms(
            Long documentoId,
            Documento.StatoDocumento stato,
            String oggetto,
            String enteEmittente,
            String numeroProtocollo,
            String tags
    ) {
        Documento documento = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento", "id", documentoId));

        if (stato != null)             documento.setStatoDocumento(stato);
        if (oggetto != null)           documento.setOggetto(oggetto);
        if (enteEmittente != null)     documento.setEnteEmittente(enteEmittente);
        if (numeroProtocollo != null)  documento.setNumeroProtocollo(numeroProtocollo);
        if (tags != null)              documento.setTags(tags);

        return documentoRepository.save(documento);
    }

    /**
     * Ricerca documenti con filtri e paginazione
     */
    public Page<Documento> searchDocumenti(
            Long stabilimentoId,
            Documento.TipoDocumento tipoDocumento,
            Integer anno,
            String keyword,
            Pageable pageable
    ) {
        Specification<Documento> spec = DocumentoSpecification.withFilters(
                stabilimentoId, tipoDocumento, anno, keyword
        );
        return documentoRepository.findAll(spec, pageable);
    }

    /**
     * Determina categoria directory da tipo documento
     */
    private String getCategoryFromTipoDocumento(Documento.TipoDocumento tipo) {
        return switch (tipo) {
            case DECRETO_AIA        -> "decreti";
            case PRESCRIZIONE_AIA   -> "prescrizioni";
            case RAPPORTO_PROVA     -> "monitoraggi";
            case DATI_LABORATORIO   -> "monitoraggi";
            case PMC_ANNUALE        -> "pmc";
            case RELAZIONE_ANNUALE  -> "relazioni";
            case COMUNICAZIONE_PEC  -> "comunicazioni";
            case PEC_RICEVUTA       -> "comunicazioni";
            case VALUTAZIONE_ACUSTICA -> "studi_tecnici";
            case STUDIO_TECNICO     -> "studi_tecnici";
            case REGISTRO_OPERATIVO -> "registri";
            case FORMULARIO_RIFIUTI -> "rifiuti";
            case INTEGRAZIONE       -> "integrazioni";
            case VERBALE            -> "verbali";
            case PLANIMETRIA        -> "planimetrie";
            case ALTRO              -> "altri";
        };
    }
}
