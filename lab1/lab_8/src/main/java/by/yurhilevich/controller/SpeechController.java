package by.yurhilevich.controller;

import by.yurhilevich.dto.SynthesisRequest;
import by.yurhilevich.dto.SynthesisResponse;
import by.yurhilevich.service.ElevenLabsService; // ИЗМЕНЕНО
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Base64;

@Controller
public class SpeechController {

    private final ElevenLabsService ttsService; // ИЗМЕНЕНО

    public SpeechController(ElevenLabsService ttsService) { // ИЗМЕНЕНО
        this.ttsService = ttsService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/synthesize")
    @ResponseBody
    public ResponseEntity<SynthesisResponse> synthesizeSpeech(@RequestBody SynthesisRequest request) {

        try {
            byte[] audioData = ttsService.synthesize(request);
            String audioBase64 = Base64.getEncoder().encodeToString(audioData);

            // ElevenLabs возвращает MP3
            SynthesisResponse response = new SynthesisResponse(audioBase64, "audio/mpeg");
            return ResponseEntity.ok(response);

        } catch (Exception e) { // Ловим общую ошибку
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}