
package com.apifuncionarios.api_funcionarios.services.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.FuncionarioException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;
import com.apifuncionarios.api_funcionarios.config.ApiProperties;

@Service
public class SincronizacionServiceImpl implements ISincronizacionService {

    private static final Logger logger = LoggerFactory.getLogger(SincronizacionServiceImpl.class);

    private final FuncionarioRepository funcionarioRepository;
    private final FuncionarioMapper funcionarioMapper;
    private final ApiDepartamentoService apiDepartamentoService;
    private final ApiProperties apiProperties;

    public SincronizacionServiceImpl(FuncionarioRepository funcionarioRepository, FuncionarioMapper funcionarioMapper, ApiDepartamentoService apiDepartamentoService, ApiProperties apiProperties) {
        this.funcionarioRepository = funcionarioRepository;
        this.funcionarioMapper = funcionarioMapper;
        this.apiDepartamentoService = apiDepartamentoService;
        this.apiProperties = apiProperties;
    }

    @Override
    public Funcionario sincronizarYActualizarFuncionario(ApiFuncionarioResponse response) {
        DepartamentoResponse depto = null;
        try {
            logger.info("Solicitando departamento a la API externa (URL: {}/api/departamentos/codex?codex={}) para el codDeptoExt: {}", apiProperties.getDepartamentoUrl(), response.getCodDeptoExt(), response.getCodDeptoExt());
            depto = apiDepartamentoService.obtenerDetalleDepartamentoByCodigoEx(response.getCodDeptoExt());
            if (depto != null) {
                logger.info("API respondió correctamente. Departamento sincronizado: id={}, nombre='{}'", depto.getId(), depto.getNombre());
            } else {
                logger.warn("La API de departamentos no devolvió datos (null) para el codDeptoExt: {}", response.getCodDeptoExt());
            }
        } catch (FuncionarioException e) {
            logger.warn("No se pudo obtener el departamento de la API externa para el funcionario con RUT {}. Se utilizarán los datos locales existentes para el departamento. Error: {}", response.getRut(), e.getMessage());
        }

        Funcionario funcionario = funcionarioRepository.findByRut(response.getRut())
                .orElseGet(() -> funcionarioMapper.toEntity(response));

        funcionarioMapper.updateEntityFromDto(funcionario, response, depto);
        
        return funcionarioRepository.save(funcionario);
    }
}
