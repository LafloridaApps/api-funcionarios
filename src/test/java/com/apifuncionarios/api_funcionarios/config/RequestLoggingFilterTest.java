package com.apifuncionarios.api_funcionarios.config;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

class RequestLoggingFilterTest {
    @Test
    void generaCorrelacionYRestableceContextoDelHilo() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/funcionario");
        var response = new MockHttpServletResponse();
        request.addHeader("X-Request-ID", "valor externo no confiable");
        MDC.put("requestId", "anterior");
        try {
            new RequestLoggingFilter().doFilter(request, response, (req, res) -> {
                assertNotEquals("anterior", MDC.get("requestId"));
                assertEquals(MDC.get("requestId"), response.getHeader("X-Request-ID"));
            });
            assertEquals("anterior", MDC.get("requestId"));
        } finally { MDC.clear(); }
    }

    @Test
    void restauraContextoAunqueFalleLaPeticionYNoExponeRutaCruda() {
        var request = new MockHttpServletRequest("GET", "/api/funcionario/foto/12345678");
        assertEquals("unmapped", RequestLoggingFilter.route(request));
        request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/funcionario/foto/{rut}");
        assertEquals("/api/funcionario/foto/{rut}", RequestLoggingFilter.route(request));
        assertThrows(IllegalStateException.class, () -> new RequestLoggingFilter().doFilter(
                request, new MockHttpServletResponse(), (req, res) -> { throw new IllegalStateException("fallo"); }));
        assertNull(MDC.get("requestId"));
    }
}
