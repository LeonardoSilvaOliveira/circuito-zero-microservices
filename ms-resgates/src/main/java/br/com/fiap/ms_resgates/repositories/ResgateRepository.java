package br.com.fiap.ms_resgates.repositories;

import br.com.fiap.ms_resgates.entities.Resgate;
import br.com.fiap.ms_resgates.entities.StatusResgate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResgateRepository extends JpaRepository<Resgate, Long> {

    List<Resgate> findByStatus(StatusResgate status);
}
