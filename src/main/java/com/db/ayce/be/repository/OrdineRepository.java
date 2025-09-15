package com.db.ayce.be.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Sessione;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
	List<Ordine> findByFlagConsegnatoFalse();

    List<Ordine> findBySessioneIn(List<Sessione> sessioni);
    
    List<Ordine> findBySessioneId(Long sessioneId);

	List<Ordine> findBySessione(Sessione sessione);
	
    List<Ordine> findByOrarioBetween(LocalDateTime from, LocalDateTime to);
    
    // Aggregazione per top prodotti (resti in DTO direttamente)
    @Query("SELECT new com.db.ayce.be.dto.ProductSalesDto(o.prodotto.id, o.prodotto.nome, SUM(o.quantita)) " +
           "FROM Ordine o WHERE o.orario BETWEEN :start AND :end GROUP BY o.prodotto.id, o.prodotto.nome " +
           "ORDER BY SUM(o.quantita) DESC")
    List<com.db.ayce.be.dto.ProductSalesDto> findTopProductsByPeriod(java.time.LocalDateTime start, java.time.LocalDateTime end, PageRequest pageRequest);

    @Query("SELECT new com.db.ayce.be.dto.ProductSalesDto(o.prodotto.id, o.prodotto.nome, SUM(o.quantita)) " +
           "FROM Ordine o WHERE o.orario BETWEEN :start AND :end GROUP BY o.prodotto.id, o.prodotto.nome " +
           "ORDER BY SUM(o.quantita) ASC")
    List<com.db.ayce.be.dto.ProductSalesDto> findBottomProductsByPeriod(java.time.LocalDateTime start, java.time.LocalDateTime end, PageRequest pageRequest);
}
