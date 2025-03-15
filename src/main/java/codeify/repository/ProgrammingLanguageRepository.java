package codeify.repository;

import codeify.entities.ProgrammingLanguage;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface ProgrammingLanguageRepository {
    List<ProgrammingLanguage> getAllProgrammingLanguage() throws SQLException;
}
