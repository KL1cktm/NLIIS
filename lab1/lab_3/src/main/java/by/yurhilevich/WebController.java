package by.yurhilevich;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class WebController {

    private final CorpusStats corpusStats;
    private final Summarizer summarizer;

    @Value("${corpus.path}")
    private String corpusPath;

    private boolean isCorpusReady = false;

    @Autowired
    public WebController(CorpusStats corpusStats, Summarizer summarizer) {
        this.corpusStats = corpusStats;
        this.summarizer = summarizer;
    }

    /**
     * Загружает и обрабатывает корпус.
     * Вызывается кнопкой "1. Загрузить корпус".
     */
    @PostMapping("/process-corpus")
    public ResponseEntity<Map<String, String>> loadCorpus() {
        try {
            File corpusDir = new File(corpusPath);
            String result = corpusStats.processCorpus(corpusDir);
            isCorpusReady = true;
            return ResponseEntity.ok(Map.of("message", result));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка обработки корпуса: " + e.getMessage()));
        }
    }

    /**
     * Принимает файл и язык, возвращает два реферата.
     * Вызывается кнопкой "2. Реферировать".
     */
    @PostMapping("/summarize")
    public ResponseEntity<Map<String, String>> summarizeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lang") String lang) {

        if (!isCorpusReady) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Корпус еще не загружен."));
        }
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Файл не предоставлен."));
        }
        if (!"ru".equals(lang) && !"en".equals(lang)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Неверный язык: " + lang));
        }

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            String classicResult = summarizer.generateClassicReferat(content, lang);

            String keywordResult = summarizer.generateKeywordReferat(content, lang);

            return ResponseEntity.ok(Map.of(
                    "classicReferat", classicResult,
                    "keywordReferat", keywordResult,
                    "sourceLink", file.getOriginalFilename()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ошибка Llama/Summarizer: " + e.getMessage()));
        }
    }
}
