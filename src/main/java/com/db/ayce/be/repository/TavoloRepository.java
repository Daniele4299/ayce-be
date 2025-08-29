package com.db.ayce.be.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Tavolo;

public interface TavoloRepository extends JpaRepository<Tavolo, Integer> {
	Tavolo findByNumero(Integer numero);
}
