package com.quozan.casita.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "spese_condominiali")
@Getter
@Setter
@NoArgsConstructor
public class SpesaCondominiale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "immobile_id", nullable = false)
    private Immobile immobile;
    @Column(nullable = false, length = 200)
    private String descrizione;
    @Column(nullable = false, length = 50)
    private String categoria;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importo;
    @Column(nullable = false)
    private LocalDate dataSpesa;
    @Column(length = 1000)
    private String note;
    private LocalDateTime deletedAt;

}
