package codeify.controllers.api;

import codeify.entities.GradeRequest;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.service.implementations.LastAttemptService;
import codeify.service.interfaces.AiEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/question/")
@CrossOrigin(origins = "http://localhost:3000")
public class QuestionController {

    @Autowired
    private QuestionRepositoryImpl questionRepositoryImpl;

    @Autowired
    private AiEvaluationService aiEvaluationService;

    @Autowired
    private LastAttemptService lastAttemptService;

    /**
     * Get all questions by language
     *
     * @param languageId ID of the language
     * @return List of questions
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/questions")
    public ResponseEntity<?> showQuestions(@RequestParam(value = "languageId", required = false) Integer languageId){
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

    @PreAuthorize("isAuthenticated()")
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
     * Get the last attempt for a question by the current user
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/last-attempt/{questionId}")
    public ResponseEntity<?> getLastAttempt(@PathVariable("questionId") Integer questionId) {
        // Get the current user from the security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        User user = (User) auth.getPrincipal();
        Optional<String> lastAttempt = lastAttemptService.getLastAttempt(user.getUserId(), questionId);

        if (lastAttempt.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("code", lastAttempt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No previous attempt found");
        }
    }

    /**
     * Grades a user's answer by evaluating it using the AI service and returns the result.
     *
     * @param gradeRequest a GradeRequest object containing the question ID and the user's answer
     * @return a ResponseEntity containing a JSON object with keys "grade", "feedback", and "message"
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/grade")
    public ResponseEntity<?> gradeAnswer(@RequestBody GradeRequest gradeRequest) {
        // Retrieve the logged-in user from the SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        User user = (User) auth.getPrincipal();

        try {
            if (gradeRequest.getQuestionId() == null) {
                return ResponseEntity.badRequest().body("Question ID cannot be null");
            }

            // Save the user's last attempt
            lastAttemptService.saveLastAttempt(user.getUserId(), gradeRequest.getQuestionId(), gradeRequest.getAnswer());

            // Retrieve the question from the database.
            Question question = questionRepositoryImpl.getQuestionById(gradeRequest.getQuestionId());
            if (question == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Question not found");
            }

            // Evaluate the answer using the AI service.
            Map<String, String> evaluationResult = aiEvaluationService.evaluateAnswer(question, gradeRequest.getAnswer());
            int grade;
            try {
                grade = Integer.parseInt(evaluationResult.get("grade"));
            } catch (NumberFormatException e) {
                grade = 0;
            }

            // Build the JSON response.
            Map<String, Object> response = new HashMap<>();
            response.put("grade", grade);
            response.put("feedback", evaluationResult.get("feedback"));
            response.put("message", "Answer graded successfully");
            return ResponseEntity.ok(response);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error grading answer: " + e.getMessage());
        }
    }
}