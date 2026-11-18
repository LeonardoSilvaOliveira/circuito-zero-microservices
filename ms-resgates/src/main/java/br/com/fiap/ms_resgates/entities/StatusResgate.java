package br.com.fiap.ms_resgates.entities;

/**
 * Ciclo de vida de um resgate (reserva de uma oferta por uma instituição).
 *
 * RESERVADO            -> oferta reservada com sucesso no ms-ofertas (Feign)
 * CONFIRMACAO_PENDENTE -> ms-ofertas indisponível: Circuit Breaker acionou o Fallback;
 *                         o resgate fica aguardando nova tentativa (PATCH /confirmar)
 * RETIRADO             -> instituição retirou a doação (oferta -> RETIRADA)
 * CANCELADO            -> resgate cancelado (oferta volta a DISPONIVEL)
 */
public enum StatusResgate {
    CONFIRMACAO_PENDENTE,
    RESERVADO,
    RETIRADO,
    CANCELADO
}
