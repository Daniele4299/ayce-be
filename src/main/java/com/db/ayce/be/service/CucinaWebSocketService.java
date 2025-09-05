package com.db.ayce.be.service;

import com.db.ayce.be.dto.CucinaMessage;

public interface CucinaWebSocketService {
    void handleCucinaMessage(CucinaMessage msg);
    void notifyNewOrder(Long ordineId);
    void notifyConsegnaChanged(Long ordineId, boolean consegnato);
}
