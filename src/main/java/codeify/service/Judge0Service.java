package codeify.service;
/*references:
https://www.youtube.com/watch?v=Tg4_Zyyx-QA&t=14003s
https://www.youtube.com/watch?v=uOlP7njiaCg&t=46s
https://ce.judge0.com/
https://www.youtube.com/watch?v=6nkNUDNhSYI
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    @Value("${judge0.api.url:https://judge0-ce.p.rapidapi.com}")
    private String judge0ApiUrl;

    @Value("${judge0.api.key:}")
    private String judge0ApiKey;

    @Value("${judge0.api.host:judge0-ce.p.rapidapi.com}")
    private String judge0ApiHost;

    private final RestTemplate restTemplate = new RestTemplate();

    // Language IDs for Judge0
    private static final Map<String, Integer> LANGUAGE_IDS = new HashMap<>();
    static {
        LANGUAGE_IDS.put("javascript", 63);
        LANGUAGE_IDS.put("java", 62);
        LANGUAGE_IDS.put("mysql", 82);
    }

    public String executeCode(String code, String language) {
        try {
            Integer languageId = LANGUAGE_IDS.get(language.toLowerCase());
            if (languageId == null) {
                return "Error: Unsupported language: " + language;
            }

            // java needs pre processing due to publiuc class errors
            if ("java".equalsIgnoreCase(language)) {
                code = preprocessJavaCode(code);
            }

            if ("mysql".equalsIgnoreCase(language)) {
                code = preprocessMySQLCode(code);
            }

            // Create the submission
            String jsonBody = String.format("{\"source_code\":\"%s\",\"language_id\":%d}",
                    escapeJsonString(code), languageId);

            // Add headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", judge0ApiKey);
            headers.set("X-RapidAPI-Host", judge0ApiHost);
            headers.set("Content-Type", "application/json");

            String endpoint = "mysql".equalsIgnoreCase(language)
                    ? judge0ApiUrl + "/submissions?wait=true&wait_time=10"
                    : judge0ApiUrl + "/submissions?wait=true";

            // Submit code
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    judge0ApiUrl + "/submissions?wait=true",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> response = responseEntity.getBody();

            if (response == null) {
                return "Error: Empty response from Judge0";
            }

            // checks for compilation errors
            if (response.containsKey("compile_output") && response.get("compile_output") != null
                    && response.get("compile_output") instanceof String) {
                String compileOutput = (String) response.get("compile_output");
                if (!compileOutput.isEmpty()) {
                    return "Compilation error: \n" + compileOutput;
                }
            }

            // Check for stderr
            if (response.containsKey("stderr") && response.get("stderr") != null
                    && response.get("stderr") instanceof String) {
                String stderr = (String) response.get("stderr");
                if (!stderr.isEmpty()) {
                    return "Runtime error: \n" + stderr;
                }
            }

            // outpt
            String output = "";
            if (response.containsKey("stdout") && response.get("stdout") != null
                    && response.get("stdout") instanceof String) {
                output = (String) response.get("stdout");
            }

            if ("mysql".equalsIgnoreCase(language) && !output.isEmpty()) {
                output = formatMySQLOutput(output);
            }

            // If no output error message from judge0
            if (output.isEmpty() && response.containsKey("message") && response.get("message") != null) {
                return "Judge0 message: " + response.get("message").toString();
            }

            return output.isEmpty() ? "Code executed successfully with no output." : output;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Judge0 error details: " + e.getMessage());
            return "Error executing code: " + e.getMessage() +
                    "\n\nPlease check that your Judge0 API key is set correctly in application.properties.";
        }
    }

    private String preprocessMySQLCode(String code) {
        return code;
    }


    private String formatMySQLOutput(String output) {
        return output;
    }


    private String preprocessJavaCode(String code) {
        if (code.contains("public class Main") && code.contains("public static void main(String[] args)")) {
            return code;
        }
        return code;
    }

    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '\"':
                    result.append("\\\"");
                    break;
                case '\\':
                    result.append("\\\\");
                    break;
                case '\b':
                    result.append("\\b");
                    break;
                case '\f':
                    result.append("\\f");
                    break;
                case '\n':
                    result.append("\\n");
                    break;
                case '\r':
                    result.append("\\r");
                    break;
                case '\t':
                    result.append("\\t");
                    break;
                default:
                    // Control chars and non-ASCII
                    if (ch < ' ' || ch > 127) {
                        String hex = Integer.toHexString(ch);
                        result.append("\\u");
                        for (int j = hex.length(); j < 4; j++) {
                            result.append('0');
                        }
                        result.append(hex);
                    } else {
                        result.append(ch);
                    }
            }
        }
        return result.toString();
    }
}