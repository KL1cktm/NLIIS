package by.yurhilevich;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class TranslationController {

    private final LlamaTranslationService llamaService;
    private final NlpService nlpService;
    private final DictionaryRepository dictionaryRepository;

    public TranslationController(LlamaTranslationService llamaService,
                                 NlpService nlpService,
                                 DictionaryRepository dictionaryRepository) {
        this.llamaService = llamaService;
        this.nlpService = nlpService;
        this.dictionaryRepository = dictionaryRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("request", new TranslationRequest());
        model.addAttribute("response", null);
        return "index";
    }

    @PostMapping("/translate")
    public String handleTranslate(@ModelAttribute TranslationRequest request, Model model) {
        String inputText = request.getText();

        // 1. Перевод
        String translatedText = llamaService.translate(inputText);

        // 2. NLP-Анализ
        String[] tokens = nlpService.tokenize(inputText);
        String[] posTags = nlpService.getPosTags(tokens);

        // --- НОВЫЙ БЛОК: Разбивка на предложения ---
        String[] sentences = nlpService.splitSentences(inputText);
        // -----------------------------------------

        // 3. Считаем частоты (код без изменений)
        Map<String, Long> wordFrequencies = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(word -> word.matches("[a-zA-Z]+"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 4. Формируем "Вкладку 1" (код без изменений)
        Map<String, String> tokenPosMap = new HashMap<>();
        for (int i = 0; i< tokens.length; i++) {
            if (!tokens[i].matches("[a-zA-Z]+")) continue;
            tokenPosMap.putIfAbsent(tokens[i].toLowerCase(), posTags[i]);
        }

        List<TranslationResponse.WordStats> frequencyList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
            String word = entry.getKey();
            String posTag = tokenPosMap.getOrDefault(word, "??");
            String posDescription = nlpService.getPosTagDescription(posTag);
            String wordTranslation = dictionaryRepository.findByEnglishWord(word)
                    .map(DictionaryEntry::getRussianTranslation)
                    .orElse("[нет в словаре]");

            frequencyList.add(new TranslationResponse.WordStats(
                    word, entry.getValue(), wordTranslation, posTag, posDescription
            ));
        }
        frequencyList.sort((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()));

        // 5. Дерево разбора (Вкладка 2)
        // --- УДАЛЯЕМ СТАРЫЙ КОД ---
        // String firstSentence = inputText.split("[.!?]")[0] + ".";
        // String parseTree = nlpService.getParseTree(firstSentence);
        // -------------------------

        // 6. Собираем DTO для ответа
        TranslationResponse response = new TranslationResponse();
        response.setTranslatedText(translatedText);
        response.setTotalWords(tokens.length);
        response.setUniqueWords(wordFrequencies.size());
        response.setFrequencyList(frequencyList);
        response.setOriginalSentences(Arrays.asList(sentences)); // <-- ПЕРЕДАЕМ СПИСОК ПРЕДЛОЖЕНИЙ

        // 7. Сохраняем в файл (код без изменений)
        saveResultsToFile(request.getText(), response);

        // 8. Передаем все на страницу
        model.addAttribute("request", request);
        model.addAttribute("response", response);
        return "index";
    }

    // --- НОВЫЙ API-ЭНДПОИНТ ДЛЯ AJAX ---
    @PostMapping("/api/get-parse-tree")
    @ResponseBody
    public Map<String, String> getParseTree(@RequestParam String sentence) {
        String tree = nlpService.getParseTree(sentence);
        return Map.of("tree", tree);
    }
    // --------------------------------------

    @PostMapping("/api/update-dictionary")
    @ResponseBody
    public Map<String, String> updateDictionaryEntry(@RequestParam String word,
                                                     @RequestParam String translation) {

        String cleanWord = word.toLowerCase().trim();
        String cleanTranslation = translation.trim();

        DictionaryEntry entry = dictionaryRepository.findByEnglishWord(cleanWord)
                .orElse(new DictionaryEntry());

        entry.setEnglishWord(cleanWord);
        entry.setRussianTranslation(cleanTranslation);

        if (entry.getPosTag() == null) {
            String[] tags = nlpService.getPosTags(new String[]{cleanWord});
            entry.setPosTag(tags[0]);
            entry.setPosTagDescription(nlpService.getPosTagDescription(tags[0]));
        }

        dictionaryRepository.save(entry);

        return Map.of("status", "success", "word", cleanWord, "newTranslation", cleanTranslation);
    }

    private void saveResultsToFile(String sourceText, TranslationResponse response) {
        // ... (код saveResultsToFile() остается без изменений) ...
        // ... (он не парсит дерево, так что все ок) ...
        String filename = "src/main/resources/static/translation_results.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {

            writer.write("--- ИСХОДНЫЙ ТЕКСТ ---");
            writer.newLine();
            writer.write(sourceText);
            writer.newLine();
            writer.newLine();

            writer.write("--- РЕЗУЛЬТАТ ПЕРЕВОДА ---");
            writer.newLine();
            writer.write(response.getTranslatedText());
            writer.newLine();
            writer.newLine();

            writer.write("--- СТАТИСТИКА ---");
            writer.newLine();
            writer.write("Всего слов (токенов): " + response.getTotalWords());
            writer.newLine();
            writer.write("Уникальных слов: " + response.getUniqueWords());
            writer.newLine();
            writer.newLine();

            writer.write("--- ЧАСТОТНЫЙ СЛОВАРЬ ---");
            writer.newLine();
            String header = String.format("%-20s | %-10s | %-25s | %s", "Слово", "Частота", "Перевод (из БД)", "Грам. инфо");
            writer.write(header);
            writer.newLine();
            writer.write("-".repeat(header.length()));
            writer.newLine();

            for (TranslationResponse.WordStats wf : response.getFrequencyList()) {
                writer.write(String.format("%-20s | %-10d | %-25s | %s (%s)",
                        wf.getWord(),
                        wf.getFrequency(),
                        wf.getTranslation(),
                        wf.getPosTag(),
                        wf.getPosTagDescription()
                ));
                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}