package by.yurhilevich;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static final String MODEL_NAME = "llama3:8b";

    private final HttpClient client;

    public LlamaService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(60))
                .build();
    }

    private String generate(String prompt) throws Exception {
        String jsonBody = new JSONObject()
                .put("model", MODEL_NAME)
                .put("prompt", prompt)
                .put("stream", false)
                .toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OLLAMA_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ollama request failed: " + response.statusCode() + "\n" + response.body());
        }

        JSONObject jsonResponse = new JSONObject(response.body());
        return jsonResponse.getString("response").trim();
    }

    public String getKeywords(String fullText, String lang) throws Exception {
        String prompt;
        if ("ru".equals(lang)) {
            prompt = "Ты — ассистент по NLP. Извлеки 20-30 самых важных ключевых слов и коротких словосочетаний (N-грамм) из следующего текста. " +
                    "Не пиши ничего, кроме списка, разделенного запятыми. Текст: \n" + fullText;
        } else {
            prompt = "You are an NLP assistant. Extract the 20-30 most important keywords and short keyphrases (N-grams) from the following text. " +
                    "Respond only with a comma-separated list. Text: \n" + fullText;
        }

        String commaSeparatedList = generate(prompt);
        return commaSeparatedList.replace(", ", "\n");
    }
}
