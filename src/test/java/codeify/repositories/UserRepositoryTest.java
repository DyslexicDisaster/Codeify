package codeify.repositories;

import codeify.entities.*;
import codeify.persistance.UserRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import codeify.util.*;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @InjectMocks
    private UserRepositoryImpl userRepositoryImpl;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    // Constants for testing
    private final int ID = 1;
    private final String USERNAME = "test";
    private final String EMAIL = "test@email.com";
    private final String SALT = "testSalt";
    private final String PASSWORD = "testPassword";
    private final String ENTERED_PASSWORD = "secretPassword";
    private final LocalDate REGISTRATION_DATE = LocalDate.of(2025, 3, 15);
    private final role ROLE = role.user;

    // Helper method to create a test user
    private User testUser() {
        return new User(ID, USERNAME, EMAIL, PASSWORD, SALT, REGISTRATION_DATE, ROLE);
    }

    UserRepositoryTest() {
    }

    // Set up mocks for each test
    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    /**
     *  1. User is found in the database by username
     *  2. User is not found in the database by username
     *  3. SQLException occurs
     */
    @Test
    void testExistsByUsername_UserFound() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        boolean exists = userRepositoryImpl.existsByUsername(USERNAME);
        assertTrue(exists);
        verify(statement).setString(1, USERNAME);
        verify(statement).executeQuery();
    }
    @Test
    void testExistsByUsername_UserNotFound() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);
        boolean exists = userRepositoryImpl.existsByUsername(USERNAME);
        assertFalse(exists);
        verify(statement).setString(1, USERNAME);
        verify(statement).executeQuery();
    }
    @Test
    void testExistsByUsername_SqlException() throws SQLException {
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.existsByUsername(USERNAME));
        verify(statement).setString(1, USERNAME);
        verify(statement).executeQuery();
    }

    /**
     *  1. User is found in the database by email
     *  2. User is not found in the database by email
     *  3. SQLException occurs
     */
    @Test
    void testExistsByEmail_UserFound() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        boolean exists = userRepositoryImpl.existsByEmail(EMAIL);
        assertTrue(exists);
        verify(statement).setString(1, EMAIL);
        verify(statement).executeQuery();
    }
    @Test
    void testExistsByEmail_UserNotFound() throws SQLException {
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0);
        boolean exists = userRepositoryImpl.existsByEmail(EMAIL);
        assertFalse(exists);
        verify(statement).setString(1, EMAIL);
        verify(statement).executeQuery();
    }
    @Test
    void testExistsByEmail_SqlException() throws SQLException {
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.existsByEmail(EMAIL));
        verify(statement).setString(1, EMAIL);
        verify(statement).executeQuery();
    }

    /**
     *  1. Role has been changed successfully
     *  2. Role has not been changed
     *  3. SQLException occurs
     */
    @Test
    void testChangeRole_Success() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);  // one row updated
        boolean result = userRepositoryImpl.changeRole(ID, ROLE);
        assertTrue(result);
        verify(statement).setString(1, String.valueOf(ROLE));
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testChangeRole_Failure() throws SQLException {
        when(statement.executeUpdate()).thenReturn(0);
        boolean changed = userRepositoryImpl.changeRole(ID, ROLE);
        assertFalse(changed);
        verify(statement).setString(1, String.valueOf(ROLE));
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testChangeRole_SqlException() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.changeRole(ID, ROLE));
        verify(statement).setString(1, String.valueOf(ROLE));
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }

    /**
     *  1. User has been registered successfully
     *  2. User has not been registered
     *  3. SQLException occurs
     *  4. NoSuchAlgorithmException occurs
     *  5. InvalidKeySpecException occurs
     *  6. NULL values are passed
     */
    @Test
    void testRegisterUser_Success() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenReturn(1);
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.hashPassword(anyString(), anyString()))
                    .thenReturn("hashedTestPassword");
            boolean result = userRepositoryImpl.register(newUser);
            assertTrue(result);
            verify(statement).setString(1, newUser.getUsername());
            verify(statement).setString(2, newUser.getEmail());
            verify(statement).setString(3, "hashedTestPassword");
            verify(statement).setString(4, newUser.getSalt());
            verify(statement).setDate(5, Date.valueOf(newUser.getRegistrationDate()));
            verify(statement).setString(6, newUser.getRole().toString());
            verify(statement).executeUpdate();
            mockedStatic.verify(() -> codeify.util.passwordHash.hashPassword(newUser.getPassword(), newUser.getSalt()));
        }
    }
    @Test
    void testRegisterUser_NoSuccess() throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenReturn(0);
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.hashPassword(anyString(), anyString()))
                    .thenReturn("hashedTestPassword");
            boolean result = userRepositoryImpl.register(newUser);
            assertFalse(result);
            verify(statement).setString(1, newUser.getUsername());
            verify(statement).setString(2, newUser.getEmail());
            verify(statement).setString(3, "hashedTestPassword");
            verify(statement).setString(4, newUser.getSalt());
            verify(statement).setDate(5, Date.valueOf(newUser.getRegistrationDate()));
            verify(statement).setString(6, newUser.getRole().toString());
            verify(statement).executeUpdate();
            mockedStatic.verify(() -> codeify.util.passwordHash.hashPassword(newUser.getPassword(), newUser.getSalt()));
        }
    }
    @Test
    void testRegisterUser_SQLException() throws SQLException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.hashPassword(anyString(), anyString()))
                    .thenReturn("hashedTestPassword");
            assertThrows(SQLException.class, () -> userRepositoryImpl.register(newUser));
            verify(statement).setString(1, newUser.getUsername());
            verify(statement).setString(2, newUser.getEmail());
            verify(statement).setString(3, "hashedTestPassword");
            verify(statement).setString(4, newUser.getSalt());
            verify(statement).setDate(5, Date.valueOf(newUser.getRegistrationDate()));
            verify(statement).setString(6, newUser.getRole().toString());
            verify(statement).executeUpdate();
            mockedStatic.verify(() -> codeify.util.passwordHash.hashPassword(newUser.getPassword(), newUser.getSalt()));
        }
    }
    @Test
    void testRegisterUser_NoSuchAlgorithmException() {
        User newUser = testUser();
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.hashPassword(anyString(), anyString()))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not found"));
            assertThrows(NoSuchAlgorithmException.class, () -> userRepositoryImpl.register(newUser));
            mockedStatic.verify(() -> codeify.util.passwordHash.hashPassword(newUser.getPassword(), newUser.getSalt()));
        }
    }
    @Test
    void testRegisterUser_InvalidKeySpecException() {
        User newUser = testUser();
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.hashPassword(anyString(), anyString()))
                    .thenThrow(new InvalidKeySpecException("Invalid key specification"));
            assertThrows(InvalidKeySpecException.class, () -> userRepositoryImpl.register(newUser));
            mockedStatic.verify(() -> codeify.util.passwordHash.hashPassword(newUser.getPassword(), newUser.getSalt()));
        }
    }
    @Test
    void testRegisterUser_NullValues() {
        User newUser = new User(null, null, null, null, null);
        assertThrows(NullPointerException.class, () -> userRepositoryImpl.register(newUser));
    }

    /**
     *  1. User has been updated successfully
     *  2. User has not been updated
     *  3. SQLException occurs
     */
    @Test
    void testUpdateUser_Success() throws SQLException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenReturn(1);
        boolean result = userRepositoryImpl.updateUser(newUser);
        assertTrue(result);
        verify(statement).setString(1, newUser.getUsername());
        verify(statement).setString(2, newUser.getEmail());
        verify(statement).setString(3, newUser.getPassword());
        verify(statement).setString(4, newUser.getSalt());
        verify(statement).setString(5, newUser.getRole().toString());
        verify(statement).setInt(6, newUser.getUserId());
        verify(statement).executeUpdate();
    }
    @Test
    void testUpdateUser_Failure() throws SQLException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenReturn(0);
        boolean result = userRepositoryImpl.updateUser(newUser);
        assertFalse(result);
        verify(statement).setString(1, newUser.getUsername());
        verify(statement).setString(2, newUser.getEmail());
        verify(statement).setString(3, newUser.getPassword());
        verify(statement).setString(4, newUser.getSalt());
        verify(statement).setString(5, newUser.getRole().toString());
        verify(statement).setInt(6, newUser.getUserId());
        verify(statement).executeUpdate();
    }
    @Test
    void testUpdateUser_SqlException() throws SQLException {
        User newUser = testUser();
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.updateUser(newUser));
        verify(statement).setString(1, newUser.getUsername());
        verify(statement).setString(2, newUser.getEmail());
        verify(statement).setString(3, newUser.getPassword());
        verify(statement).setString(4, newUser.getSalt());
        verify(statement).setString(5, newUser.getRole().toString());
        verify(statement).setInt(6, newUser.getUserId());
        verify(statement).executeUpdate();
    }

    /**
     *  1. User has been deleted successfully
     *  2. User has not been deleted
     *  3. SQLException occurs
     */
    @Test
    void testDeleteUser_Success() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        boolean result = userRepositoryImpl.deleteUserById(ID);
        assertTrue(result);
        verify(statement).setInt(1, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testDeleteUser_Failure() throws SQLException {
        when(statement.executeUpdate()).thenReturn(0);
        boolean result = userRepositoryImpl.deleteUserById(ID);
        assertFalse(result);
        verify(statement).setInt(1, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testDeleteUser_SqlException() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.deleteUserById(ID));
        verify(statement).setInt(1, ID);
        verify(statement).executeUpdate();
    }

    /**
     *  1. Password has been reset successfully
     *  2. Password has not been reset
     *  3. SQLException occurs
     */
    @Test
    void testResetPassword_Success() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);
        boolean result = userRepositoryImpl.resetPassword(ID, PASSWORD);
        assertTrue(result);
        verify(statement).setString(1, PASSWORD);
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testResetPassword_Failure() throws SQLException {
        when(statement.executeUpdate()).thenReturn(0);
        boolean result = userRepositoryImpl.resetPassword(ID, PASSWORD);
        assertFalse(result);
        verify(statement).setString(1, PASSWORD);
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }
    @Test
    void testResetPassword_SqlException() throws SQLException {
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.resetPassword(ID, PASSWORD));
        verify(statement).setString(1, PASSWORD);
        verify(statement).setInt(2, ID);
        verify(statement).executeUpdate();
    }

    /**
     *  1. User has been logged in successfully
     *  2. User has not been found
     *  3. SQLException occurs
     *  4. Invalid password
     *  5. Connection is null
     */
    @Test
    void testLogin_Success() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("user_id")).thenReturn(ID);
        when(resultSet.getString("username")).thenReturn(USERNAME);
        when(resultSet.getString("password")).thenReturn(PASSWORD);
        when(resultSet.getString("salt")).thenReturn(SALT);
        when(resultSet.getString("email")).thenReturn(EMAIL);
        when(resultSet.getDate("registration_date")).thenReturn(Date.valueOf(REGISTRATION_DATE));
        when(resultSet.getString("role")).thenReturn(ROLE.toString());
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.validatePassword(anyString(), anyString(), anyString()))
                    .thenReturn(true);
            User user = userRepositoryImpl.login(USERNAME, PASSWORD);
            assertNotNull(user);
            assertEquals(ID, user.getUserId());
            assertEquals(USERNAME, user.getUsername());
            assertEquals(EMAIL, user.getEmail());
            assertEquals(PASSWORD, user.getPassword());
            assertEquals(SALT, user.getSalt());
            assertEquals(REGISTRATION_DATE, user.getRegistrationDate());
            assertEquals(ROLE, user.getRole());
            verify(statement).setString(1, USERNAME);
            verify(statement).executeQuery();
            mockedStatic.verify(() -> codeify.util.passwordHash.validatePassword(PASSWORD, PASSWORD, SALT));
        }
    }
    @Test
    void testLogin_UserNotFound() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        User user = userRepositoryImpl.login(USERNAME, PASSWORD);
        assertNull(user);
        verify(statement).setString(1, USERNAME);
        verify(statement).executeQuery();
    }
    @Test
    void testLogin_SqlException() throws SQLException {
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.login(USERNAME, PASSWORD));
        verify(statement).setString(1, USERNAME);
        verify(statement).executeQuery();
    }
    @Test
    void testLogin_InvalidPassword() throws Exception {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("password")).thenReturn(PASSWORD);
        when(resultSet.getString("salt")).thenReturn(SALT);
        when(resultSet.getString("role")).thenReturn(ROLE.toString());
        when(resultSet.getDate("registration_date")).thenReturn(Date.valueOf(REGISTRATION_DATE));
        when(resultSet.getString("username")).thenReturn(USERNAME);
        when(resultSet.getString("email")).thenReturn(EMAIL);
        try (MockedStatic<passwordHash> mockedStatic = mockStatic(passwordHash.class)) {
            mockedStatic.when(() -> codeify.util.passwordHash.validatePassword(ENTERED_PASSWORD, PASSWORD, SALT))
                    .thenReturn(false);
            User user = userRepositoryImpl.login(USERNAME, ENTERED_PASSWORD);
            assertNull(user);
            verify(statement).setString(1, USERNAME);
            verify(statement).executeQuery();
            mockedStatic.verify(() -> codeify.util.passwordHash.validatePassword(ENTERED_PASSWORD, PASSWORD, SALT));
        }
    }
    @Test
    void testLogin_ConnectionNull() throws Exception {
        when(dataSource.getConnection()).thenReturn(null);

        SQLException thrown = assertThrows(SQLException.class,
                () -> userRepositoryImpl.login(USERNAME, ENTERED_PASSWORD));
        assertEquals("Unable to connect to the database!", thrown.getMessage());
    }

    /**
     *  1. Retrieve all users from the database
     *  2. SQLException occurs
     *  3. No users found
     */
    @Test
    void testGetAllUsers_Success() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("user_id")).thenReturn(1, 2);
        when(resultSet.getString("username")).thenReturn("user1", "user2");
        when(resultSet.getString("password")).thenReturn("password1", "password2");
        when(resultSet.getString("salt")).thenReturn("salt1", "salt2");
        when(resultSet.getString("email")).thenReturn("user1@example.com", "user2@example.com");
        when(resultSet.getDate("registration_date")).thenReturn(
                Date.valueOf(LocalDate.of(2025, 1, 1)),
                Date.valueOf(LocalDate.of(2025, 2, 2))
        );
        when(resultSet.getString("role")).thenReturn("user", "admin");
        List<User> users = userRepositoryImpl.getAllUsers();
        assertNotNull(users);
        assertEquals(2, users.size());
        User user1 = users.getFirst();
        assertEquals(1, user1.getUserId());
        assertEquals("user1", user1.getUsername());
        assertEquals("password1", user1.getPassword());
        assertEquals("salt1", user1.getSalt());
        assertEquals("user1@example.com", user1.getEmail());
        assertEquals(LocalDate.of(2025, 1, 1), user1.getRegistrationDate());
        assertEquals(role.user, user1.getRole());
        User user2 = users.get(1);
        assertEquals(2, user2.getUserId());
        assertEquals("user2", user2.getUsername());
        assertEquals("password2", user2.getPassword());
        assertEquals("salt2", user2.getSalt());
        assertEquals("user2@example.com", user2.getEmail());
        assertEquals(LocalDate.of(2025, 2, 2), user2.getRegistrationDate());
        assertEquals(role.admin, user2.getRole());
        verify(statement).executeQuery();
    }
    @Test
    void testGetAllUsers_SqlException() throws SQLException {
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));
        assertThrows(SQLException.class, () -> userRepositoryImpl.getAllUsers());
        verify(statement).executeQuery();
    }
    @Test
    void testGetAllUsers_NoUsersFound() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        List<User> users = userRepositoryImpl.getAllUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(statement).executeQuery();
    }
}