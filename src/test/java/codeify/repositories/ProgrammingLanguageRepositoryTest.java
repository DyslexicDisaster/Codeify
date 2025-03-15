package codeify.repositories;

import codeify.entities.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ProgrammingLanguageRepositoryTest {

    @InjectMocks
    private ProgrammingLanguageRepositoryImpl repositoryImpl;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement statement;

    @Mock
    private ResultSet resultSet;

    // Sample test constants.
    private final int LANGUAGE_ID = 1;
    private final String LANGUAGE_NAME = "Java";

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        // By default, all preparedStatement calls return our mock statement.
        when(connection.prepareStatement(anyString())).thenReturn(statement);
    }

    ProgrammingLanguageRepositoryTest() {
    }

    /**
     * 1. Retrieve all programming languages from the database
     * 2. Retrieve an empty list of programming languages
     * 3. SQLException is thrown
     */
    @Test
    void getAllProgrammingLanguage_LanguagesList() throws SQLException {
        String query = "SELECT * FROM programming_languages ORDER BY name";

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getInt("id")).thenReturn(LANGUAGE_ID, 2);
        when(resultSet.getString("name")).thenReturn(LANGUAGE_NAME, "Python");

        List<ProgrammingLanguage> languages = repositoryImpl.getAllProgrammingLanguage();
        assertNotNull(languages);
        assertEquals(2, languages.size());

        assertEquals(LANGUAGE_ID, languages.get(0).getId());
        assertEquals(LANGUAGE_NAME, languages.get(0).getName());
        assertEquals(2, languages.get(1).getId());
        assertEquals("Python", languages.get(1).getName());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }
    @Test
    void getAllProgrammingLanguage_EmptyList() throws SQLException {
        String query = "SELECT * FROM programming_languages ORDER BY name";

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<ProgrammingLanguage> languages = repositoryImpl.getAllProgrammingLanguage();
        assertNotNull(languages);
        assertEquals(0, languages.size());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }
    @Test
    void getAllProgrammingLanguage_SQLException() throws SQLException {
        String query = "SELECT * FROM programming_languages ORDER BY name";
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.getAllProgrammingLanguage());

        verify(connection).prepareStatement(query);
        verify(statement).executeQuery();
    }

    /**
     * 1. Retrieve a programming language by its ID
     * 2. Programming language not found
     * 3. SQLException is thrown
     */

    @Test
    void getProgrammingLanguageById_Found() throws SQLException {
        String query = "SELECT * FROM programming_languages WHERE id = ?";

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(LANGUAGE_ID);
        when(resultSet.getString("name")).thenReturn(LANGUAGE_NAME);

        ProgrammingLanguage language = repositoryImpl.getProgrammingLanguageById(LANGUAGE_ID);
        assertNotNull(language);
        assertEquals(LANGUAGE_ID, language.getId());
        assertEquals(LANGUAGE_NAME, language.getName());

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }
    @Test
    void getProgrammingLanguageById_NotFound() throws SQLException {
        String query = "SELECT * FROM programming_languages WHERE id = ?";

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        ProgrammingLanguage language = repositoryImpl.getProgrammingLanguageById(LANGUAGE_ID);
        assertNull(language);

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }
    @Test
    void getProgrammingLanguageById_SQLException() throws SQLException {
        String query = "SELECT * FROM programming_languages WHERE id = ?";
        when(statement.executeQuery()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.getProgrammingLanguageById(LANGUAGE_ID));

        verify(connection).prepareStatement(query);
        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeQuery();
    }

    /**
     * 1. Add a new programming language to the database
     * 2. Programming language is not added
     * 3. SQLException is thrown
     */
    @Test
    void addProgrammingLanguage_Success() throws SQLException {
        String query = "INSERT INTO programming_languages (name) VALUES (?)";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.addProgrammingLanguage(language);
        assertTrue(result);

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).executeUpdate();
    }
    @Test
    void addProgrammingLanguage_Failure() throws SQLException {
        String query = "INSERT INTO programming_languages (name) VALUES (?)";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.addProgrammingLanguage(language);
        assertFalse(result);

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).executeUpdate();
    }
    @Test
    void addProgrammingLanguage_SQLException() throws SQLException {
        String query = "INSERT INTO programming_languages (name) VALUES (?)";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.addProgrammingLanguage(language));

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).executeUpdate();
    }

    /**
     * 1. Update an existing programming language in the database
     * 2. Programming language is not updated
     * 3. SQLException is thrown
     */
    @Test
    void updateProgrammingLanguage_Success() throws SQLException {
        String query = "UPDATE programming_languages SET name = ? WHERE id = ?";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setId(LANGUAGE_ID);
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.updateProgrammingLanguage(language);
        assertTrue(result);

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).setInt(2, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void updateProgrammingLanguage_Failure() throws SQLException {
        String query = "UPDATE programming_languages SET name = ? WHERE id = ?";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setId(LANGUAGE_ID);
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.updateProgrammingLanguage(language);
        assertFalse(result);

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).setInt(2, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void updateProgrammingLanguage_SQLException() throws SQLException {
        String query = "UPDATE programming_languages SET name = ? WHERE id = ?";

        ProgrammingLanguage language = new ProgrammingLanguage();
        language.setId(LANGUAGE_ID);
        language.setName(LANGUAGE_NAME);
        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.updateProgrammingLanguage(language));

        verify(statement).setString(1, LANGUAGE_NAME);
        verify(statement).setInt(2, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }

    /**
     * 1. Delete a programming language by its ID
     * 2. Programming language is not deleted
     * 3. SQLException is thrown
     */
    @Test
    void deleteProgrammingLanguageById_Success() throws SQLException {
        String query = "DELETE FROM programming_languages WHERE id = ?";

        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        boolean result = repositoryImpl.deleteProgrammingLanguageById(LANGUAGE_ID);
        assertTrue(result);

        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void deleteProgrammingLanguageById_Failure() throws SQLException {
        String query = "DELETE FROM programming_languages WHERE id = ?";

        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(0);

        boolean result = repositoryImpl.deleteProgrammingLanguageById(LANGUAGE_ID);
        assertFalse(result);

        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }
    @Test
    void deleteProgrammingLanguageById_SQLException() throws SQLException {
        String query = "DELETE FROM programming_languages WHERE id = ?";

        when(connection.prepareStatement(query)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(new SQLException("Database error"));

        assertThrows(SQLException.class, () -> repositoryImpl.deleteProgrammingLanguageById(LANGUAGE_ID));

        verify(statement).setInt(1, LANGUAGE_ID);
        verify(statement).executeUpdate();
    }
}