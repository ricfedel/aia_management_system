package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.MovimentoRifiuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentoRifiutoRepository extends JpaRepository<MovimentoRifiuto, Long> {

    List<MovimentoRifiuto> findByCodiceRifiutoIdOrderByAnnoDescMeseDescCreatedAtDesc(Long codiceRifiutoId);

    List<MovimentoRifiuto> findByCodiceRifiutoIdAndAnnoOrderByMeseAscTipoMovimentoAsc(Long codiceRifiutoId, Integer anno);

    List<MovimentoRifiuto> findByCodiceRifiutoIdAndAnnoAndMese(Long codiceRifiutoId, Integer anno, Integer mese);

    @Query("""
        SELECT m FROM MovimentoRifiuto m
        WHERE m.codiceRifiuto.stabilimento.id = :stabilimentoId
          AND m.anno = :anno
        ORDER BY m.codiceRifiuto.codiceCer ASC, m.mese ASC
    """)
    List<MovimentoRifiuto> findByStabilimentoAndAnno(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("anno") Integer anno);

    @Query("""
        SELECT m FROM MovimentoRifiuto m
        WHERE m.codiceRifiuto.stabilimento.id = :stabilimentoId
          AND m.anno = :anno
          AND m.mese = :mese
        ORDER BY m.codiceRifiuto.codiceCer ASC, m.tipoMovimento ASC
    """)
    List<MovimentoRifiuto> findByStabilimentoAndAnnoAndMese(
            @Param("stabilimentoId") Long stabilimentoId,
            @Param("anno") Integer anno,
            @Param("mese") Integer mese);

    @Query("SELECT DISTINCT m.anno FROM MovimentoRifiuto m WHERE m.codiceRifiuto.stabilimento.id = :stabilimentoId ORDER BY m.anno DESC")
    List<Integer> findAnniByStabilimento(@Param("stabilimentoId") Long stabilimentoId);
}
