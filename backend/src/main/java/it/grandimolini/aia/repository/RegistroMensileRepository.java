package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.RegistroMensile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroMensileRepository extends JpaRepository<RegistroMensile, Long> {

    List<RegistroMensile> findByStabilimentoIdOrderByAnnoDescMeseDesc(Long stabilimentoId);

    List<RegistroMensile> findByAnnoOrderByStabilimentoNomeAscMeseAsc(Integer anno);

    List<RegistroMensile> findByStabilimentoIdAndAnnoOrderByMeseAsc(Long stabilimentoId, Integer anno);

    Optional<RegistroMensile> findByStabilimentoIdAndAnnoAndMese(Long stabilimentoId, Integer anno, Integer mese);

    @Query("SELECT DISTINCT r.anno FROM RegistroMensile r ORDER BY r.anno DESC")
    List<Integer> findAnniDistinti();

    @Query("SELECT DISTINCT r.anno FROM RegistroMensile r WHERE r.stabilimento.id = :stabilimentoId ORDER BY r.anno DESC")
    List<Integer> findAnniByStabilimento(@Param("stabilimentoId") Long stabilimentoId);
}
