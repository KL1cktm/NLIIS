package by.yurhilevich;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class TranslationResponse {

    private String translatedText;
    private int totalWords;
    private int uniqueWords;
    private List<WordStats> frequencyList;
    private List<String> originalSentences;

    @Data
    @AllArgsConstructor
    public static class WordStats {
        private String word;
        private long frequency;
        private String translation;
        private String posTag;
        private String posTagDescription;
    }
}