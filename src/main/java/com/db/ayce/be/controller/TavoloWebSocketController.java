// File: com/db/ayce/be/controller/TavoloWebSocketController.java
package com.db.ayce.be.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
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
        if (!(principal instanceof UsernamePasswordAuthenticationToken auth)
                || !(auth.getPrincipal() instanceof Claims claims)
                || !Constants.ROLE_CLIENT.equals(claims.get("role", String.class))) {
            throw new IllegalAccessException("Utente non autorizzato");
        }

        // Recupero sessione e tavolo
        final Long sessioneId = claims.get("sessioneId", Long.class);
        final Sessione sessione = sessioneService.findById(sessioneId);
        if (sessione == null || !"ATTIVA".equals(sessione.getStato())) {
            throw new IllegalStateException("Sessione non attiva");
        }
        final Integer tavoloId = sessione.getTavolo().getId();

        // Totale portate già presenti (temporaneo)
        int totalePortate = tavoloTempService.getTotalePortate(tavoloId);

        switch (msg.getTipoEvento()) {
            case Constants.MSG_ADD_ITEM_TEMP, Constants.MSG_REMOVE_ITEM_TEMP -> {
                TavoloMessagePayload payload = objectMapper.readValue(msg.getPayload(), TavoloMessagePayload.class);
                Long prodottoId = payload.getProdottoId();
                int delta = payload.getQuantita();
                if (msg.getTipoEvento().equals(Constants.MSG_REMOVE_ITEM_TEMP)) delta = -delta;

                // Controllo massimo portate (vale solo per sessioni AYCE)
                if (Boolean.TRUE.equals(sessione.getIsAyce())) {
                    int nuovoTotale = totalePortate + delta;
                    int maxPortate = sessione.getNumeroPartecipanti() * Constants.MAX_PORTATE_PER_PERSONA;
                    if (nuovoTotale > maxPortate) {
                        messagingTemplate.convertAndSend(
                            "/topic/tavolo/" + tavoloId,
                            new TavoloMessage(Constants.MSG_ERROR, "Limite portate raggiunto")
                        );
                        return;
                    }
                }

                // Controllo isLimitedPartecipanti: se prodotto limitato e sessione AYCE, verifico quanto è già stato ordinato (DB + ordine temporaneo)
                Prodotto prodotto = prodottoService.findById(prodottoId);
                if (Boolean.TRUE.equals(sessione.getIsAyce())
                        && prodotto != null
                        && Boolean.TRUE.equals(prodotto.getIsLimitedPartecipanti())
                        && delta > 0) {

                    // ordini già confermati per questa sessione dal DB
                    List<Ordine> ordiniConfermati = ordineService.findBySessione(sessione);
                    int confermatiPerProdotto = ordiniConfermati.stream()
                            .filter(o -> o.getProdotto() != null && prodottoId.equals(o.getProdotto().getId()))
                            .mapToInt(Ordine::getQuantita)
                            .sum();

                    // quantità già presente nell'ordine temporaneo (attuale)
                    Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
                    int tempPerProdotto = ordineTemp.getOrDefault(prodottoId, 0);

                    int maxPerSessione = sessione.getNumeroPartecipanti(); // uno a persona per tutta la sessione
                    int restante = maxPerSessione - confermatiPerProdotto - tempPerProdotto;

                    if (restante <= 0) {
                        messagingTemplate.convertAndSend(
                            "/topic/tavolo/" + tavoloId,
                            new TavoloMessage(Constants.MSG_ERROR, "Limite per questo prodotto raggiunto (max " + maxPerSessione + " - 1 a persona per sessione)")
                        );
                        return;
                    }

                    if (delta > restante) {
                        messagingTemplate.convertAndSend(
                            "/topic/tavolo/" + tavoloId,
                            new TavoloMessage(Constants.MSG_ERROR, "Puoi aggiungere al massimo " + restante + " di questo prodotto per la sessione")
                        );
                        return;
                    }
                }

                // se tutti i controlli passano, aggiorno temporaneo
                if (delta > 0) tavoloTempService.addItem(tavoloId, prodottoId, delta);
                else tavoloTempService.removeItem(tavoloId, prodottoId, -delta);

                Map<Long, Integer> ordineAggiornato = tavoloTempService.getOrdineTemp(tavoloId);
                messagingTemplate.convertAndSend(
                    "/topic/tavolo/" + tavoloId,
                    new TavoloMessage(Constants.MSG_UPDATE_TEMP, objectMapper.writeValueAsString(ordineAggiornato))
                );
            }
            case Constants.MSG_ORDER_SENT -> {
                final Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);

                // controlli e salvataggio: per prodotti limited check again (solo per AYCE) e salva solo quantità consentita
                StringBuilder warning = new StringBuilder();
                boolean savedAnyNormalProduct = false;
                boolean savedAny = false;

                for (Map.Entry<Long, Integer> e : ordineTemp.entrySet()) {
                    Long prodottoId = e.getKey();
                    Integer quantita = e.getValue();
                    if (quantita == null || quantita <= 0) continue;

                    Prodotto prodotto = prodottoService.findById(prodottoId);
                    if (prodotto == null) continue;

                    if (Boolean.TRUE.equals(sessione.getIsAyce())
                            && Boolean.TRUE.equals(prodotto.getIsLimitedPartecipanti())) {
                        // quantità già confermata nel DB per questa sessione
                        List<Ordine> ordiniConfermati = ordineService.findBySessione(sessione);
                        int confermatiPerProdotto = ordiniConfermati.stream()
                                .filter(o -> o.getProdotto() != null && prodottoId.equals(o.getProdotto().getId()))
                                .mapToInt(Ordine::getQuantita)
                                .sum();

                        int maxPerSessione = sessione.getNumeroPartecipanti();
                        int restante = maxPerSessione - confermatiPerProdotto;

                        if (restante <= 0) {
                            // niente da salvare per questo prodotto
                            warning.append("Nessuna quantità disponibile per ").append(prodotto.getNome()).append(". ");
                            continue;
                        }

                        if (quantita > restante) {
                            // salva solo la parte consentita e avvisa
                            warning.append("Quantità per ").append(prodotto.getNome()).append(" ridotta a ").append(restante).append(". ");
                            quantita = restante;
                        }
                    }

                    // salva l'ordine (quantita già eventualmente ridotta)
                    if (quantita != null && quantita > 0) {
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
                        savedAny = true;

                        // se il prodotto è "normale" (<100) segnalo che dobbiamo aggiornare il lastOrder (AYCE only)
                        if (prodotto.getCategoria() != null && prodotto.getCategoria().getId() < 100) {
                            savedAnyNormalProduct = true;
                        }
                    }
                }

                // pulisco ordine temporaneo
                tavoloTempService.clearOrdine(tavoloId);

                // Aggiorno timestamp ultimo ordine SOLO SE:
                // - sessione è AYCE
                // - e sono stati salvati prodotti "normali" (categoria < 100)
                if (Boolean.TRUE.equals(sessione.getIsAyce()) && savedAnyNormalProduct) {
                    sessione.setUltimoOrdineInviato(LocalDateTime.now());
                    sessioneService.save(sessione);

                    // notifico il tavolo: ORDER_SENT (per attivare cooldown)
                    messagingTemplate.convertAndSend(
                        "/topic/tavolo/" + tavoloId,
                        new TavoloMessage(Constants.MSG_ORDER_SENT, "")
                    );
                } else {
                    // non aggiorno ultimoOrdine (es. solo bevande) -> non mando ORDER_SENT -> no cooldown
                }

                // sempre notifico UPDATE_TEMP (ordine vuoto)
                Map<Long, Integer> ordineAggiornato = tavoloTempService.getOrdineTemp(tavoloId);
                messagingTemplate.convertAndSend(
                    "/topic/tavolo/" + tavoloId,
                    new TavoloMessage(Constants.MSG_UPDATE_TEMP, objectMapper.writeValueAsString(ordineAggiornato))
                );

                if (warning.length() > 0) {
                    messagingTemplate.convertAndSend(
                        "/topic/tavolo/" + tavoloId,
                        new TavoloMessage(Constants.MSG_ERROR, warning.toString())
                    );
                }
            }
            case "GET_STATUS" -> {
                Map<Long, Integer> ordineTemp = tavoloTempService.getOrdineTemp(tavoloId);
                messagingTemplate.convertAndSend(
                    "/topic/tavolo/" + tavoloId,
                    new TavoloMessage(Constants.MSG_UPDATE_TEMP,
                        objectMapper.writeValueAsString(Map.of(
                            "ordine", ordineTemp,
                            "lastOrder", sessione.getUltimoOrdineInviato()
                        ))
                    )
                );
                break;
            }
            default -> throw new IllegalArgumentException("Tipo messaggio non gestito: " + msg.getTipoEvento());
        }
    }
}
