package it.grandimolini.aia.scheduler;

import it.grandimolini.aia.model.TaskProcesso;
import it.grandimolini.aia.model.TaskProcesso.StatoTask;
import it.grandimolini.aia.model.TaskProcesso.TipoTask;
import it.grandimolini.aia.repository.TaskProcessoRepository;
import it.grandimolini.aia.service.BpmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler che controlla i SERVICE_TASK di tipo TIMER in attesa
 * e li sblocca quando la dataEsecuzioneProgrammata è trascorsa.
 */
@Component
public class BpmSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(BpmSchedulerService.class);

    @Autowired private TaskProcessoRepository taskRepo;
    @Autowired private BpmService bpmService;

    /** Controlla ogni minuto se ci sono timer scaduti da sbloccare */
    @Scheduled(fixedDelay = 60_000)
    public void processTimerTasks() {
        List<TaskProcesso> timerTask = taskRepo.findByTipoTaskAndStatoTaskAndDataEsecuzioneProgrammataBefore(
                TipoTask.SERVICE_TASK, StatoTask.IN_CORSO, LocalDateTime.now());
        for (TaskProcesso t : timerTask) {
            try {
                log.info("Timer scaduto per task [{}] - sblocco processo {}",
                         t.getNomeTask(), t.getProcesso().getId());
                bpmService.avanzaDaTimer(t.getProcesso().getId(), t.getId());
            } catch (Exception e) {
                log.error("Errore sblocco timer task {}: {}", t.getId(), e.getMessage());
            }
        }
    }
}
