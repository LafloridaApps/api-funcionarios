package com.apifuncionarios.api_funcionarios.serivces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.SearchFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.SearchFuncionarioService;

@Service
public class SearchFuncionarioServiceImpl implements SearchFuncionarioService {

    private final FuncionarioRepository funcionarioRepository;

    private final FuncionarioService funcionarioService;

    public SearchFuncionarioServiceImpl(FuncionarioRepository funcionarioRepository,
            FuncionarioService funcionarioService) {
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioService = funcionarioService;
    }

    @Override
    public SearchFuncionarioResponse searchByName(String pattern, int pageNumber) {

        Pageable pageable = PageRequest.of(pageNumber, 10, Sort.by("apellidoPaterno").ascending());

        Page<Funcionario> pageFuncionarios = funcionarioRepository
                .findByNombresContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(
                        pattern, pattern, pattern, pageable);

        SearchFuncionarioResponse response = new SearchFuncionarioResponse();


        response.setCurrentPage(pageFuncionarios.getNumber());
        response.setTotalItems(pageFuncionarios.getNumberOfElements());
        response.setTotalPages(pageFuncionarios.getTotalPages());
        response.setSize(pageFuncionarios.getTotalElements());
        
        List<FuncionarioResponse> funcionarios = pageFuncionarios.getContent().stream()
                .map(funcionario -> funcionarioService.getFuncionarioInfo(funcionario.getRut())).toList();

        response.setFuncionarios(funcionarios);

        return response;


    }

}