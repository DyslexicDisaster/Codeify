package codeify.persistance;

import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.repository.QuestionRepository;
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

    @Override
    public boolean addQuestion(Question question) throws SQLException {
        String query = "INSERT INTO questions (title, description, programming_language_id, question_type, difficulty, starter_code, ai_solution_required, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, question.getTitle());
            statement.setString(2, question.getDescription());
            statement.setInt(3, question.getProgrammingLanguage().getId());
            statement.setString(4, question.getQuestionType().name());
            statement.setString(5, question.getDifficulty().name());
            statement.setString(6, question.getStarterCode());
            statement.setBoolean(7, question.isAiSolutionRequired());
            statement.setString(8, question.getCorrectAnswer());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateQuestion(Question question) throws SQLException {
        String query = "UPDATE questions SET title = ?, description = ?, programming_language_id = ?, question_type = ?, difficulty = ?, starter_code = ?, ai_solution_required = ?, correct_answer = ? WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, question.getTitle());
            statement.setString(2, question.getDescription());
            statement.setInt(3, question.getProgrammingLanguage().getId());
            statement.setString(4, question.getQuestionType().name());
            statement.setString(5, question.getDifficulty().name());
            statement.setString(6, question.getStarterCode());
            statement.setBoolean(7, question.isAiSolutionRequired());
            statement.setString(8, question.getCorrectAnswer());
            statement.setInt(9, question.getId());

            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteQuestion(int questionId) throws SQLException {
        String query = "DELETE FROM questions WHERE id = ?";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, questionId);
            return statement.executeUpdate() > 0;
        }
    }

    @Override
    public List<Question> getQuestions() throws SQLException {
        List<Question> questionList = new ArrayList<>();
        String query = "SELECT * FROM questions ORDER BY title";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Question question = new Question();
                question.setId(resultSet.getInt("id"));
                question.setTitle(resultSet.getString("title"));
                question.setDescription(resultSet.getString("description"));

                ProgrammingLanguage programmingLanguage = new ProgrammingLanguage();
                programmingLanguage.setId(resultSet.getInt("programming_language_id"));
                question.setProgrammingLanguage(programmingLanguage);

                question.setQuestionType(Question.QuestionType.valueOf(resultSet.getString("question_type")));
                question.setDifficulty(Question.Difficulty.valueOf(resultSet.getString("difficulty")));

                question.setStarterCode(resultSet.getString("starter_code"));
                question.setAiSolutionRequired(resultSet.getBoolean("ai_solution_required"));
                question.setCorrectAnswer(resultSet.getString("correct_answer"));
                question.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());

                questionList.add(question);
            }
        }
        return questionList;
    }
}
