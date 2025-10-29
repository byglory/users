package com.ra2.users.users.controller;

import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class UserController {
    
    private final UserRepository userRepository;
    
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @PostMapping("/users")
    public ResponseEntity<String> creaUsuari(@RequestBody User usuari) {
        LocalDateTime now = LocalDateTime.now();
        usuari.setDataCreated(now);
        usuari.setDataUpdated(now);
        
        User usuarioGuardado = userRepository.save(usuari);
        
        return ResponseEntity.ok(usuarioGuardado.getName() + " creat correctament");
    }
}