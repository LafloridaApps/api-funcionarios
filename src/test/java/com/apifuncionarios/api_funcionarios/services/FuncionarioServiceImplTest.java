package com.apifuncionarios.api_funcionarios.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

}
