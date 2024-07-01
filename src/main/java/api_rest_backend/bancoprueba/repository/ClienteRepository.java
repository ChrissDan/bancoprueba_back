package api_rest_backend.bancoprueba.repository;

import api_rest_backend.bancoprueba.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByDni(String dni);
    Optional<Cliente> findByCuenta(long cuenta);
    Optional<Cliente> findById(long id);
    //Optional<Cliente> deleteById(long id);
}
