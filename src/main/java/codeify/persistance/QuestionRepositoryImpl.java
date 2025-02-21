package codeify.persistance;

import codeify.model.ProgrammingLanguage;
import codeify.model.Question;
import codeify.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class QuestionRepositoryImpl implements QuestionRepository {

    @Autowired
    private DataSource dataSource;

    @Override
    public List<Question> getQuestionByLanguage(int languageId) throws SQLException {
        List<Question> questionList = new ArrayList<>();
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE pl.id = ? ORDER BY q.difficulty, q.id";

        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)){

            statement.setInt(1, languageId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()){
                    Question question = new Question();
                    question.setId(resultSet.getInt("id"));
                    question.setTitle(resultSet.getString("title"));
                    question.setDescription(resultSet.getString("description"));

                    ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
                    programmingLanguage.setId(resultSet.getInt("programming_language_id"));
                    question.setProgrammingLanguage(programmingLanguage);

                    // Sets enums
                    question.setQuestionType(Question.QuestionType.valueOf(resultSet.getString("question_type")));
                    question.setDifficulty(Question.Difficulty.valueOf(resultSet.getString("difficulty")));

                    question.setStarterCode(resultSet.getString("starter_code"));
                    question.setAiSolutionRequired(resultSet.getBoolean("ai_solution_required"));
                    question.setCorrectAnswer(resultSet.getString("correct_answer"));
                    question.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                    questionList.add(question);
                }
            }
        }
        return questionList;
    }

    @Override
    public Question getQuestionById(int questionId) throws SQLException {
        String query = "SELECT q.*, pl.name as language_name FROM questions q JOIN programming_languages pl ON q.programming_language_id = pl.id WHERE q.id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, questionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Question question = new Question();
                    question.setId(resultSet.getInt("id"));
                    question.setTitle(resultSet.getString("title"));
                    question.setDescription(resultSet.getString("description"));

                    ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
                    programmingLanguage.setId(resultSet.getInt("programming_language_id"));
                    programmingLanguage.setName(resultSet.getString("language_name"));
                    question.setProgrammingLanguage(programmingLanguage);

                    // Sets enums
                    question.setQuestionType(Question.QuestionType.valueOf(resultSet.getString("question_type")));
                    question.setDifficulty(Question.Difficulty.valueOf(resultSet.getString("difficulty")));

                    question.setStarterCode(resultSet.getString("starter_code"));
                    question.setAiSolutionRequired(resultSet.getBoolean("ai_solution_required"));
                    question.setCorrectAnswer(resultSet.getString("correct_answer"));
                    question.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                    return question;
                }
            }
        }
        return null;
    }
}
