package it.grandimolini.aia.service;

import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StabilimentoService {

    @Autowired
    private StabilimentoRepository stabilimentoRepository;

    public List<Stabilimento> findAll() {
        return stabilimentoRepository.findAll();
    }

    public List<Stabilimento> findAllAttivi() {
        return stabilimentoRepository.findByAttivoTrue();
    }

    public Optional<Stabilimento> findById(Long id) {
        return stabilimentoRepository.findById(id);
    }

    public Optional<Stabilimento> findBySigla(String sigla) {
        return stabilimentoRepository.findBySiglaIgnoreCase(sigla);
    }

    public Stabilimento save(Stabilimento stabilimento) {
        return stabilimentoRepository.save(stabilimento);
    }

    public void deleteById(Long id) {
        stabilimentoRepository.deleteById(id);
    }
}
