package br.com.fiap.ms_resgates.service;

import br.com.fiap.ms_resgates.client.OfertaClient;
import br.com.fiap.ms_resgates.dto.ResgateRequestDTO;
import br.com.fiap.ms_resgates.dto.ResgateResponseDTO;
import br.com.fiap.ms_resgates.entities.Resgate;
import br.com.fiap.ms_resgates.entities.StatusResgate;
import br.com.fiap.ms_resgates.exceptions.DatabaseException;
import br.com.fiap.ms_resgates.exceptions.RegraNegocioException;
import br.com.fiap.ms_resgates.exceptions.ResourceNotFoundException;
import br.com.fiap.ms_resgates.repositories.ResgateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ResgateService {

    private final ResgateRepository resgateRepository;
    private final OfertaClient ofertaClient;

    // ---------------------------------------------------------------- CRUD

    @Transactional(readOnly = true)
    public List<ResgateResponseDTO> findAll(StatusResgate status) {
        List<Resgate> resgates = (status == null)
                ? resgateRepository.findAll()
                : resgateRepository.findByStatus(status);

        return resgates.stream()
                .map(ResgateResponseDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResgateResponseDTO findById(Long id) {
        return new ResgateResponseDTO(buscarResgate(id));
    }

    /**
     * Cria o resgate reservando a oferta no ms-ofertas (comunicação síncrona).
     * Oferta: DISPONIVEL -> RESERVADA | Resgate: RESERVADO
     *
     * Se o ms-ofertas estiver fora, a exceção sobe até o @CircuitBreaker do
     * controller, que aciona o fallback {@link #createPendente}.
     */
    @Transactional
    public ResgateResponseDTO create(ResgateRequestDTO dto) {
        ofertaClient.reservar(dto.ofertaId());

        Resgate resgate = new Resgate();
        copyDtoToEntity(dto, resgate);
        resgate.setDataReserva(LocalDateTime.now());
        resgate.setStatus(StatusResgate.RESERVADO);

        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    /**
     * Usado pelo Fallback: registra o pedido da instituição sem perder a solicitação,
     * com status CONFIRMACAO_PENDENTE. A reserva é refeita depois via {@link #confirmar}.
     */
    @Transactional
    public ResgateResponseDTO createPendente(ResgateRequestDTO dto) {
        Resgate resgate = new Resgate();
        copyDtoToEntity(dto, resgate);
        resgate.setDataReserva(LocalDateTime.now());
        resgate.setStatus(StatusResgate.CONFIRMACAO_PENDENTE);

        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    @Transactional
    public ResgateResponseDTO update(Long id, ResgateRequestDTO dto) {
        Resgate resgate = buscarResgate(id);

        if (resgate.getStatus() == StatusResgate.RETIRADO || resgate.getStatus() == StatusResgate.CANCELADO) {
            throw new RegraNegocioException(String.format(
                    "Resgate id %d está %s e não pode mais ser alterado", id, resgate.getStatus()));
        }

        if (!Objects.equals(resgate.getOfertaId(), dto.ofertaId())
                && resgate.getStatus() != StatusResgate.CONFIRMACAO_PENDENTE) {
            throw new RegraNegocioException(String.format(
                    "Resgate id %d já reservou a oferta %d. Para trocar de oferta, cancele este resgate e crie outro",
                    id, resgate.getOfertaId()));
        }

        copyDtoToEntity(dto, resgate);
        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    @Transactional
    public void delete(Long id) {
        Resgate resgate = buscarResgate(id);

        if (resgate.getStatus() == StatusResgate.RESERVADO) {
            throw new RegraNegocioException(String.format(
                    "Resgate id %d está RESERVADO. Cancele o resgate antes de excluí-lo para liberar a oferta", id));
        }

        try {
            resgateRepository.delete(resgate);
            resgateRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Falha de integridade referencial ao excluir o resgate id " + id);
        }
    }

    // ------------------------------------------- Transições de status
    // Todas alteram também o status da oferta no ms-ofertas via OpenFeign

    /** CONFIRMACAO_PENDENTE -> RESERVADO (nova tentativa após o fallback). */
    @Transactional
    public ResgateResponseDTO confirmar(Long id) {
        Resgate resgate = buscarResgate(id);
        validarStatus(resgate, StatusResgate.CONFIRMACAO_PENDENTE, "confirmado");

        ofertaClient.reservar(resgate.getOfertaId());

        resgate.setStatus(StatusResgate.RESERVADO);
        resgate.setDataReserva(LocalDateTime.now());
        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    /** RESERVADO -> RETIRADO | Oferta: RESERVADA -> RETIRADA. */
    @Transactional
    public ResgateResponseDTO retirar(Long id) {
        Resgate resgate = buscarResgate(id);
        validarStatus(resgate, StatusResgate.RESERVADO, "retirado");

        ofertaClient.retirar(resgate.getOfertaId());

        resgate.setStatus(StatusResgate.RETIRADO);
        resgate.setDataRetirada(LocalDateTime.now());
        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    /**
     * RESERVADO -> CANCELADO           | Oferta: RESERVADA -> DISPONIVEL (volta ao feed)
     * CONFIRMACAO_PENDENTE -> CANCELADO | Oferta não chegou a ser reservada: sem chamada remota
     */
    @Transactional
    public ResgateResponseDTO cancelar(Long id) {
        Resgate resgate = buscarResgate(id);

        switch (resgate.getStatus()) {
            case RESERVADO -> ofertaClient.liberar(resgate.getOfertaId());
            case CONFIRMACAO_PENDENTE -> { /* nada a desfazer no ms-ofertas */ }
            default -> throw new RegraNegocioException(String.format(
                    "Resgate id %d está %s e não pode ser cancelado", id, resgate.getStatus()));
        }

        resgate.setStatus(StatusResgate.CANCELADO);
        return new ResgateResponseDTO(resgateRepository.save(resgate));
    }

    // ------------------------------------------------------- auxiliares

    private Resgate buscarResgate(Long id) {
        return resgateRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Resgate não encontrado. ID: " + id));
    }

    private void validarStatus(Resgate resgate, StatusResgate esperado, String acao) {
        if (resgate.getStatus() != esperado) {
            throw new RegraNegocioException(String.format(
                    "Resgate id %d está %s e não pode ser %s (status esperado: %s)",
                    resgate.getId(), resgate.getStatus(), acao, esperado));
        }
    }

    private void copyDtoToEntity(ResgateRequestDTO dto, Resgate resgate) {
        resgate.setOfertaId(dto.ofertaId());
        resgate.setInstituicaoNome(dto.instituicaoNome());
        resgate.setResponsavelNome(dto.responsavelNome());
        resgate.setTelefone(dto.telefone());
    }
}
