package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.ParametroMonitoraggio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroMonitoraggioRepository extends JpaRepository<ParametroMonitoraggio, Long> {
    List<ParametroMonitoraggio> findByMonitoraggioId(Long monitoraggioId);
    List<ParametroMonitoraggio> findByMonitoraggioIdAndAttivoTrue(Long monitoraggioId);
}
