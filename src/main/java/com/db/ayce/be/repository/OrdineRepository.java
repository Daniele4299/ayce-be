package com.db.ayce.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Sessione;

public interface OrdineRepository extends JpaRepository<Ordine, Long> {
	List<Ordine> findByFlagConsegnatoFalse();

    List<Ordine> findBySessioneIn(List<Sessione> sessioni);
}
