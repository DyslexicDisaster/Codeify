package codeify.persistance;

import codeify.business.User;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

public interface UserDao {
    boolean register(User user) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;

    User login(String username, String password) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException;
}
