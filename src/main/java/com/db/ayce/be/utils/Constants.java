package com.db.ayce.be.utils;

import java.util.Map;

public final class Constants {
    private Constants() {}

    // Mappa dei ruoli dal DB -> nome ruolo
    public static final Map<Integer, String> ROLE_MAP = Map.of(
        0, "ADMIN",
        1, "DIPEN"
    );
    
    // Costanti per ruoli legati al DB
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DIPEN = "DIPEN";

    // Ruolo speciale per il client tavolo, non legato al DB
    public static final String ROLE_CLIENT = "CLIENT";

    /**
     * Restituisce il nome ruolo corrispondente al livello DB.
     * Se non esiste, lancia eccezione (fail fast)
     */
    public static String getRoleName(int livello) {
        String role = ROLE_MAP.get(livello);
        if (role == null) throw new IllegalArgumentException("Ruolo non valido per livello: " + livello);
        return role;
    }
    
    // Tipi messaggi WebSocket
    public static final String MSG_ADD_ITEM_TEMP = "ADD_ITEM_TEMP";
    public static final String MSG_REMOVE_ITEM_TEMP = "REMOVE_ITEM_TEMP";
    public static final String MSG_UPDATE_TEMP = "UPDATE_TEMP";
    public static final String MSG_ORDER_SENT = "ORDER_SENT";
    public static final String MSG_ERROR = "ERROR";

    // Limiti
    public static final int MAX_PORTATE_PER_PERSONA = 5;
    public static final int COOLDOWN_MINUTI_ORDINE = 15;

}
