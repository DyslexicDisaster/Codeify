package codeify.repository;

import org.springframework.stereotype.Repository;

import codeify.entities.User;

import java.sql.SQLException;
import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findByUsername(String username) throws SQLException;
    User save(User user) throws SQLException;
}