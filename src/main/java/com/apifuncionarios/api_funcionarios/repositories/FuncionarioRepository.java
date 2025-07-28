package com.apifuncionarios.api_funcionarios.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apifuncionarios.api_funcionarios.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    Optional<Funcionario> findByRut(Integer rut);

    Page<Funcionario> findByNombresContainingIgnoreCaseOrApellidoPaternoContainingIgnoreCaseOrApellidoMaternoContainingIgnoreCase(
        String nombres, String paterno, String materno, Pageable pageable);

}
