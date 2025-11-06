package by.yurhilevich;

import jakarta.annotation.PostConstruct;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME; // <-- НОВЫЙ ИМПОРТ
import opennlp.tools.sentdetect.SentenceModel;      // <-- НОВЫЙ ИМПОРТ
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class NlpService {

    private SentenceDetectorME sentenceDetector; // <-- НОВОЕ ПОЛЕ
    private TokenizerME tokenizer;
    private POSTaggerME posTagger;
    private Parser parser;
    private final Map<String, String> posTagMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try (InputStream sentModelIn = getClass().getResourceAsStream("/models/en-sent.bin"); // <-- ЗАГРУЗКА НОВОЙ МОДЕЛИ
             InputStream tokenModelIn = getClass().getResourceAsStream("/models/en-token.bin");
             InputStream posModelIn = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
             InputStream parserModelIn = getClass().getResourceAsStream("/models/en-parser-chunking.bin")) {

            if (sentModelIn == null || tokenModelIn == null || posModelIn == null || parserModelIn == null) {
                String missing = (sentModelIn == null) ? "en-sent.bin" : (tokenModelIn == null) ? "en-token.bin" : (posModelIn == null) ? "en-pos-maxent.bin" : "en-parser-chunking.bin";
                throw new RuntimeException("!!! ОШИБКА !!! Не найдена модель: " + missing +
                        ". Пожалуйста, скачайте все 4 модели в src/main/resources/models/");
            }

            this.sentenceDetector = new SentenceDetectorME(new SentenceModel(sentModelIn)); // <-- ИНИЦИАЛИЗАЦИЯ
            this.tokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
            this.posTagger = new POSTaggerME(new POSModel(posModelIn));
            this.parser = ParserFactory.create(new ParserModel(parserModelIn));

            fillPosTagMap();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки моделей OpenNLP", e);
        }
    }

    public String[] splitSentences(String text) {
        return sentenceDetector.sentDetect(text);
    }

    public String[] tokenize(String text) {
        return tokenizer.tokenize(text);
    }

    public String[] getPosTags(String[] tokens) {
        return posTagger.tag(tokens);
    }

    public String getParseTree(String sentence) {
        StringBuffer sb = new StringBuffer();
        Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);

        if (topParses.length > 0) {
            topParses[0].show(sb);
            return sb.toString();
        }
        return "Не удалось построить дерево для предложения.";
    }

    public String getPosTagDescription(String tag) {
        return posTagMap.getOrDefault(tag, "Неизвестный тег");
    }

    private void fillPosTagMap() {
        posTagMap.put("CC", "Coordinating conjunction");
        posTagMap.put("CD", "Cardinal number");
        posTagMap.put("DT", "Determiner");
        posTagMap.put("EX", "Existential there");
        posTagMap.put("FW", "Foreign word");
        posTagMap.put("IN", "Preposition or subordinating conjunction");
        posTagMap.put("JJ", "Adjective");
        posTagMap.put("JJR", "Adjective, comparative");
        posTagMap.put("JJS", "Adjective, superlative");
        posTagMap.put("LS", "List item marker");
        posTagMap.put("MD", "Modal");
        posTagMap.put("NN", "Noun, singular or mass");
        posTagMap.put("NNS", "Noun, plural");
        posTagMap.put("NNP", "Proper noun, singular");
        posTagMap.put("NNPS", "Proper noun, plural");
        posTagMap.put("PDT", "Predeterminer");
        posTagMap.put("POS", "Possessive ending");
        posTagMap.put("PRP", "Personal pronoun");
        posTagMap.put("PRP$", "Possessive pronoun");
        posTagMap.put("RB", "Adverb");
        posTagMap.put("RBR", "Adverb, comparative");
        posTagMap.put("RBS", "Adverb, superlative");
        posTagMap.put("RP", "Particle");
        posTagMap.put("SYM", "Symbol");
        posTagMap.put("TO", "to");
        posTagMap.put("UH", "Interjection");
        posTagMap.put("VB", "Verb, base form");
        posTagMap.put("VBD", "Verb, past tense");
        posTagMap.put("VBG", "Verb, gerund or present participle");
        posTagMap.put("VBN", "Verb, past participle");
        posTagMap.put("VBP", "Verb, non-3rd person singular present");
        posTagMap.put("VBZ", "Verb, 3rd person singular present");
        posTagMap.put("WDT", "Wh-determiner");
        posTagMap.put("WP", "Wh-pronoun");
        posTagMap.put("WP$", "Possessive wh-pronoun");
        posTagMap.put("WRB", "Wh-adverb");
    }
}