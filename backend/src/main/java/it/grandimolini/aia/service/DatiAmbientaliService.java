package it.grandimolini.aia.service;

import it.grandimolini.aia.model.DatiAmbientali;
import it.grandimolini.aia.repository.DatiAmbientaliRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class DatiAmbientaliService {

    @Autowired
    private DatiAmbientaliRepository datiAmbientaliRepository;

    public List<DatiAmbientali> findAll() {
        return datiAmbientaliRepository.findAll();
    }

    public List<DatiAmbientali> findByMonitoraggio(Long monitoraggioId) {
        return datiAmbientaliRepository.findByMonitoraggioId(monitoraggioId);
    }

    public List<DatiAmbientali> findByStabilimentoAndAnno(Long stabilimentoId, int anno) {
        return datiAmbientaliRepository.findByStabilimentoAndAnno(stabilimentoId, anno);
    }

    public Optional<DatiAmbientali> findById(Long id) {
        return datiAmbientaliRepository.findById(id);
    }

    public DatiAmbientali save(DatiAmbientali datiAmbientali) {
        return datiAmbientaliRepository.save(datiAmbientali);
    }

    public List<DatiAmbientali> findNonConformi() {
        return datiAmbientaliRepository.findByStatoConformita(DatiAmbientali.StatoConformita.NON_CONFORME);
    }

    public void deleteById(Long id) {
        datiAmbientaliRepository.deleteById(id);
    }
}
