package com.apifuncionarios.api_funcionarios.serivces.interfaces;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;

public interface ApiFuncionarioService {

    ApiFuncionarioResponse obtenerDetalleColaborador(Integer rut);

}
