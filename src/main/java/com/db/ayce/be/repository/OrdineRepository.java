package com.db.ayce.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Ordine;

public interface OrdineRepository extends JpaRepository<Ordine, Integer> {
}
