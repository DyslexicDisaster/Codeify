package codeify.persistance;

import codeify.business.User;
import codeify.business.role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoImplTest {

    private static UserDaoImpl userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl("database_test.properties");
    }

    @Test
    void register() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User(0, "newUser", "password", "", "newuser@example.com", LocalDate.now(), role.user);
        boolean isRegistered = userDao.register(user);
        assertTrue(isRegistered);

        User loggedInUser = userDao.login(user.getUsername(), user.getPassword());
        assertNotNull(loggedInUser);
        assertEquals(user.getUsername(), loggedInUser.getUsername());
    }

    @Test
    void login() throws SQLException {
        String username = "newUser";
        String password = "password";

        User user = userDao.login(username, password);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @AfterAll
    static void tearDown() throws SQLException {
        userDao.deleteUserByUsername("newUser");
    }
}