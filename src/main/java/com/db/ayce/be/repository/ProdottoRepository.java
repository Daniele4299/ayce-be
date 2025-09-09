package com.db.ayce.be.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.db.ayce.be.dto.UltimoOrdineDto;
import com.db.ayce.be.entity.Prodotto;

public interface ProdottoRepository extends JpaRepository<Prodotto, Long> {
	
	@Query("""
		    SELECT new com.db.ayce.be.dto.UltimoOrdineDto(o.prodotto.nome, SUM(o.quantita))
		    FROM Ordine o
		    WHERE o.orario >= :inizio AND o.orario < :fine
		    GROUP BY o.prodotto.nome
		    """)
		List<UltimoOrdineDto> getQuantitaOrdinataGiornata(
		        @Param("inizio") LocalDateTime inizio,
		        @Param("fine") LocalDateTime fine
		);

	
}
