package com.db.ayce.be.service.impl;

import com.db.ayce.be.entity.UtenteProdotto;
import com.db.ayce.be.entity.UtenteProdottoId;
import com.db.ayce.be.repository.UtenteProdottoRepository;
import com.db.ayce.be.service.UtenteProdottoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UtenteProdottoServiceImpl implements UtenteProdottoService {

    private final UtenteProdottoRepository repository;

    public UtenteProdottoServiceImpl(UtenteProdottoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<UtenteProdotto> getByUtente(Long utenteId) {
        return repository.findById_UtenteId(utenteId);
    }

    @Override
    public Optional<UtenteProdotto> getOne(Long utenteId, Long prodottoId) {
        return repository.findById(new UtenteProdottoId(utenteId, prodottoId));
    }

    @Transactional
    @Override
    public UtenteProdotto setRiceveComanda(Long utenteId, Long prodottoId, boolean riceveComanda) {
        UtenteProdottoId id = new UtenteProdottoId(utenteId, prodottoId);
        UtenteProdotto up = repository.findById(id)
                .orElse(new UtenteProdotto(id, riceveComanda));
        up.setRiceveComanda(riceveComanda);
        return repository.save(up); // Hibernate fa UPDATE se esiste già
    }

    @Transactional
    @Override
    public void deleteByUtenteAndProdotto(Long utenteId, Long prodottoId) {
        repository.deleteById(new UtenteProdottoId(utenteId, prodottoId));
    }
}
