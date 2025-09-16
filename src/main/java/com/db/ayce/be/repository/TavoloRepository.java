package com.db.ayce.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Tavolo;

public interface TavoloRepository extends JpaRepository<Tavolo, Integer> {
    Tavolo findByNumeroAndIsDeletedFalse(Integer numero);
    Tavolo findByNumeroAndIsDeletedTrue(Integer numero);
    List<Tavolo> findByIsDeletedFalse();
}

