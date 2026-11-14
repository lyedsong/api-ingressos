
package com.projeto.apiingressos.repository;

import com.projeto.apiingressos.model.Ingresso;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngressoRepository extends JpaRepository<Ingresso, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Ingresso i ORDER BY i.id ASC")
    List<Ingresso> buscarEstoqueComBloqueio();

}
