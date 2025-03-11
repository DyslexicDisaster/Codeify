package codeify.services;

import codeify.model.Question;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiEvaluationService {

    private RestTemplate restTemplate = new RestTemplate();

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    //Api Documentation: https://api-docs.deepseek.com/guides/reasoning_model

    private final String aiApiUrl = "https://api.deepseek.com/v1/chat/completions";
    private final String apiKey = "sk-23a76332c034413f8fdd50cec8f90174";

    public String evaluateAnswer(Question question, String answer) {
        // Construct a prompt using the question description and the user's answer.
        String prompt = "Question: " + question.getDescription() + "\n" +
                "User Answer: " + answer + "\n" +
                "Grade the answer on a scale of 0 to 100. Provide only the numeric grade.";

        // Build the request payload as a chat completion request.
        //The map represents JSON payload that will be sent in the HTTP POST
        Map<String, Object> requestPayload = new HashMap<>();
        //We choose what model of ai we want
        requestPayload.put("model", "deepseek-reasoner");

        //The API expects a list of messages so we create a single message as a map with keys role/content
        //this contains the propmpt
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        requestPayload.put("messages", List.of(message));
        //Limiting the amount of tokens we use
        requestPayload.put("max_tokens", 10);

        HttpHeaders headers = new HttpHeaders();
        //Our payload is in JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //Add api key
        headers.setBearerAuth(apiKey);

        //wrapps the payload and headers together
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        //We send it to the apiurl with entity
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(aiApiUrl, entity, Map.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            Map responseBody = responseEntity.getBody();
            // Expect the response to contain a "choices" array with a "message" object This has what we actually want
            List choices = (List) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map firstChoice = (Map) choices.get(0);
                Map messageObj = (Map) firstChoice.get("message");
                String evaluation = (String) messageObj.get("content");
                return evaluation.trim();
            }
        }
        return "0"; // Default grade if evaluation fails.
    }
    // Main method for testing
    public static void main(String[] args) {
        AiEvaluationService service = new AiEvaluationService();

        // Create a sample question object.
        Question sampleQuestion = new Question();
        sampleQuestion.setTitle("Sample Question");
        sampleQuestion.setDescription("What is the output of 2+2?");

        String userAnswer = "4";

        String evaluationResult = service.evaluateAnswer(sampleQuestion, userAnswer);
        System.out.println("Evaluation result: " + evaluationResult);
    }
}