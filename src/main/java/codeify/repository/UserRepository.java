package codeify.repository;

import org.springframework.stereotype.Repository;

import codeify.model.User;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

@Repository
public interface UserRepository {
    boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
    User login(String username, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
}