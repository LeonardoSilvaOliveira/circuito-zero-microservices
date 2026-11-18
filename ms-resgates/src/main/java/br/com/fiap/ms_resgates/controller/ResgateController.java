package br.com.fiap.ms_resgates.controller;

import br.com.fiap.ms_resgates.dto.ResgateRequestDTO;
import br.com.fiap.ms_resgates.dto.ResgateResponseDTO;
import br.com.fiap.ms_resgates.entities.StatusResgate;
import br.com.fiap.ms_resgates.exceptions.RegraNegocioException;
import br.com.fiap.ms_resgates.exceptions.ResourceNotFoundException;
import br.com.fiap.ms_resgates.exceptions.ServicoIndisponivelException;
import br.com.fiap.ms_resgates.service.ResgateService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/resgates")
@RequiredArgsConstructor
@Tag(name = "Resgates", description = "Reserva e retirada das ofertas pelas instituições sociais")
public class ResgateController {

    /** Nome da instância do Circuit Breaker (configurada no application.properties). */
    private static final String CB_OFERTAS = "ms-ofertas";

    private final ResgateService resgateService;

    // ---------------------------------------------------------------- CRUD

    @Operation(summary = "Lista os resgates (filtro opcional por status)")
    @GetMapping
    public ResponseEntity<List<ResgateResponseDTO>> findAll(
            @Parameter(description = "Filtra pelo status do resgate")
            @RequestParam(required = false) StatusResgate status) {

        return ResponseEntity.ok(resgateService.findAll(status));
    }

    @Operation(summary = "Busca um resgate pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResgateResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(resgateService.findById(id));
    }

    @Operation(summary = "Instituição reserva uma oferta",
            description = "Chama o ms-ofertas via OpenFeign (oferta → RESERVADA) e cria o resgate RESERVADO (201). "
                    + "Se o ms-ofertas estiver indisponível, o Circuit Breaker aciona o Fallback: "
                    + "o resgate é salvo como CONFIRMACAO_PENDENTE e a resposta é 503 Service Unavailable "
                    + "com os dados do resgate no corpo.")
    @PostMapping
    @CircuitBreaker(name = CB_OFERTAS, fallbackMethod = "createFallback")
    public ResponseEntity<ResgateResponseDTO> create(@RequestBody @Valid ResgateRequestDTO dto) {
        ResgateResponseDTO response = resgateService.create(dto);
        return ResponseEntity.created(uriDoRecurso(response.id())).body(response);
    }

    @Operation(summary = "Atualiza os dados do resgate (não permitido se RETIRADO/CANCELADO)")
    @PutMapping("/{id}")
    public ResponseEntity<ResgateResponseDTO> update(@PathVariable Long id,
                                                     @RequestBody @Valid ResgateRequestDTO dto) {
        return ResponseEntity.ok(resgateService.update(id, dto));
    }

    @Operation(summary = "Exclui um resgate (se RESERVADO, é preciso cancelar antes)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resgateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------- Alteração de status
    // Cada endpoint altera o status do resgate E da oferta (via OpenFeign)

    @Operation(summary = "CONFIRMACAO_PENDENTE → RESERVADO (refaz a reserva no ms-ofertas)")
    @PatchMapping("/{id}/confirmar")
    @CircuitBreaker(name = CB_OFERTAS, fallbackMethod = "statusFallback")
    public ResponseEntity<ResgateResponseDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(resgateService.confirmar(id));
    }

    @Operation(summary = "RESERVADO → RETIRADO (oferta → RETIRADA no ms-ofertas)")
    @PatchMapping("/{id}/retirar")
    @CircuitBreaker(name = CB_OFERTAS, fallbackMethod = "statusFallback")
    public ResponseEntity<ResgateResponseDTO> retirar(@PathVariable Long id) {
        return ResponseEntity.ok(resgateService.retirar(id));
    }

    @Operation(summary = "Cancela o resgate (oferta volta para DISPONIVEL no ms-ofertas)")
    @PatchMapping("/{id}/cancelar")
    @CircuitBreaker(name = CB_OFERTAS, fallbackMethod = "statusFallback")
    public ResponseEntity<ResgateResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(resgateService.cancelar(id));
    }

    // ------------------------------------------------------- Fallbacks

    /**
     * Fallback do POST: o ms-ofertas não respondeu (fora do ar, sem instâncias no
     * Eureka ou circuito ABERTO). A solicitação da instituição não é perdida:
     * fica registrada como CONFIRMACAO_PENDENTE e pode ser confirmada depois.
     * Responde 503 com o resgate no corpo (padrão da aula 12).
     */
    public ResponseEntity<ResgateResponseDTO> createFallback(ResgateRequestDTO dto, Throwable e) {
        relancarSeErroDeNegocio(e);

        log.warn("ms-ofertas indisponível ao reservar a oferta {}. Fallback acionado: {}",
                dto.ofertaId(), e.toString());

        ResgateResponseDTO response = resgateService.createPendente(dto);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /** Fallback das transições de status: nada é alterado e o cliente recebe 503. */
    public ResponseEntity<ResgateResponseDTO> statusFallback(Long id, Throwable e) {
        relancarSeErroDeNegocio(e);

        log.warn("ms-ofertas indisponível ao alterar o status do resgate {}. Fallback acionado: {}",
                id, e.toString());

        throw new ServicoIndisponivelException(
                "O serviço de ofertas está indisponível no momento. "
                        + "O status do resgate " + id + " não foi alterado; tente novamente em instantes.");
    }

    /**
     * Erros de negócio (404 / 409) são respostas válidas e devem chegar ao cliente
     * como estão, sem passar pelo fallback.
     */
    private void relancarSeErroDeNegocio(Throwable e) {
        if (e instanceof ResourceNotFoundException ex) throw ex;
        if (e instanceof RegraNegocioException ex) throw ex;
    }

    private URI uriDoRecurso(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
