package codeify.controllers;

import codeify.business.Question;
import codeify.business.ProgrammingLanguage;
import codeify.persistance.QuestionDao;
import codeify.persistance.QuestionDaoImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.List;

@Slf4j
@Controller
public class QuestionController {
    private final QuestionDao questionDao;

    public QuestionController() {
        this.questionDao = new QuestionDaoImpl("database.properties");
    }

    /**
     * Shows all questions for the selected language or the first available language.
     */
    @GetMapping("/questions")
    public String showQuestions(
            @RequestParam(name = "languageId", required = false) Integer languageId,
            Model model) {
        try {
            List<ProgrammingLanguage> languages = questionDao.getAllProgrammingLanguages();
            model.addAttribute("languages", languages);

            // If languageId is provided, use it otherwise use the first language tjats available
            int selectedLanguageId = languageId != null ? languageId :
                    (!languages.isEmpty() ? languages.get(0).getId() : 0);

            if (selectedLanguageId > 0) {
                List<Question> questions = questionDao.getQuestionsByLanguage(selectedLanguageId);
                model.addAttribute("questions", questions);
                model.addAttribute("selectedLanguage",
                        languages.stream()
                                .filter(l -> l.getId() == selectedLanguageId)
                                .findFirst()
                                .orElse(null));
            }

            return "questions";
        } catch (SQLException e) {
            log.error("Error loading questions: {}", e.getMessage());
            model.addAttribute("errMsg", "Error loading questions");
            return "error";
        }
    }
}