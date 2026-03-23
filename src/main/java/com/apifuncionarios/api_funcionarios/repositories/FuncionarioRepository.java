package com.apifuncionarios.api_funcionarios.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apifuncionarios.api_funcionarios.dto.SearchnNombreDto;
import com.apifuncionarios.api_funcionarios.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

    Optional<Funcionario> findByRut(Integer rut);

    Page<Funcionario> findByNombresContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(
            String nombres, String paterno, String materno, Pageable pageable);

    @Query(value = "SELECT nombre_completo, rut, vrut FROM vfuncionarios WHERE nombre_completo LIKE CONCAT('%', :nombres, '%')", nativeQuery = true)
    Page<SearchnNombreDto> findByNombre(String nombres, Pageable pageable);

}
