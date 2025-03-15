package codeify.controllers;

import codeify.entities.ProgrammingLanguage;
import codeify.entities.Question;
import codeify.entities.User;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import codeify.persistance.QuestionRepositoryImpl;
import codeify.persistance.UserRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // Get all users
    @GetMapping("/get_all_users")
    public ResponseEntity<String> getAllUsers() {
        try{
            return ResponseEntity.ok(userRepositoryImpl.getAllUsers().toString());
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Add user
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

    // Delete user
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

    // Update user
    @PutMapping("/update_user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        try{
            boolean updated = userRepositoryImpl.updateUser(user);
            if(updated){
                return ResponseEntity.ok("User updated successfully");
            } else {
                return ResponseEntity.badRequest().body("User not updated");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Change user role
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

    // Reset user password
    @PutMapping("/reset_password/{id}")
    public ResponseEntity<String> resetPassword(@PathVariable int id, @RequestParam String password) {
        try{
            boolean updated = userRepositoryImpl.resetPassword(id, password);
            if(updated){
                return ResponseEntity.ok("Password reset successfully");
            } else {
                return ResponseEntity.badRequest().body("Password not reset");
            }
        } catch (Exception e){
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Get all questions
    @GetMapping("/get_all_questions")
    public ResponseEntity<String> getAllQuestions() {
        try {
            List<Question> questions = questionRepositoryImpl.getQuestions();
            return ResponseEntity.ok(questions.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Get question by ID
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

    // Get questions by programming language
    @GetMapping("/questions/language/{languageId}")
    public ResponseEntity<?> getQuestionsByLanguage(@PathVariable int languageId) {
        try {
            List<Question> questions = questionRepositoryImpl.getQuestionByLanguage(languageId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Add question
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

    // Update question
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

    // Delete a question
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
