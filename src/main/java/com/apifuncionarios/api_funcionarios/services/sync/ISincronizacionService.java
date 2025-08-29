
package com.apifuncionarios.api_funcionarios.services.sync;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;

public interface ISincronizacionService {

    Funcionario sincronizarYActualizarFuncionario(ApiFuncionarioResponse response);
    
}
