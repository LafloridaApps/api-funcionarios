package com.apifuncionarios.api_funcionarios.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.apifuncionarios.api_funcionarios.dto.ApiFuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.DepartamentoResponse;
import com.apifuncionarios.api_funcionarios.dto.FuncionarioResponse;
import com.apifuncionarios.api_funcionarios.dto.ResumenAdm;
import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;
import com.apifuncionarios.api_funcionarios.exceptions.NotFounException;
import com.apifuncionarios.api_funcionarios.mappers.FuncionarioMapper;
import com.apifuncionarios.api_funcionarios.repositories.FuncionarioRepository;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiDepartamentoService;
import com.apifuncionarios.api_funcionarios.services.interfaces.ApiFuncionarioService;
import com.apifuncionarios.api_funcionarios.services.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.services.sync.ISincronizacionService;
import com.apifuncionarios.api_funcionarios.utils.RepositoryUtils;

@Service
public class FuncionarioServiceImpl implements FuncionarioService {

    private final ApiFuncionarioService apiFuncionarioService;
    private final FuncionarioRepository funcionarioRepository;
    private final ApiDepartamentoService apiDepartamentoService;
    private final ISincronizacionService sincronizacionService;
    private final FuncionarioMapper funcionarioMapper;

    private static final String NOT_FOUND_MESSAGE = "No se encontro el funcionario con el rut %d";
    private static final Logger logger = LoggerFactory.getLogger(FuncionarioServiceImpl.class);

    public FuncionarioServiceImpl(ApiFuncionarioService apiFuncionarioService,
            FuncionarioRepository funcionarioRepository,
            ApiDepartamentoService apiDepartamentoService,
            ISincronizacionService sincronizacionService,
            FuncionarioMapper funcionarioMapper) {
        this.apiFuncionarioService = apiFuncionarioService;
        this.funcionarioRepository = funcionarioRepository;
        this.apiDepartamentoService = apiDepartamentoService;
        this.sincronizacionService = sincronizacionService;
        this.funcionarioMapper = funcionarioMapper;
    }

    @Override
    public FuncionarioResponse getFuncionarioInfo(Integer rut) {
        ApiFuncionarioResponse response = null;
        try {
            response = apiFuncionarioService.obtenerDetalleColaborador(rut);
        } catch (Exception e) {
            logger.warn("No se pudo obtener el detalle del colaborador desde el servicio externo. Usando datos locales.",
                    e);
        }

        if (response != null) {
            // Modo Online: Sincronizar y devolver datos frescos
            try {
                Funcionario funcionario = sincronizacionService.sincronizarYActualizarFuncionario(response);
                return buildFuncionarioResponse(funcionario, response.getFoto());
            } catch (Exception e) {
                logger.warn("Fallo la sincronizacion. Usando datos locales.", e);
            }
        }

        // Modo Offline: Usar datos locales
        Funcionario funcionario = RepositoryUtils.findOrThrow(
                funcionarioRepository.findByRut(rut),
                String.format(NOT_FOUND_MESSAGE, rut));
        return buildFuncionarioResponse(funcionario, null);
    }

    private FuncionarioResponse buildFuncionarioResponse(Funcionario funcionario, String foto) {
        DepartamentoResponse depto = null;
        try {
            depto = apiDepartamentoService.obtenerDetalleDepartamentoById(funcionario.getIdDepto());
        } catch (Exception e) {
            logger.warn("No se pudo obtener el detalle del departamento desde el servicio externo.", e);
        }

        Funcionario jefeDepto = new Funcionario();
        if (depto != null && depto.getRutJefe() != null) {
            if (depto.getRutJefe().equals(funcionario.getRut())) {
                jefeDepto = funcionarioRepository.findByRut(depto.getRutJefeSuperior()).orElse(new Funcionario());
            } else {
                jefeDepto = funcionarioRepository.findByRut(depto.getRutJefe()).orElse(new Funcionario());
            }
        }

        return funcionarioMapper.toResponseDto(funcionario, depto, jefeDepto, foto);
    }

    @Override
    public ResumenFeriadoLegal resumenFeriadosLegales(Integer rut, Integer ident) {
        ResumenFeriadoLegal resumen = apiFuncionarioService.obtenerFeriadosLegales(rut, ident);
        if (resumen == null) {
            throw new NotFounException(String.format("No se encontraron feriados para el rut %d", rut));
        }
        return resumen;
    }

    @Override
    public ResumenAdm resumenAdm(Integer rut, Integer ident) {
        ResumenAdm resumen = apiFuncionarioService.obtenerAdministrativos(rut, ident);
        if (resumen == null) {
            throw new NotFounException(String.format("No se encontraron dias administrativos para el rut %d", rut));
        }
        return resumen;
    }
}