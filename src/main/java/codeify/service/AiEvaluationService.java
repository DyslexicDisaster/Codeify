package codeify.service;

import codeify.entities.Question;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiEvaluationService {

    // RestTemplate is used to make HTTP requests.
    private RestTemplate restTemplate = new RestTemplate();

    // Setter allows us to inject a different RestTemplate during testing.
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // The API endpoint and key for DeepSeek's reasoning model.
    private final String aiApiUrl = "https://api.deepseek.com/v1/chat/completions";
    private final String apiKey = "sk-23a76332c034413f8fdd50cec8f90174";

    /**
     * Evaluates the answer using the AI API.
     * The prompt instructs the AI to return output in the format: <grade>||<feedback>
     *
     * @param question The question object containing the description.
     * @param answer   The user's answer as a string.
     * @return A Map with keys "grade" and "feedback" containing the AI's response.
     */
    public Map<String, String> evaluateAnswer(Question question, String answer) {
        // Build a prompt that includes the question description, the user's answer,
        // and instructs the AI to grade the answer and provide feedback separated by "||".
        String prompt = "Question: " + question.getDescription() + "\n" +
                "User Answer: " + answer + "\n" +
                "Grade the answer on a scale of 0 to 100 and provide a brief feedback message. " +
                "Return your response in the format: <grade>||<feedback>";

        // Prepare the request payload as a JSON object.
        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("model", "deepseek-reasoner"); // Specify the AI model.

        // Create a message object that holds the prompt.
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        // The API expects an array of messages.
        requestPayload.put("messages", List.of(message));

        // Set max_tokens to allow enough room for feedback.
        requestPayload.put("max_tokens", 100);

        // Set up HTTP headers for the request.
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // We're sending JSON.
        headers.setBearerAuth(apiKey); // Include the API key for authorization.

        // Wrap the payload and headers into an HttpEntity.
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        // Send the POST request to the AI API.
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(aiApiUrl, entity, Map.class);
        String rawResult = null;
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            Map responseBody = responseEntity.getBody();
            // Extract the "choices" array from the response.
            List choices = (List) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                // Use the first choice.
                Map firstChoice = (Map) choices.get(0);
                Map messageObj = (Map) firstChoice.get("message");
                rawResult = (String) messageObj.get("content");
            }
        }
        // If the API did not return a valid result, default to a grade of "0" and feedback.
        if (rawResult == null || rawResult.isEmpty()) {
            Map<String, String> result = new HashMap<>();
            result.put("grade", "0");
            result.put("feedback", "Unable to evaluate answer.");
            return result;
        }
        rawResult = rawResult.trim();
        // Split the result on "||" to separate the grade from the feedback.
        String[] parts = rawResult.split("\\|\\|");
        Map<String, String> result = new HashMap<>();
        if (parts.length >= 2) {
            result.put("grade", parts[0].trim());
            result.put("feedback", parts[1].trim());
        } else {
            // If the delimiter isn't found, return the entire output as the grade and leave feedback empty.
            result.put("grade", rawResult);
            result.put("feedback", "");
        }
        return result;
    }

    // Main method for testing the AI evaluation service independently.
    public static void main(String[] args) {
        AiEvaluationService service = new AiEvaluationService();

        // Create a sample question object.
        Question sampleQuestion = new Question();
        sampleQuestion.setTitle("Sample Question");
        sampleQuestion.setDescription("What is the output of 2+2?");

        String userAnswer = "4";

        // Evaluate the answer.
        Map<String, String> evaluationResult = service.evaluateAnswer(sampleQuestion, userAnswer);
        System.out.println("Grade: " + evaluationResult.get("grade"));
        System.out.println("Feedback: " + evaluationResult.get("feedback"));
    }
}