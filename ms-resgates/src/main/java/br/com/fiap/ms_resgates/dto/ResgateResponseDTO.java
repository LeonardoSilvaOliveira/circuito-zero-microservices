package br.com.fiap.ms_resgates.dto;

import br.com.fiap.ms_resgates.entities.Resgate;
import br.com.fiap.ms_resgates.entities.StatusResgate;

import java.time.LocalDateTime;

/**
 * Dados de saída de um resgate (a entidade nunca é exposta diretamente).
 */
public record ResgateResponseDTO(
        Long id,
        Long ofertaId,
        String instituicaoNome,
        String responsavelNome,
        String telefone,
        LocalDateTime dataReserva,
        LocalDateTime dataRetirada,
        StatusResgate status
) {
    public ResgateResponseDTO(Resgate resgate) {
        this(resgate.getId(),
                resgate.getOfertaId(),
                resgate.getInstituicaoNome(),
                resgate.getResponsavelNome(),
                resgate.getTelefone(),
                resgate.getDataReserva(),
                resgate.getDataRetirada(),
                resgate.getStatus());
    }
}
