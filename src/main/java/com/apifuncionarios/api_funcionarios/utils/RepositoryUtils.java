package com.apifuncionarios.api_funcionarios.utils;

import java.util.Optional;

import com.apifuncionarios.api_funcionarios.exceptions.NotFounException;


public class RepositoryUtils {

    private RepositoryUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> T findOrThrow(Optional<T> optional, String mensajeError) {
        return optional.orElseThrow(() -> new NotFounException(mensajeError));
    }

}
