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
import java.util.Optional;

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
        if (funcionario == null) {
            throw new NotFounException("Funcionario nulo al construir la respuesta");
        }

        DepartamentoResponse depto = null;
        Long idDepto = funcionario.getIdDepto();
        if (idDepto != null) {
            try {
                depto = apiDepartamentoService.obtenerDetalleDepartamentoById(idDepto);
            } catch (Exception e) {
                logger.warn("No se pudo obtener el detalle del departamento id={} para rut={}", idDepto,
                        funcionario.getRut(), e);
            }
        } else {
            logger.debug("Funcionario rut={} no tiene idDepto definido", funcionario.getRut());
        }

        Funcionario jefeDepto = resolveJefeDepto(depto, funcionario.getRut());

        return funcionarioMapper.toResponseDto(funcionario, depto, jefeDepto, foto);
    }

    // Extrae la lógica de resolución del jefe de departamento para facilitar tests y legibilidad
    Funcionario resolveJefeDepto(DepartamentoResponse depto, Integer rutFuncionario) {
        if (depto == null) return null;

        DepartamentoResponse current = depto;
        final int maxHops = 20; // seguridad para evitar loops infinitos

        for (int hops = 0; hops < maxHops; hops++) {

            // Intentar resolver en el departamento actual
            Optional<Funcionario> resolved = attemptResolveInDept(current, rutFuncionario);
            if (resolved.isPresent()) return resolved.get();

            // Ascender
            Long parentId = current.getIdDeptoSuperior();
            if (parentId == null) return null;

            DepartamentoResponse parent = safeObtenerDepto(parentId);
            if (parent == null) return null;

            // Intento final si es nivel de parada
            if (isStopLevel(parent)) {
                Optional<Funcionario> finalResolved = attemptResolveInDept(parent, rutFuncionario);
                return finalResolved.orElse(null);
            }

            current = parent;
        }

        return null;
    }

    private Optional<Integer> getCandidateRut(DepartamentoResponse depto, Integer rutFuncionario) {
        if (depto == null) return Optional.empty();
        Integer rutJefe = depto.getRutJefe();
        Integer rutJefeSup = depto.getRutJefeSuperior();

        if (rutJefe != null && !java.util.Objects.equals(rutJefe, rutFuncionario)) {
            return Optional.of(rutJefe);
        }
        if (rutJefe == null && rutJefeSup != null && !java.util.Objects.equals(rutJefeSup, rutFuncionario)) {
            return Optional.of(rutJefeSup);
        }
        return Optional.empty();
    }

    private Optional<Funcionario> findFuncionarioByRut(Integer rut) {
        if (rut == null) return Optional.empty();
        try {
            return funcionarioRepository.findByRut(rut);
        } catch (Exception e) {
            logger.warn("Error al buscar funcionario por rut={}", rut, e);
            return Optional.empty();
        }
    }

    private Optional<Funcionario> attemptResolveInDept(DepartamentoResponse depto, Integer rutFuncionario) {
        Optional<Integer> candidateRutOpt = getCandidateRut(depto, rutFuncionario);
        if (candidateRutOpt.isEmpty()) return Optional.empty();
        return findFuncionarioByRut(candidateRutOpt.get());
    }

    private DepartamentoResponse safeObtenerDepto(Long id) {
        try {
            return apiDepartamentoService.obtenerDetalleDepartamentoById(id);
        } catch (Exception e) {
            logger.warn("Fallo al obtener departamento id={}", id, e);
            return null;
        }
    }

    private boolean isStopLevel(DepartamentoResponse depto) {
        if (depto == null || depto.getNombre() == null) return false;
        String nombre = depto.getNombre().toUpperCase();
        return nombre.contains("SUBDIRECCION") || nombre.contains("DIRECCION");
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