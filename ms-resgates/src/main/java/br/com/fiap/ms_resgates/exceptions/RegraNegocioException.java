package br.com.fiap.ms_resgates.exceptions;

/**
 * Violação de regra de negócio (ex.: retirar um resgate que não está RESERVADO,
 * ou o ms-ofertas recusou a transição de status da oferta).
 * Mapeada para HTTP 409 (Conflict).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String message) {
        super(message);
    }
}
