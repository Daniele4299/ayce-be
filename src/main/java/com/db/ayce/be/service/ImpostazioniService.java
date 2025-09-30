package com.db.ayce.be.service;

import com.db.ayce.be.entity.Impostazioni;

import java.util.List;
import java.util.Optional;

public interface ImpostazioniService {

    List<Impostazioni> findAll();

    Optional<Impostazioni> findByChiave(String chiave);

    Impostazioni save(Impostazioni Impostazioni);

    Impostazioni update(String chiave, String valore);

	Integer getIntValue(String string, int i);
	
	boolean getBooleanValue(String chiave, boolean defaultValue);

	Double getDoubleValue(String chiave, double defaultValue);
}
