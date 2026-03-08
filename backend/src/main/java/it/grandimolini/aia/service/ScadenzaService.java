package it.grandimolini.aia.service;

import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.repository.ScadenzaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ScadenzaService {

    @Autowired
    private ScadenzaRepository scadenzaRepository;

    public List<Scadenza> findAll() {
        return scadenzaRepository.findAll();
    }

    public List<Scadenza> findByStabilimento(Long stabilimentoId) {
        return scadenzaRepository.findByStabilimentoId(stabilimentoId);
    }

    public List<Scadenza> findScadenzeImminenti(int giorni) {
        LocalDate dataLimite = LocalDate.now().plusDays(giorni);
        return scadenzaRepository.findScadenzeImminenti(dataLimite);
    }

    public List<Scadenza> findScadenzeProssimi30Giorni() {
        LocalDate oggi = LocalDate.now();
        LocalDate tra30Giorni = oggi.plusDays(30);
        return scadenzaRepository.findScadenzeInPeriodo(oggi, tra30Giorni);
    }

    public Optional<Scadenza> findById(Long id) {
        return scadenzaRepository.findById(id);
    }

    public Scadenza save(Scadenza scadenza) {
        return scadenzaRepository.save(scadenza);
    }

    public void deleteById(Long id) {
        scadenzaRepository.deleteById(id);
    }
}
