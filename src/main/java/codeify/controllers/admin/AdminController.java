package codeify.controllers.admin;

import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.implementations.ProgrammingLanguageRepositoryImpl;
import codeify.persistance.implementations.QuestionRepositoryImpl;
import codeify.persistance.implementations.UserRepositoryImpl;
import codeify.util.passwordHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/get_all_users")
    public ResponseEntity<?> getAllUsers() {
        try{
            List<User> users = userRepositoryImpl.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /*
     * Get user by ID
     * @param id User ID
     * @return User object
     */
    @PreAuthorize("hasRole('admin')")
    @PostMapping("/add_user")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        try{
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

    /*
     * Get user by ID
     * @param id User ID
     * @return User object
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Update user
     * @param updatedUser User object with updated information
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Change user role
     * @param id User ID
     * @param role New role
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Reset user password
     * @param id User ID
     * @param password New password
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Get all questions
     * @return List of questions
     */
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/get_all_questions")
    public ResponseEntity<?> getAllQuestions() {
        try {
            List<Question> questions = questionRepositoryImpl.getQuestions();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /*
     * Get question by ID
     * @param id Question ID
     * @return Question object
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Get questions by language ID
     * @param languageId Language ID
     * @return List of questions
     */
    @PreAuthorize("hasRole('admin')")
    @GetMapping("/questions/language/{languageId}")
    public ResponseEntity<?> getQuestionsByLanguage(@PathVariable int languageId) {
        try {
            List<Question> questions = questionRepositoryImpl.getQuestionByLanguage(languageId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    /*
     * Add a new question
     * @param question Question object
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Update a question
     * @param question Question object with updated information
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Delete a question
     * @param id Question ID
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Get all programming languages
     * @return List of programming languages
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Get programming language by ID
     * @param id Programming language ID
     * @return Programming language object
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Add a new programming language
     * @param language Programming language object
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Delete a programming language
     * @param id Programming language ID
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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

    /*
     * Delete a programming language
     * @param id Programming language ID
     * @return Response message
     */
    @PreAuthorize("hasRole('admin')")
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
}
