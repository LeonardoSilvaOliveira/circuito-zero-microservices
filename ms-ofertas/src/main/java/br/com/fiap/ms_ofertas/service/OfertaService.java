package br.com.fiap.ms_ofertas.service;

import br.com.fiap.ms_ofertas.dto.OfertaRequestDTO;
import br.com.fiap.ms_ofertas.dto.OfertaResponseDTO;
import br.com.fiap.ms_ofertas.entities.Oferta;
import br.com.fiap.ms_ofertas.entities.StatusOferta;
import br.com.fiap.ms_ofertas.exceptions.DatabaseException;
import br.com.fiap.ms_ofertas.exceptions.RegraNegocioException;
import br.com.fiap.ms_ofertas.exceptions.ResourceNotFoundException;
import br.com.fiap.ms_ofertas.repositories.OfertaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfertaService {

    private final OfertaRepository ofertaRepository;

    // ---------------------------------------------------------------- CRUD

    @Transactional(readOnly = true)
    public List<OfertaResponseDTO> findAll(StatusOferta status) {
        List<Oferta> ofertas = (status == null)
                ? ofertaRepository.findAll()
                : ofertaRepository.findByStatus(status);

        return ofertas.stream()
                .map(OfertaResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfertaResponseDTO findById(Long id) {
        return new OfertaResponseDTO(buscarOferta(id));
    }

    @Transactional
    public OfertaResponseDTO create(OfertaRequestDTO dto) {
        Oferta oferta = new Oferta();
        copyDtoToEntity(dto, oferta);
        oferta.setCriadoEm(LocalDateTime.now());
        oferta.setStatus(StatusOferta.DISPONIVEL);

        oferta = ofertaRepository.save(oferta);
        return new OfertaResponseDTO(oferta);
    }

    @Transactional
    public OfertaResponseDTO update(Long id, OfertaRequestDTO dto) {
        Oferta oferta = buscarOferta(id);

        if (oferta.getStatus() != StatusOferta.DISPONIVEL) {
            throw new RegraNegocioException(String.format(
                    "Oferta id %d está %s e só pode ser alterada enquanto estiver DISPONIVEL",
                    id, oferta.getStatus()));
        }

        copyDtoToEntity(dto, oferta);
        oferta = ofertaRepository.save(oferta);
        return new OfertaResponseDTO(oferta);
    }

    @Transactional
    public void delete(Long id) {
        Oferta oferta = buscarOferta(id);

        if (oferta.getStatus() == StatusOferta.RESERVADA) {
            throw new RegraNegocioException(String.format(
                    "Oferta id %d está RESERVADA por uma instituição e não pode ser excluída", id));
        }

        try {
            ofertaRepository.delete(oferta);
            ofertaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial ao excluir a oferta id " + id);
        }
    }

    // ------------------------------------------- Transições de status
    // Chamadas pelo ms-resgates via OpenFeign (comunicação síncrona)

    /** DISPONIVEL -> RESERVADA (instituição reservou a doação). */
    @Transactional
    public OfertaResponseDTO reservar(Long id) {
        Oferta oferta = buscarOferta(id);

        validarStatus(oferta, StatusOferta.DISPONIVEL, "reservada");

        if (oferta.getDataLimiteRetirada().isBefore(LocalDateTime.now())) {
            throw new RegraNegocioException(String.format(
                    "Oferta id %d expirou em %s e não pode mais ser reservada",
                    id, oferta.getDataLimiteRetirada()));
        }

        oferta.setStatus(StatusOferta.RESERVADA);
        return new OfertaResponseDTO(ofertaRepository.save(oferta));
    }

    /** RESERVADA -> RETIRADA (instituição buscou a doação no comércio). */
    @Transactional
    public OfertaResponseDTO retirar(Long id) {
        Oferta oferta = buscarOferta(id);
        validarStatus(oferta, StatusOferta.RESERVADA, "retirada");

        oferta.setStatus(StatusOferta.RETIRADA);
        return new OfertaResponseDTO(ofertaRepository.save(oferta));
    }

    /** RESERVADA -> DISPONIVEL (resgate cancelado: a doação volta para o feed). */
    @Transactional
    public OfertaResponseDTO liberar(Long id) {
        Oferta oferta = buscarOferta(id);
        validarStatus(oferta, StatusOferta.RESERVADA, "liberada");

        oferta.setStatus(StatusOferta.DISPONIVEL);
        return new OfertaResponseDTO(ofertaRepository.save(oferta));
    }

    /** DISPONIVEL -> CANCELADA (o próprio comércio retirou a oferta). */
    @Transactional
    public OfertaResponseDTO cancelar(Long id) {
        Oferta oferta = buscarOferta(id);
        validarStatus(oferta, StatusOferta.DISPONIVEL, "cancelada");

        oferta.setStatus(StatusOferta.CANCELADA);
        return new OfertaResponseDTO(ofertaRepository.save(oferta));
    }

    // ------------------------------------------------------- auxiliares

    private Oferta buscarOferta(Long id) {
        return ofertaRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Oferta não encontrada. ID: " + id));
    }

    private void validarStatus(Oferta oferta, StatusOferta esperado, String acao) {
        if (oferta.getStatus() != esperado) {
            throw new RegraNegocioException(String.format(
                    "Oferta id %d está %s e não pode ser %s (status esperado: %s)",
                    oferta.getId(), oferta.getStatus(), acao, esperado));
        }
    }

    private void copyDtoToEntity(OfertaRequestDTO dto, Oferta oferta) {
        oferta.setComercioNome(dto.comercioNome());
        oferta.setDescricao(dto.descricao());
        oferta.setQuantidade(dto.quantidade());
        oferta.setEndereco(dto.endereco());
        oferta.setDataLimiteRetirada(dto.dataLimiteRetirada());
    }
}
