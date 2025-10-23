package by.yurhilevich;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWords {

    private static final Set<String> RU_STOP_WORDS = new HashSet<>(Arrays.asList(
            "и", "в", "во", "не", "что", "он", "на", "я", "с", "со", "как", "а", "то",
            "все", "она", "так", "его", "но", "да", "ты", "к", "у", "же", "вы", "за",
            "бы", "по", "только", "ее", "мне", "было", "вот", "от", "для", "о", "из"
    ));

    private static final Set<String> EN_STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
            "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "were", "will", "with"
    ));

    public static boolean isStopWord(String word, String lang) {
        if (word == null || word.length() < 2) return true; // Также отсеиваем короткие

        if ("ru".equals(lang)) {
            return RU_STOP_WORDS.contains(word.toLowerCase());
        } else if ("en".equals(lang)) {
            return EN_STOP_WORDS.contains(word.toLowerCase());
        }
        return false;
    }
}
