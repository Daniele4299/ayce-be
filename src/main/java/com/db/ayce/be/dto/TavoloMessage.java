package com.db.ayce.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TavoloMessage {
    private Long sessioneId;
    private Integer tavoloId;
    private String tipoEvento; // ADD_ITEM_TEMP, REMOVE_ITEM_TEMP, UPDATE_TEMP, ORDER_SENT
    private String payload; // JSON string (TavoloMessagePayload o stato completo)
}