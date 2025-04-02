package codeify.service;

import codeify.entities.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

public class AiEvaluationServiceImplTest {

    //References: https://www.baeldung.com/integration-testing-a-rest-api
    private AiEvaluationServiceImpl service;
    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        service = new AiEvaluationServiceImpl();
        restTemplate = new RestTemplate();
        service.setRestTemplate(restTemplate);
        // Create the mock server to intercept requests made by restTemplate.
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void testEvaluateAnswerSuccess() {
        // Prepare a fake API response with proper delimiter.
        String apiResponse = "{\"choices\": [{\"message\": {\"content\": \"85||Excellent job on your answer!\"}}]}";

        // Expect a POST request to the AI API endpoint and respond with our fake response.
        mockServer.expect(requestTo("https://api.deepseek.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(apiResponse, MediaType.APPLICATION_JSON));

        // Create a sample question and answer.
        Question question = new Question();
        question.setTitle("Sample Question");
        question.setDescription("What is the output of 2+2?");
        String answer = "4";

        // Call the method.
        Map<String, String> result = service.evaluateAnswer(question, answer);

        // Verify the response.
        assertEquals("85", result.get("grade"));
        assertEquals("Excellent job on your answer!", result.get("feedback"));

        // Verify that the expected request was made.
        mockServer.verify();
    }

    @Test
    public void testEvaluateAnswerEmptyResponse() {
        // Prepare a fake API response with no choices.
        String apiResponse = "{\"choices\": []}";
        mockServer.expect(requestTo("https://api.deepseek.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(apiResponse, MediaType.APPLICATION_JSON));

        // Sample question and answer.
        Question question = new Question();
        question.setTitle("Sample Question");
        question.setDescription("What is the output of 2+2?");
        String answer = "4";

        Map<String, String> result = service.evaluateAnswer(question, answer);

        // Expect default values.
        assertEquals("0", result.get("grade"));
        assertEquals("Unable to evaluate answer.", result.get("feedback"));

        mockServer.verify();
    }

    @Test
    public void testEvaluateAnswerUnexpectedFormat() {
        // Prepare a fake API response where the AI returns a grade without delimiter.
        String apiResponse = "{\"choices\": [{\"message\": {\"content\": \"90\"}}]}";
        mockServer.expect(requestTo("https://api.deepseek.com/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(apiResponse, MediaType.APPLICATION_JSON));

        Question question = new Question();
        question.setTitle("Sample Question");
        question.setDescription("What is the output of 2+2?");
        String answer = "4";

        Map<String, String> result = service.evaluateAnswer(question, answer);

        // Expect the entire output to be used as grade and no feedback.
        assertEquals("90", result.get("grade"));
        assertEquals("", result.get("feedback"));

        mockServer.verify();
    }
}
