package com.apifuncionarios.api_funcionarios.services.interfaces;

import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;

public interface ApiDepartamentoService {

    DepartamentoResponse obtenerDetalleDepartamentoByCodigoEx(String codEx);

     DepartamentoResponse obtenerDetalleDepartamentoById(Long id);


}
