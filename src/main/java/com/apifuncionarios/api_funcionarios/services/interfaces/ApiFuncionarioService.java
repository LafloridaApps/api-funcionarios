package com.apifuncionarios.api_funcionarios.services.interfaces;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;

public interface ApiFuncionarioService {

    ApiFuncionarioResponse obtenerDetalleColaborador(Integer rut);

    ResumenFeriadoLegal obtenerFeriadosLegales(Integer rut, Integer ident);

    ResumenAdm obtenerAdministrativos(Integer rut, Integer ident);

}
