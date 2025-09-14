package com.db.ayce.be.repository;

import com.db.ayce.be.entity.Impostazioni;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImpostazioniRepository extends JpaRepository<Impostazioni, String> {
    // Possiamo aggiungere metodi custom se serve
}
