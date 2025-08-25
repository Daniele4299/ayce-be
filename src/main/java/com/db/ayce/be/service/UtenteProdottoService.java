package com.db.ayce.be.service;

import com.db.ayce.be.entity.UtenteProdotto;

import java.util.List;
import java.util.Optional;

public interface UtenteProdottoService {
    List<UtenteProdotto> getByUtente(Long utenteId);
    UtenteProdotto setRiceveComanda(Long utenteId, Long prodottoId, boolean riceveComanda);
    Optional<UtenteProdotto> getOne(Long utenteId, Long prodottoId);
    void deleteByUtenteAndProdotto(Long utenteId, Long prodottoId);
}
