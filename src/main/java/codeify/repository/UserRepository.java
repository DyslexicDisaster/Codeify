package codeify.repository;

import codeify.entities.role;
import org.springframework.stereotype.Repository;

import codeify.entities.User;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface UserRepository {
    boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
    User login(String username, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
    boolean deleteUserById(int id) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    boolean updateUser(User user) throws SQLException;
    boolean resetPassword(int id, String password) throws SQLException;
    boolean existsByUsername(String username) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    boolean changeRole(int id, role role) throws SQLException;
    User getUserById(int id) throws SQLException;
}