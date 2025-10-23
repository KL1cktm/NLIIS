package by.yurhilevich;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.lucene.analysis.ru.RussianLightStemmer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Preprocessor {

    private final SentenceDetectorME ruSentenceDetector;
    private final TokenizerME ruTokenizer;
    private final RussianLightStemmer ruStemmer;

    private final SentenceDetectorME enSentenceDetector;
    private final TokenizerME enTokenizer;
    private final PorterStemmer enStemmer;

    public Preprocessor(String ruSentModel, String ruTokenModel, String enSentModel, String enTokenModel) throws IOException {
        try (InputStream modelIn = new FileInputStream(ruSentModel)) {
            ruSentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
        }
        try (InputStream modelIn = new FileInputStream(ruTokenModel)) {
            ruTokenizer = new TokenizerME(new TokenizerModel(modelIn));
        }
        ruStemmer = new RussianLightStemmer();

        try (InputStream modelIn = new FileInputStream(enSentModel)) {
            enSentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
        }
        try (InputStream modelIn = new FileInputStream(enTokenModel)) {
            enTokenizer = new TokenizerME(new TokenizerModel(modelIn));
        }
        enStemmer = new PorterStemmer();
    }

    public String[] splitSentences(String text, String lang) {
        if ("ru".equals(lang)) {
            return ruSentenceDetector.sentDetect(text);
        } else {
            return enSentenceDetector.sentDetect(text);
        }
    }

    public String[] tokenize(String sentence, String lang) {
        if ("ru".equals(lang)) {
            return ruTokenizer.tokenize(sentence);
        } else {
            return enTokenizer.tokenize(sentence);
        }
    }

    public String getStem(String word, String lang) {
        String lowerWord = word.toLowerCase();
        if ("ru".equals(lang)) {
            char[] chars = lowerWord.toCharArray();
            int len = ruStemmer.stem(chars, chars.length);
            return new String(chars, 0, len);
        } else {
            return enStemmer.stem(lowerWord);
        }
    }

    /**
     * Полная обработка текста: токенизация, очистка, стемминг.
     */
    public List<String> preprocessText(String text, String lang) {
        List<String> stems = new ArrayList<>();
        for (String sentence : splitSentences(text, lang)) {
            for (String token : tokenize(sentence, lang)) {
                if (token.matches("[\\p{Punct}\\d\\s]+")) {
                    continue;
                }

                String cleanToken = token.toLowerCase();
                if (StopWords.isStopWord(cleanToken, lang)) {
                    continue;
                }

                stems.add(getStem(cleanToken, lang));
            }
        }
        return stems;
    }

    /**
     * Обработка одного предложения (для F_tfidf)
     */
    public List<String> preprocessSentence(String sentence, String lang) {
        List<String> stems = new ArrayList<>();
        for (String token : tokenize(sentence, lang)) {
            if (token.matches("[\\p{Punct}\\d\\s]+")) {
                continue;
            }
            String cleanToken = token.toLowerCase();
            if (StopWords.isStopWord(cleanToken, lang)) {
                continue;
            }
            stems.add(getStem(cleanToken, lang));
        }
        return stems;
    }
}