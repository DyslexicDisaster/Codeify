package codeify.controllers;

import codeify.model.Question;
import codeify.persistance.QuestionRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/question/")
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
}