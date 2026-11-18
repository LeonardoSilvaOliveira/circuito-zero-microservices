package br.com.fiap.ms_resgates.config;

import br.com.fiap.ms_resgates.exceptions.RegraNegocioException;
import br.com.fiap.ms_resgates.exceptions.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduz as respostas de erro do ms-ofertas em exceções de negócio.
 *
 * - 404 -> ResourceNotFoundException (oferta não existe)
 * - 409 -> RegraNegocioException     (oferta não está no status esperado)
 *
 * Esses erros são "respostas válidas" do serviço remoto, por isso NÃO devem
 * acionar o Fallback nem abrir o Circuit Breaker (ver ignore-exceptions no
 * application.properties). Erros 5xx / indisponibilidade seguem como
 * FeignException e acionam o Fallback.
 */
@Configuration
public class FeignConfig {

    private static final Pattern ERROR_FIELD = Pattern.compile("\"error\"\\s*:\\s*\"([^\"]*)\"");

    @Bean
    public ErrorDecoder errorDecoder() {
        ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new ResourceNotFoundException(
                    mensagem(response, "Oferta não encontrada no ms-ofertas"));
            case 409 -> new RegraNegocioException(
                    mensagem(response, "O ms-ofertas recusou a alteração de status da oferta"));
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    /** Reaproveita a mensagem de erro devolvida pelo ms-ofertas (campo "error"). */
    private static String mensagem(Response response, String padrao) {
        if (response.body() == null) {
            return padrao;
        }
        try (InputStream in = response.body().asInputStream()) {
            String body = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            Matcher m = ERROR_FIELD.matcher(body);
            return m.find() ? "ms-ofertas: " + m.group(1) : padrao;
        } catch (IOException e) {
            return padrao;
        }
    }
}
