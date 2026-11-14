
package com.projeto.apiingressos.controller;

import com.projeto.apiingressos.model.Ingresso;
import com.projeto.apiingressos.service.IngressoService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IngressoController {

    private final IngressoService ingressoService;

    public IngressoController(IngressoService ingressoService) {
        this.ingressoService = ingressoService;
    }

    @GetMapping("/pool")
    public Map<String, Integer> consultarEstoque() {

        Ingresso estoque = ingressoService.obterEstoque();

        return Map.of(
            "total", estoque.getQuantidadeTotal(),
            "ocupadas", estoque.getQuantidadeTotal() - estoque.getQuantidadeDisponivel(),
            "livres", estoque.getQuantidadeDisponivel()
        );
    }
}
