package by.yurhilevich;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NeuralNetworkService implements LanguageService {

    // Внедряем URL из application.properties
    @Value("${ollama.api.url}")
    private String ollamaApiUrl;

    // RestTemplate - стандартный инструмент Spring для выполнения HTTP-запросов
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Language recognize(String text) {
        if (text == null || text.isBlank()) {
            return Language.UNKNOWN;
        }

        String prompt = buildPrompt(text);

        OllamaRequest request = new OllamaRequest("llama3:8b", prompt);

        try {
            OllamaResponse response = restTemplate.postForObject(ollamaApiUrl, request, OllamaResponse.class);

            if (response != null && response.getResponse() != null) {
                String langCode = response.getResponse().trim().toLowerCase();
                switch (langCode) {
                    case "ru":
                        return Language.RUSSIAN;
                    case "it":
                        return Language.ITALIAN;
                    default:
                        return Language.UNKNOWN;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обращении к Ollama: " + e.getMessage());
            return Language.UNKNOWN;
        }

        return Language.UNKNOWN;
    }

    private String buildPrompt(String text) {
        String truncatedText = text.length() > 1000 ? text.substring(0, 1000) : text;

        return "You are an expert language identifier. Analyze the following text and determine its language. " +
                "Your response MUST be ONLY the two-letter ISO 639-1 code of the language (e.g., 'en', 'fr', 'ru', 'it'). " +
                "Do not add any explanation or punctuation. The text is: \n\n\"" + truncatedText + "\"";
    }

    @Override
    public String getMethodName() {
        return "Модель Ollama (llama3)";
    }
}