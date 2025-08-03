package com.db.ayce.be.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.repository.SessioneRepository;
import com.db.ayce.be.service.SessioneService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessioneServiceImpl implements SessioneService {

    private final SessioneRepository sessioneRepository;

    @Override
    public List<Sessione> findAll() {
        return sessioneRepository.findAll();
    }

    @Override
    public Sessione findById(Integer id) {
        return sessioneRepository.findById(id).orElse(null);
    }

    @Override
    public Sessione save(Sessione sessione) {
        return sessioneRepository.save(sessione);
    }

    @Override
    public Sessione update(Integer id, Sessione sessione) {
        sessione.setId(id);
        return sessioneRepository.save(sessione);
    }

    @Override
    public void delete(Integer id) {
        sessioneRepository.deleteById(id);
    }
}
