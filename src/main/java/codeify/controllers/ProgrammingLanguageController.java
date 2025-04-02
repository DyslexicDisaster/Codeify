package codeify.controllers;

import codeify.entities.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/programming_language/")
@CrossOrigin(origins = "http://localhost:3000")
public class ProgrammingLanguageController {

    // Injects the repository
    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/programming_languages")
    public ResponseEntity<?> showLanguages(){
        try{
            // Retrieve all programming languages
            List<ProgrammingLanguage> languages = programmingLanguageRepositoryImpl.getAllProgrammingLanguage();

            // Checks if the list is null
            if (languages == null) {
                return ResponseEntity.status(500).body("Retrieval of languages has failed.");
            }

            // Checks if the list is empty
            if (languages.isEmpty()){
                return ResponseEntity.status(204).body("No programming languages has been found.");
            }

            // Checks if the list contains null values
            if (languages.contains(null)) {
                return ResponseEntity.status(500).body("Retrieval of languages has failed: List contains null values.");
            }

            // Returns the list of programming languages
            return ResponseEntity.ok(languages);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving languages: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error retrieving languages: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> showLanguageById(@PathVariable int id){
        try{
            // Retrieve programming language by id
            ProgrammingLanguage language = programmingLanguageRepositoryImpl.getProgrammingLanguageById(id);

            // Checks if the language is null
            if (language == null) {
                return ResponseEntity.status(500).body("Retrieval of language has failed.");
            }

            // Checks if the language contains null values
            if (language.equals(null)) {
                return ResponseEntity.status(500).body("Retrieval of language has failed: Language contains null values.");
            }

            // Returns the programming language
            return ResponseEntity.ok(language);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error retrieving language: " + e.getMessage());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addLanguage(@RequestBody ProgrammingLanguage language){
        try{
            // Add programming language
            boolean success = programmingLanguageRepositoryImpl.addProgrammingLanguage(language);

            // Checks if the language has been added
            if (!success) {
                return ResponseEntity.status(500).body("Adding language has failed.");
            }

            // Returns the success message
            return ResponseEntity.ok("Language has been added successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error adding language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error adding language: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateLanguage(@RequestBody ProgrammingLanguage language){
        try{
            // Update programming language
            boolean success = programmingLanguageRepositoryImpl.updateProgrammingLanguage(language);

            // Checks if the language has been updated
            if (!success) {
                return ResponseEntity.status(500).body("Updating language has failed.");
            }

            // Returns the success message
            return ResponseEntity.ok("Language has been updated successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error updating language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error updating language: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteLanguageById(@PathVariable int id){
        try{
            // Delete programming language by id
            boolean success = programmingLanguageRepositoryImpl.deleteProgrammingLanguageById(id);

            // Checks if the language has been deleted
            if (!success) {
                return ResponseEntity.status(500).body("Deleting language has failed.");
            }

            // Returns the success message
            return ResponseEntity.ok("Language has been deleted successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error deleting language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error deleting language: " + e.getMessage());
        }
    }
}
