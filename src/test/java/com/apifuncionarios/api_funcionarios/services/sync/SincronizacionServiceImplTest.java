package com.apifuncionarios.api_funcionarios.services.sync;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;

class SincronizacionServiceImplTest {
    private FuncionarioRepository repository;
    private SincronizacionServiceImpl service;
    private ApiFuncionarioResponse response;
    private ApiDepartamentoService departamentos;

    @BeforeEach
    void setup() {
        repository = mock(FuncionarioRepository.class);
        PlatformTransactionManager transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenAnswer(invocation -> new SimpleTransactionStatus());
        departamentos = mock(ApiDepartamentoService.class);
        service = new SincronizacionServiceImpl(repository, new FuncionarioMapper(),
                departamentos, transactions);
        response = new ApiFuncionarioResponse();
        response.setRut(12345678);
        response.setVrut("k");
        response.setNombres("Actualizado");
        response.setIdent(1);
        response.setVigente(true);
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void actualizaRegistroExistenteConservandoIdYDepartamento() {
        Funcionario existente = new Funcionario();
        existente.setId(10L);
        existente.setRut(response.getRut());
        existente.setIdDepto(20L);
        existente.setFechafinContrato(LocalDate.of(2025, 1, 1));
        response.setFechafinContrato(null);
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        Funcionario result = service.sincronizarYActualizarFuncionario(response);
        assertSame(existente, result);
        assertEquals(10L, result.getId());
        assertEquals(20L, result.getIdDepto());
        assertEquals("Actualizado", result.getNombres());
        assertEquals(Character.valueOf('K'), result.getVrut());
        assertNull(result.getFechafinContrato());
    }

    @Test
    void creaFuncionarioCuandoNoExiste() {
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.empty());
        assertEquals(response.getRut(), service.sincronizarYActualizarFuncionario(response).getRut());
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void actualizaRegistroCreadoPorSolicitudConcurrente() {
        Funcionario existente = new Funcionario();
        existente.setId(50L);
        existente.setRut(response.getRut());
        when(repository.findByRutForUpdate(response.getRut()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate rut"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        assertSame(existente, service.sincronizarYActualizarFuncionario(response));
        assertEquals("Actualizado", existente.getNombres());
    }

    @Test
    void rechazaRutInvalidoAntesDeGuardar() {
        for (Integer rut : new Integer[] { null, 0, -1 }) {
            response.setRut(rut);
            assertThrows(FuncionarioException.class, () -> service.sincronizarYActualizarFuncionario(response));
        }
        assertThrows(FuncionarioException.class, () -> service.sincronizarYActualizarFuncionario(null));
        verifyNoInteractions(repository);
    }

    @Test
    void rechazaDigitoVerificadorInvalido() {
        response.setVrut("KK");
        assertThrows(FuncionarioException.class, () -> service.sincronizarYActualizarFuncionario(response));
        verifyNoInteractions(repository);
    }

    @Test
    void noOcultaOtrosErroresDeIntegridad() {
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("otro error"));
        assertThrows(DataIntegrityViolationException.class, () -> service.sincronizarYActualizarFuncionario(response));
    }

    @Test
    void camposAusentesConservanDatosYValoresExplicitosLosActualizan() {
        Funcionario existente = new Funcionario();
        existente.setRut(response.getRut());
        existente.setEmail("local@example.cl");
        existente.setGrado(12);
        existente.setVigente(true);
        existente.setFechafinContrato(LocalDate.of(2027, 1, 1));
        ApiFuncionarioResponse parcial = new ApiFuncionarioResponse();
        parcial.setRut(response.getRut());
        parcial.setVrut("K");
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        service.sincronizarYActualizarFuncionario(parcial);
        assertEquals("local@example.cl", existente.getEmail());
        assertEquals(12, existente.getGrado());
        assertTrue(existente.isVigente());
        assertEquals(LocalDate.of(2027, 1, 1), existente.getFechafinContrato());
        parcial.setEmail(null);
        parcial.setGrado(0);
        parcial.setVigente(false);
        parcial.setFechafinContrato(null);
        service.sincronizarYActualizarFuncionario(parcial);
        assertNull(existente.getEmail());
        assertEquals(0, existente.getGrado());
        assertFalse(existente.isVigente());
        assertNull(existente.getFechafinContrato());
    }

    @Test
    void departamentoSinIdNoBorraAsociacionLocal() {
        Funcionario existente = new Funcionario();
        existente.setIdDepto(20L);
        response.setCodDeptoExt("ABC");
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        when(departamentos.obtenerDetalleDepartamentoByCodigoEx("ABC"))
                .thenReturn(new com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse(null, null, null));
        assertEquals(20L, service.sincronizarYActualizarFuncionario(response).getIdDepto());
    }

    @Test
    void respuestaAntiguaNoSobrescribeDatosRecientes() {
        Funcionario existente = new Funcionario();
        existente.setNombres("Reciente");
        existente.setUltimaSincronizacion(java.time.Instant.parse("2026-11-10T12:00:00Z"));
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        assertSame(existente, service.sincronizarYActualizarFuncionario(response,
                java.time.Instant.parse("2026-11-10T11:00:00Z")));
        assertEquals("Reciente", existente.getNombres());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void conflictoDeInsercionNoSobrescribeRegistroMasReciente() {
        Funcionario existente = new Funcionario();
        existente.setNombres("Reciente");
        existente.setUltimaSincronizacion(java.time.Instant.parse("2026-11-10T12:00:00Z"));
        when(repository.findByRutForUpdate(response.getRut()))
                .thenReturn(Optional.empty()).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate rut"));
        assertSame(existente, service.sincronizarYActualizarFuncionario(response,
                java.time.Instant.parse("2026-11-10T11:00:00Z")));
        assertEquals("Reciente", existente.getNombres());
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void rechazaAltaIncompleta() {
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.empty());
        response.setNombres(null);
        assertThrows(FuncionarioException.class, () -> service.sincronizarYActualizarFuncionario(response));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void deserializacionDistingueAusentesDeValoresExplicitos() throws Exception {
        var mapper = tools.jackson.databind.json.JsonMapper.builder().build();
        ApiFuncionarioResponse ausente = mapper.readValue("{}", ApiFuncionarioResponse.class);
        ApiFuncionarioResponse explicito = mapper.readValue(
                "{\"vigente\":false,\"grado\":0,\"email\":null,\"fechaFinContrato\":null}", ApiFuncionarioResponse.class);
        assertFalse(ausente.contieneCampo("vigente"));
        assertFalse(ausente.contieneCampo("email"));
        assertTrue(explicito.contieneCampo("vigente"));
        assertTrue(explicito.contieneCampo("grado"));
        assertTrue(explicito.contieneCampo("email"));
        assertTrue(explicito.contieneCampo("fechafinContrato"));
    }

    @Test
    void reintentaInterbloqueoEnTransaccionNueva() {
        when(repository.findByRutForUpdate(response.getRut()))
                .thenThrow(new org.springframework.dao.CannotAcquireLockException("deadlock"))
                .thenReturn(Optional.empty());
        assertEquals(response.getRut(), service.sincronizarYActualizarFuncionario(response).getRut());
        verify(repository, times(2)).findByRutForUpdate(response.getRut());
        verify(repository, times(1)).saveAndFlush(any());
    }

    @Test
    void falloDeDepartamentoConservaAsociacionYGuardaOtrosDatos() {
        Funcionario existente = new Funcionario();
        existente.setIdDepto(20L);
        response.setCodDeptoExt("ABC");
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        when(departamentos.obtenerDetalleDepartamentoByCodigoEx("ABC"))
                .thenThrow(new FuncionarioException("timeout"));
        Funcionario resultado = service.sincronizarYActualizarFuncionario(response);
        assertEquals(20L, resultado.getIdDepto());
        assertEquals("Actualizado", resultado.getNombres());
    }

    @Test
    void cambioDeDominioActualizaIdentSinCrearOtroFuncionario() {
        Funcionario existente = new Funcionario();
        existente.setId(10L);
        existente.setRut(response.getRut());
        existente.setIdent(100);
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        response.setIdent(200);
        Funcionario resultado = service.sincronizarYActualizarFuncionario(response,
                java.time.Instant.parse("2026-11-10T12:00:00Z"));
        assertSame(existente, resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(response.getRut(), resultado.getRut());
        assertEquals(200, resultado.getIdent());
        response.setIdent(300);
        resultado = service.sincronizarYActualizarFuncionario(response,
                java.time.Instant.parse("2026-11-10T13:00:00Z"));
        assertSame(existente, resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(300, resultado.getIdent());
        verify(repository, times(2)).saveAndFlush(same(existente));
    }

    @Test
    void altaConcurrenteConOtroIdentActualizaLaFilaDelMismoRut() {
        Funcionario existente = new Funcionario();
        existente.setId(50L);
        existente.setRut(response.getRut());
        existente.setIdent(100);
        response.setIdent(200);
        when(repository.findByRutForUpdate(response.getRut()))
                .thenReturn(Optional.empty()).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate rut"))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Funcionario resultado = service.sincronizarYActualizarFuncionario(response);
        assertSame(existente, resultado);
        assertEquals(50L, resultado.getId());
        assertEquals(response.getRut(), resultado.getRut());
        assertEquals(200, resultado.getIdent());
        verify(repository).saveAndFlush(same(existente));
    }

    @Test
    void identAusenteConservaDominioActual() {
        Funcionario existente = new Funcionario();
        existente.setId(10L);
        existente.setRut(response.getRut());
        existente.setIdent(100);
        ApiFuncionarioResponse parcial = new ApiFuncionarioResponse();
        parcial.setRut(response.getRut());
        parcial.setVrut("K");
        when(repository.findByRutForUpdate(response.getRut())).thenReturn(Optional.of(existente));
        assertEquals(100, service.sincronizarYActualizarFuncionario(parcial).getIdent());
    }
}
