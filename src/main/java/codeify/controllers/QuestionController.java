package codeify.controllers;

import codeify.model.Question;
import codeify.persistance.QuestionRepositoryImpl;
import codeify.persistance.UserProgressRepositoryImpl;
import codeify.services.AiEvaluationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/question/")
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {

    @Autowired
    private QuestionRepositoryImpl questionRepositoryImpl;

    @Autowired
    private AiEvaluationService aiEvaluationService;

    // Assume you have a bean for UserProgressRepositoryImpl
    @Autowired
    private UserProgressRepositoryImpl userProgressRepositoryImpl;


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

    /**
     * Grades a user's answer using an AI API and updates the user_progress table.
     *
     * @param questionId The ID of the question.
     * @param answer     The user's answer.
     * @param session    The HTTP session (used to get the logged-in user).
     * @return A JSON response containing the grade and feedback.
     */
    @PostMapping("/grade")
    public ResponseEntity<?> gradeAnswer(@RequestParam("questionId") Integer questionId,
                                         @RequestParam("answer") String answer,
                                         HttpSession session) {
        // Retrieve the currently logged in user from session.
        // (Assuming that during login you stored a User object in session under "loggedInUser")
        Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        // In a robust system, you might cast to your User model.
        // For this sample, assume the user has an ID property.
        int userId = ((codeify.model.User) userObj).getUserId();

        try {
            if (questionId == null) {
                return ResponseEntity.badRequest().body("Question ID cannot be null");
            }

            // Retrieve the question from the database.
            Question question = questionRepositoryImpl.getQuestionById(questionId);
            if (question == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
            }

            // Evaluate the answer via the AI service.
            String evaluationText = aiEvaluationService.evaluateAnswer(question, answer);
            int grade;
            try {
                grade = Integer.parseInt(evaluationText);
            } catch (NumberFormatException e) {
                // If parsing fails, default to grade 0 or return an error.
                grade = 0;
            }

            // Update the user_progress table.
            boolean updated = userProgressRepositoryImpl.updateUserProgress(userId, questionId, grade);
            if (!updated) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update user progress");
            }

            // Return the evaluation results.
            Map<String, Object> response = new HashMap<>();
            response.put("grade", grade);
            response.put("feedback", evaluationText); // or additional parsed feedback if available.
            response.put("message", "Answer graded successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error grading answer: " + e.getMessage());
        }
    }
}