package br.com.fiap.ms_ofertas.exceptions;

/**
 * Violação de regra de negócio (ex.: reservar uma oferta que não está DISPONIVEL).
 * Mapeada para HTTP 409 (Conflict) no GlobalExceptionHandler.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
