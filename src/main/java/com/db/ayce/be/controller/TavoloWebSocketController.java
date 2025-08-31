package com.db.ayce.be.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import com.db.ayce.be.dto.TavoloMessage;
import com.db.ayce.be.dto.TavoloMessagePayload;
import com.db.ayce.be.entity.Ordine;
import com.db.ayce.be.entity.Prodotto;
import com.db.ayce.be.entity.Sessione;
import com.db.ayce.be.service.OrdineService;
import com.db.ayce.be.service.ProdottoService;
import com.db.ayce.be.service.SessioneService;
import com.db.ayce.be.service.TavoloTempService;
import com.db.ayce.be.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TavoloWebSocketController {

    private final TavoloTempService tavoloTempService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final OrdineService ordineService;
    private final SessioneService sessioneService;
    private final ProdottoService prodottoService;

    @MessageMapping("/tavolo")
    public void handleTavoloMessage(TavoloMessage msg, Principal principal) throws Exception {
        Claims claims = extractClaims(principal);
        Sessione sessione = getSessioneAttiva(claims);
        Integer tavoloId = sessione.getTavolo().getId();

        switch (msg.getTipoEvento()) {
            case Constants.MSG_ADD_ITEM_TEMP, Constants.MSG_REMOVE_ITEM_TEMP -> handleTempItem(msg, sessione, tavoloId);
            case Constants.MSG_ORDER_SENT -> handleOrderSent(sessione, tavoloId);
            case "GET_STATUS" -> sendStatus(sessione, tavoloId);
            default -> throw new IllegalArgumentException("Tipo messaggio non gestito: " + msg.getTipoEvento());
        }
    }

    private Claims extractClaims(Principal principal) throws IllegalAccessException {
        if (!(principal instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Claims claims)
                || !Constants.ROLE_CLIENT.equals(claims.get("role", String.class))) {
            throw new IllegalAccessException("Utente non autorizzato");
        }
        return (Claims) auth.getPrincipal();
    }

    private Sessione getSessioneAttiva(Claims claims) {
        Long sessioneId = claims.get("sessioneId", Long.class);
        Sessione sessione = sessioneService.findById(sessioneId);
        if (sessione == null || !"ATTIVA".equals(sessione.getStato()))
            throw new IllegalStateException("Sessione non attiva");
        return sessione;
    }

    private void handleTempItem(TavoloMessage msg, Sessione sessione, Integer tavoloId) throws Exception {
        TavoloMessagePayload payload = objectMapper.readValue(msg.getPayload(), TavoloMessagePayload.class);
        Long prodottoId = payload.getProdottoId();
        int delta = msg.getTipoEvento().equals(Constants.MSG_REMOVE_ITEM_TEMP) ? -payload.getQuantita() : payload.getQuantita();
        Prodotto prodotto = prodottoService.findById(prodottoId);

        // Controllo massimo portate solo per prodotti normali
        if (Boolean.TRUE.equals(sessione.getIsAyce()) && isProdottoNormale(prodotto)) {
            int totalePortate = tavoloTempService.getOrdineTemp(tavoloId).entrySet().stream()
                    .mapToInt(e -> isProdottoNormale(prodottoService.findById(e.getKey())) ? e.getValue() : 0).sum();
            int maxPortate = sessione.getNumeroPartecipanti() * Constants.MAX_PORTATE_PER_PERSONA;
            if (totalePortate + delta > maxPortate) {
                sendError(tavoloId, "Limite portate raggiunto");
                return;
            }
        }

        // Controllo prodotti limitati
        if (Boolean.TRUE.equals(sessione.getIsAyce()) && prodotto != null
                && Boolean.TRUE.equals(prodotto.getIsLimitedPartecipanti()) && delta > 0) {

            int restante = sessione.getNumeroPartecipanti() - ordineService.findBySessione(sessione).stream()
                    .filter(o -> prodottoId.equals(o.getProdotto().getId()))
                    .mapToInt(Ordine::getQuantita).sum()
                    - tavoloTempService.getOrdineTemp(tavoloId).getOrDefault(prodottoId, 0);

            if (restante <= 0) {
                sendError(tavoloId, "Limite per questo prodotto raggiunto (max " + sessione.getNumeroPartecipanti() + " - 1 a persona per sessione)");
                return;
            }
            if (delta > restante) {
                sendError(tavoloId, "Puoi aggiungere al massimo " + restante + " di questo prodotto per la sessione");
                return;
            }
        }

        // Aggiorno ordine temporaneo
        if (delta > 0) tavoloTempService.addItem(tavoloId, prodottoId, delta);
        else tavoloTempService.removeItem(tavoloId, prodottoId, -delta);
        sendUpdateTemp(tavoloId);
    }

    private void handleOrderSent(Sessione sessione, Integer tavoloId) throws Exception {
        Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
        StringBuilder warning = new StringBuilder();
        boolean savedAnyNormalProduct = false;

        for (Map.Entry<Long, Integer> e : ordineTemp.entrySet()) {
            Long prodottoId = e.getKey();
            Integer quantita = e.getValue();
            if (quantita == null || quantita <= 0) continue;

            Prodotto prodotto = prodottoService.findById(prodottoId);
            if (prodotto == null) continue;

            if (Boolean.TRUE.equals(sessione.getIsAyce()) && Boolean.TRUE.equals(prodotto.getIsLimitedPartecipanti()) && isProdottoNormale(prodotto)) {
                int confermati = ordineService.findBySessione(sessione).stream()
                        .filter(o -> prodottoId.equals(o.getProdotto().getId()))
                        .mapToInt(Ordine::getQuantita).sum();

                int restante = sessione.getNumeroPartecipanti() - confermati;
                if (restante <= 0) {
                    warning.append("Nessuna quantità disponibile per ").append(prodotto.getNome()).append(". ");
                    continue;
                }
                if (quantita > restante) {
                    warning.append("Quantità per ").append(prodotto.getNome()).append(" ridotta a ").append(restante).append(". ");
                    quantita = restante;
                }
            }

            Ordine ordine = new Ordine();
            ordine.setFlagConsegnato(false);
            ordine.setOrario(LocalDateTime.now());
            ordine.setPrezzoUnitario(Boolean.TRUE.equals(sessione.getIsAyce()) ? 0 : prodotto.getPrezzo());
            ordine.setProdotto(prodotto);
            ordine.setQuantita(quantita);
            ordine.setSessione(sessione);
            ordine.setStato("INVIATO");
            ordine.setTavolo(sessione.getTavolo());
            ordineService.save(ordine);

            if (isProdottoNormale(prodotto)) savedAnyNormalProduct = true;
        }

        tavoloTempService.clearOrdine(tavoloId);
        if (Boolean.TRUE.equals(sessione.getIsAyce()) && savedAnyNormalProduct) {
            sessione.setUltimoOrdineInviato(LocalDateTime.now());
            sessioneService.save(sessione);
            messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, new TavoloMessage(Constants.MSG_ORDER_SENT, ""));
        }
        sendUpdateTemp(tavoloId);
        if (warning.length() > 0) sendError(tavoloId, warning.toString());
    }

    private void sendStatus(Sessione sessione, Integer tavoloId) throws Exception {
        Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId,
                new TavoloMessage(Constants.MSG_UPDATE_TEMP,
                        objectMapper.writeValueAsString(Map.of(
                                "ordine", ordineTemp,
                                "lastOrder", sessione.getUltimoOrdineInviato()
                        ))));
    }

    private void sendUpdateTemp(Integer tavoloId) throws Exception {
        Map<Long, Integer> ordineAggiornato = tavoloTempService.getOrdineTemp(tavoloId);
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId,
                new TavoloMessage(Constants.MSG_UPDATE_TEMP, objectMapper.writeValueAsString(ordineAggiornato)));
    }

    private void sendError(Integer tavoloId, String msg) {
        messagingTemplate.convertAndSend("/topic/tavolo/" + tavoloId, new TavoloMessage(Constants.MSG_ERROR, msg));
    }

    private boolean isProdottoNormale(Prodotto p) {
        return p != null && p.getCategoria() != null && p.getCategoria().getId() < 100;
    }
}
