package br.com.fiap.ms_resgates.client;

import br.com.fiap.ms_resgates.client.dto.OfertaClientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Comunicação síncrona com o ms-ofertas.
 *
 * Não há URL fixa: "ms-ofertas" é o nome registrado no Eureka Server e o
 * Spring Cloud LoadBalancer escolhe uma das instâncias disponíveis a cada chamada.
 */
@FeignClient(name = "ms-ofertas", path = "/api/v1/ofertas")
public interface OfertaClient {

    @PatchMapping("/{id}/reservar")
    OfertaClientDTO reservar(@PathVariable("id") Long id);

    @PatchMapping("/{id}/retirar")
    OfertaClientDTO retirar(@PathVariable("id") Long id);

    @PatchMapping("/{id}/liberar")
    OfertaClientDTO liberar(@PathVariable("id") Long id);
}
