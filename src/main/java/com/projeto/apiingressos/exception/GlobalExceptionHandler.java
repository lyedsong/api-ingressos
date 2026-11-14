
package com.projeto.apiingressos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> tratarErro(
            IllegalStateException erro) {

        String mensagem = erro.getMessage();

        if ("POOL_CHEIO".equals(mensagem)
        || "COTA_PESSOAL".equals(mensagem)
        || "PEDIDO_NAO_RESERVAVEL".equals(mensagem)
        || "FILA_NAO_DISPONIVEL".equals(mensagem)
        || "PEDIDO_NAO_ENFILEIRAVEL".equals(mensagem)) {
            
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", mensagem));
        }

        if ("PEDIDO_NAO_ENCONTRADO".equals(mensagem)) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", mensagem));
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", mensagem));
    }
}
