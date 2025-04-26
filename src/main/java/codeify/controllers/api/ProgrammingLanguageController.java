package codeify.controllers.api;

import codeify.entities.ProgrammingLanguage;
import codeify.persistance.implementations.ProgrammingLanguageRepositoryImpl;
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
            List<ProgrammingLanguage> languages = programmingLanguageRepositoryImpl.getAllProgrammingLanguage();

            if (languages == null) {
                return ResponseEntity.status(500).body("Retrieval of languages has failed.");
            }

            if (languages.isEmpty()){
                return ResponseEntity.status(204).body("No programming languages has been found.");
            }

            if (languages.contains(null)) {
                return ResponseEntity.status(500).body("Retrieval of languages has failed: List contains null values.");
            }

            return ResponseEntity.ok(languages);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving languages: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error retrieving languages: " + e.getMessage());
        }
    }

    /**
     * Get programming language by ID
     *
     * @param id ID of the programming language
     * @return Programming language object
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> showLanguageById(@PathVariable int id){
        try{
            ProgrammingLanguage language = programmingLanguageRepositoryImpl.getProgrammingLanguageById(id);

            if (language == null) {
                return ResponseEntity.status(500).body("Retrieval of language has failed.");
            }

            if (language.equals(null)) {
                return ResponseEntity.status(500).body("Retrieval of language has failed: Language contains null values.");
            }

            return ResponseEntity.ok(language);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error retrieving language: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/add")
    public ResponseEntity<?> addLanguage(@RequestBody ProgrammingLanguage language){
        try{
            boolean success = programmingLanguageRepositoryImpl.addProgrammingLanguage(language);

            if (!success) {
                return ResponseEntity.status(500).body("Adding language has failed.");
            }

            return ResponseEntity.ok("Language has been added successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error adding language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error adding language: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/update")
    public ResponseEntity<?> updateLanguage(@RequestBody ProgrammingLanguage language){
        try{
            boolean success = programmingLanguageRepositoryImpl.updateProgrammingLanguage(language);

            if (!success) {
                return ResponseEntity.status(500).body("Updating language has failed.");
            }

            return ResponseEntity.ok("Language has been updated successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error updating language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error updating language: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteLanguageById(@RequestParam int id){
        try{
            boolean success = programmingLanguageRepositoryImpl.deleteProgrammingLanguageById(id);

            if (!success) {
                return ResponseEntity.status(500).body("Deleting language has failed.");
            }

            return ResponseEntity.ok("Language has been deleted successfully.");
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error deleting language: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error deleting language: " + e.getMessage());
        }
    }
}
