package com.ra2.users.users.service;
import java.io.BufferedReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    
    @Autowired
    private ObjectMapper mapper;
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
    public String insertAllStudentsByCsv(MultipartFile csvFile) {
    int numRegInsert = 0;
    int numeroLinia = 0;
    
    try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
        String linia;
        
        while ((linia = br.readLine()) != null) {
            // Saltar línea vacías
            if (linia.trim().isEmpty()) {
                continue;
            }
            
            if (numeroLinia == 0) {
                // Validar cabecera
                if (!linia.equals("name,description,email,password")) {
                    return "Formato de CSV incorrecto. Cabecera esperada: name,description,email,password";
                }
                numeroLinia++;
                continue;
            }
            
            // Separar por comas
            String[] camps = linia.split(",");
            
            // Validar que tenga exactamente 4 campos
            if (camps.length != 4) {
                System.err.println("Línea ignorada - campos insuficientes: " + linia);
                continue;
            }
            
            try {
                User user = new User();
                user.setName(camps[0].trim());
                user.setDescription(camps[1].trim());
                user.setEmail(camps[2].trim());
                user.setPassword(camps[3].trim());
                // user.setImage(null); // O establecer un valor por defecto
                
                addUser(user);
                numRegInsert++;
                
            } catch (Exception e) {
                System.err.println("Error creando usuario desde línea: " + linia);
                e.printStackTrace();
            }
            
            numeroLinia++;
        }
        
        // Guardar el CSV procesado (opcional)
        Path csvDir = Paths.get("src/main/resources/private/csv_processed");
        if (!Files.exists(csvDir)) {
            Files.createDirectories(csvDir);
        }
        
        String csvName = "students_import_" + System.currentTimeMillis() + ".csv";
        Path destinationFile = csvDir.resolve(csvName);
        Files.copy(csvFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
        
    } catch (Exception e) {
        e.printStackTrace();
        return "Error procesando CSV: " + e.getMessage();
    }
    
    return "Creados " + numRegInsert + " usuarios correctamente";
}
        public String insertAllUsersByJson(MultipartFile jsonFile) {
        int numRegInsert = 0;
        
        try {
            // Llegir el fitxer JSON i parsejar-lo
            JsonNode arrel = mapper.readTree(jsonFile.getInputStream());
            
            // Accedir al node "data" del JSON
            JsonNode dataNode = arrel.path("data");
            
            // Obtenir els valors de control i count
            String control = dataNode.path("control").asText();
            int count = dataNode.path("count").asInt();
            
            // Accedir a l'array d'usuaris
            JsonNode usersNode = dataNode.path("users");
            
            // Validar que el control sigui "OK"
            if (!"OK".equals(control)) {
                return "Error: El control no és 'OK'";
            }
            
            // Validar que el count coincideixi amb el nombre d'usuaris
            if (count != usersNode.size()) {
                return "Error: El count (" + count + ") no coincideix amb el nombre d'usuaris (" + usersNode.size() + ")";
            }
            
            // Recórrer cada usuari de l'array
            for (JsonNode userNode : usersNode) {
                try {
                    // Obtenir les dades de cada usuari
                    String name = userNode.path("name").asText();
                    String description = userNode.path("description").asText();
                    String email = userNode.path("email").asText();
                    String password = userNode.path("password").asText();
                    
                    // Crear nou usuari
                    User user = new User();
                    user.setName(name);
                    user.setDescription(description);
                    user.setEmail(email);
                    user.setPassword(password);
                    
                    // Guardar usuari a la base de dades
                    addUser(user);
                    numRegInsert++;
                    
                } catch (Exception e) {
                    // Log d'error per usuari específic
                    System.err.println("Error creant usuari des de JSON: " + userNode);
                    e.printStackTrace();
                }
            }
            
            // Guardar el fitxer JSON a la carpeta de processats
            Path jsonDir = Paths.get("src/main/resources/private/json_processed");
            if (!Files.exists(jsonDir)) {
            Files.createDirectories(jsonDir);
            }
            
            // Generar nom únic per al fitxer
            String jsonName = "users_import_" + System.currentTimeMillis() + ".json";
            Path destinationFile = jsonDir.resolve(jsonName);
            
            // Copiar el fitxer al directori de processats
            Files.copy(jsonFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
                
            } catch (Exception e) {
                e.printStackTrace();
                return "Error processant JSON: " + e.getMessage();
            }
        
        return "Creats " + numRegInsert + " usuaris correctament";
    }
}