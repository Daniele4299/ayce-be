package com.db.ayce.be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.db.ayce.be.entity.CostoProdotto;

@Repository
public interface CostoProdottoRepository extends JpaRepository<CostoProdotto, Long> {
    Optional<CostoProdotto> findByProdottoId(Long prodottoId);
}

