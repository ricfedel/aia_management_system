package it.grandimolini.aia.specification;

import it.grandimolini.aia.model.Documento;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class DocumentoSpecification {

    /**
     * Crea specification per ricerca documenti con filtri multipli
     */
    public static Specification<Documento> withFilters(
            Long stabilimentoId,
            Documento.TipoDocumento tipoDocumento,
            Integer anno,
            String keyword
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filtro per stabilimento
            if (stabilimentoId != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("stabilimento").get("id"), stabilimentoId
                ));
            }

            // Filtro per tipo documento
            if (tipoDocumento != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("tipoDocumento"), tipoDocumento
                ));
            }

            // Filtro per anno
            if (anno != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("anno"), anno
                ));
            }

            // Filtro per keyword (cerca in nome, nomeFile e descrizione)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";

                Predicate nomeMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")), likePattern
                );
                Predicate nomeFileMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nomeFile")), likePattern
                );
                Predicate descrizioneMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("descrizione")), likePattern
                );

                // OR tra i tre campi
                predicates.add(criteriaBuilder.or(nomeMatch, nomeFileMatch, descrizioneMatch));
            }

            // AND tra tutti i predicati
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification per documenti di uno stabilimento
     */
    public static Specification<Documento> hasStabilimento(Long stabilimentoId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("stabilimento").get("id"), stabilimentoId);
    }

    /**
     * Specification per tipo documento
     */
    public static Specification<Documento> hasTipoDocumento(Documento.TipoDocumento tipo) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("tipoDocumento"), tipo);
    }

    /**
     * Specification per anno
     */
    public static Specification<Documento> hasAnno(Integer anno) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("anno"), anno);
    }

    /**
     * Specification per keyword (cerca in nome, nomeFile, descrizione)
     */
    public static Specification<Documento> containsKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + keyword.toLowerCase() + "%";

            Predicate nomeMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nome")), likePattern
            );
            Predicate nomeFileMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("nomeFile")), likePattern
            );
            Predicate descrizioneMatch = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("descrizione")), likePattern
            );

            return criteriaBuilder.or(nomeMatch, nomeFileMatch, descrizioneMatch);
        };
    }

    /**
     * Specification per prescrizione
     */
    public static Specification<Documento> hasPrescrizione(Long prescrizioneId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("prescrizione").get("id"), prescrizioneId);
    }

    /**
     * Specification per ente destinatario
     */
    public static Specification<Documento> hasEnteDestinatario(String enteDestinatario) {
        return (root, query, criteriaBuilder) -> {
            if (enteDestinatario == null || enteDestinatario.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("enteDestinatario")),
                    "%" + enteDestinatario.toLowerCase() + "%"
            );
        };
    }
}
