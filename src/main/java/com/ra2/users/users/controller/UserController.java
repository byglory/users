package com.ra2.users.users.controller;

import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;
import com.ra2.users.users.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired  
    UserService userService;
    
    
    
    
    // Crea un nou usuari
    @PostMapping("/users")
    public ResponseEntity<String> postUser(@RequestBody User usuari) {
        User usuarioGuardado = userService.addUser(usuari);
        return ResponseEntity.ok(usuarioGuardado.getName() + " creat correctament");
    }
    
    // Obté tots els usuaris
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        if (users == null || users.isEmpty()) {
           return ResponseEntity.ok(null); // Retorna null si no hi ha usuaris
        }
        return ResponseEntity.ok(users); // Retorna llista d'usuaris
    }
    
    // Obté un usuari per ID
    @GetMapping("/users/{user_id}")
    public ResponseEntity<User> getUserById(@PathVariable("user_id") Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(user); // Retorna l'usuari o null
    }
    
    // Actualitza completament un usuari
    @PutMapping("/users/{user_id}")
    public ResponseEntity<String> updateUser(@PathVariable("user_id") Long userId, @RequestBody User user) {
        user.setDataUpdated(LocalDateTime.now()); // Actualitza data modificació
    
        boolean updated = userService.update(userId, user);
    
        if (updated) {
            return ResponseEntity.ok("Usuari amb ID " + userId + " actualitzat correctament");
        } else {
            return ResponseEntity.ok("Usuari amb ID " + userId + " no existeix");
        }
    }
    
    // Actualitza només el nom d'un usuari
    @PatchMapping("/users/{user_id}/name")
    public ResponseEntity<User> updateUserName(@PathVariable("user_id") Long userId, @RequestParam String name) {
        User existingUser = userService.findById(userId);
        if (existingUser == null) {
            return ResponseEntity.ok(null); // Usuari no trobat
        }
        LocalDateTime now = LocalDateTime.now();
        boolean updated = userService.updateName(userId, name, now);
        if (updated) {
            User updatedUser = userService.findById(userId); // Obtenir usuari actualitzat
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.ok(null); // Error en l'actualització
    }
    
    // Elimina un usuari
    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<String> deleteUser(@PathVariable("user_id") Long userId) {
        boolean deleted = userService.delete(userId);
        if (deleted) {
            return ResponseEntity.ok("Usuari amb ID " + userId + " eliminat correctament");
        } else {
            return ResponseEntity.ok("Usuari amb ID " + userId + " no existeix");
        }
    }
    // Afegeix una imatge
    @PostMapping("/users/{user_id}/image")
    public String addImageProfile(@PathVariable Long user_id, @RequestParam MultipartFile imagFile) {
        System.out.println(imagFile);
        return "ok";
    }
    
}