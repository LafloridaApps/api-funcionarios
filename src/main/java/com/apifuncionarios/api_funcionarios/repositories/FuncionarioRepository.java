package com.apifuncionarios.api_funcionarios.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apifuncionarios.api_funcionarios.entities.Funcionario;

public interface FuncionarioRepository extends JpaRepository<Funcionario,Long> {

    Optional<Funcionario> findByRut(Integer rut);

}
