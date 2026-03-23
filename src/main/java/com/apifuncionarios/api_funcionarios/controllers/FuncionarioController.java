package com.apifuncionarios.api_funcionarios.controllers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apifuncionarios.api_funcionarios.dto.ResumenFeriadoLegal;
import com.apifuncionarios.api_funcionarios.services.interfaces.FotoService;
import com.apifuncionarios.api_funcionarios.services.interfaces.FuncionarioService;
import com.apifuncionarios.api_funcionarios.services.interfaces.SearchFuncionarioService;
import com.apifuncionarios.api_funcionarios.services.interfaces.SearchViewFuncionarioService;

@RestController
@RequestMapping("/api/funcionario")
@CrossOrigin(origins = "http://localhost:5173")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final FotoService fotoService;

    private final SearchFuncionarioService searchFuncionarioService;
    private final SearchViewFuncionarioService viewSearch;

    public FuncionarioController(FuncionarioService funcionarioService,
            SearchFuncionarioService searchFuncionarioService,
            FotoService fotoService, SearchViewFuncionarioService viewSearch) {
        this.funcionarioService = funcionarioService;
        this.searchFuncionarioService = searchFuncionarioService;
        this.fotoService = fotoService;
        this.viewSearch = viewSearch;
    }

    @GetMapping
    public ResponseEntity<Object> obtenerDetalleColaborador(@RequestParam Integer rut) {

        return ResponseEntity.ok(funcionarioService.getFuncionarioInfo(rut));
    }

    @GetMapping("/search")
    public ResponseEntity<Object> obtenerColaboradoresByName(@RequestParam String pattern,
            @RequestParam int pageNumber) {

        return ResponseEntity.ok(searchFuncionarioService.searchByName(pattern, pageNumber));
    }

    @GetMapping("/feriados")
    public ResponseEntity<Object> obtenerDetalleFeriados(@RequestParam Integer rut, @RequestParam Integer ident) {

        ResumenFeriadoLegal response = funcionarioService.resumenFeriadosLegales(rut, ident);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No se encontraron feriados"));
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @GetMapping("/administrativos")
    public ResponseEntity<Object> obtenerAdministrativos(@RequestParam Integer rut, @RequestParam Integer ident) {

        return ResponseEntity.ok(funcionarioService.resumenAdm(rut, ident));
    }

    @GetMapping("/foto/{rut}")
    public ResponseEntity<Object> obtenerFoto(@PathVariable Integer rut) {

        return ResponseEntity.ok(fotoService.getFotoFuncionario(rut));
    }

    @GetMapping("/search-view")
    public ResponseEntity<Object> viewColaboradoresByName(@RequestParam String pattern,
            @RequestParam int pageNumber) {

        return ResponseEntity.ok(viewSearch.findByNombre(pattern, pageNumber));
    }

}
