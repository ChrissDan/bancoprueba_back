package api_rest_backend.bancoprueba.repository;

import api_rest_backend.bancoprueba.model.Cliente;
import api_rest_backend.bancoprueba.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByCliente(Cliente cliente);
}
