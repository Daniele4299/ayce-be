package com.db.ayce.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Ordine;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
	List<Ordine> findByFlagConsegnatoFalse();
}
