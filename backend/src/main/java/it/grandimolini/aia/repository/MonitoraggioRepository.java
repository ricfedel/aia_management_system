package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.Monitoraggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MonitoraggioRepository extends JpaRepository<Monitoraggio, Long> {
    List<Monitoraggio> findByStabilimentoId(Long stabilimentoId);
    List<Monitoraggio> findByAttivoTrue();
}
