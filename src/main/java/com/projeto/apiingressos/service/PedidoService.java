
package com.projeto.apiingressos.service;

import com.projeto.apiingressos.model.Ingresso;
import com.projeto.apiingressos.model.Pedido;
import com.projeto.apiingressos.repository.IngressoRepository;
import com.projeto.apiingressos.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final IngressoRepository ingressoRepository;

    public PedidoService(
            PedidoRepository pedidoRepository,
            IngressoRepository ingressoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.ingressoRepository = ingressoRepository;
    }

    @Transactional
    public Pedido criarPedido(Long showId, String username) {

        Pedido pedido = new Pedido();

        pedido.setShowId(showId);
        pedido.setUsername(username);
        pedido.setStatus(Pedido.StatusPedido.NO_CARRINHO);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido reservarPedido(Long pedidoId, String username) {

        // Bloqueia o estoque para evitar reservas simultâneas.
        Ingresso estoque = ingressoRepository.buscarEstoqueComBloqueio()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                    new IllegalStateException("ESTOQUE_NAO_INICIALIZADO")
                );

        // Busca o pedido.
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                    new IllegalStateException("PEDIDO_NAO_ENCONTRADO")
                );

        // Verifica se o pedido pertence ao usuário.
        if (!pedido.getUsername().equals(username)) {
            throw new IllegalStateException("PEDIDO_NAO_ENCONTRADO");
        }

        // Verifica se o pedido está no estado correto.
        if (pedido.getStatus() != Pedido.StatusPedido.NO_CARRINHO) {
            throw new IllegalStateException("PEDIDO_NAO_RESERVAVEL");
        }

        // Verifica a quantidade de reservas do usuário.
        long reservas = pedidoRepository.countByUsernameAndStatus(
                username,
                Pedido.StatusPedido.RESERVADO
        );

        if (reservas >= 2) {
            throw new IllegalStateException("COTA_PESSOAL");
        }

        // Verifica se ainda existem ingressos.
        if (estoque.getQuantidadeDisponivel() <= 0) {
            throw new IllegalStateException("POOL_CHEIO");
        }

        // Atualiza o estoque e o pedido.
        estoque.setQuantidadeDisponivel(
                estoque.getQuantidadeDisponivel() - 1
        );

        pedido.setStatus(Pedido.StatusPedido.RESERVADO);

        return pedidoRepository.save(pedido);
    }

@Transactional
public Pedido cancelarPedido(Long pedidoId, String username) {

    // Bloqueia o estoque durante o cancelamento.
    Ingresso estoque = ingressoRepository.buscarEstoqueComBloqueio()
            .stream()
            .findFirst()
            .orElseThrow(() ->
                new IllegalStateException("ESTOQUE_NAO_INICIALIZADO")
            );

    // Busca o pedido.
    Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() ->
                new IllegalStateException("PEDIDO_NAO_ENCONTRADO")
            );

    // Confere se o pedido pertence ao usuário.
    if (!pedido.getUsername().equals(username)) {
        throw new IllegalStateException("PEDIDO_NAO_ENCONTRADO");
    }

    // Só permite cancelar pedidos reservados.
    if (pedido.getStatus() != Pedido.StatusPedido.RESERVADO) {
        throw new IllegalStateException("PEDIDO_NAO_RESERVAVEL");
    }

    // Devolve o ingresso ao estoque.
    estoque.setQuantidadeDisponivel(
            estoque.getQuantidadeDisponivel() + 1
    );

    // Atualiza o status do pedido.
    pedido.setStatus(Pedido.StatusPedido.CANCELADO);

    return pedidoRepository.save(pedido);
}

}
