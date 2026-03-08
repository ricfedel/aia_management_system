package it.grandimolini.aia.repository;

import it.grandimolini.aia.model.DefinizioneFlusso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefinizioneFlussoRepository extends JpaRepository<DefinizioneFlusso, Long> {

    List<DefinizioneFlusso> findByAttivaTrue();

    List<DefinizioneFlusso> findByAttivaOrderByNomeAsc(boolean attiva);

    boolean existsByNomeAndAttivaTrue(String nome);
}
