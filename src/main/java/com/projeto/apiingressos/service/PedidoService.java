
package com.projeto.apiingressos.service;

import com.projeto.apiingressos.model.Ingresso;
import com.projeto.apiingressos.model.Pedido;
import com.projeto.apiingressos.repository.IngressoRepository;
import com.projeto.apiingressos.repository.PedidoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

        Ingresso estoque = obterEstoqueComBloqueio();

        Pedido pedido = buscarPedidoDoUsuario(pedidoId, username);

        if (pedido.getStatus() != Pedido.StatusPedido.NO_CARRINHO) {
            throw new IllegalStateException("PEDIDO_NAO_RESERVAVEL");
        }

        long reservas = pedidoRepository.countByUsernameAndStatus(
                username,
                Pedido.StatusPedido.RESERVADO
        );

        if (reservas >= 2) {
            throw new IllegalStateException("COTA_PESSOAL");
        }

        if (estoque.getQuantidadeDisponivel() <= 0) {
            throw new IllegalStateException("POOL_CHEIO");
        }

        estoque.setQuantidadeDisponivel(
                estoque.getQuantidadeDisponivel() - 1
        );

        pedido.setStatus(Pedido.StatusPedido.RESERVADO);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido entrarNaFila(Long pedidoId, String username) {

        Ingresso estoque = obterEstoqueComBloqueio();

        Pedido pedido = buscarPedidoDoUsuario(pedidoId, username);

        if (pedido.getStatus() != Pedido.StatusPedido.NO_CARRINHO) {
            throw new IllegalStateException("PEDIDO_NAO_ENFILEIRAVEL");
        }

        if (estoque.getQuantidadeDisponivel() > 0) {
            throw new IllegalStateException("FILA_NAO_DISPONIVEL");
        }

        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_FILA);
        pedido.setEntradaFila(LocalDateTime.now());

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long pedidoId, String username) {

        Ingresso estoque = obterEstoqueComBloqueio();

        Pedido pedido = buscarPedidoDoUsuario(pedidoId, username);

        // Permite cancelar uma entrada que ainda está na fila.
        if (pedido.getStatus() == Pedido.StatusPedido.AGUARDANDO_FILA) {
            pedido.setStatus(Pedido.StatusPedido.CANCELADO);
            pedido.setEntradaFila(null);

            return pedidoRepository.save(pedido);
        }

        // Só reservas confirmadas podem liberar ingressos.
        if (pedido.getStatus() != Pedido.StatusPedido.RESERVADO) {
            throw new IllegalStateException("PEDIDO_NAO_RESERVAVEL");
        }

        pedido.setStatus(Pedido.StatusPedido.CANCELADO);
        pedidoRepository.save(pedido);

        // Procura o próximo usuário elegível da fila.
        List<Pedido> fila = pedidoRepository
                .findByStatusOrderByEntradaFilaAscIdAsc(
                        Pedido.StatusPedido.AGUARDANDO_FILA
                );

        for (Pedido aguardando : fila) {

            long reservas = pedidoRepository.countByUsernameAndStatus(
                    aguardando.getUsername(),
                    Pedido.StatusPedido.RESERVADO
            );

            // Usuários que atingiram a cota são ignorados por enquanto.
            if (reservas >= 2) {
                continue;
            }

            // Transfere o ingresso para o primeiro usuário elegível.
            aguardando.setStatus(Pedido.StatusPedido.RESERVADO);
            aguardando.setEntradaFila(null);

            pedidoRepository.save(aguardando);

            return pedido;
        }

        // Se ninguém puder receber o ingresso, devolve ao estoque.
        estoque.setQuantidadeDisponivel(
                estoque.getQuantidadeDisponivel() + 1
        );

        return pedido;
    }

    private Ingresso obterEstoqueComBloqueio() {

        return ingressoRepository.buscarEstoqueComBloqueio()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("ESTOQUE_NAO_INICIALIZADO")
                );
    }

    private Pedido buscarPedidoDoUsuario(Long pedidoId, String username) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalStateException("PEDIDO_NAO_ENCONTRADO")
                );

        if (!pedido.getUsername().equals(username)) {
            throw new IllegalStateException("PEDIDO_NAO_ENCONTRADO");
        }

        return pedido;
    }
}
