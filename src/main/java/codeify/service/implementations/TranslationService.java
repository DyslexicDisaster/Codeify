package codeify.service.implementations;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TranslationService {
    private final RestTemplate rest = new RestTemplate();
    private final String url = "https://libretranslate.com/translate";

    public String translate(String text, String targetLang) {
        // build payload
        Map<String,String> payload = Map.of(
                "q",            text,
                "source",       "en",
                "target",       targetLang,
                "format",       "text"
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,String>> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> resp = rest.postForEntity(url, req, Map.class);
        if (resp.getStatusCode() == HttpStatus.OK && resp.getBody()!=null) {
            return (String) resp.getBody().get("translatedText");
        }
        throw new RuntimeException("Translation failed: " + resp.getStatusCode());
    }
}