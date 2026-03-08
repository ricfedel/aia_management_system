package it.grandimolini.aia.service;

import it.grandimolini.aia.model.Monitoraggio;
import it.grandimolini.aia.repository.MonitoraggioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonitoraggioService {

    @Autowired
    private MonitoraggioRepository monitoraggioRepository;

    public List<Monitoraggio> findAll() {
        return monitoraggioRepository.findAll();
    }

    public Optional<Monitoraggio> findById(Long id) {
        return monitoraggioRepository.findById(id);
    }

    public List<Monitoraggio> findByStabilimentoId(Long stabilimentoId) {
        return monitoraggioRepository.findByStabilimentoId(stabilimentoId);
    }

    public List<Monitoraggio> findAllAttivi() {
        return monitoraggioRepository.findByAttivoTrue();
    }

    public Monitoraggio save(Monitoraggio monitoraggio) {
        return monitoraggioRepository.save(monitoraggio);
    }

    public void deleteById(Long id) {
        monitoraggioRepository.deleteById(id);
    }
}
