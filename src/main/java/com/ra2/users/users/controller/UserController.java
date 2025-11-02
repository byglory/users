package com.ra2.users.users.controller;

import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private final UserRepository userRepository;
    
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // Crea un nou usuari
    @PostMapping("/users")
    public ResponseEntity<String> postUser(@RequestBody User usuari) {
        LocalDateTime now = LocalDateTime.now();
        usuari.setDataCreated(now); // Data de creació
        usuari.setDataUpdated(now); // Data d'actualització
        
        User usuarioGuardado = userRepository.save(usuari);
        
        return ResponseEntity.ok(usuarioGuardado.getName() + " creat correctament");
    }
    
    // Obté tots els usuaris
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users == null || users.isEmpty()) {
           return ResponseEntity.ok(null); // Retorna null si no hi ha usuaris
        }
        return ResponseEntity.ok(users); // Retorna llista d'usuaris
    }
    
    // Obté un usuari per ID
    @GetMapping("/users/{user_id}")
    public ResponseEntity<User> getUserById(@PathVariable("user_id") Long userId) {
        User user = userRepository.findById(userId);
        return ResponseEntity.ok(user); // Retorna l'usuari o null
    }
    
    // Actualitza completament un usuari
    @PutMapping("/users/{user_id}")
    public ResponseEntity<String> updateUser(@PathVariable("user_id") Long userId, @RequestBody User user) {
        user.setDataUpdated(LocalDateTime.now()); // Actualitza data modificació
    
        boolean updated = userRepository.update(userId, user);
    
        if (updated) {
            return ResponseEntity.ok("Usuari amb ID " + userId + " actualitzat correctament");
        } else {
            return ResponseEntity.ok("Usuari amb ID " + userId + " no existeix");
        }
    }
    
    // Actualitza només el nom d'un usuari
    @PatchMapping("/users/{user_id}/name")
    public ResponseEntity<User> updateUserName(@PathVariable("user_id") Long userId, @RequestParam String name) {
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            return ResponseEntity.ok(null); // Usuari no trobat
        }
        LocalDateTime now = LocalDateTime.now();
        boolean updated = userRepository.updateName(userId, name, now);
        if (updated) {
            User updatedUser = userRepository.findById(userId); // Obtenir usuari actualitzat
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.ok(null); // Error en l'actualització
    }
    
    // Elimina un usuari
    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<String> deleteUser(@PathVariable("user_id") Long userId) {
        boolean deleted = userRepository.delete(userId);
        if (deleted) {
            return ResponseEntity.ok("Usuari amb ID " + userId + " eliminat correctament");
        } else {
            return ResponseEntity.ok("Usuari amb ID " + userId + " no existeix");
        }
    }
}