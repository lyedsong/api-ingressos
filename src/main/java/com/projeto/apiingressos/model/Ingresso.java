package com.projeto.apiingressos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ingressos")
public class Ingresso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantidadeTotal = 100;

    private Integer quantidadeDisponivel = 100;



public Long getId() {
    return id;
}

public Integer getQuantidadeTotal() {
    return quantidadeTotal;
}

public Integer getQuantidadeDisponivel() {
    return quantidadeDisponivel;
}

public void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
    this.quantidadeDisponivel = quantidadeDisponivel;
}
}