package codeify.service;

import codeify.entities.Question;
import java.util.Map;

public interface AiEvaluationService {
    Map<String, String> evaluateAnswer(Question question, String answer);
}