package com.db.ayce.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UltimoOrdineDto {
	private String nomeProdotto;
	private Long numOrdinati;
}
