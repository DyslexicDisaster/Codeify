package codeify.controllers;

import codeify.model.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/programming_language/")
public class ProgrammingLanguageController {

    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

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
}
