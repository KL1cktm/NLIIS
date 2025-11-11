package by.yurhilevich.service;

import by.yurhilevich.dto.SynthesisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

record VoiceSettings(
        double stability,
        double similarity_boost,
        double speed
) {}

record TtsRequestBody(
        String text,
        String model_id,
        VoiceSettings voice_settings
) {}

@Service
public class ElevenLabsService {

    private final WebClient webClient;

    private static final String VOICE_ID = "21m00Tcm4TlvDq8ikWAM";
    private static final String MODEL_ID = "eleven_multilingual_v2";

    public ElevenLabsService(
            WebClient.Builder builder,
            @Value("${elevenlabs.api.key}") String apiKey,
            @Value("${elevenlabs.api.url}") String apiUrl
    ) {
        this.webClient = builder
                .baseUrl(apiUrl)
                .defaultHeader("xi-api-key", apiKey)
                .defaultHeader("Accept", "audio/mpeg")
                .build();
    }

    public byte[] synthesize(SynthesisRequest request) {
        double stability = clamp(request.stability(), 0.0, 1.0);
        double similarityBoost = clamp(request.similarityBoost(), 0.0, 1.0);
        double speed = clamp(request.speakingRate(), 0.7, 1.2);

        VoiceSettings voiceSettings = new VoiceSettings(stability, similarityBoost, speed);

        TtsRequestBody body = new TtsRequestBody(request.text(), MODEL_ID, voiceSettings);

        try {
            return webClient.post()
                    .uri("/text-to-speech/{voiceId}", VOICE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при обращении к API ElevenLabs. Проверьте API-ключ и параметры запроса.", e);
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
