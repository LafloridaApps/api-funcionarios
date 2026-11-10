package com.apifuncionarios.api_funcionarios.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class RutUnicoSchemaValidatorTest {
    @Test
    void permiteArranqueConIndiceUnicoPorRut() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L);
        assertDoesNotThrow(() -> new RutUnicoSchemaValidator(jdbc).run(null));
    }

    @Test
    void rechazaEsquemaSinUnicidadPorRut() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(0L);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new RutUnicoSchemaValidator(jdbc).run(null));
        assertTrue(error.getMessage().contains("UNIQUE sobre rut solamente"));
    }

    @Test
    void rechazaRutNullableAunqueExistaIndiceUnico() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn(1L, 0L);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new RutUnicoSchemaValidator(jdbc).run(null));
        assertTrue(error.getMessage().contains("NOT NULL"));
        verify(jdbc, never()).execute(anyString());
        verify(jdbc, never()).update(anyString());
    }

    @Test
    void rechazaValidacionSinResultado() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Long.class))).thenReturn((Long) null);
        assertThrows(IllegalStateException.class,
                () -> new RutUnicoSchemaValidator(jdbc).run(null));
    }
}
