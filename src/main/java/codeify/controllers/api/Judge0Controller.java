package codeify.controllers.api;
/*references:
https://www.youtube.com/watch?v=Tg4_Zyyx-QA&t=14003s
https://www.youtube.com/watch?v=uOlP7njiaCg&t=46s
https://ce.judge0.com/
https://www.youtube.com/watch?v=6nkNUDNhSYI
 */
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.persistance.implementations.UserProgressRepositoryImpl;
import codeify.service.implementations.Judge0Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/execute")
@CrossOrigin(origins = "http://localhost:3000")
public class Judge0Controller {

    @Autowired
    private Judge0Service judge0Service;

    @Autowired
    private QuestionRepositoryImpl questionRepository;

    @Autowired
    private UserProgressRepositoryImpl userProgressRepository;

    @PostMapping
    public ResponseEntity<?> executeCode(@RequestBody CodeExecutionRequest request) {
        try {
            String language = request.getLanguage().toLowerCase();
            String code = request.getCode();
            Integer questionId = request.getQuestionId(); // Add questionId to the request

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && questionId != null) {
                User user = (User) auth.getPrincipal();
                try {
                    Question question = questionRepository.getQuestionById(questionId);
                    if (question != null) {
                        userProgressRepository.updateUserProgress(user.getUserId(), questionId, 10);
                    }
                } catch (Exception e) {
                    System.err.println("Error tracking progress: " + e.getMessage());
                }
            }

            String output = judge0Service.executeCode(code, language);

            Map<String, String> response = new HashMap<>();
            response.put("output", output);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error executing code: " + e.getMessage());
        }
    }

    public static class CodeExecutionRequest {
        private String code;
        private String language;
        private Integer questionId;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Integer getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Integer questionId) {
            this.questionId = questionId;
        }
    }
}