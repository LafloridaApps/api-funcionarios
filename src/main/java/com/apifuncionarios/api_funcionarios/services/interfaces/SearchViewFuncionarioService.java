package com.apifuncionarios.api_funcionarios.services.interfaces;

import com.apifuncionarios.api_funcionarios.dto.SearchViewFuncionarioResponse;

public interface SearchViewFuncionarioService {

    SearchViewFuncionarioResponse findByNombre(String pattern, int pageNumber);

}
