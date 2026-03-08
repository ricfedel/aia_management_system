package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.TaskProcesso;
import it.grandimolini.aia.model.TaskProcesso.StatoTask;
import it.grandimolini.aia.model.TaskProcesso.TipoTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskProcessoRepository extends JpaRepository<TaskProcesso, Long> {

    List<TaskProcesso> findByProcessoId(Long processoId);

    List<TaskProcesso> findByAssegnatoA(String username);

    List<TaskProcesso> findByStatoTask(StatoTask statoTask);

    @Query("SELECT t FROM TaskProcesso t WHERE t.assegnatoA = :username AND t.statoTask IN ('CREATO','IN_CORSO')")
    List<TaskProcesso> findTaskAttiviPerUtente(@Param("username") String username);

    @Query("SELECT t FROM TaskProcesso t WHERE t.processo.id = :processoId AND t.statoTask IN ('CREATO','IN_CORSO')")
    Optional<TaskProcesso> findTaskCorrenteByProcesso(@Param("processoId") Long processoId);

    @Query("SELECT COUNT(t) FROM TaskProcesso t WHERE t.assegnatoA = :username AND t.statoTask IN ('CREATO','IN_CORSO')")
    long countTaskAttiviPerUtente(@Param("username") String username);

    @Query("SELECT t FROM TaskProcesso t WHERE t.tipoTask = :tipoTask AND t.statoTask = :statoTask AND t.dataEsecuzioneProgrammata IS NOT NULL AND t.dataEsecuzioneProgrammata <= :now")
    List<TaskProcesso> findByTipoTaskAndStatoTaskAndDataEsecuzioneProgrammataBefore(
        @Param("tipoTask") TipoTask tipoTask,
        @Param("statoTask") StatoTask statoTask,
        @Param("now") LocalDateTime now);

    /**
     * Tutti i task di un processo appartenenti allo stesso ramo parallelo (AND fork).
     * Usato per verificare se tutti i rami paralleli sono completati (AND join).
     */
    @Query("SELECT t FROM TaskProcesso t WHERE t.processo.id = :processoId AND t.parallelGroupId = :groupId")
    List<TaskProcesso> findByProcessoIdAndParallelGroupId(
        @Param("processoId") Long processoId,
        @Param("groupId") String groupId);

    /**
     * Task di tipo CALL_ACTIVITY con un dato childProcessoId.
     * Usato per trovare il task padre quando un sotto-processo si completa.
     */
    @Query("SELECT t FROM TaskProcesso t WHERE t.childProcessoId = :childProcessoId")
    Optional<TaskProcesso> findByChildProcessoId(@Param("childProcessoId") Long childProcessoId);
}
