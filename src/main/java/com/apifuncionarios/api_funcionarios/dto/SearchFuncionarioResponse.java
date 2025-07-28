package com.apifuncionarios.api_funcionarios.dto;

import java.util.List;

public class SearchFuncionarioResponse {

    private int currentPage;
    private int totalPages;
    private int totalItems;
    private Long size;

    
    List<FuncionarioResponse> funcionarios;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }



    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public List<FuncionarioResponse> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<FuncionarioResponse> funcionarios) {
        this.funcionarios = funcionarios;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

}
