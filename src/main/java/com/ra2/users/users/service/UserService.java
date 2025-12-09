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
import com.ra2.users.users.logging.UserLogging;
import com.ra2.users.users.model.User;
import com.ra2.users.users.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    UserLogging logging; // Injectem la nostra classe de logging
    
    private final String CLASS_NAME = "UserService"; // Constant per al nom de la classe als logs
    
    public User addUser(User user) {
        logging.info(CLASS_NAME, "createStudent", "Creant un estudiant"); // [cite: 135]

        try {
            LocalDateTime now = LocalDateTime.now();
            user.setDataCreated(now);
            user.setDataUpdated(now);
            User savedUser = userRepository.save(user);
            
            logging.info(CLASS_NAME, "createStudent", "Estudiant creat correctament"); // [cite: 137]
            return savedUser;
        } catch (Exception e) {
            // Log d'error en cas que falli (ex: índex únic duplicat)
            logging.error(CLASS_NAME, "createStudent", "L'estudiant amb nom: " + user.getName() + ", no s'ha creat correctament. Missatge d'error: " + e.getMessage()); // [cite: 144]
            throw e;
        }
    }

    public List<User> getAllUsers() {
        logging.info(CLASS_NAME, "getAllStudents", "Consultant tots els estudiants"); // [cite: 103]
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            logging.info(CLASS_NAME, "getStudentsById", "Consultant l'estudiant amb id: " + userId); // [cite: 107]
            return user;
        } else {
            logging.error(CLASS_NAME, "getStudentsById", "L'estudiant amb id: " + userId + " no existeix"); // [cite: 132]
            return null;
        }
    }

    public boolean update(Long userId, User userDetails) {
        logging.info(CLASS_NAME, "updateAllStudent", "Modificant l'estudiant amb id: " + userId); // [cite: 153]
        
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            // Actualitzar camps
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            // Actualitzar altres camps segons sigui necessari
            // Actualitzar data de modificació
            existingUser.setDataUpdated(LocalDateTime.now());       
            userRepository.save(existingUser);
            
            logging.info(CLASS_NAME, "updateAllStudent", "Estudiant modificat correctament"); // [cite: 155]
            return true;
        }
        
        logging.error(CLASS_NAME, "updateAllStudent", "L'estudiant amb id: " + userId + " no existeix"); // [cite: 162]
        return false;
    }

    public boolean updateName(Long userId, String name, LocalDateTime updateTime) {
        logging.info(CLASS_NAME, "updateStudent", "Modificant l'estudiant amb id: " + userId); // [cite: 171]
        
        User existingUser = userRepository.findById(userId);
        if (existingUser != null) {
            existingUser.setName(name);
            existingUser.setDataUpdated(updateTime);
            userRepository.save(existingUser);
            
            logging.info(CLASS_NAME, "updateStudent", "Estudiant modificat correctament"); // [cite: 173]
            return true;
        }
        
        logging.error(CLASS_NAME, "updateStudent", "L'estudiant amb id: " + userId + " no existeix"); // [cite: 181]
        return false;
    }

    public boolean delete(Long userId) {
        logging.info(CLASS_NAME, "deleteStudent", "Borrant l'estudiant amb id: " + userId); // [cite: 188]
        
        // Comprovem abans si existeix per fer el log correcte d'error si cal
        if (userRepository.findById(userId) == null) {
            logging.error(CLASS_NAME, "deleteStudent", "L'estudiant amb id: " + userId + " no existeix"); // [cite: 201]
            return false;
        }

        boolean reg = userRepository.delete(userId);
        if (reg) {
            logging.info(CLASS_NAME, "deleteStudent", "L'estudiant amb id: " + userId + " s'ha borrat correctament"); // [cite: 192]
        }
        return reg;
    }
   
    public String uploadImage(Long userId, MultipartFile image) {
        logging.info(CLASS_NAME, "uploadImage", "Afegint la imatge " + image.getOriginalFilename() + " per a l'estudiant amb id: " + userId); // [cite: 221]
        
        // Consultar si existeix l'usuari amb la id
        User existingUser = userRepository.findById(userId);
        if (existingUser == null) {
            logging.error(CLASS_NAME, "uploadImage", "L'usuari amb id " + userId + " no existeix"); // [cite: 225]
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
            
            logging.info(CLASS_NAME, "uploadImage", "La imatge s'ha guardat correctament. El path és: " + destinationFile.toString()); // [cite: 222]
            
            // Retornar la URL de la imatge
            return imagePath;
            
        } catch (IOException e) {
            logging.error(CLASS_NAME, "uploadImage", "Error guardant imatge: " + e.getMessage());
            throw new RuntimeException("Error en guardar la imatge: " + e.getMessage());
        }
    }

    // Crea 10 usuaris fent servir un csv
    public String insertAllStudentsByCsv(MultipartFile csvFile) {
        logging.info(CLASS_NAME, "insertAllStudentByCsv", "Carregant la informació del fitxer " + csvFile.getOriginalFilename()); // [cite: 209]

        int numRegInsert = 0;
        int numErrors = 0;
        int numeroLinia = 0;
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            String linia;
            
            while ((linia = br.readLine()) != null) {
                // Saltar línies buides
                if (linia.trim().isEmpty()) {
                    continue;
                }
                
                if (numeroLinia == 0) {
                    // Validar capçalera
                    if (!linia.equals("name,description,email,password")) {
                        return "Format de CSV incorrecte. Capçalera esperada: name,description,email,password";
                    }
                    numeroLinia++;
                    continue;
                }
                
                // Separar per comes
                String[] camps = linia.split(",");
                
                // Validar que tingui exactament 4 camps
                if (camps.length != 4) {
                    System.err.println("Línia ignorada - camps insuficients: " + linia);
                    numErrors++; // Comptem error
                    continue;
                }
                
                try {
                    User user = new User();
                    user.setName(camps[0].trim());
                    user.setDescription(camps[1].trim());
                    user.setEmail(camps[2].trim());
                    user.setPassword(camps[3].trim());
                    
                    // Guardem manualment per evitar duplicar logs de 'addUser'
                    LocalDateTime now = LocalDateTime.now();
                    user.setDataCreated(now);
                    user.setDataUpdated(now);
                    userRepository.save(user);
                    
                    numRegInsert++;
                    
                } catch (Exception e) {
                    numErrors++;
                    // Log error específic per línia
                    logging.error(CLASS_NAME, "insertAllStudentByCsv", "Error en la línia " + numeroLinia + " del fitxer. Missatge d'error: " + e.getMessage()); // [cite: 212]
                }
                
                numeroLinia++;
            }
            
            // Guardar el CSV processat (opcional)
            Path csvDir = Paths.get("src/main/resources/private/csv_processed");
            if (!Files.exists(csvDir)) {
                Files.createDirectories(csvDir);
            }
            
            String csvName = "students_import_" + System.currentTimeMillis() + ".csv";
            Path destinationFile = csvDir.resolve(csvName);
            Files.copy(csvFile.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (Exception e) {
            logging.error(CLASS_NAME, "insertAllStudentByCsv", "Error general processant CSV: " + e.getMessage());
            e.printStackTrace();
            return "Error processant CSV: " + e.getMessage();
        }
        
        // Log final resum
        logging.info(CLASS_NAME, "insertAllStudentByCsv", "S'han guardat correctament " + numRegInsert + " registres i han donat error " + numErrors + " registres"); // [cite: 210]
        
        return "Creats " + numRegInsert + " usuaris correctament";
    }

    public String insertAllUsersByJson(MultipartFile jsonFile) {
        logging.info(CLASS_NAME, "insertAllUsersByJson", "Carregant la informació del fitxer " + jsonFile.getOriginalFilename());

        int numRegInsert = 0;
        int numErrors = 0;
        
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
                    LocalDateTime now = LocalDateTime.now();
                    user.setDataCreated(now);
                    user.setDataUpdated(now);
                    userRepository.save(user);
                    
                    numRegInsert++;
                    
                } catch (Exception e) {
                    numErrors++;
                    System.err.println("Error creant usuari des de JSON: " + userNode);
                    logging.error(CLASS_NAME, "insertAllUsersByJson", "Error amb usuari JSON: " + e.getMessage());
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
            logging.error(CLASS_NAME, "insertAllUsersByJson", "Error general JSON: " + e.getMessage());
            e.printStackTrace();
            return "Error processant JSON: " + e.getMessage();
        }
        
        logging.info(CLASS_NAME, "insertAllUsersByJson", "S'han guardat correctament " + numRegInsert + " registres i han donat error " + numErrors + " registres");

        return "Creats " + numRegInsert + " usuaris correctament";
    }
}