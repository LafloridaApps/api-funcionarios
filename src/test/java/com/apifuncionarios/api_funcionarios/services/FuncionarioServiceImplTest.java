package com.apifuncionarios.api_funcionarios.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;

class FuncionarioServiceImplTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService apiDepartamentoService;

    @Mock
    private com.apifuncionarios.api_funcionarios.services.interfaces.ApiFuncionarioService apiFuncionarioService;

    @Mock
    private com.apifuncionarios.api_funcionarios.services.sync.ISincronizacionService sincronizacionService;

    @Mock
    private FuncionarioMapper funcionarioMapper;

    @InjectMocks
    private FuncionarioServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Funcionario buildFuncionario(Integer rut, Long idDepto) {
        Funcionario f = new Funcionario();
        f.setRut(rut);
        f.setIdDepto(idDepto);
        f.setNombres("Nombre");
        f.setApellidoPaterno("P");
        f.setApellidoMaterno("M");
        return f;
    }

    @Test
    void resolveJefeDepto_deptoNull_returnsNull() {
        Funcionario f = buildFuncionario(123, null);
        // depto null -> resolveJefeDepto debe retornar null
        assertNull(service.resolveJefeDepto(null, f.getRut()));
    }

    @Test
    void resolveJefeDepto_rutJefeDifferent_findsJefe() {
        Funcionario jefe = buildFuncionario(999, 1L);
        DepartamentoResponse depto = new DepartamentoResponse(1L, "D", 999);

        when(funcionarioRepository.findByRut(999)).thenReturn(Optional.of(jefe));

        Funcionario resultado = service.resolveJefeDepto(depto, 123);
        assertNotNull(resultado);
        assertEquals(999, resultado.getRut());
    }

    @Test
    void resolveJefeDepto_rutJefeEqualsFuncionario_usesRutJefeSuperior() {
        Funcionario jefeSup = buildFuncionario(555, 2L);
        DepartamentoResponse depto = new DepartamentoResponse(2L, "D2", 123);
        // simular que el departamento tiene un padre
        Long parentId = 10L;
        depto.setIdDeptoSuperior(parentId);

        DepartamentoResponse parent = new DepartamentoResponse(parentId, "Parent", 555);

        when(apiDepartamentoService.obtenerDetalleDepartamentoById(parentId)).thenReturn(parent);
        when(funcionarioRepository.findByRut(555)).thenReturn(Optional.of(jefeSup));

        Funcionario resultado = service.resolveJefeDepto(depto, 123);
        assertNotNull(resultado);
        assertEquals(555, resultado.getRut());
    }

    @Test
    void resolveJefeDepto_jefeNotFound_returnsNull() {
        DepartamentoResponse depto = new DepartamentoResponse(3L, "D3", 777);
        when(funcionarioRepository.findByRut(777)).thenReturn(Optional.empty());
        Funcionario resultado = service.resolveJefeDepto(depto, 123);
        assertNull(resultado);
    }


    @Test
    void rutExternoDistintoNoSeSincronizaYUsaFuncionarioLocal() {
        var externo = new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse();
        externo.setRut(999);
        Funcionario local = buildFuncionario(123, null);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenReturn(externo);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.of(local));
        service.getFuncionarioInfo(123);
        verifyNoInteractions(sincronizacionService);
        verify(funcionarioMapper).toResponseDto(local, null, null, null);
    }

    @Test
    void rutCorrectoSeSincronizaConInicioDeConsulta() {
        var externo = new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse();
        externo.setRut(123);
        externo.setVrut("K");
        Funcionario local = buildFuncionario(123, null);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenReturn(externo);
        when(sincronizacionService.sincronizarYActualizarFuncionario(eq(externo), any(java.time.Instant.class)))
                .thenReturn(local);
        service.getFuncionarioInfo(123);
        verify(sincronizacionService).sincronizarYActualizarFuncionario(eq(externo), any(java.time.Instant.class));
    }

    @Test
    void falloExternoSinDatosLocalesConservaErrorOriginal() {
        var error = new com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException(503, null);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenThrow(error);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.empty());
        assertSame(error, assertThrows(
                com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException.class,
                () -> service.getFuncionarioInfo(123)));
    }

    @Test
    void falloExternoConDatosLocalesRespondeDesdeRepositorio() {
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenThrow(
                new com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException(503, null));
        Funcionario local = buildFuncionario(123, null);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.of(local));
        service.getFuncionarioInfo(123);
        verify(funcionarioMapper).toResponseDto(local, null, null, null);
        verifyNoInteractions(sincronizacionService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1})
    void rutDeEntradaInvalidoSeRechazaSinConsultarNiGuardar(Integer rut) {
        assertThrows(com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException.class,
                () -> service.getFuncionarioInfo(rut));
        verifyNoInteractions(apiFuncionarioService, funcionarioRepository, sincronizacionService,
                apiDepartamentoService, funcionarioMapper);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {0, -1, 999})
    void rutExternoInvalidoConservaDatosLocales(Integer rutExterno) {
        var externo = new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse();
        externo.setRut(rutExterno);
        externo.setVrut("K");
        Funcionario local = buildFuncionario(123, null);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenReturn(externo);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.of(local));
        service.getFuncionarioInfo(123);
        verifyNoInteractions(sincronizacionService);
        verify(funcionarioMapper).toResponseDto(local, null, null, null);
        verify(funcionarioRepository).findByRut(123);
        verifyNoMoreInteractions(funcionarioRepository);
    }

    @Test
    void respuestaSinRutNiRespaldoLocalEsFalloExterno() {
        when(apiFuncionarioService.obtenerDetalleColaborador(123))
                .thenReturn(new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse());
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.empty());
        assertThrows(com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException.class,
                () -> service.getFuncionarioInfo(123));
        verifyNoInteractions(sincronizacionService);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "XX"})
    void dvExternoInvalidoNoSeGuarda(String dv) {
        var externo = new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse();
        externo.setRut(123);
        externo.setVrut(dv);
        Funcionario local = buildFuncionario(123, null);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenReturn(externo);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.of(local));
        service.getFuncionarioInfo(123);
        verifyNoInteractions(sincronizacionService);
        verify(funcionarioMapper).toResponseDto(local, null, null, null);
    }

    @Test
    void departamentoSinAsociacionSeInformaAunqueExistaDepartamentoAnterior() {
        prepararValidacionDepartamento(7L);
        var respuesta = service.getFuncionarioInfo(123);
        assertEquals("SIN_ASOCIACION", respuesta.getConfiguracionDepartamento().estado());
        assertEquals("17100801", respuesta.getConfiguracionDepartamento().codigoExterno());
        assertFalse(respuesta.getConfiguracionDepartamento().permiteSolicitudes());
        assertNull(respuesta.getCodDepto());
        assertEquals(7L, respuesta.getConfiguracionDepartamento().idDeptoInterno());
        assertNull(respuesta.getConfiguracionDepartamento().idDeptoAsociado());
    }

    @Test
    void caidaDeDepartamentosNoSeConfundeConAsociacionFaltante() {
        prepararValidacionDepartamento(null);
        when(apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx("17100801"))
                .thenThrow(new com.apifuncionarios.api_funcionarios.exceptions.ExternalServiceException(503, null));
        assertEquals("SERVICIO_NO_DISPONIBLE", service.getFuncionarioInfo(123).getConfiguracionDepartamento().estado());
    }

    @Test
    void asociacionCorregidaHabilitaSolicitudes() {
        prepararValidacionDepartamento(190L);
        when(apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx("17100801"))
                .thenReturn(new DepartamentoResponse(190L, "Departamento correcto", null));
        assertTrue(service.getFuncionarioInfo(123).getConfiguracionDepartamento().permiteSolicitudes());
    }

    @Test
    void idInternoNuloSeDistingueDeAsociacionFaltante() {
        prepararValidacionDepartamento(null);
        when(apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx("17100801"))
                .thenReturn(new DepartamentoResponse(190L, "Departamento correcto", null));
        var diagnostico = service.getFuncionarioInfo(123).getConfiguracionDepartamento();
        assertEquals("SIN_ID_INTERNO", diagnostico.estado());
        assertNull(diagnostico.idDeptoInterno());
        assertEquals(190L, diagnostico.idDeptoAsociado());
    }

    @Test
    void diferenciaDeIdsSeInformaConAmbosValores() {
        prepararValidacionDepartamento(7L);
        when(apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx("17100801"))
                .thenReturn(new DepartamentoResponse(190L, "Departamento correcto", null));
        var diagnostico = service.getFuncionarioInfo(123).getConfiguracionDepartamento();
        assertEquals("ASOCIACION_INCONSISTENTE", diagnostico.estado());
        assertEquals(7L, diagnostico.idDeptoInterno());
        assertEquals(190L, diagnostico.idDeptoAsociado());
    }

    @Test
    void respaldoConservaDepartamentoSmcSiFallaLaSincronizacion() {
        var origen = prepararValidacionDepartamento(7L);
        when(sincronizacionService.sincronizarYActualizarFuncionario(eq(origen), any(java.time.Instant.class)))
                .thenThrow(new com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException("Fallo de sincronización"));
        var local = buildFuncionario(123, 7L);
        when(funcionarioRepository.findByRut(123)).thenReturn(Optional.of(local));
        when(funcionarioMapper.toResponseDto(eq(local), any(), any(), any()))
                .thenReturn(new com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse.Builder().rut(123).codDepto(7L).build());
        var diagnostico = service.getFuncionarioInfo(123).getConfiguracionDepartamento();
        assertEquals("NO_VERIFICADO", diagnostico.estado());
        assertEquals("Oficina Local de la Niñez", diagnostico.nombreExterno());
        assertEquals("17100801", diagnostico.codigoExterno());
    }

    private com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse prepararValidacionDepartamento(Long idDepto) {
        var origen = new com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse();
        origen.setRut(123); origen.setVrut("K"); origen.setCodDeptoExt("17100801");
        origen.setDepartamento("Oficina Local de la Niñez");
        var local = buildFuncionario(123, idDepto);
        when(apiFuncionarioService.obtenerDetalleColaborador(123)).thenReturn(origen);
        when(sincronizacionService.sincronizarYActualizarFuncionario(eq(origen), any(java.time.Instant.class)))
                .thenReturn(local);
        var respuesta = new com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse.Builder().rut(123).codDepto(idDepto).build();
        when(funcionarioMapper.toResponseDto(eq(local), any(), any(), any())).thenReturn(respuesta);
        return origen;
    }
}
