package com.db.ayce.be.controller;

import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.db.ayce.be.dto.TavoloMessage;
import com.db.ayce.be.dto.TavoloMessagePayload;
import com.db.ayce.be.service.TavoloTempService;

import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequiredArgsConstructor
public class TavoloWebSocketController {

    private final TavoloTempService tavoloTempService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @MessageMapping("/tavolo")
    public void handleTavoloMessage(TavoloMessage msg) throws Exception {
        Integer tavoloId = msg.getTavoloId();
        String tipo = msg.getTipoEvento();

        if ("ADD_ITEM_TEMP".equals(tipo) || "REMOVE_ITEM_TEMP".equals(tipo)) {
            TavoloMessagePayload payload = objectMapper.readValue(msg.getPayload(), TavoloMessagePayload.class);

            if ("ADD_ITEM_TEMP".equals(tipo)) {
                tavoloTempService.addItem(tavoloId, payload.getProdottoId(), payload.getQuantita());
            } else {
                tavoloTempService.removeItem(tavoloId, payload.getProdottoId(), payload.getQuantita());
            }

            // Invio a tutti i client lo stato aggiornato
            Map<Long, Integer> ordineAggiornato = tavoloTempService.getOrdineTemp(tavoloId);
            messagingTemplate.convertAndSend(
                "/topic/tavolo/" + tavoloId,
                new TavoloMessage(msg.getSessioneId(), tavoloId, "UPDATE_TEMP", objectMapper.writeValueAsString(ordineAggiornato))
            );

        } else if ("ORDER_SENT".equals(tipo)) {
            // blocco modifica ordine
            tavoloTempService.clearOrdine(tavoloId);

            messagingTemplate.convertAndSend(
                "/topic/tavolo/" + tavoloId,
                new TavoloMessage(msg.getSessioneId(), tavoloId, "ORDER_SENT", "")
            );
        }
    }
}
