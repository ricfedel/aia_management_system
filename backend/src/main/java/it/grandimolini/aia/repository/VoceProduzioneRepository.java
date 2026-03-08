package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.VoceProduzione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoceProduzioneRepository extends JpaRepository<VoceProduzione, Long> {

    List<VoceProduzione> findByRegistroMensileIdOrderByCategoriaAscSortOrderAscDescrizioneAsc(Long registroMensileId);

    List<VoceProduzione> findByRegistroMensileIdAndCategoria(Long registroMensileId, VoceProduzione.CategoriaVoce categoria);
}
