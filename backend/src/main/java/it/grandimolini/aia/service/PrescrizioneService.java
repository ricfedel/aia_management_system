package it.grandimolini.aia.service;

import it.grandimolini.aia.model.Prescrizione;
import it.grandimolini.aia.repository.PrescrizioneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class PrescrizioneService {

    @Autowired
    private PrescrizioneRepository prescrizioneRepository;

    public List<Prescrizione> findAll() {
        return prescrizioneRepository.findAll();
    }

    public List<Prescrizione> findByStabilimento(Long stabilimentoId) {
        return prescrizioneRepository.findByStabilimentoId(stabilimentoId);
    }

    public Optional<Prescrizione> findById(Long id) {
        return prescrizioneRepository.findById(id);
    }

    public Prescrizione save(Prescrizione prescrizione) {
        return prescrizioneRepository.save(prescrizione);
    }

    public void deleteById(Long id) {
        prescrizioneRepository.deleteById(id);
    }

    public List<Prescrizione> findByStato(Prescrizione.StatoPrescrizione stato) {
        return prescrizioneRepository.findByStato(stato);
    }
}
