package br.com.fiap.ms_resgates.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Dados de entrada para a instituição reservar uma oferta.
 * O status NÃO é recebido aqui: ele é definido pelo resultado da
 * comunicação síncrona com o ms-ofertas.
 */
public record ResgateRequestDTO(

        @Schema(example = "1")
        @NotNull(message = "O ID da oferta é obrigatório")
        @Positive(message = "O ID da oferta deve ser positivo")
        Long ofertaId,

        @Schema(example = "ONG Mãos Solidárias")
        @NotBlank(message = "O nome da instituição é obrigatório")
        @Size(max = 120, message = "O nome da instituição deve ter no máximo 120 caracteres")
        String instituicaoNome,

        @Schema(example = "Maria Souza")
        @NotBlank(message = "O nome do responsável pela retirada é obrigatório")
        @Size(max = 100, message = "O nome do responsável deve ter no máximo 100 caracteres")
        String responsavelNome,

        @Schema(example = "11987654321")
        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter DDD + número (10 ou 11 dígitos, só números)")
        String telefone
) {
}
