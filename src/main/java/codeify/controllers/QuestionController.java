package codeify.controllers;

import codeify.model.Question;
import codeify.persistance.QuestionRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/question/")
@CrossOrigin(origins = "http://localhost:3005")
public class QuestionController {

    @Autowired
    private QuestionRepositoryImpl questionRepositoryImpl;

    /**
     * Shows all questions for the selected language or the first available language.
     */
    @GetMapping("/questions")
    public ResponseEntity<?> showQuestions(@RequestParam(value = "languageId") Integer lanaguageId){
        try{
            List<Question> questions;
            if (lanaguageId == null){
                return ResponseEntity.badRequest().body("Id of language cannot be null");
            } else {
                questions = questionRepositoryImpl.getQuestionByLanguage(lanaguageId);
            }
            return ResponseEntity.ok(questions);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving questions: " + e.getMessage());
        }
    }

    @GetMapping("/question/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable("id") Integer questionId) {
        try {
            if (questionId == null) {
                return ResponseEntity.badRequest().body("Question ID cannot be null");
            }

            Question question = questionRepositoryImpl.getQuestionById(questionId);

            if (question == null) {
                return ResponseEntity.status(404).body("Question not found");
            }

            return ResponseEntity.ok(question);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving question: " + e.getMessage());
        }
    }
}