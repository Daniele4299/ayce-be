package com.db.ayce.be.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sessione")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sessione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_tavolo")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Tavolo tavolo;

    private LocalDateTime orarioInizio;

    private Integer numeroPartecipanti;
    
    @Column(name = "is_ayce", nullable = false)
    private Boolean isAyce = false;

    private String stato;
    
    @Column(name = "ultimo_ordine_inviato", nullable = true)
    private LocalDateTime ultimoOrdineInviato;
}
