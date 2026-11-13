package com.apifuncionarios.api_funcionarios.services;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.apifuncionarios.api_funcionarios.config.ApiProperties;
import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;

class ApiFuncionarioListTest {
    @Test
    void listadoConservaCamposNuevosYContratosNoVigentes() {
        var properties = new ApiProperties(); properties.setFuncionarioSmcUrl("http://smc.test");
        var builder = WebClient.builder().exchangeFunction(request -> {
            assertEquals("/api/funcionario/123/list", request.url().getPath());
            return Mono.just(ClientResponse.create(HttpStatus.OK).header("Content-Type", "application/json")
                    .body("[{\"rut\":123,\"vigente\":true,\"numresolcontr\":\"001/2026\",\"obs_adicional_cont\":\"Observación de contrato\"},"
                        + "{\"rut\":123,\"vigente\":false,\"numresolcontr\":null,\"obs_adicional_cont\":null}]").build());
        });
        var contratos = new ApiFuncionarioServiceImpl(builder, properties).obtenerListDetalleColaborador(123);
        assertEquals(2, contratos.size());
        assertEquals("001/2026", contratos.getFirst().numResolContr());
        assertEquals("Observación de contrato", contratos.getFirst().obsAdicionalCont());
        assertFalse(contratos.getLast().vigente());
        assertNull(contratos.getLast().numResolContr());
        assertNull(contratos.getLast().obsAdicionalCont());
        var json = tools.jackson.databind.json.JsonMapper.builder().build().valueToTree(contratos);
        assertEquals("001/2026", json.get(0).get("numresolcontr").asText());
        assertEquals("Observación de contrato", json.get(0).get("obs_adicional_cont").asText());
        assertTrue(json.get(1).get("numresolcontr").isNull());
    }

    @Test
    void respuestaIndividualNoIncorporaCamposDelListado() {
        var json = tools.jackson.databind.json.JsonMapper.builder().build().valueToTree(new ApiFuncionarioResponse());
        assertFalse(json.has("numresolcontr"));
        assertFalse(json.has("obs_adicional_cont"));
    }
}
