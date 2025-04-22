package codeify.persistance.implementations;

import codeify.entities.ProgrammingLanguage;
import codeify.persistance.interfaces.ProgrammingLanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProgrammingLanguageRepositoryImpl implements ProgrammingLanguageRepository {

    @Autowired
    private DataSource dataSource;

    /**
     * Method to get all programming languages from the database
     *
     * @return List<ProgrammingLanguage> - List of programming languages
     * @throws SQLException - If there is an error in the SQL query
     */
    @Override
    public List<ProgrammingLanguage> getAllProgrammingLanguage() throws SQLException {

        // Empty list of languages
        List<ProgrammingLanguage> languageList = new ArrayList<>();

        // Query waiting for being executed
        String query = "SELECT * FROM programming_languages ORDER BY name";

        // Sets up connection and executes statement
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                // Adds properties to an empty language object
                ProgrammingLanguage language = new ProgrammingLanguage();
                language.setId(resultSet.getInt("id"));
                language.setName(resultSet.getString("name"));

                // Adds language to the list
                languageList.add(language);
            }
        }
        return languageList;
    }

    /**
     * Method to get a programming language by its ID
     *
     * @param languageId - ID of the programming language
     * @return ProgrammingLanguage - Programming language object
     * @throws SQLException - If there is an error in the SQL query
     */
    @Override
    public ProgrammingLanguage getProgrammingLanguageById(int languageId) throws SQLException {
        String query = "SELECT * FROM programming_languages WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, languageId);
            try(ResultSet resultSet = statement.executeQuery()) {
                if(resultSet.next()) {
                    ProgrammingLanguage language = new ProgrammingLanguage();
                    language.setId(resultSet.getInt("id"));
                    language.setName(resultSet.getString("name"));
                    return language;
                }
            }
        }
        return null;
    }

    /**
     * Method to add a programming language to the database
     *
     * @param language - Programming language object
     * @return boolean - true if the language was added successfully, false otherwise
     * @throws SQLException - If there is an error in the SQL query
     */
    @Override
    public boolean addProgrammingLanguage(ProgrammingLanguage language) throws SQLException {
        String query = "INSERT INTO programming_languages (name) VALUES (?)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, language.getName());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Method to update a programming language in the database
     *
     * @param language ProgrammingLanguage to be updated
     * @return true if the update was successful, false otherwise
     * @throws SQLException if there is an error in the SQL query
     */
    @Override
    public boolean updateProgrammingLanguage(ProgrammingLanguage language) throws SQLException {
        String query = "UPDATE programming_languages SET name = ? WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, language.getName());
            statement.setInt(2, language.getId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Method to delete a programming language from the database
     *
     * @param languageId ID of the programming language to be deleted
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if there is an error in the SQL query
     */
    @Override
    public boolean deleteProgrammingLanguageById(int languageId) throws SQLException {
        String query = "DELETE FROM programming_languages WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, languageId);
            return statement.executeUpdate() > 0;
        }
    }
}
