package com.ra2.users.users.service;

import java.io.BufferedReader;

import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User addUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setDataCreated(now);
        user.setDataUpdated(now);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId);
    }

    public boolean update(Long userId, User userDetails) {
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            // Actualizar campos
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            // Actualizar otros campos según sea necesario
            
            // Actualizar fecha de modificación
            existingUser.setDataUpdated(LocalDateTime.now());
            
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    public boolean updateName(Long userId, String name, LocalDateTime updateTime) {
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            existingUser.setName(name);
            existingUser.setDataUpdated(updateTime);
            userRepository.save(existingUser);
            return true;
        }
        return false;
    }

    public boolean delete(Long userId) {
        return userRepository.delete(userId);
        
    }
    public String uploadImage(Long userId, MultipartFile image) {
        // Consultar si existeix l'usuari amb la id
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            throw new RuntimeException("Usuari amb ID " + userId + " no trobat");
        }
        try {
            // Crear la carpeta dins del projecte 'src/main/resources/public/images'
            Path imagesDir = Paths.get("src/main/resources/public/images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            
            // Guardar la imatge amb un nom que identifiqui la imatge
            String imageName = "user_" + userId + "_profile.jpg";
            Path destinationFile = imagesDir.resolve(imageName);
            
            // Guardar la imatge amb NIO2
            Files.copy(image.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            // Guardar la ruta de la imatge en el camp image_path de la taula usuaris
            String imagePath = "/images/" + imageName;
            boolean updated = userRepository.updateImagePath(userId, imagePath, LocalDateTime.now());
            
            if (!updated) {
                throw new RuntimeException("Error en guardar la ruta a la base de dades");
            }
            
            // Retornar la URL de la imatge
            return imagePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Error en guardar la imatge: " + e.getMessage());
        }
    }
    // Crea 10 usuaris fent servir un csv
    public String insertAllStudentsByCsv(MultipartFile csvFile){
        int numRegInsert = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))){
            String linia = br.readLine();
            int numeroLinia= 0;
            while (linia != null){
                numRegInsert++;
                //La primera linea identifica el orden 
                if (numeroLinia == 0) {
                    System.out.println("CAPCELERA"+ linia);
                    
                }
                //separar por comas
                else{
                    String[] camps = linia.split(",");
                    User user = new User();
                    user.setName(camps[0]);
                    user.setDescription(camps[1]);
                    user.setEmail(camps[2]);
                    user.setPassword(camps[3]);
                    user.setImage(camps[4]);
                    addUser(user);
                }
                linia = br.readLine();
            }
            Path csvDir = Paths.get("src/main/resources/private/csv_processed");
            if (!Files.exists(csvDir)) {
                Files.createDirectories(csvDir);
            }
            
            // Guardar el CSV con un nombre que identifique el archivo
            String csvName = "students_import_" + System.currentTimeMillis() + ".csv";
            Path destinationFile = csvDir.resolve(csvName);
            
            // Guardar el CSV con NIO2
            Files.copy(csvFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
             
        }catch (Exception e){

        }
        return "creados" +numRegInsert + "usuarios";
    }

   
}