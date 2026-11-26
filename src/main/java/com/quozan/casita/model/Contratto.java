package com.quozan.casita.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contratti")
@Getter
@Setter
@NoArgsConstructor
public class Contratto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "immobile_id", nullable = false)
    private Immobile immobile;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inquilino_id", nullable = false)
    private Inquilino inquilino;
    @Column(nullable = false)
    private LocalDate dataInizio;
    @Column(nullable = false)
    private LocalDate dataFine;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal canoneMensile;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deposito;
    @Column(length = 1000)
    private String note;
    private LocalDateTime deletedAt;

}
