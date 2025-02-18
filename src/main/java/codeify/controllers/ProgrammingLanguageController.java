package codeify.controllers;

import codeify.model.ProgrammingLanguage;
import codeify.persistance.ProgrammingLanguageRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("api/programming_language/")
@CrossOrigin(origins = "http://localhost:3005")
public class ProgrammingLanguageController {

    @Autowired
    private ProgrammingLanguageRepositoryImpl programmingLanguageRepositoryImpl;

    @GetMapping("/programming_languages")
    public ResponseEntity<?> showLanguages(){
        try{
            List<ProgrammingLanguage> languages = programmingLanguageRepositoryImpl.getAllProgrammingLanguage();

            if (languages.isEmpty()){
                return ResponseEntity.status(204).body("No programming languages has been found.");
            }

            return ResponseEntity.ok(languages);
        } catch (SQLException e) {
            return ResponseEntity.status(500).body("Error retrieving languages: " + e.getMessage());
        }
    }
}
