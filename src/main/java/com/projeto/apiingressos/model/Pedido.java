
package com.projeto.apiingressos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long showId;

    private String username;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @Column(name = "entrada_fila")
    private LocalDateTime entradaFila;

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public Long getShowId() {
        return showId;
    }

    public void setShowId(Long showId) {
        this.showId = showId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public LocalDateTime getEntradaFila() {
        return entradaFila;
    }

    public void setEntradaFila(LocalDateTime entradaFila) {
        this.entradaFila = entradaFila;
    }

    public enum StatusPedido {
        NO_CARRINHO,
        RESERVADO,
        CANCELADO,
        AGUARDANDO_FILA
    }
}
