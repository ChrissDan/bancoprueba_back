package api_rest_backend.bancoprueba.controller;

import api_rest_backend.bancoprueba.model.User;
import api_rest_backend.bancoprueba.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bancoprueba/api/users")
@CrossOrigin("https://bancoprueba-front.vercel.app/")
//@CrossOrigin(origins = "https://app-front-banco-a.vercel.app")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        System.out.println("Usuario recibido: " + user.getUsername() + "/" + user.getPassword());
        return ResponseEntity.ok(userService.register(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<User> editarUser(@PathVariable Long id, @RequestBody User newUser) {
        Optional<User> updatedUser = userService.updateUser(id, newUser);
        return updatedUser.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarUser(@PathVariable Long id) {
        Optional<User> optionalUser = userService.findUserById(id);
        if (optionalUser.isPresent()) {
            userService.deleteUserById(optionalUser.get().getId());
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        Optional<User> loggedInUser = userService.login(user.getUsername(), user.getPassword());
        System.out.println(user.getUsername() + "/" + user.getPassword());
        if(loggedInUser.isPresent()){
            return ResponseEntity.ok("Acceso Correcto");
        } else {
            return ResponseEntity.status(401).body("Usuario o contraseña incorrectos");
        }
    }

}
