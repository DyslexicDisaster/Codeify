package codeify.persistance.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import codeify.entities.User;
import codeify.entities.role;
import codeify.util.passwordHash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.sql.*;
import java.time.LocalDate;
import java.sql.Date;

class UserRepositoryImplTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private passwordHash passwordHash;

    @InjectMocks
    private UserRepositoryImpl repository;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void testUpdatePasswordSuccess() throws SQLException {
        int userId = 42;
        String newPwd = "securePass123";

        assertDoesNotThrow(() -> repository.updatePassword(userId, newPwd));

        verify(connection).prepareStatement("UPDATE users SET password = ? WHERE user_id = ?");
        verify(preparedStatement).setString(1, newPwd);
        verify(preparedStatement).setInt(2, userId);
        verify(preparedStatement).executeUpdate();
        verify(connection).close();
    }

    @Test
    void testUpdatePasswordPrepareStatementThrows() throws SQLException {
        int userId = 7;
        String newPwd = "pass";
        when(connection.prepareStatement(anyString())).thenThrow(new SQLException("prep failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> repository.updatePassword(userId, newPwd));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void testUpdatePasswordExecuteUpdateThrows() throws SQLException {
        int userId = 99;
        String newPwd = "anotherPass";
        doThrow(new SQLException("exec failed")).when(preparedStatement).executeUpdate();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> repository.updatePassword(userId, newPwd));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void testUpdatePasswordWithNullPassword() throws SQLException {
        int userId = 55;
        String newPwd = null;

        assertDoesNotThrow(() -> repository.updatePassword(userId, newPwd));

        verify(preparedStatement).setString(1, newPwd);
        verify(preparedStatement).setInt(2, userId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testUpdatePasswordConnectionThrows() throws SQLException {
        int userId = 13;
        String newPwd = "failConn";
        when(dataSource.getConnection()).thenThrow(new SQLException("conn failed"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> repository.updatePassword(userId, newPwd));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    @Test
    void testSaveSuccess() throws SQLException {
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("pass123");
        user.setEmail("john@example.com");
        user.setRegistrationDate(java.time.LocalDate.of(2025, 4, 21));
        user.setRole(role.user);
        user.setProvider("local");

        String sql = """
            INSERT INTO users (
                username, password, email,
                registration_date, role, provider
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;
        when(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(preparedStatement);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1001);

        User saved = repository.save(user);

        verify(connection).prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        verify(preparedStatement).setString(1, user.getUsername());
        verify(preparedStatement).setString(2, user.getPassword());
        verify(preparedStatement).setString(3, user.getEmail());
        verify(preparedStatement).setDate(4, Date.valueOf(user.getRegistrationDate()));
        verify(preparedStatement).setString(5, user.getRole().name());
        verify(preparedStatement).setString(6, user.getProvider());
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
        assertEquals(1001, saved.getUserId());
    }

    @Test
    void testSaveNoRowsAffected() throws SQLException {
        User user = new User();
        user.setRegistrationDate(LocalDate.of(2025, 1, 1));
        user.setRole(role.user);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        SQLException ex = assertThrows(SQLException.class,
                () -> repository.save(user));
        assertEquals("Creating user failed, no rows affected.", ex.getMessage());
    }

    @Test
    void testSaveNoGeneratedKeys() throws SQLException {
        User user = new User();
        user.setRegistrationDate(LocalDate.of(2025, 1, 1));
        user.setRole(role.user);
        when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        User returned = repository.save(user);

        assertEquals(0, returned.getUserId(), "Expected default userId of 0 when no keys returned");
    }

    @Test
    void testSaveConnectionFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("conn failed"));
        assertThrows(SQLException.class,
                () -> repository.save(new User()));
    }
}