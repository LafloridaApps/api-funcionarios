package com.apifuncionarios.api_funcionarios.services;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.apifuncionarios.api_funcionarios.dto.SearchnNombreDto;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;

class SearchViewDireccionTest {
    @Test
    void conservaFiltroYPaginacionYTotalDeLaDireccion() {
        var departamentos = List.of(10L, 20L);
        var persona = new SearchnNombreDto("Ana Pérez", 123, 'K');
        var repository = (FuncionarioRepository) Proxy.newProxyInstance(FuncionarioRepository.class.getClassLoader(),
                new Class<?>[]{FuncionarioRepository.class}, (proxy, method, args) -> {
                    assertEquals("findByNombreEnDepartamentos", method.getName());
                    assertEquals("Ana", args[0]);
                    assertEquals(departamentos, args[1]);
                    Pageable page = (Pageable) args[2];
                    assertEquals(1, page.getPageNumber());
                    assertEquals(10, page.getPageSize());
                    return new PageImpl<>(List.of(persona), page, 11);
                });
        var resultado = new SearchViewFuncionarioServiceImpl(repository).findByNombreEnDepartamentos("Ana", 1, departamentos);
        assertEquals(11, resultado.getTotalItems());
        assertEquals(2, resultado.getTotalPages());
        assertEquals(1, resultado.getCurrentPage());
        assertEquals(List.of(persona), resultado.getFuncionarios());
    }

    @Test
    void noPermiteConsultarSinDepartamentos() {
        var service = new SearchViewFuncionarioServiceImpl(null);
        assertThrows(IllegalArgumentException.class, () -> service.findByNombreEnDepartamentos("Ana", 0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.findByNombreEnDepartamentos("Ana", 0, null));
    }
}
