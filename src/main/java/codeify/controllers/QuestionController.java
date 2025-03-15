package codeify.controllers;

import codeify.entities.GradeRequest;
import codeify.entities.Question;
import codeify.persistance.QuestionRepositoryImpl;
import codeify.persistance.UserProgressRepositoryImpl;
import codeify.service.AiEvaluationService;
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
     * Grades a user's answer by evaluating it using the AI service and returns the result.
     *
     * The method receives a JSON payload containing the question ID and the user's answer, retrieves the
     * corresponding question from the database, and calls the AI evaluation service to get a grade and feedback.
     * It then parses the grade to an integer (defaulting to 0 if parsing fails) and builds a JSON response
     * with the grade, AI-generated feedback, and a success message.
     *
     *
     * @param gradeRequest a GradeRequest object containing the question ID and the user's answer
     * @param session      the HTTP session used to retrieve the logged-in user
     * @return a ResponseEntity containing a JSON object with keys "grade", "feedback", and "message", or
     *         an error message if the grading process fails
     */
    @PostMapping("/grade")
    public ResponseEntity<?> gradeAnswer(@RequestBody GradeRequest gradeRequest, HttpSession session) {
        // Retrieve the logged-in user from the session.
        /*Object userObj = session.getAttribute("loggedInUser");
        if (userObj == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        int userId = ((codeify.entities.User) userObj).getUserId();*/

        try {
            // Validate input.
            if (gradeRequest.getQuestionId() == null) {
                return ResponseEntity.badRequest().body("Question ID cannot be null");
            }

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

            // Update the user_progress table.
            // boolean updated = userProgressRepositoryImpl.updateUserProgress(userId, gradeRequest.getQuestionId(), grade);
            // if (!updated) {
            //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            //             .body("Failed to update user progress");
            // }

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