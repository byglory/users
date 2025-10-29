package com.ra2.users.users.repository;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static final class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setDescription(rs.getString("description"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            
            // Mapear fechas (pueden ser null)
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
        
        // Obtener el ID generado
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        user.setId(id);
        
        return user;
    }
}
