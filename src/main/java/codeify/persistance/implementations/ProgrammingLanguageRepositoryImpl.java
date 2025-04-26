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

    @Override
    public List<ProgrammingLanguage> getAllProgrammingLanguage() throws SQLException {

        List<ProgrammingLanguage> languageList = new ArrayList<>();

        String query = "SELECT * FROM programming_languages ORDER BY name";


        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {


                ProgrammingLanguage language = new ProgrammingLanguage();
                language.setId(resultSet.getInt("id"));
                language.setName(resultSet.getString("name"));

                languageList.add(language);
            }
        }
        return languageList;
    }

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

    @Override
    public boolean addProgrammingLanguage(ProgrammingLanguage language) throws SQLException {
        String query = "INSERT INTO programming_languages (name) VALUES (?)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, language.getName());
            return statement.executeUpdate() > 0;
        }
    }

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
