package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.Stabilimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StabilimentoRepository extends JpaRepository<Stabilimento, Long> {
    List<Stabilimento> findByAttivoTrue();
    List<Stabilimento> findByCitta(String citta);
    java.util.Optional<Stabilimento> findBySiglaIgnoreCase(String sigla);
}
