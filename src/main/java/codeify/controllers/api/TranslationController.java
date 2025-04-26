// src/main/java/codeify/controllers/api/TranslationController.java
package codeify.controllers.api;

import codeify.service.implementations.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @PostMapping
    public ResponseEntity<?> translate(@RequestBody Map<String,String> req) {
        String text       = req.get("text");
        String targetLang = req.get("targetLang");
        if (text == null || targetLang == null) {
            return ResponseEntity.badRequest().body("Missing text or targetLang");
        }
        String translated = translationService.translate(text, targetLang);
        return ResponseEntity.ok(Map.of("translatedText", translated));
    }
}
