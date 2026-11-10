package com.apifuncionarios.api_funcionarios.utils;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException;

class ExternalApiClientTest {
    private ApiFuncionarioResponse consultar(HttpStatus status, String body) {
        WebClient client = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(status).header("Content-Type", "application/json").body(body).build())).build();
        return ExternalApiClient.get("test", "detalle", () -> client.get().uri("http://test"),
                new ParameterizedTypeReference<ApiFuncionarioResponse>() {});
    }

    @Test
    void error404NoDeserializaUnCuerpoQuePareceFuncionario() {
        assertNull(consultar(HttpStatus.NOT_FOUND, "{\"rut\":123,\"vrut\":\"K\"}"));
    }

    @Test
    void error500NoSeConfundeConDatosAusentes() {
        ExternalServiceException error = assertThrows(ExternalServiceException.class,
                () -> consultar(HttpStatus.INTERNAL_SERVER_ERROR, "detalle confidencial"));
        assertEquals(500, error.getUpstreamStatus());
        assertFalse(error.getMessage().contains("confidencial"));
    }

    @Test
    void respuestaCorrectaSeDeserializa() {
        assertEquals(123, consultar(HttpStatus.OK, "{\"rut\":123,\"vrut\":\"K\"}").getRut());
    }

    @Test
    void jsonInvalidoConservaLaCausaSinExponerlaEnElMensajePublico() {
        ExternalServiceException error = assertThrows(ExternalServiceException.class,
                () -> consultar(HttpStatus.OK, "json invalido"));
        assertNotNull(error.getCause());
        assertEquals(200, error.getUpstreamStatus());
    }

    @Test
    void errorDeTransporteNoSeConvierteEnNull() {
        WebClient client = WebClient.builder().exchangeFunction(request -> Mono.error(
                new IllegalStateException("detalle interno"))).build();
        ExternalServiceException error = assertThrows(ExternalServiceException.class,
                () -> ExternalApiClient.get("test", "detalle", () -> client.get().uri("http://test"),
                        new ParameterizedTypeReference<ApiFuncionarioResponse>() {}));
        assertEquals(0, error.getUpstreamStatus());
        assertNotNull(error.getCause());
    }
}
