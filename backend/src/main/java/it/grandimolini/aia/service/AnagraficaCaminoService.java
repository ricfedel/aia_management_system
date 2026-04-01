package it.grandimolini.aia.service;

import it.grandimolini.aia.dto.AnagraficaCaminoDTO;
import it.grandimolini.aia.exception.ResourceNotFoundException;
import it.grandimolini.aia.model.AnagraficaCamino;
import it.grandimolini.aia.model.Stabilimento;
import it.grandimolini.aia.repository.AnagraficaCaminoRepository;
import it.grandimolini.aia.repository.StabilimentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnagraficaCaminoService {

    private final AnagraficaCaminoRepository anagraficaCaminoRepository;
    private final StabilimentoRepository stabilimentoRepository;

    public AnagraficaCaminoService(AnagraficaCaminoRepository anagraficaCaminoRepository,
                                   StabilimentoRepository stabilimentoRepository) {
        this.anagraficaCaminoRepository = anagraficaCaminoRepository;
        this.stabilimentoRepository = stabilimentoRepository;
    }

    // ─── READ methods (readOnly = true) ────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AnagraficaCaminoDTO> findAllAsDTOs() {
        return anagraficaCaminoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnagraficaCaminoDTO> findByStabilimentoIdAsDTOs(Long stabilimentoId) {
        return anagraficaCaminoRepository.findByStabilimentoIdOrderBySiglaAsc(stabilimentoId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnagraficaCaminoDTO> findByStabilimentoIdAndFaseProcessoAsDTOs(
            Long stabilimentoId, AnagraficaCamino.FaseProcesso faseProcesso) {
        return anagraficaCaminoRepository.findByStabilimentoIdAndFaseProcessoOrderBySiglaAsc(stabilimentoId, faseProcesso).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnagraficaCaminoDTO> findByStabilimentoIdAndAttivoAsDTOs(Long stabilimentoId, Boolean attivo) {
        return anagraficaCaminoRepository.findByStabilimentoIdAndAttivoOrderBySiglaAsc(stabilimentoId, attivo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AnagraficaCaminoDTO> findByIdAsDTO(Long id) {
        return anagraficaCaminoRepository.findById(id).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<AnagraficaCaminoDTO> findByStabilimentoIdAndSiglaAsDTO(Long stabilimentoId, String sigla) {
        return anagraficaCaminoRepository.findByStabilimentoIdAndSigla(stabilimentoId, sigla)
                .map(this::toDTO);
    }

    // ─── WRITE methods ────────────────────────────────────────────────────

    @Transactional
    public AnagraficaCaminoDTO createFromDTO(AnagraficaCaminoDTO dto) {
        AnagraficaCamino c = new AnagraficaCamino();
        applyDtoToEntity(dto, c);
        AnagraficaCamino saved = anagraficaCaminoRepository.save(c);
        return toDTO(saved);
    }

    @Transactional
    public AnagraficaCaminoDTO updateFromDTO(Long id, AnagraficaCaminoDTO dto) {
        AnagraficaCamino c = anagraficaCaminoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AnagraficaCamino", "id", id));
        applyDtoToEntity(dto, c);
        AnagraficaCamino updated = anagraficaCaminoRepository.save(c);
        return toDTO(updated);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!anagraficaCaminoRepository.existsById(id)) {
            throw new ResourceNotFoundException("AnagraficaCamino", "id", id);
        }
        anagraficaCaminoRepository.deleteById(id);
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private AnagraficaCaminoDTO toDTO(AnagraficaCamino c) {
        return AnagraficaCaminoDTO.builder()
                .id(c.getId())
                .stabilimentoId(c.getStabilimento().getId())
                .stabilimentoNome(c.getStabilimento().getNome())
                .sigla(c.getSigla())
                .faseProcesso(c.getFaseProcesso())
                .origine(c.getOrigine())
                .portataNomc3h(c.getPortataNomc3h())
                .sezioneM2(c.getSezioneM2())
                .velocitaMs(c.getVelocitaMs())
                .temperaturaC(c.getTemperaturaC())
                .temperaturaAmbiente(c.getTemperaturaAmbiente())
                .altezzaM(c.getAltezzaM())
                .durataHGiorno(c.getDurataHGiorno())
                .durataGAnno(c.getDurataGAnno())
                .impiantoAbbattimento(c.getImpiantoAbbattimento())
                .note(c.getNote())
                .attivo(c.getAttivo())
                .createdAt(c.getCreatedAt())
                .build();
    }

    private AnagraficaCamino applyDtoToEntity(AnagraficaCaminoDTO dto, AnagraficaCamino c) {
        if (dto.getStabilimentoId() != null) {
            Stabilimento st = stabilimentoRepository.findById(dto.getStabilimentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Stabilimento", "id", dto.getStabilimentoId()));
            c.setStabilimento(st);
        }
        if (dto.getSigla() != null)                 c.setSigla(dto.getSigla().toUpperCase());
        if (dto.getFaseProcesso() != null)           c.setFaseProcesso(dto.getFaseProcesso());
        if (dto.getOrigine() != null)                c.setOrigine(dto.getOrigine());
        if (dto.getPortataNomc3h() != null)          c.setPortataNomc3h(dto.getPortataNomc3h());
        if (dto.getSezioneM2() != null)              c.setSezioneM2(dto.getSezioneM2());
        if (dto.getVelocitaMs() != null)             c.setVelocitaMs(dto.getVelocitaMs());
        // temperatura: null = ambiente
        c.setTemperaturaC(dto.getTemperaturaC());
        c.setTemperaturaAmbiente(dto.getTemperaturaAmbiente() != null ? dto.getTemperaturaAmbiente() : dto.getTemperaturaC() == null);
        if (dto.getAltezzaM() != null)               c.setAltezzaM(dto.getAltezzaM());
        if (dto.getDurataHGiorno() != null)          c.setDurataHGiorno(dto.getDurataHGiorno());
        if (dto.getDurataGAnno() != null)            c.setDurataGAnno(dto.getDurataGAnno());
        if (dto.getImpiantoAbbattimento() != null)   c.setImpiantoAbbattimento(dto.getImpiantoAbbattimento());
        if (dto.getNote() != null)                   c.setNote(dto.getNote());
        c.setAttivo(dto.getAttivo() != null ? dto.getAttivo() : true);
        return c;
    }
}
