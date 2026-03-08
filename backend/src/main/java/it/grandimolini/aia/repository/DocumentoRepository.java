package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Long>, JpaSpecificationExecutor<Documento> {
    List<Documento> findByPrescrizioneId(Long prescrizioneId);
    List<Documento> findByStabilimentoId(Long stabilimentoId);
    List<Documento> findByAnno(Integer anno);
}
