package com.apifuncionarios.api_funcionarios.exceptionscontrolers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import com.apifuncionarios.api_funcionarios.dto.ErrorResponse;
import com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException;

class HandlerExceptionsTest {
    private final HandlerExceptions handler = new HandlerExceptions();

    @Test
    void falloExternoSeRespondeComo502SinDetallesInternos() {
        var response = handler.handlerExternalException(new ExternalServiceException(503,
                new IllegalStateException("secreto")), new MockHttpServletRequest());
        assertEquals(502, response.getStatusCode().value());
        assertFalse(((ErrorResponse) response.getBody()).getMensaje().contains("secreto"));
    }

    @Test
    void falloInesperadoNoExponeMensajeInterno() {
        var response = handler.handlerUnexpectedException(new IllegalStateException("SQL secreto"),
                new MockHttpServletRequest());
        assertEquals(500, response.getStatusCode().value());
        assertFalse(((ErrorResponse) response.getBody()).getMensaje().contains("SQL"));
    }

    @Test
    void erroresHttpDeSpringConservanSuEstadoYHeaders() {
        var response = handler.handlerUnexpectedException(
                new HttpRequestMethodNotSupportedException("POST", java.util.List.of("GET")), new MockHttpServletRequest());
        assertEquals(405, response.getStatusCode().value());
        assertTrue(response.getHeaders().getAllow().contains(org.springframework.http.HttpMethod.GET));
    }
}
