package api_rest_backend.bancoprueba.controller;

import api_rest_backend.bancoprueba.model.Cliente;
import api_rest_backend.bancoprueba.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/bancoprueba/api/clientes")
@CrossOrigin("https://bancoprueba-front.vercel.app/")
//@CrossOrigin(origins = "https://app-front-banco-a.vercel.app")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    //@Autowired
    //private MovimientoService movimientoService;

    @PostMapping("/register")
    public ResponseEntity<Cliente> register(@RequestBody Cliente cliente){
        System.out.println("Cliente recibido: " + cliente.getDni() + "/" + cliente.getContrasena());
        return ResponseEntity.ok(clienteService.register(cliente));
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClientes(){ return ResponseEntity.ok((clienteService.getAllClientes()));}

    @PutMapping("/editar/{id}")
    public ResponseEntity<Cliente> editarCliente(@PathVariable Long id, @RequestBody Cliente nuevoCliente) {
        Optional<Cliente> updatedCliente = clienteService.updateClient(id, nuevoCliente);
        return updatedCliente.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        Optional<Cliente> client = clienteService.getClientById(id);
        System.out.println("id: " + id);
        if (client.isPresent()) {
            clienteService.deleteClienteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Cliente cliente){
        Optional<Cliente> loggedInCliente = clienteService.login(cliente.getDni(), cliente.getContrasena());
        System.out.println(cliente.getDni() + "/" + cliente.getContrasena());
        if (loggedInCliente.isPresent()){
            return ResponseEntity.ok("Acceso Correcto");
        } else {
            return ResponseEntity.status(401).body("DNI o Contraseña incorrectos");
        }
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Object> getClienteByDni(@PathVariable String dni) {
        Optional<Cliente> cliente = clienteService.getClienteByDni(dni);
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente con DNI " + dni + " no encontrado.");
        }
    }

    @GetMapping("/cuenta/{cuenta}")
    public ResponseEntity<Object> getClienteByCuenta(@PathVariable long cuenta) {
        Optional<Cliente> cliente = clienteService.getClienteByCuenta(cuenta); // Asegúrate de tener este método en el servicio
        if (cliente.isPresent()) {
            return ResponseEntity.ok(cliente.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente con cuenta " + cuenta + " no encontrado.");
        }
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Cliente> getClientById(@PathVariable Long id) {
        Optional<Cliente> client = clienteService.getClientById(id);
        return client.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PutMapping("/update-saldo/{dni}")
    public ResponseEntity<Cliente> updateSaldo(@PathVariable String dni, @RequestParam long amount) {
        Cliente updatedCliente = clienteService.updateSaldo(dni, amount);
        return ResponseEntity.ok(updatedCliente);
    }

    @PutMapping("/retirar-saldo/{dni}")
    public ResponseEntity<Cliente> retirarSaldo(@PathVariable String dni, @RequestParam long amount) {
        Cliente updatedCliente = clienteService.retirarSaldo(dni, amount);
        return ResponseEntity.ok(updatedCliente);
    }

    @PutMapping("/transferir-saldo/{fromCuenta}/{toCuenta}")
    public ResponseEntity<Cliente> transferirSaldo(@PathVariable long fromCuenta, @PathVariable long toCuenta, @RequestParam long amount) {
        Cliente updatedCliente = clienteService.transferirSaldo(fromCuenta, toCuenta, amount);
        return ResponseEntity.ok(updatedCliente);
    }

    @PutMapping("/transferir-interbancaria")
    public ResponseEntity<String> transferirInterbancaria(
            @RequestParam long fromCuenta,
            @RequestParam long toCuenta,
            @RequestParam long amount,
            @RequestParam String bancoDestinoUrl) {

        Optional<Cliente> fromClienteOpt = clienteService.getClienteByCuenta(fromCuenta);
        System.out.println(bancoDestinoUrl);

        if (fromClienteOpt.isPresent()) {
            Cliente fromCliente = fromClienteOpt.get();

            if (fromCliente.getSaldo() >= amount) {
                fromCliente.setSaldo(fromCliente.getSaldo() - amount);
                clienteService.save(fromCliente);

                // Llamar a la API del banco destino para completar la transferencia
                RestTemplate restTemplate = new RestTemplate();
                String transferirUrl = bancoDestinoUrl + "/bancob/api/clientes/recibir-interbancaria";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("toCuenta", toCuenta);
                requestBody.put("amount", amount);

                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<String> response = restTemplate.exchange(
                        transferirUrl,
                        HttpMethod.PUT,
                        requestEntity,
                        String.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    return ResponseEntity.ok("Transferencia interbancaria exitosa.");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error al completar la transferencia en el banco destino.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fondos insuficientes.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuenta de origen no encontrada.");
        }
    }

    @PutMapping("/recibir-interbancaria")
    public ResponseEntity<String> recibirInterbancaria(@RequestBody Map<String, Object> request) {
        long toCuenta = ((Number) request.get("toCuenta")).longValue();
        long amount = ((Number) request.get("amount")).longValue();

        Optional<Cliente> toClienteOpt = clienteService.getClienteByCuenta(toCuenta);

        if (toClienteOpt.isPresent()) {
            Cliente toCliente = toClienteOpt.get();
            toCliente.setSaldo(toCliente.getSaldo() + amount);
            clienteService.save(toCliente);
            return ResponseEntity.ok("Transferencia recibida con éxito.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cuenta de destino no encontrada.");
        }
    }

    /*@GetMapping("/{cuenta}/movimientos")
    public ResponseEntity<List<Movimiento>> getMovimientos(@PathVariable long cuenta) {
        List<Movimiento> movimientos = clienteService.getMovimientos(cuenta);
        return ResponseEntity.ok(movimientos);
    }*/
}