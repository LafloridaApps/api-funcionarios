package com.apifuncionarios.api_funcionarios.serivces.interfaces;

import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;

public interface FuncionarioService {

    FuncionarioResponse getFuncionarioInfo(Integer rut);

}
