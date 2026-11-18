package br.com.fiap.ms_resgates.client.dto;

/**
 * Representação (parcial) da oferta devolvida pelo ms-ofertas.
 */
public record OfertaClientDTO(
        Long id,
        String comercioNome,
        String descricao,
        String status
) {
}
