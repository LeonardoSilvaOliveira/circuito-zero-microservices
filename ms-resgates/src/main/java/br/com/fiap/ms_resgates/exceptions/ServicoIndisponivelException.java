package br.com.fiap.ms_resgates.exceptions;

/**
 * Lançada pelos fallbacks do Circuit Breaker quando o ms-ofertas não responde.
 * Mapeada para HTTP 503 (Service Unavailable).
 */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String message) {
        super(message);
    }
}
