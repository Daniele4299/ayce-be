package com.db.ayce.be.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.db.ayce.be.dto.TavoloMessage;

@Service
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Invia messaggio a tutti i client del tavolo
    public void sendToTavolo(Integer tavoloId, TavoloMessage message) {
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, message);
    }
}
