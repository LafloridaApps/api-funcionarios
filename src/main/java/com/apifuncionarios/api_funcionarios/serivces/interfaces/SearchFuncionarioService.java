package com.apifuncionarios.api_funcionarios.serivces.interfaces;


import com.apifuncionarios.api_funcionarios.dto.SearchFuncionarioResponse;

public interface SearchFuncionarioService {

    SearchFuncionarioResponse searchByName(String pattern, int pageNumber);

}
