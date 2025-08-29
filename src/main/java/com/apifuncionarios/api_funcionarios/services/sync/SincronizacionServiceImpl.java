
package com.apifuncionarios.api_funcionarios.services.sync;

import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;

@Service
public class SincronizacionServiceImpl implements ISincronizacionService {

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final ApiDepartamentoService apiDepartamentoService;

    public SincronizacionServiceImpl(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper, ApiDepartamentoService apiDepartamentoService) {
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.apiDepartamentoService = apiDepartamentoService;
    }

    @Override
    public Funcionario sincronizarYActualizarFuncionario(ApiFuncionarioResponse response) {
        DepartamentoResponse depto = apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx(response.getCodDeptoExt());
        if (depto == null) {
            throw new FuncionarioException("No se encontro el departamento en la base de datos municipalidad");
        }

        Funcionario funcionario = funcionarioRepository.findByRut(response.getRut())
                .orElseGet(() -> funcionarioMapper.toEntity(response));

        funcionarioMapper.updateEntityFromDto(funcionario, response, depto);
        
        return funcionarioRepository.save(funcionario);
    }
}
