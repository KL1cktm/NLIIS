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
        TranslationRequest tr = new TranslationRequest();
        tr.setDirection("en-ru");
        model.addAttribute("request", tr);
        model.addAttribute("response", null);
        return "index";
    }

    @PostMapping("/translate")
    public String handleTranslate(@ModelAttribute TranslationRequest request, Model model) {
        String inputText = request.getText();
        String direction = request.getDirection();
        String sourceLang = direction.split("-")[0];

        String translatedText = llamaService.translate(inputText, direction);

        String[] tokens = nlpService.tokenize(inputText, sourceLang);
        String[] posTags = nlpService.getPosTags(tokens, sourceLang);
        String[] sentences = nlpService.splitSentences(inputText, sourceLang);

        Map<String, Long> wordFrequencies = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(word -> word.matches("[\\p{L}]+"))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<String, String> tokenPosMap = new HashMap<>();
        for (int i = 0; i < tokens.length; i++) {
            if (!tokens[i].matches("[\\p{L}]+")) continue;
            tokenPosMap.putIfAbsent(tokens[i].toLowerCase(), posTags[i]);
        }

        List<TranslationResponse.WordStats> frequencyList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : wordFrequencies.entrySet()) {
            String word = entry.getKey();
            String posTag = tokenPosMap.getOrDefault(word, "??");
            String posDescription = nlpService.getPosTagDescription(posTag, sourceLang);

            String wordTranslation;
            if ("en".equals(sourceLang)) {
                wordTranslation = dictionaryRepository.findByEnglishWord(word)
                        .map(DictionaryEntry::getRussianTranslation)
                        .orElse("[нет в словаре]");
            } else {
                wordTranslation = dictionaryRepository.findByRussianTranslation(word)
                        .map(DictionaryEntry::getEnglishWord)
                        .orElse("[нет в словаре]");
            }

            frequencyList.add(new TranslationResponse.WordStats(
                    word, entry.getValue(), wordTranslation, posTag, posDescription
            ));
        }
        frequencyList.sort((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()));

        TranslationResponse response = new TranslationResponse();
        response.setTranslatedText(translatedText);

        long actualWordCount = Arrays.stream(tokens).filter(word -> word.matches("[\\p{L}]+")).count();
        response.setTotalWords((int) actualWordCount);

        response.setUniqueWords(wordFrequencies.size());
        response.setFrequencyList(frequencyList);
        response.setOriginalSentences(Arrays.asList(sentences));

        saveResultsToFile(request.getText(), response);

        model.addAttribute("request", request);
        model.addAttribute("response", response);
        return "index";
    }

    @PostMapping("/api/get-parse-tree")
    @ResponseBody
    public Map<String, String> getParseTree(@RequestParam String sentence, @RequestParam String lang) {
        String tree = nlpService.getParseTree(sentence, lang);
        return Map.of("tree", tree);
    }

    @PostMapping("/api/update-dictionary")
    @ResponseBody
    public Map<String, String> updateDictionaryEntry(@RequestParam String word,
                                                     @RequestParam String translation,
                                                     @RequestParam String direction) {

        String cleanWord = word.toLowerCase().trim();
        String cleanTranslation = translation.trim();
        DictionaryEntry entry;

        if ("en-ru".equals(direction)) {
            entry = dictionaryRepository.findByEnglishWord(cleanWord)
                    .orElse(new DictionaryEntry());
            entry.setEnglishWord(cleanWord);
            entry.setRussianTranslation(cleanTranslation);
        } else {
            entry = dictionaryRepository.findByRussianTranslation(cleanWord)
                    .orElse(new DictionaryEntry());
            entry.setRussianTranslation(cleanWord);
            entry.setEnglishWord(cleanTranslation);
        }

        if (entry.getPosTag() == null) {
            String sourceLang = direction.split("-")[0];
            String[] tags = nlpService.getPosTags(new String[]{cleanWord}, sourceLang);
            if(tags.length > 0) {
                entry.setPosTag(tags[0]);
                entry.setPosTagDescription(nlpService.getPosTagDescription(tags[0], sourceLang));
            }
        }

        dictionaryRepository.save(entry);

        return Map.of("status", "success");
    }

    private void saveResultsToFile(String sourceText, TranslationResponse response) {
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