package codeify.service.implementations;

import codeify.entities.*;
import codeify.persistance.implementations.ProgrammingLanguageRepositoryImpl;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.persistance.implementations.UserProgressRepositoryImpl;
import codeify.persistance.implementations.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private UserRepositoryImpl userRepository;

    @Autowired
    private QuestionRepositoryImpl questionRepository;

    @Autowired
    private UserProgressRepositoryImpl userProgressRepository;

    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepository;

    /**
     * Get statistics for the admin dashboard
     *
     * @return A map containing all statistics
     * @throws SQLException if there's a database error
     */
    public Map<String, Object> getAdminStatistics() throws SQLException {
        Map<String, Object> statistics = new HashMap<>();

        statistics.put("userStats", getUserStatistics());

        statistics.put("questionStats", getQuestionStatistics());

        statistics.put("performanceStats", getPerformanceStatistics());

        statistics.put("weeklyActivity", getWeeklyActivity());

        return statistics;
    }

    private Map<String, Object> getUserStatistics() throws SQLException {
        Map<String, Object> userStats = new HashMap<>();

        List<User> allUsers = userRepository.getAllUsers();
        userStats.put("totalUsers", allUsers.size());

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        long newUsersThisMonth = allUsers.stream()
                .filter(user -> user.getRegistrationDate() != null &&
                        !user.getRegistrationDate().isBefore(firstDayOfMonth))
                .count();
        userStats.put("newUsersThisMonth", newUsersThisMonth);

        long activeUsers = allUsers.stream()
                .filter(user -> user.getRegistrationDate() != null &&
                        !user.getRegistrationDate().isBefore(LocalDate.now().minusDays(30)))
                .count();
        userStats.put("activeUsers", activeUsers);

        Map<String, Integer> roleCount = new HashMap<>();
        for (User user : allUsers) {
            String roleName = user.getRole().name();
            roleCount.put(roleName, roleCount.getOrDefault(roleName, 0) + 1);
        }
        userStats.put("userRoles", roleCount);

        return userStats;
    }


    private Map<String, Object> getQuestionStatistics() throws SQLException {
        Map<String, Object> questionStats = new HashMap<>();

        List<Question> allQuestions = questionRepository.getQuestions();
        questionStats.put("totalQuestions", allQuestions.size());

        List<Map<String, Object>> questionsByLanguage = new ArrayList<>();
        List<ProgrammingLanguage> languages = programmingLanguageRepository.getAllProgrammingLanguage();

        for (ProgrammingLanguage language : languages) {
            List<Question> questions = questionRepository.getQuestionByLanguage(language.getId());
            Map<String, Object> langData = new HashMap<>();
            langData.put("name", language.getName());
            langData.put("value", questions.size());
            questionsByLanguage.add(langData);
        }
        questionStats.put("questionsByLanguage", questionsByLanguage);

        Map<String, Long> difficultyCount = allQuestions.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getDifficulty().name(),
                        Collectors.counting()
                ));

        List<Map<String, Object>> questionsByDifficulty = new ArrayList<>();
        for (Map.Entry<String, Long> entry : difficultyCount.entrySet()) {
            Map<String, Object> diffData = new HashMap<>();
            diffData.put("name", entry.getKey());
            diffData.put("value", entry.getValue());
            questionsByDifficulty.add(diffData);
        }
        questionStats.put("questionsByDifficulty", questionsByDifficulty);

        List<Map<String, Object>> mostAttemptedQuestions = userProgressRepository.getMostAttemptedQuestions();
        questionStats.put("mostAttemptedQuestions", mostAttemptedQuestions);

        List<Map<String, Object>> lowestCompletionRateQuestions = userProgressRepository.getLowestCompletionRateQuestions();
        questionStats.put("lowestCompletionRateQuestions", lowestCompletionRateQuestions);

        return questionStats;
    }

    private Map<String, Object> getPerformanceStatistics() throws SQLException {
        Map<String, Object> performanceStats = new HashMap<>();

        int totalAttempts = userProgressRepository.getTotalAttempts();
        int completedQuestions = userProgressRepository.getTotalCompletedQuestions();

        performanceStats.put("totalAttempts", totalAttempts);
        performanceStats.put("completedQuestions", completedQuestions);

        int overallCompletionRate = totalAttempts > 0 ? (completedQuestions * 100 / totalAttempts) : 0;
        performanceStats.put("overallCompletionRate", overallCompletionRate);

        List<Map<String, Object>> avgScoreByLanguage = getAverageScoreByLanguage();
        performanceStats.put("averageScoreByLanguage", avgScoreByLanguage);

        List<Map<String, Object>> avgScoreByDifficulty = getAverageScoreByDifficulty();
        performanceStats.put("averageScoreByDifficulty", avgScoreByDifficulty);

        return performanceStats;
    }

    private List<Map<String, Object>> getWeeklyActivity() {
        List<Map<String, Object>> weeklyActivity = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (int i = 1; i <= 7; i++) {
            DayOfWeek day = DayOfWeek.of(i);
            Map<String, Object> dayData = new HashMap<>();

            dayData.put("day", day.getDisplayName(TextStyle.FULL, Locale.ENGLISH));


            try {
                int attempts = userProgressRepository.getAttemptsForDayOfWeek(i);
                int completions = userProgressRepository.getCompletionsForDayOfWeek(i);

                dayData.put("attempts", attempts);
                dayData.put("completions", completions);
            } catch (SQLException e) {
                dayData.put("attempts", 0);
                dayData.put("completions", 0);
            }

            weeklyActivity.add(dayData);
        }

        return weeklyActivity;
    }

    private List<Map<String, Object>> getAverageScoreByLanguage() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        List<ProgrammingLanguage> languages = programmingLanguageRepository.getAllProgrammingLanguage();

        for (ProgrammingLanguage language : languages) {
            Map<String, Object> languageData = new HashMap<>();

            languageData.put("name", language.getName());

            try {
                int score = userProgressRepository.getAverageScoreForLanguage(language.getId());
                languageData.put("score", score);
            } catch (SQLException e) {
                languageData.put("score", 75);
            }

            result.add(languageData);
        }

        return result;
    }

    private List<Map<String, Object>> getAverageScoreByDifficulty() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Question.Difficulty difficulty : Question.Difficulty.values()) {
            Map<String, Object> difficultyData = new HashMap<>();

            difficultyData.put("name", difficulty.name());

            try {
                int score = userProgressRepository.getAverageScoreForDifficulty(difficulty.name());
                difficultyData.put("score", score);
            } catch (SQLException e) {
                int defaultScore;
                switch (difficulty) {
                    case EASY:
                        defaultScore = 85;
                        break;
                    case MEDIUM:
                        defaultScore = 70;
                        break;
                    case HARD:
                        defaultScore = 55;
                        break;
                    default:
                        defaultScore = 75;
                }
                difficultyData.put("score", defaultScore);
            }

            result.add(difficultyData);
        }

        return result;
    }
}