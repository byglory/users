package com.ra2.users.users.repository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.ra2.users.users.model.User;

@Repository
public class UserRepository {
    
    @Autowired
    JdbcTemplate jdbcTemplate;
    
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
        String sql = "INSERT INTO users (name, description, email, password, ultimAcces, dataCreated, dataUpdated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql, 
            user.getName(),
            user.getDescription(),
            user.getEmail(),
            user.getPassword(),
            user.getUltimAcces(),
            user.getDataCreated(),
            user.getDataUpdated()
        );
        
        // Obtenir l'ID generat automàticament
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        user.setId(id);
        
        return user;
    }
    
    // Retorna tots els usuaris de la base de dades
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        try {
            List<User> users = jdbcTemplate.query(sql, new UserRowMapper());
            return users;
        } catch (Exception e) {
            System.out.println("Error en findAll: " + e.getMessage());
            return List.of(); // Retorna llista buida en cas d'error
        }
    }
    
    // Busca un usuari pel seu ID
    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
            return user;
        } catch (Exception e) {
            return null; // Retorna null si no troba l'usuari
        }
    }
    
    // Actualitza tots els camps d'un usuari
    public boolean update(Long id, User user) {
        String sql = "UPDATE users SET name = ?, description = ?, email = ?, password = ?, ultimAcces = ?, dataUpdated = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
            user.getName(),
            user.getDescription(),
            user.getEmail(),
            user.getPassword(),
            user.getUltimAcces(),
            user.getDataUpdated(),
            id
        );
        return rowsAffected > 0; // True si s'actualitzà almenys una fila
    }
    
    // Actualitza només el nom d'un usuari
    public boolean updateName(Long id, String name, LocalDateTime dataUpdated) {
        String sql = "UPDATE users SET name = ?, dataUpdated = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, name, dataUpdated, id);
        return rowsAffected > 0; // True si s'actualitzà almenys una fila
    }
    
    // Elimina un usuari de la base de dades
    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        return rowsAffected > 0; // True si s'eliminà almenys una fila
    }
}