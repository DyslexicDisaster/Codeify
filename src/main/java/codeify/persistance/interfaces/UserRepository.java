package codeify.persistance.interfaces;

import codeify.entities.User;
import codeify.entities.role;
import org.springframework.stereotype.Repository;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository {
    boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;

    boolean existsByEmail(String email) throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    String login(String username, String password) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    List<User> getAllUsers() throws SQLException;

    Optional<User> getUserById(int userId) throws SQLException;

    boolean deleteUserById(int userId) throws SQLException;

    boolean updateUser(User user) throws SQLException;

    boolean changeRole(int id, role role) throws SQLException;

    boolean resetPassword(int id, String hashedPassword) throws SQLException;
}