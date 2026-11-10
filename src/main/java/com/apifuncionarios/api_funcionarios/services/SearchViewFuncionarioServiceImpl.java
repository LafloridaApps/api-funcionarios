package com.apifuncionarios.api_funcionarios.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.SearchViewFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.SearchnNombreDto;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.SearchViewFuncionarioService;

@Service
public class SearchViewFuncionarioServiceImpl implements SearchViewFuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    public SearchViewFuncionarioServiceImpl(FuncionarioRepository funcionarioRepository) {
        this.funcionarioRepository = funcionarioRepository;
    }

    @Override
    @SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
    public SearchViewFuncionarioResponse findByNombre(String pattern, int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("nombre_completo").ascending());

        Page<SearchnNombreDto> pageResutl = funcionarioRepository.findByNombre(pattern, pageable);

        SearchViewFuncionarioResponse dto = new SearchViewFuncionarioResponse();

        dto.setCurrentPage(pageResutl.getNumber());
        dto.setTotalItems(pageResutl.getNumberOfElements());
        dto.setTotalPages(pageResutl.getTotalPages());
        dto.setSize(pageResutl.getTotalElements());

        List<SearchnNombreDto> listSearch = pageResutl.getContent();
        dto.setFuncionarios(listSearch);

        return dto;

    }


    @Override
    public SearchViewFuncionarioResponse findByNombreEnDepartamentos(String pattern, int pageNumber, List<Long> deptos) {
        if (deptos == null || deptos.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar los departamentos de la dirección.");
        }
        var page = funcionarioRepository.findByNombreEnDepartamentos(pattern, deptos,
                PageRequest.of(pageNumber, 10, Sort.by("nombre_completo").ascending().and(Sort.by("rut"))));
        var dto = new SearchViewFuncionarioResponse();
        dto.setCurrentPage(page.getNumber());
        dto.setTotalItems(Math.toIntExact(page.getTotalElements()));
        dto.setTotalPages(page.getTotalPages());
        dto.setSize(page.getTotalElements());
        dto.setFuncionarios(page.getContent());
        return dto;
    }
}
