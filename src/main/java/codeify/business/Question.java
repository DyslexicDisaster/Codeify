package codeify.business;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private int id;
    private String title;
    private String description;
    private ProgrammingLanguage programmingLanguage;
    private QuestionType questionType;
    private Difficulty difficulty;
    private String starterCode;
    private boolean aiSolutionRequired;
    private String correctAnswer;

    public enum QuestionType {
        CODING,
        LOGIC
    }

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }
}