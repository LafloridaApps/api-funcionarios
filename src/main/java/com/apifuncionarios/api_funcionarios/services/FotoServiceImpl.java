package com.apifuncionarios.api_funcionarios.services;

import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.services.interfaces.FotoService;
import com.apifuncionarios.api_funcionarios.services.interfaces.FuncionarioService;

@Service
public class FotoServiceImpl implements FotoService {

    private final FuncionarioService funcionarioService;

    public FotoServiceImpl(FuncionarioService funcionarioService) {
        this.funcionarioService = funcionarioService;
    }

    @Override
    public String getFotoFuncionario(Integer rut) {
        return funcionarioService.getFuncionarioInfo(rut).getFoto();
    }

}
