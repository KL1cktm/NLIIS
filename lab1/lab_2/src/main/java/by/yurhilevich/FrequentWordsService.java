package by.yurhilevich;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.nio.file.Files;
import org.springframework.core.io.ClassPathResource;
import java.util.*;

@Service
public class FrequentWordsService implements LanguageService {
    private Set<String> russianWords;
    private Set<String> italianWords;

    @PostConstruct
    public void init() throws Exception {
        russianWords = loadWords("profiles/ru_frequent_words.txt");
        italianWords = loadWords("profiles/it_frequent_words.txt");
    }

    @Override
    public Language recognize(String text) {
        String[] words = text.split("\\s+");
        int russianScore = 0;
        int italianScore = 0;

        for (String word : words) {
            if (russianWords.contains(word)) russianScore++;
            if (italianWords.contains(word)) italianScore++;
        }

        if (russianScore > italianScore) return Language.RUSSIAN;
        if (italianScore > russianScore) return Language.ITALIAN;
        return Language.UNKNOWN;
    }

    @Override
    public String getMethodName() {
        return "Метод частотных слов";
    }

    private Set<String> loadWords(String path) throws Exception {
        return new HashSet<>(Files.readAllLines(new ClassPathResource(path).getFile().toPath()));
    }
}