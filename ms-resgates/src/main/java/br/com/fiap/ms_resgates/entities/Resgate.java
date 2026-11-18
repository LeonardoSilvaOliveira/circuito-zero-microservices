package br.com.fiap.ms_resgates.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reserva/retirada de uma oferta de excedente por uma instituição social
 * (ONG, cozinha solidária ou líder comunitário).
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "tb_resgate")
public class Resgate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID da oferta no ms-ofertas (cada MS tem seu próprio banco, sem FK entre eles). */
    @Column(nullable = false)
    private Long ofertaId;

    @Column(nullable = false, length = 120)
    private String instituicaoNome;

    @Column(nullable = false, length = 100)
    private String responsavelNome;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Column(nullable = false)
    private LocalDateTime dataReserva;

    private LocalDateTime dataRetirada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private StatusResgate status;
}
