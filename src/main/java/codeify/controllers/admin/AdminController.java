package codeify.controllers.admin;

import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.implementations.ProgrammingLanguageRepositoryImpl;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.persistance.implementations.UserRepositoryImpl;
import codeify.service.implementations.StatisticsService;
import codeify.util.passwordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static codeify.util.passwordHash.hashPassword;

@RestController
@RequestMapping("/admin/")
@CrossOrigin(origins = "http://localhost:3000")
public class AdminController {

    @Autowired
    private UserRepositoryImpl userRepositoryImpl;

    @Autowired
    private QuestionRepositoryImpl questionRepositoryImpl;

    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

    /**
     * Get all users
     * @return List of users
     */
    @GetMapping("/get_all_users")
    public ResponseEntity<?> getAllUsers() {
        try{
            List<User> users = userRepositoryImpl.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Add a new user
     * @param user User object
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/add_user")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        try{
            if (user.getRegistrationDate() == null) {
                user.setRegistrationDate(LocalDate.now());
            }

            if (user.getProvider() == null) {
                user.setProvider("local");
            }
            boolean added = userRepositoryImpl.register(user);
            if(added){
                return ResponseEntity.ok("User added successfully");
            } else {
                return ResponseEntity.badRequest().body("User not added");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Delete a user by ID
     * @param id User ID
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try{
            boolean deleted = userRepositoryImpl.deleteUserById(id);
            if(deleted){
                return ResponseEntity.ok("User deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("User not deleted");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Update user information
     * @param updatedUser User object with updated information
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/update_user")
    public ResponseEntity<String> updateUser(@RequestBody User updatedUser) {
        try {
            Optional<User> existingUserOpt = userRepositoryImpl.getUserById(updatedUser.getUserId());
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User existingUser = existingUserOpt.get();
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            String newSalt = passwordHash.generateSalt();

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                existingUser.setPassword(newSalt + ":" + hashPassword(updatedUser.getPassword(), newSalt));
            }

            existingUser.setRole(updatedUser.getRole());

            boolean updated = userRepositoryImpl.updateUser(existingUser);
            if (updated) {
                return ResponseEntity.ok("User updated successfully");
            } else {
                return ResponseEntity.badRequest().body("User not updated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Change user role
     * @param id User ID
     * @param role New role to assign
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/change_role/{id}")
    public ResponseEntity<String> changeRole(@PathVariable int id, @RequestParam String role) {
        try{
            boolean updated = userRepositoryImpl.changeRole(id, codeify.entities.role.valueOf(role));
            if(updated){
                return ResponseEntity.ok("Role changed successfully");
            } else {
                return ResponseEntity.badRequest().body("Role not changed");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Reset user password
     * @param id User ID
     * @param password New password
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/reset_password/{id}")
    public ResponseEntity<String> resetPassword(@PathVariable int id, @RequestParam String password) {
        try{
            Optional<User> user = userRepositoryImpl.getUserById(id);
            String newSalt = passwordHash.generateSalt();
            boolean updated = userRepositoryImpl.resetPassword(id, newSalt + ":" + hashPassword(password, newSalt));
            if(updated){
                return ResponseEntity.ok("Password reset successfully");
            } else {
                return ResponseEntity.badRequest().body("Password not reset");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get all questions
     * @return List of questions
     */
    @GetMapping("/get_all_questions")
    public ResponseEntity<?> getAllQuestions() {
        try {
            List<Question> questions = questionRepositoryImpl.getQuestions();

            // Check if the list is empty
            if (questions.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            // Ensure all questions have programming language info
            for (Question question : questions) {
                if (question.getProgrammingLanguage() == null || question.getProgrammingLanguage().getId() == 0) {
                    try {
                        ProgrammingLanguage language = programmingLanguageRepositoryImpl.getProgrammingLanguageById(
                                question.getProgrammingLanguage().getId());
                        question.setProgrammingLanguage(language);
                    } catch (Exception e) {
                        ProgrammingLanguage defaultLang = new ProgrammingLanguage();
                        defaultLang.setId(0);
                        defaultLang.setName("Unknown");
                        question.setProgrammingLanguage(defaultLang);
                    }
                }
            }

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching questions: " + e.getMessage());
        }
    }


    /**
     * Get question by ID
     * @param id Question ID
     * @return ResponseEntity with question details or error message
     */
    @GetMapping("/question/{id}")
    public ResponseEntity<?> getQuestionById(@PathVariable int id) {
        try {
            Question question = questionRepositoryImpl.getQuestionById(id);
            if (question != null) {
                return ResponseEntity.ok(question);
            } else {
                return ResponseEntity.status(404).body("Question not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get all questions by language
     * @param languageId ID of the language
     * @return List of questions for the specified language
     */
    @GetMapping("/get_questions_by_language/{languageId}")
    public ResponseEntity<?> getQuestionsByLanguage(@PathVariable int languageId) {
        try {
            List<Question> questions;
            if (languageId > 0) {
                questions = questionRepositoryImpl.getQuestionByLanguage(languageId);
            } else {
                questions = questionRepositoryImpl.getQuestions();
            }

            if (questions.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching questions: " + e.getMessage());
        }
    }

    /**
     * Add a new question
     * @param question Question object
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/add_question")
    public ResponseEntity<String> addQuestion(@RequestBody Question question) {
        try {
            boolean added = questionRepositoryImpl.addQuestion(question);
            if (added) {
                return ResponseEntity.ok("Question added successfully");
            } else {
                return ResponseEntity.badRequest().body("Question not added");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Update an existing question
     * @param question Question object with updated information
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/update_question")
    public ResponseEntity<String> updateQuestion(@RequestBody Question question) {
        try {
            boolean updated = questionRepositoryImpl.updateQuestion(question);
            if (updated) {
                return ResponseEntity.ok("Question updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Question not updated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Delete a question by ID
     * @param id Question ID
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/delete_question/{id}")
    public ResponseEntity<String> deleteQuestion(@PathVariable int id) {
        try {
            boolean deleted = questionRepositoryImpl.deleteQuestion(id);
            if (deleted) {
                return ResponseEntity.ok("Question deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("Question not deleted");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get all programming languages
     * @return List of programming languages
     */
    @GetMapping("/programming_languages")
    public ResponseEntity<?> getAllProgrammingLanguages() {
        try {
            List<ProgrammingLanguage> languages = programmingLanguageRepositoryImpl.getAllProgrammingLanguage();
            return ResponseEntity.ok(languages);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Get programming language by ID
     * @param id Programming language ID
     * @return ResponseEntity with programming language details or error message
     */
    @GetMapping("/programming_language/{id}")
    public ResponseEntity<?> getProgrammingLanguageById(@PathVariable int id) {
        try {
            ProgrammingLanguage language = programmingLanguageRepositoryImpl.getProgrammingLanguageById(id);
            if (language != null) {
                return ResponseEntity.ok(language);
            } else {
                return ResponseEntity.status(404).body("Programming language not found");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Add a new programming language
     * @param language ProgrammingLanguage object
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/add_programming_language")
    public ResponseEntity<String> addProgrammingLanguage(@RequestBody ProgrammingLanguage language) {
        try {
            boolean added = programmingLanguageRepositoryImpl.addProgrammingLanguage(language);
            if (added) {
                return ResponseEntity.ok("Programming language added successfully");
            } else {
                return ResponseEntity.badRequest().body("Programming language not added");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Update an existing programming language
     * @param language ProgrammingLanguage object with updated information
     * @return ResponseEntity with success or error message
     */
    @PutMapping("/update_programming_language")
    public ResponseEntity<String> updateProgrammingLanguage(@RequestBody ProgrammingLanguage language) {
        try {
            boolean updated = programmingLanguageRepositoryImpl.updateProgrammingLanguage(language);
            if (updated) {
                return ResponseEntity.ok("Programming language updated successfully");
            } else {
                return ResponseEntity.badRequest().body("Programming language not updated");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    /**
     * Delete a programming language by ID
     * @param id Programming language ID
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/delete_programming_language/{id}")
    public ResponseEntity<String> deleteProgrammingLanguage(@PathVariable int id) {
        try {
            boolean deleted = programmingLanguageRepositoryImpl.deleteProgrammingLanguageById(id);
            if (deleted) {
                return ResponseEntity.ok("Programming language deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("Programming language not deleted");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @Autowired
    private StatisticsService statisticsService;

    /**
     * Get admin statistics
     * @return ResponseEntity with statistics data
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics() {
        try {
            Map<String, Object> statistics = statisticsService.getAdminStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching statistics: " + e.getMessage());
        }
    }
}
