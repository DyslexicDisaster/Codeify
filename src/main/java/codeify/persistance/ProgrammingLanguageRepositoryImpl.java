package codeify.persistance;

import codeify.model.ProgrammingLanguage;
import codeify.repository.ProgrammingLanguageRepository;
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
}
