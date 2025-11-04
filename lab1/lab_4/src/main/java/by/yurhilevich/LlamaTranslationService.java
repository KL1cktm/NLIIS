package by.yurhilevich;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LlamaTranslationService {

    @Value("${llama.api.url}")
    private String llamaApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String translate(String textToTranslate) {

        // Промпт, заточенный под ваш Вариант 8 [cite: 37]
        String prompt = String.format(
                "Translate the following English text to Russian. " +
                        "The text domain is 'scientific articles on medicine' or 'critique of visual art objects'. " +
                        "Provide ONLY the Russian translation, without any explanations or preambles.\n\n" +
                        "ENGLISH TEXT:\n\"%s\"\n\nRUSSIAN TRANSLATION:",
                textToTranslate
        );

        // Структура JSON-запроса (для Ollama). Адаптируйте, если у вас другой API.
        Map<String, Object> requestBody = Map.of(
                "model", "llama3:8b",
                "prompt", prompt,
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(llamaApiUrl, entity, Map.class);

            // Парсим JSON-ответ (для Ollama)
            if (response.getBody() != null && response.getBody().containsKey("response")) {
                // Убираем лишние кавычки, которые Llama 3 любит добавлять
                return ((String) response.getBody().get("response")).trim().replaceAll("^\"|\"$", "");
            }
            return "ОШИБКА: Не удалось разобрать ответ от Llama 3.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ОШИБКА: Не удалось подключиться к Llama 3 API по адресу " + llamaApiUrl;
        }
    }
}