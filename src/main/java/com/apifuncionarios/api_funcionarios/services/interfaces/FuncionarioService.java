package com.apifuncionarios.api_funcionarios.services.interfaces;

import java.util.List;

import com.apifuncionarios.api_funcionarios.dto.FuncionarioContratoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;

public interface FuncionarioService {

    FuncionarioResponse getFuncionarioInfo(Integer rut);

    List<FuncionarioContratoResponse> obtenerContratos(Integer rut);

    ResumenFeriadoLegal resumenFeriadosLegales(Integer rut, Integer ident);

    ResumenAdm resumenAdm(Integer rut, Integer ident);

}
