package br.com.fiap.ms_ofertas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Dados de entrada para cadastrar/atualizar uma oferta.
 * O status NÃO é recebido aqui: ele é controlado pelas regras de negócio
 * (inicia como DISPONIVEL e muda via comunicação com o ms-resgates).
 */
public record OfertaRequestDTO(

        @Schema(example = "Padaria Pão Quente")
        @NotBlank(message = "O nome do comércio é obrigatório")
        @Size(max = 100, message = "O nome do comércio deve ter no máximo 100 caracteres")
        String comercioNome,

        @Schema(example = "20 pães franceses e 5 marmitas")
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @Schema(example = "25")
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantidade,

        @Schema(example = "Rua Augusta, 1500 - Consolação, São Paulo/SP")
        @NotBlank(message = "O endereço de retirada é obrigatório")
        @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres")
        String endereco,

        @Schema(example = "2027-12-31T22:00:00")
        @NotNull(message = "A data limite de retirada é obrigatória")
        @Future(message = "A data limite de retirada deve estar no futuro")
        LocalDateTime dataLimiteRetirada
) {
}
