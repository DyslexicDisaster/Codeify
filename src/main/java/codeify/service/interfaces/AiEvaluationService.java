package codeify.service.interfaces;

import codeify.entities.Question;
import java.util.Map;

public interface AiEvaluationService {
    Map<String, String> evaluateAnswer(Question question, String answer);
}