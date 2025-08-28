package com.db.ayce.be.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Sessione;

public interface SessioneRepository extends JpaRepository<Sessione, Long> {
	List<Sessione> findByStatoIgnoreCase(String stato);
}
