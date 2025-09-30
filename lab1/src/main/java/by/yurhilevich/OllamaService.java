package by.yurhilevich;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class OllamaService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final String apiUrl;
    private final String modelName;

    public OllamaService(
            @Value("${ollama.api.url}") String apiUrl,
            @Value("${ollama.model.name}") String modelName
    ) {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
        this.apiUrl = apiUrl;
        this.modelName = modelName;
    }

    public String ask(String query) {
        ObjectNode jsonBody = objectMapper.createObjectNode();
        jsonBody.put("model", modelName);
        jsonBody.put("prompt", query);
        jsonBody.put("stream", false);

        // --- ИЗМЕНЕНИЕ: Добавляем системную инструкцию ---
        jsonBody.put("system", "Отвечай на все вопросы только на русском языке. Длина ответа не должна превышать 100-150 слов.");
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---

        try {
            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.get("application/json"));
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return "Ошибка при обращении к локальной нейросети. Убедитесь, что Ollama запущена.";
                }

                String responseBody = response.body().string();
                JsonNode rootNode = objectMapper.readTree(responseBody);

                String generatedText = rootNode.get("response").asText();
                Node document = markdownParser.parse(generatedText);
                return htmlRenderer.render(document);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof java.net.SocketTimeoutException) {
                return "Ошибка: локальная нейросеть не успела ответить за 60 секунд. Возможно, ваш компьютер сильно нагружен.";
            }
            return "Ошибка подключения к локальной нейросети. Запущена ли команда 'ollama run mistral'?";
        }
    }
}