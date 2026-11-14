
package com.projeto.apiingressos.repository;

import com.projeto.apiingressos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsernameAndStatus(
        String username,
        Pedido.StatusPedido status
    );
    
    long countByUsernameAndStatus(
    String username,
    Pedido.StatusPedido status
);

}
