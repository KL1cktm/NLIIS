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
            prompt = "Ты — редактор-референт. Следующий текст — это набор важных предложений, вырванных из статьи. " +
                    "Твоя задача — улучшить его связность и читаемость. Удали вводные конструкции, замени местоимения (анафоры) на существительные, к которым они относятся, и внеси минимальные правки для гладкости текста. " +
                    "Не добавляй никакой новой информации и не меняй смысл. Ответь только исправленным текстом. " +
                    "ВАЖНО: Отвечай только на русском языке. Текст: \n" + fullText;
        } else {
            prompt = "You are an NLP assistant. Extract the 20-30 most important keywords and short keyphrases (N-grams) from the following text. " +
                    "Respond only with a comma-separated list. Text: \n" + fullText;
        }

        String commaSeparatedList = generate(prompt);
        return commaSeparatedList.replace(", ", "\n");
    }

    public String transformReferat(String extractedSentences, String lang) throws Exception {
        String prompt;
        if ("ru".equals(lang)) {
            prompt = "Ты — редактор-референт. Следующий текст — это набор важных предложений, вырванных из статьи. " +
                    "Твоя задача — улучшить его связность и читаемость. Удали вводные конструкции, замени местоимения (анафоры) на существительные, к которым они относятся, и внеси минимальные правки для гладкости текста. " +
                    "Не добавляй никакой новой информации и не меняй смысл. Ответь только исправленным текстом. Текст: \n" + extractedSentences;
        } else {
            prompt = "You are an editor. The following text is a set of important sentences extracted from an article. " +
                    "Your task is to improve its coherence and readability. Remove filler words, resolve anaphora (replace pronouns with the nouns they refer to), and make minimal edits for flow. " +
                    "Do not add any new information or change the meaning. Respond only with the edited text. Text: \n" + extractedSentences;
        }
        return generate(prompt);
    }
}
