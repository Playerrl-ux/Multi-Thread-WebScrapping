package com.web.initialization.validation;

import com.web.enums.FetchMode;
import com.web.initialization.validation.parsers.FetchModeParser;
import com.web.initialization.validation.parsers.IntegerParser;
import com.web.initialization.validation.parsers.Parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class Validator {

    private final Params params = new Params();

    private final Map<String, ParamValidator<?>> VALIDATORS = Map.of(
            "r", new ParamValidator<>(new IntegerParser(),
                    (value) -> params.requisitions = value),
            "m", new ParamValidator<>(new FetchModeParser(),
                    (value) -> params.fetchMode = value),
            "t", new ParamValidator<>(new IntegerParser(),
                    (value) -> params.attempts = value));

    public Params validate(String[] args) {
        if (!validateFile(args[args.length - 1])) {
            throw new IllegalArgumentException("O nome do arquivo com as urls deve vir ao final");
        }

        params.fileName = args[args.length - 1];
        for (int i = 0; i < args.length - 1; i += 2) {
            if (!validateParam(args[i], args, i)) {
                throw new IllegalArgumentException("Opcao invalida: " + args[i]);
            }
        }
        if (params.requisitions > 3000) {
            System.out.println("O numero maximo de requisicoes eh 3000");
            params.requisitions = 3000;
        }
        System.out.println(params);
        return params;
    }

    private boolean validateParam(String param, String[] params, int index) {
        if (param.length() != 2) return false;
        if (param.charAt(0) != '-') return false;

        var key = String.valueOf(param.charAt(1));
        var validatorParam = VALIDATORS.get(key);
        if (validatorParam == null) return false;

        if (index + 1 >= params.length) return false;
        if (params[index + 1].charAt(0) == '-') return false;

        return runValidator(validatorParam, params[index + 1]) != null;
    }

    private <T> T runValidator(ParamValidator<T> validatorParam, String param) {
        T parsed = validatorParam.parser.parse(param);
        if (parsed == null) return null;
        validatorParam.consumer.accept(parsed);

        return parsed;
    }

    private boolean validateFile(String fileName) {
        return Files.exists(Path.of(fileName));
    }

    private record ParamValidator<T>(Parser<T> parser, Consumer<T> consumer) {
    }

    public static class Params {
        Integer requisitions;
        FetchMode fetchMode;
        Integer attempts;
        String fileName;

        public Params() {
            requisitions = 100;
            fetchMode = FetchMode.ADAPTATIVE;
            attempts = 3;
            fileName = null;
        }

        public String toString() {
            return "Requisicoes: " + requisitions + " Modo: " + fetchMode +
                    " Tentativas: " + attempts + " Arquivo: " + fileName;
        }

        public Integer getRequisitions() {
            return requisitions;
        }

        public FetchMode getFetchMode() {
            return fetchMode;
        }

        public Integer getAttempts() {
            return attempts;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
