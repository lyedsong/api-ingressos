package com.projeto.apiingressos.service;

import com.projeto.apiingressos.model.Ingresso;
import com.projeto.apiingressos.repository.IngressoRepository;
import org.springframework.stereotype.Service;

@Service
public class IngressoService {

    private final IngressoRepository ingressoRepository;

    public IngressoService(IngressoRepository ingressoRepository) {
        this.ingressoRepository = ingressoRepository;
    }

    public Ingresso obterEstoque() {

        return ingressoRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> ingressoRepository.save(new Ingresso()));

    }
}