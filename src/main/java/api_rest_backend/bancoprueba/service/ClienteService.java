package api_rest_backend.bancoprueba.service;

import api_rest_backend.bancoprueba.model.Cliente;
import api_rest_backend.bancoprueba.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    //@Autowired
    //private MovimientoRepository movimientoRepository;

    public Cliente register(Cliente cliente){return clienteRepository.save(cliente);}

    public List<Cliente> getAllClientes(){return clienteRepository.findAll();}

    public void deleteClienteById(long id) {
        clienteRepository.deleteById((int) id);
    }

    public Optional<Cliente> updateClient(Long id, Cliente clientDetails) {
        return clienteRepository.findById(id).map(client -> {
            client.setNombre(clientDetails.getNombre());
            client.setApellido(clientDetails.getApellido());
            client.setDni(clientDetails.getDni());
            client.setCuenta(clientDetails.getCuenta());
            client.setSaldo(clientDetails.getSaldo());
            client.setContrasena(clientDetails.getContrasena());
            return clienteRepository.save(client);
        });
    }

    public Optional<Cliente> login(String dni, String contrasena){
        return clienteRepository.findByDni(dni).filter(cliente -> cliente.getContrasena().equals(contrasena));
    }

    public Optional<Cliente> getClienteByDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public Optional<Cliente> getClienteByCuenta(long cuenta) {
        return clienteRepository.findByCuenta(cuenta);
    }

    public Optional<Cliente> getClientById(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente updateSaldo(String dni, long amount) {
        Cliente cliente = clienteRepository.findByDni(dni).orElseThrow();
        cliente.setSaldo(cliente.getSaldo() + amount);
        return clienteRepository.save(cliente);
    }

    public Cliente retirarSaldo(String dni, long amount) {
        Cliente cliente = clienteRepository.findByDni(dni).orElseThrow();
        cliente.setSaldo(cliente.getSaldo() - amount);
        return clienteRepository.save(cliente);
    }

    public Cliente transferirSaldo(long fromCuenta, long toCuenta, long amount) {
        Cliente fromCliente = clienteRepository.findByCuenta(fromCuenta).orElseThrow();
        Cliente toCliente = clienteRepository.findByCuenta(toCuenta).orElseThrow();

        fromCliente.setSaldo(fromCliente.getSaldo() - amount);
        toCliente.setSaldo(toCliente.getSaldo() + amount);

        clienteRepository.save(toCliente);
        return clienteRepository.save(fromCliente);
    }

    public Cliente transferirSaldoInterbancario(long fromCuenta, long toCuenta, long amount, String bancoDestinoUrl) {
        Cliente fromCliente = clienteRepository.findByCuenta(fromCuenta)
                .orElseThrow(() -> new RuntimeException("Cuenta de origen no encontrada"));

        // Verificar que el cliente tiene suficiente saldo
        if (fromCliente.getSaldo() < amount) {
            throw new RuntimeException("Saldo insuficiente para la transferencia");
        }

        // Reducir el saldo del cliente de origen
        fromCliente.setSaldo(fromCliente.getSaldo() - amount);
        clienteRepository.save(fromCliente);

        // Realizar la solicitud al banco de destino
        RestTemplate restTemplate = new RestTemplate();
        String url = bancoDestinoUrl + "/api/clientes/recibir-transferencia";

        Map<String, Object> request = new HashMap<>();
        request.put("toCuenta", toCuenta);
        request.put("amount", amount);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return fromCliente;
        } else {
            // Revertir la reducción de saldo si la transferencia falló
            fromCliente.setSaldo(fromCliente.getSaldo() + amount);
            clienteRepository.save(fromCliente);
            throw new RuntimeException("Error en la transferencia interbancaria: " + response.getBody());
        }
    }


    /*public List<Movimiento> getMovimientos(long cuenta) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCuenta(cuenta);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            return movimientoRepository.findByCliente(cliente);
        }
        return null;
    }*/
}