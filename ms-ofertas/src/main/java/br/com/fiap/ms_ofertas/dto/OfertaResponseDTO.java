package br.com.fiap.ms_ofertas.dto;

import br.com.fiap.ms_ofertas.entities.Oferta;
import br.com.fiap.ms_ofertas.entities.StatusOferta;

import java.time.LocalDateTime;

/**
 * Dados de saída de uma oferta (a entidade nunca é exposta diretamente).
 */
public record OfertaResponseDTO(
        Long id,
        String comercioNome,
        String descricao,
        Integer quantidade,
        String endereco,
        LocalDateTime dataLimiteRetirada,
        LocalDateTime criadoEm,
        StatusOferta status
) {
    public OfertaResponseDTO(Oferta oferta) {
        this(oferta.getId(),
                oferta.getComercioNome(),
                oferta.getDescricao(),
                oferta.getQuantidade(),
                oferta.getEndereco(),
                oferta.getDataLimiteRetirada(),
                oferta.getCriadoEm(),
                oferta.getStatus());
    }
}
