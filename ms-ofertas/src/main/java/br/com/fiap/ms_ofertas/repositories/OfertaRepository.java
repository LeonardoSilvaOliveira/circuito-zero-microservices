package br.com.fiap.ms_ofertas.repositories;

import br.com.fiap.ms_ofertas.entities.Oferta;
import br.com.fiap.ms_ofertas.entities.StatusOferta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByStatus(StatusOferta status);
}
