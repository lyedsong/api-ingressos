
package com.projeto.apiingressos.controller;

import com.projeto.apiingressos.model.Pedido;
import com.projeto.apiingressos.service.PedidoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criarPedido(
            @RequestBody PedidoRequest requisicao,
            Authentication autenticacao) {

        Pedido pedido = pedidoService.criarPedido(
                requisicao.showId(),
                autenticacao.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", pedido.getId(),
                        "status", pedido.getStatus().name()
                )
        );
    }

    @PostMapping("/{id}/reservar")
    public ResponseEntity<Map<String, Object>> reservarPedido(
            @PathVariable Long id,
            Authentication autenticacao) {

        Pedido pedido = pedidoService.reservarPedido(
                id,
                autenticacao.getName()
        );

        return ResponseEntity.ok()
                .header("X-Lote", "7F3A")
                .body(Map.of(
                        "id", pedido.getId(),
                        "status", pedido.getStatus().name()
                ));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Map<String, Object>> cancelarPedido(
            @PathVariable Long id,
            Authentication autenticacao) {

        Pedido pedido = pedidoService.cancelarPedido(
                id,
                autenticacao.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "id", pedido.getId(),
                        "status", pedido.getStatus().name()
                )
        );
    }

    public record PedidoRequest(Long showId) {
    }
}
