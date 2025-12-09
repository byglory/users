package com.ra2.users.users.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ra2.users.users.logging.CustomLogging; // Importem la nostra classe de logging
import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    CustomLogging logging; // Injectem la classe de logging
    
    private final String CLASS_NAME = "UserService"; // Constant per al nom de la classe als logs
    
    // --- CREATE ---
    public User addUser(User user) {
        logging.info(CLASS_NAME, "addUser", "Creant un usuari: " + user.getName());
        
        try {
            LocalDateTime now = LocalDateTime.now();
            user.setDataCreated(now);
            user.setDataUpdated(now);
            User savedUser = userRepository.save(user);
            
            logging.info(CLASS_NAME, "addUser", "Usuari creat correctament");
            return savedUser;
        } catch (Exception e) {
            logging.error(CLASS_NAME, "addUser", "L'usuari amb nom: " + user.getName() + ", no s'ha creat correctament. Missatge d'error: " + e.getMessage());
            throw e;
        }
    }

    // --- READ ---
    public List<User> getAllUsers() {
        logging.info(CLASS_NAME, "getAllUsers", "Consultant tots els usuaris");
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        logging.info(CLASS_NAME, "findById", "Consultant l'usuari amb id: " + userId);
        
        User user = userRepository.findById(userId);
        
        if (user != null) {
            return user;
        } else {
            logging.error(CLASS_NAME, "findById", "L'usuari amb id: " + userId + " no existeix");
            return null;
        }
    }

    // --- UPDATE ---
    public boolean update(Long userId, User userDetails) {
        logging.info(CLASS_NAME, "update", "Modificant l'usuari amb id: " + userId);
        
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            // Actualitzar camps
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            // Actualitzar altres camps segons sigui necessari
            existingUser.setDataUpdated(LocalDateTime.now());       
            
            userRepository.save(existingUser); // Nota: Si el save fa insert, hauries d'usar un mètode update específic al repositori, però aquí seguim la teva lògica.
            
            logging.info(CLASS_NAME, "update", "Usuari modificat correctament");
            return true;
        }
        
        logging.error(CLASS_NAME, "update", "L'usuari amb id: " + userId + " no existeix");
        return false;
    }

    public boolean updateName(Long userId, String name, LocalDateTime updateTime) {
        logging.info(CLASS_NAME, "updateName", "Modificant el nom de l'usuari amb id: " + userId);
        
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            existingUser.setName(name);
            existingUser.setDataUpdated(updateTime);
            userRepository.save(existingUser); // Mateixa nota sobre l'update vs save
            
            logging.info(CLASS_NAME, "updateName", "Nom modificat correctament");
            return true;
        }
        
        logging.error(CLASS_NAME, "updateName", "L'usuari amb id: " + userId + " no existeix");
        return false;
    }

    // --- DELETE ---
    public boolean delete(Long userId) {
        logging.info(CLASS_NAME, "delete", "Esborrant l'usuari amb id: " + userId);
        
        // Comprovem primer si existeix per fer el log d'error si cal
        if (userRepository.findById(userId) == null) {
            logging.error(CLASS_NAME, "delete", "L'usuari amb id: " + userId + " no existeix");
            return false;
        }

        boolean reg = userRepository.delete(userId);
        if (reg) {
            logging.info(CLASS_NAME, "delete", "L'usuari amb id: " + userId + " s'ha esborrat correctament");
        }
        return reg;
    }
   
    // --- UPLOAD IMAGE ---
    public String uploadImage(Long userId, MultipartFile image) {
        logging.info(CLASS_NAME, "uploadImage", "Afegint la imatge " + image.getOriginalFilename() + " per a l'usuari amb id: " + userId);
        
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            logging.error(CLASS_NAME, "uploadImage", "L'usuari amb id " + userId + " no existeix");
            throw new RuntimeException("Usuari amb ID " + userId + " no trobat");
        }
        
        try {
            Path imagesDir = Paths.get("src/main/resources/public/images");
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }
            
            String imageName = "user_" + userId + "_profile.jpg";
            Path destinationFile = imagesDir.resolve(imageName);
            
            Files.copy(image.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            String imagePath = "/images/" + imageName;
            boolean updated = userRepository.updateImagePath(userId, imagePath, LocalDateTime.now());
            
            if (!updated) {
                throw new RuntimeException("Error en guardar la ruta a la base de dades");
            }
            
            logging.info(CLASS_NAME, "uploadImage", "La imatge s'ha guardat correctament. El path és: " + destinationFile.toString());
            return imagePath;
            
        } catch (IOException e) {
            logging.error(CLASS_NAME, "uploadImage", "Error guardant la imatge: " + e.getMessage());
            throw new RuntimeException("Error en guardar la imatge: " + e.getMessage());
        }
    }

    // --- CSV IMPORT ---
    public String insertAllStudentsByCsv(MultipartFile csvFile) {
        logging.info(CLASS_NAME, "insertAllStudentsByCsv", "Carregant la informació del fitxer " + csvFile.getOriginalFilename());

        int numRegInsert = 0;
        int numErrors = 0;
        int numeroLinia = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String linia;
            
            while ((linia = br.readLine()) != null) {
                if (linia.trim().isEmpty()) continue;
                
                if (numeroLinia == 0) {
                    if (!linia.equals("name,description,email,password")) {
                        return "Format de CSV incorrecte. Capçalera esperada: name,description,email,password";
                    }
                    numeroLinia++;
                    continue;
                }
                
                String[] camps = linia.split(",");
                if (camps.length != 4) {
                    System.err.println("Línia ignorada - camps insuficients: " + linia);
                    numErrors++;
                    continue;
                }
                
                try {
                    User user = new User();
                    user.setName(camps[0].trim());
                    user.setDescription(camps[1].trim());
                    user.setEmail(camps[2].trim());
                    user.setPassword(camps[3].trim());
                    
                    // Fem el save manualment per evitar duplicar logs si cridéssim addUser()
                    LocalDateTime now = LocalDateTime.now();
                    user.setDataCreated(now);
                    user.setDataUpdated(now);
                    userRepository.save(user);
                    
                    numRegInsert++;
                    
                } catch (Exception e) {
                    numErrors++;
                    logging.error(CLASS_NAME, "insertAllStudentsByCsv", "Error en la línia " + numeroLinia + ". Missatge: " + e.getMessage());
                }
                numeroLinia++;
            }
            
            // Guardar còpia del CSV (opcional)
            Path csvDir = Paths.get("src/main/resources/private/csv_processed");
            if (!Files.exists(csvDir)) Files.createDirectories(csvDir);
            
            String csvName = "students_import_" + System.currentTimeMillis() + ".csv";
            Path destinationFile = csvDir.resolve(csvName);
            Files.copy(csvFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (Exception e) {
            logging.error(CLASS_NAME, "insertAllStudentsByCsv", "Error general CSV: " + e.getMessage());
            e.printStackTrace();
            return "Error processant CSV: " + e.getMessage();
        }
        
        logging.info(CLASS_NAME, "insertAllStudentsByCsv", "S'han guardat correctament " + numRegInsert + " registres i han donat error " + numErrors + " registres");
        return "Creats " + numRegInsert + " usuaris correctament";
    }

    // --- JSON IMPORT ---
    public String insertAllUsersByJson(MultipartFile jsonFile) {
        logging.info(CLASS_NAME, "insertAllUsersByJson", "Carregant la informació del fitxer " + jsonFile.getOriginalFilename());

        int numRegInsert = 0;
        int numErrors = 0;
        
        try {
            JsonNode arrel = mapper.readTree(jsonFile.getInputStream());
            JsonNode dataNode = arrel.path("data");
            
            String control = dataNode.path("control").asText();
            int count = dataNode.path("count").asInt();
            JsonNode usersNode = dataNode.path("users");
            
            if (!"OK".equals(control)) return "Error: El control no és 'OK'";
            if (count != usersNode.size()) return "Error: El count no coincideix";
            
            for (JsonNode userNode : usersNode) {
                try {
                    User user = new User();
                    user.setName(userNode.path("name").asText());
                    user.setDescription(userNode.path("description").asText());
                    user.setEmail(userNode.path("email").asText());
                    user.setPassword(userNode.path("password").asText());
                    
                    LocalDateTime now = LocalDateTime.now();
                    user.setDataCreated(now);
                    user.setDataUpdated(now);
                    userRepository.save(user);
                    
                    numRegInsert++;
                } catch (Exception e) {
                    numErrors++;
                    logging.error(CLASS_NAME, "insertAllUsersByJson", "Error amb usuari JSON: " + e.getMessage());
                }
            }
            
            // Guardar còpia del JSON (opcional)
            Path jsonDir = Paths.get("src/main/resources/private/json_processed");
            if (!Files.exists(jsonDir)) Files.createDirectories(jsonDir);
            
            String jsonName = "users_import_" + System.currentTimeMillis() + ".json";
            Path destinationFile = jsonDir.resolve(jsonName);
            Files.copy(jsonFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                
        } catch (Exception e) {
            logging.error(CLASS_NAME, "insertAllUsersByJson", "Error general JSON: " + e.getMessage());
            e.printStackTrace();
            return "Error processant JSON: " + e.getMessage();
        }
        
        logging.info(CLASS_NAME, "insertAllUsersByJson", "S'han guardat correctament " + numRegInsert + " registres i han donat error " + numErrors + " registres");
        return "Creats " + numRegInsert + " usuaris correctament";
    }
}