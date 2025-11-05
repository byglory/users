package com.ra2.users.users.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    

    public User addUser(User user){
        LocalDateTime now = LocalDateTime.now();
        user.setDataCreated(now); // Data de creació
        user.setDataUpdated(now); // Data d'actualització
        return userRepository.save(user);
    }
    public List<User> getAllUsers() {
        
        return userRepository.findAll();
    }
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    public boolean update(Long id, User user) {
        return userRepository.update(id, user);
    }


}
