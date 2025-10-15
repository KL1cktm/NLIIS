package by.yurhilevich;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;


public class ProfileGenerator {

    private static final String RU_CORPUS_PATH = "ru_corpus.txt";
    private static final String IT_CORPUS_PATH = "it_corpus.txt";
    private static final String OUTPUT_DIR = "src/main/resources/profiles/";
    private static final int TOP_WORDS_COUNT = 300;
    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) throws IOException {
        System.out.println("Начинаем генерацию профилей...");

        Files.createDirectories(Paths.get(OUTPUT_DIR));

        System.out.println("Обрабатываем русский корпус...");
        String russianText = cleanText(Files.readString(Paths.get(RU_CORPUS_PATH)));
        generateFrequentWordsFile(russianText, "ru_frequent_words.txt");
        generateAlphabetProfileFile(russianText, "ru_alphabet_profile.json");
        System.out.println("Профили для русского языка созданы.");

        System.out.println("Обрабатываем итальянский корпус...");
        String italianText = cleanText(Files.readString(Paths.get(IT_CORPUS_PATH)));
        generateFrequentWordsFile(italianText, "it_frequent_words.txt");
        generateAlphabetProfileFile(italianText, "it_alphabet_profile.json");
        System.out.println("Профили для итальянского языка созданы.");

        System.out.println("\nГенерация завершена! Файлы находятся в папке: " + OUTPUT_DIR);
    }

    private static String cleanText(String rawText) {
        return rawText.toLowerCase().replaceAll("[^а-яa-zàèéìòù\\s]", " ").trim();
    }

    private static void generateFrequentWordsFile(String text, String fileName) throws IOException {
        Map<String, Integer> wordCounts = new HashMap<>();
        Arrays.stream(text.split("\\s+"))
                .filter(word -> !word.isBlank() && word.length() > 2)
                .forEach(word -> wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1));

        String content = wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(TOP_WORDS_COUNT)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n"));

        Files.writeString(Paths.get(OUTPUT_DIR, fileName), content);
    }

    private static void generateAlphabetProfileFile(String text, String fileName) throws IOException {
        Map<Character, Integer> charCounts = new HashMap<>();
        int totalLetters = 0;

        for (char ch : text.toCharArray()) {
            if (Character.isLetter(ch)) {
                charCounts.put(ch, charCounts.getOrDefault(ch, 0) + 1);
                totalLetters++;
            }
        }

        Map<Character, Double> profile = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : charCounts.entrySet()) {
            profile.put(entry.getKey(), (double) entry.getValue() / totalLetters);
        }

        Map<Character, Double> sortedProfile = profile.entrySet().stream()
                .sorted(Map.Entry.<Character, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        MAPPER.writeValue(Paths.get(OUTPUT_DIR, fileName).toFile(), sortedProfile);
    }
}