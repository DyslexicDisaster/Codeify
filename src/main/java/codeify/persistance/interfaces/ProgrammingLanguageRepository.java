package codeify.persistance.interfaces;

import codeify.entities.ProgrammingLanguage;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface ProgrammingLanguageRepository {
    List<ProgrammingLanguage> getAllProgrammingLanguage() throws SQLException;
    ProgrammingLanguage getProgrammingLanguageById(int languageId) throws SQLException;
    boolean addProgrammingLanguage(ProgrammingLanguage language) throws SQLException;
    boolean updateProgrammingLanguage(ProgrammingLanguage language) throws SQLException;
    boolean deleteProgrammingLanguageById(int languageId) throws SQLException;
}
