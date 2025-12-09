package com.ra2.users.users.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.ra2.users.users.logging.CustomLogging; // Importem la nostra classe de logging
import com.ra2.users.users.model.User;

@Repository
public class UserRepository {
    
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    CustomLogging logging; // Injectem el component de logging

    private final String CLASS_NAME = "UserRepository"; // Constant per identificar la classe al log
    
    // Mapeja una fila de la base de dades a un objecte User
    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setDescription(rs.getString("description"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setImage(rs.getString("image_path"));
            
            // Mapejar dates (poden ser null)
            if (rs.getTimestamp("ultimAcces") != null) {
                user.setUltimAcces(rs.getTimestamp("ultimAcces").toLocalDateTime());
            }
            if (rs.getTimestamp("dataCreated") != null) {
                user.setDataCreated(rs.getTimestamp("dataCreated").toLocalDateTime());
            }
            if (rs.getTimestamp("dataUpdated") != null) {
                user.setDataUpdated(rs.getTimestamp("dataUpdated").toLocalDateTime());
            }
            
            return user;
        }
    }
    
    // Guarda un nou usuari a la base de dades
    public User save(User user) {
        logging.info(CLASS_NAME, "save", "Intentant guardar l'usuari: " + user.getName());
        
        String sql = "INSERT INTO users (name, description, email, password, image_path, ultimAcces, dataCreated, dataUpdated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try {
            jdbcTemplate.update(sql, 
                user.getName(),
                user.getDescription(),
                user.getEmail(),
                user.getPassword(),
                user.getImage(),
                user.getUltimAcces(),
                user.getDataCreated(),
                user.getDataUpdated()
            );
            
            // Obtenir l'ID generat automàticament
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            user.setId(id);
            
            logging.info(CLASS_NAME, "save", "Usuari guardat correctament amb ID: " + id);
            return user;
            
        } catch (Exception e) {
            logging.error(CLASS_NAME, "save", "Error guardant l'usuari: " + e.getMessage());
            throw e; // Relancem l'excepció perquè el Service la gestioni si cal
        }
    }
    
    // Retorna tots els usuaris de la base de dades
    public List<User> findAll() {
        logging.info(CLASS_NAME, "findAll", "Consultant tots els usuaris");
        String sql = "SELECT * FROM users";
        try {
            List<User> users = jdbcTemplate.query(sql, new UserRowMapper());
            return users;
        } catch (Exception e) {
            logging.error(CLASS_NAME, "findAll", "Error consultant usuaris: " + e.getMessage());
            System.out.println("Error en findAll: " + e.getMessage());
            return List.of(); // Retorna llista buida en cas d'error
        }
    }
    
    // Busca un usuari pel seu ID
    public User findById(Long id) {
        logging.info(CLASS_NAME, "findById", "Consultant usuari amb ID: " + id);
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return user;
        } catch (Exception e) {
            logging.error(CLASS_NAME, "findById", "No s'ha trobat l'usuari amb ID: " + id);
            return null; // Retorna null si no troba l'usuari
        }
    }
    
    // Actualitza tots els camps d'un usuari
    public boolean update(Long id, User user) {
        logging.info(CLASS_NAME, "update", "Actualitzant usuari amb ID: " + id);
        String sql = "UPDATE users SET name = ?, description = ?, email = ?, password = ?, ultimAcces = ?, dataUpdated = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql,
                user.getName(),
                user.getDescription(),
                user.getEmail(),
                user.getPassword(),
                user.getUltimAcces(),
                user.getDataUpdated(),
                id
            );
            
            if (rowsAffected > 0) {
                logging.info(CLASS_NAME, "update", "Usuari actualitzat correctament");
                return true;
            } else {
                logging.error(CLASS_NAME, "update", "No s'ha pogut actualitzar. Usuari no trobat.");
                return false;
            }
        } catch (Exception e) {
            logging.error(CLASS_NAME, "update", "Error actualitzant usuari: " + e.getMessage());
            return false;
        }
    }
    
    // Actualitza només el nom d'un usuari
    public boolean updateName(Long id, String name, LocalDateTime dataUpdated) {
        logging.info(CLASS_NAME, "updateName", "Actualitzant nom per a l'usuari ID: " + id);
        String sql = "UPDATE users SET name = ?, dataUpdated = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, name, dataUpdated, id);
            if (rowsAffected > 0) {
                logging.info(CLASS_NAME, "updateName", "Nom actualitzat correctament");
                return true;
            } else {
                logging.error(CLASS_NAME, "updateName", "No s'ha trobat l'usuari per actualitzar el nom");
                return false;
            }
        } catch (Exception e) {
            logging.error(CLASS_NAME, "updateName", "Error actualitzant nom: " + e.getMessage());
            return false;
        }
    }
    
    // Elimina un usuari de la base de dades
    public boolean delete(Long id) {
        logging.info(CLASS_NAME, "delete", "Intentant eliminar usuari amb ID: " + id);
        String sql = "DELETE FROM users WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, id);
            if (rowsAffected > 0) {
                logging.info(CLASS_NAME, "delete", "Usuari eliminat correctament");
                return true;
            } else {
                logging.error(CLASS_NAME, "delete", "No s'ha trobat l'usuari per eliminar");
                return false;
            }
        } catch (Exception e) {
            logging.error(CLASS_NAME, "delete", "Error eliminant usuari: " + e.getMessage());
            return false;
        }
    }

    public boolean updateImagePath(Long userId, String imagePath, LocalDateTime dataUpdated) {
        logging.info(CLASS_NAME, "updateImagePath", "Actualitzant imatge per a l'usuari ID: " + userId);
        String sql = "UPDATE users SET image_path = ?, dataUpdated = ? WHERE id = ?";
        
        try {
            int rowsAffected = jdbcTemplate.update(sql, imagePath, dataUpdated, userId);
            if (rowsAffected > 0) {
                logging.info(CLASS_NAME, "updateImagePath", "Ruta de imatge actualitzada correctament");
                return true;
            } else {
                logging.error(CLASS_NAME, "updateImagePath", "No s'ha trobat l'usuari per actualitzar la imatge");
                return false;
            }
        } catch (Exception e) {
            logging.error(CLASS_NAME, "updateImagePath", "Error actualitzant imatge: " + e.getMessage());
            return false;
        }
    }
}