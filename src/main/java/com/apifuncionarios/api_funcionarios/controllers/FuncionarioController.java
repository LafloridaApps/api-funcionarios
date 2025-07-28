package com.apifuncionarios.api_funcionarios.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apifuncionarios.api_funcionarios.serivces.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.serivces.interfaces.SearchFuncionarioService;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;

    private final SearchFuncionarioService searchFuncionarioService;

    public FuncionarioController(FuncionarioService funcionarioService,
            SearchFuncionarioService searchFuncionarioService) {
        this.funcionarioService = funcionarioService;
        this.searchFuncionarioService = searchFuncionarioService;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerDetalleColaborador(@RequestParam Integer rut) {

        return ResponseEntity.ok(funcionarioService.getFuncionarioInfo(rut));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> obtenerColaboradoresByName(@RequestParam String pattern,
    @RequestParam int pageNumber) {

        return ResponseEntity.ok(searchFuncionarioService.searchByName(pattern,pageNumber));
    }

}
