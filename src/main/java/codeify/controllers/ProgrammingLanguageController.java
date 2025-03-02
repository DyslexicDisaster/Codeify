package codeify.controllers;

import codeify.model.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/programming_language/")
@CrossOrigin(origins = "http://localhost:3005")
public class ProgrammingLanguageController {

    // Injects the repository
    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

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
}
