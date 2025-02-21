package codeify.repository;

import codeify.model.ProgrammingLanguage;
import codeify.model.Question;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.List;

@Repository
public interface QuestionRepository {
    List<Question> getQuestionByLanguage(int languageId) throws SQLException;
    Question getQuestionById(int questionId) throws SQLException;
}
