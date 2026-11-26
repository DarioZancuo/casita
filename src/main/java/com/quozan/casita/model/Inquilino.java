package com.quozan.casita.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inquilini")
@Getter
@Setter
@NoArgsConstructor
public class Inquilino {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String nome;
    @Column(nullable = false, length = 80)
    private String cognome;
    @Column(nullable = false, length = 20)
    private String codiceFiscale;
    @Column(length = 160)
    private String email;
    @Column(length = 30)
    private String telefono;
    private LocalDateTime deletedAt;

}
