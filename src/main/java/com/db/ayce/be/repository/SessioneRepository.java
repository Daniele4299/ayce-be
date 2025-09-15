package com.db.ayce.be.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.entity.Tavolo;

public interface SessioneRepository extends JpaRepository<Sessione, Long> {
	List<Sessione> findByStatoIgnoreCase(String stato);
	Optional<Sessione> findByTavoloAndStato(Tavolo tavolo, String stato);
	List<Sessione> findByOrarioInizioBetween(LocalDateTime inizio, LocalDateTime fine);
}
