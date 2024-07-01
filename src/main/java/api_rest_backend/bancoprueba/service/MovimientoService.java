package api_rest_backend.bancoprueba.service;

import api_rest_backend.bancoprueba.model.Cliente;
import api_rest_backend.bancoprueba.model.Movimiento;
import api_rest_backend.bancoprueba.repository.MovimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimientoService {

    @Autowired
    private MovimientoRepository movimientoRepository;

    public List<Movimiento> getMovimientosByCliente(Cliente cliente) {
        return movimientoRepository.findByCliente(cliente);
    }
}
