package br.com.fiap.ms_ofertas.controller;

import br.com.fiap.ms_ofertas.dto.OfertaRequestDTO;
import br.com.fiap.ms_ofertas.dto.OfertaResponseDTO;
import br.com.fiap.ms_ofertas.entities.StatusOferta;
import br.com.fiap.ms_ofertas.service.OfertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ofertas")
@RequiredArgsConstructor
@Tag(name = "Ofertas", description = "Excedentes alimentares sinalizados pelos comércios de bairro")
public class OfertaController {

    private final OfertaService ofertaService;

    /** Usado para ler a porta aleatória real da instância (definida só após o Tomcat subir). */
    private final Environment environment;

    // ---------------------------------------------------------------- CRUD

    @Operation(summary = "Lista as ofertas (filtro opcional por status, ex.: DISPONIVEL para o feed)")
    @GetMapping
    public ResponseEntity<List<OfertaResponseDTO>> findAll(
            @Parameter(description = "Filtra pelo status da oferta")
            @RequestParam(required = false) StatusOferta status) {

        return ResponseEntity.ok(ofertaService.findAll(status));
    }

    @Operation(summary = "Busca uma oferta pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.findById(id));
    }

    @Operation(summary = "Comércio sinaliza um novo excedente (status inicial DISPONIVEL)")
    @PostMapping
    public ResponseEntity<OfertaResponseDTO> create(@RequestBody @Valid OfertaRequestDTO dto) {
        OfertaResponseDTO response = ofertaService.create(dto);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Atualiza os dados de uma oferta (somente enquanto DISPONIVEL)")
    @PutMapping("/{id}")
    public ResponseEntity<OfertaResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody @Valid OfertaRequestDTO dto) {
        return ResponseEntity.ok(ofertaService.update(id, dto));
    }

    @Operation(summary = "Exclui uma oferta (não permitido se estiver RESERVADA)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ofertaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------- Alteração de status
    // Endpoints consumidos pelo ms-resgates via OpenFeign

    @Operation(summary = "DISPONIVEL → RESERVADA (chamado pelo ms-resgates)")
    @PatchMapping("/{id}/reservar")
    public ResponseEntity<OfertaResponseDTO> reservar(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.reservar(id));
    }

    @Operation(summary = "RESERVADA → RETIRADA (chamado pelo ms-resgates)")
    @PatchMapping("/{id}/retirar")
    public ResponseEntity<OfertaResponseDTO> retirar(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.retirar(id));
    }

    @Operation(summary = "RESERVADA → DISPONIVEL, devolve a oferta ao feed (chamado pelo ms-resgates)")
    @PatchMapping("/{id}/liberar")
    public ResponseEntity<OfertaResponseDTO> liberar(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.liberar(id));
    }

    @Operation(summary = "DISPONIVEL → CANCELADA (o comércio desistiu da doação)")
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OfertaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.cancelar(id));
    }

    // ------------------------------------------- Load Balancer (demo)

    @Operation(summary = "Mostra a porta da instância que respondeu (demonstração do Load Balancer)")
    @GetMapping("/instancia")
    public ResponseEntity<Map<String, String>> instancia() {
        return ResponseEntity.ok(Map.of(
                "servico", "ms-ofertas",
                "porta", environment.getProperty("local.server.port", "desconhecida")));
    }
}
