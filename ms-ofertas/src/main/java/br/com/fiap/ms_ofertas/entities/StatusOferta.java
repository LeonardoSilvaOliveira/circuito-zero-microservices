package br.com.fiap.ms_ofertas.entities;

/**
 * Ciclo de vida de uma oferta de excedente alimentar.
 *
 * DISPONIVEL -> RESERVADA -> RETIRADA
 *            \-> CANCELADA          (comerciante desistiu)
 * RESERVADA  -> DISPONIVEL          (instituição cancelou o resgate)
 *
 * As transições RESERVADA / RETIRADA / DISPONIVEL (liberar) são disparadas
 * pelo ms-resgates via comunicação síncrona (OpenFeign).
 */
public enum StatusOferta {
    DISPONIVEL,
    RESERVADA,
    RETIRADA,
    CANCELADA
}
