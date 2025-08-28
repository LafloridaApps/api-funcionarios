package com.apifuncionarios.api_funcionarios.serivces.interfaces;

import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;

public interface FuncionarioService {

    FuncionarioResponse getFuncionarioInfo(Integer rut);

    ResumenFeriadoLegal resumenFeriadosLegales(Integer rut, Integer ident);

    ResumenAdm resumenAdm(Integer rut, Integer ident);

}
