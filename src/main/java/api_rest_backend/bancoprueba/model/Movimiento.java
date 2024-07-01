package api_rest_backend.bancoprueba.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    private String tipo; // "deposito", "retiro", "transferencia"
    private long monto;
    private LocalDateTime fecha;
    private String detalle;

    public Movimiento() {}

    public Movimiento(Cliente cliente, long monto, String tipo) {
        this.cliente = cliente;
        this.monto = monto;
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
    }
}
