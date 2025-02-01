package codeify.business;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Question {
    private int id;
    private String title;
    private String description;
    private int programmingLanguageId;
    private String programmingLanguageName;
    private QuestionType questionType;
    private DifficultyLevel difficulty;
    private String starterCode;
    private boolean aiSolutionRequired;
    private String correctAnswer;

    public enum QuestionType {
        CODING, LOGIC
    }

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}