package codeify.controllers;

import codeify.model.Question;
import codeify.persistance.QuestionRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * Get all questions by language
     *
     * @param languageId ID of the language
     * @return List of questions
     */
    @GetMapping("/questions")
    public ResponseEntity<?> showQuestions(@RequestParam(value = "languageId") Integer languageId){
        try{
            List<Question> questions;
            if (languageId == null){
                return ResponseEntity.badRequest().body("ID of language cannot be null");
            } else {
                questions = questionRepositoryImpl.getQuestionByLanguage(languageId);
            }

            if (questions.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error retrieving questions: List is empty");
            }

            return ResponseEntity.ok(questions);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving questions: " + e.getMessage());
        }
    }
}