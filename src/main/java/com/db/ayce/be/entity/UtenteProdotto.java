package com.db.ayce.be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utente_prodotto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UtenteProdotto {

    @EmbeddedId
    private UtenteProdottoId id;

    @Column(name = "riceve_comanda", nullable = false)
    private boolean riceveComanda = true;
}
