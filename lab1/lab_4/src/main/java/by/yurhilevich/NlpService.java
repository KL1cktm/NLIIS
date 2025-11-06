package by.yurhilevich;

import jakarta.annotation.PostConstruct;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class NlpService {

    private SentenceDetectorME enSentenceDetector;
    private TokenizerME enTokenizer;
    private POSTaggerME enPosTagger;
    private Parser enParser;
    private final Map<String, String> enPosTagMap = new HashMap<>();

    private SentenceDetectorME ruSentenceDetector;
    private TokenizerME ruTokenizer;
    private POSTaggerME ruPosTagger;
    private Parser ruParser;
    private final Map<String, String> ruPosTagMap = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            try (InputStream sentModelIn = getClass().getResourceAsStream("/models/en-sent.bin");
                 InputStream tokenModelIn = getClass().getResourceAsStream("/models/en-token.bin");
                 InputStream posModelIn = getClass().getResourceAsStream("/models/en-pos-maxent.bin");
                 InputStream parserModelIn = getClass().getResourceAsStream("/models/en-parser-chunking.bin")) {

                if (sentModelIn == null || tokenModelIn == null || posModelIn == null || parserModelIn == null) {
                    throw new RuntimeException("!!! ОШИБКА !!! Не найдены АНГЛИЙСКИЕ модели OpenNLP.");
                }
                this.enSentenceDetector = new SentenceDetectorME(new SentenceModel(sentModelIn));
                this.enTokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                this.enPosTagger = new POSTaggerME(new POSModel(posModelIn));
                this.enParser = ParserFactory.create(new ParserModel(parserModelIn));
                fillEnPosTagMap();
            }

            try (InputStream sentModelIn = getClass().getResourceAsStream("/models/ru-sent.bin");
                 InputStream tokenModelIn = getClass().getResourceAsStream("/models/ru-token.bin");
                 InputStream posModelIn = getClass().getResourceAsStream("/models/ru-pos-maxent.bin");
                 InputStream parserModelIn = getClass().getResourceAsStream("/models/ru-parser-chunking.bin")) {

                if (sentModelIn == null || tokenModelIn == null || posModelIn == null || parserModelIn == null) {
                    System.err.println("!!! ВНИМАНИЕ !!! Русские модели NLP не найдены. Анализ Ru->En будет ограничен.");
                } else {
                    this.ruSentenceDetector = new SentenceDetectorME(new SentenceModel(sentModelIn));
                    this.ruTokenizer = new TokenizerME(new TokenizerModel(tokenModelIn));
                    this.ruPosTagger = new POSTaggerME(new POSModel(posModelIn));
                    this.ruParser = ParserFactory.create(new ParserModel(parserModelIn));
                    fillRuPosTagMap();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки моделей OpenNLP", e);
        }
    }

    public String[] splitSentences(String text, String lang) {
        if ("ru".equals(lang) && ruSentenceDetector != null) {
            return ruSentenceDetector.sentDetect(text);
        }
        return enSentenceDetector.sentDetect(text);
    }

    public String[] tokenize(String text, String lang) {
        if ("ru".equals(lang) && ruTokenizer != null) {
            return ruTokenizer.tokenize(text);
        }
        return enTokenizer.tokenize(text);
    }

    public String[] getPosTags(String[] tokens, String lang) {
        if ("ru".equals(lang) && ruPosTagger != null) {
            return ruPosTagger.tag(tokens);
        }
        return enPosTagger.tag(tokens);
    }

    public String getParseTree(String sentence, String lang) {
        Parser parser = ("ru".equals(lang) && ruParser != null) ? ruParser : enParser;
        if (parser == null) {
            return "Модель парсера для языка '" + lang + "' не загружена.";
        }

        StringBuffer sb = new StringBuffer();
        Parse[] topParses = ParserTool.parseLine(sentence, parser, 1);

        if (topParses.length > 0) {
            topParses[0].show(sb);
            return sb.toString();
        }
        return "Не удалось построить дерево для предложения.";
    }

    public String getPosTagDescription(String tag, String lang) {
        if ("ru".equals(lang)) {
            return ruPosTagMap.getOrDefault(tag, tag);
        }
        return enPosTagMap.getOrDefault(tag, "Unknown tag");
    }

    private void fillRuPosTagMap() {
        ruPosTagMap.put("ADJ", "Прилагательное");
        ruPosTagMap.put("ADP", "Предлог / Послелог (Adposition)");
        ruPosTagMap.put("ADV", "Наречие");
        ruPosTagMap.put("AUX", "Вспомогательный глагол");
        ruPosTagMap.put("CCONJ", "Сочинительный союз");
        ruPosTagMap.put("DET", "Определитель (Местоимение-прилагательное)");
        ruPosTagMap.put("INTJ", "Междометие");
        ruPosTagMap.put("NOUN", "Существительное");
        ruPosTagMap.put("NUM", "Числительное");
        ruPosTagMap.put("PART", "Частица");
        ruPosTagMap.put("PRON", "Местоимение");
        ruPosTagMap.put("PROPN", "Имя собственное");
        ruPosTagMap.put("PUNCT", "Знак пунктуации");
        ruPosTagMap.put("SCONJ", "Подчинительный союз");
        ruPosTagMap.put("SYM", "Символ");
        ruPosTagMap.put("VERB", "Глагол");
        ruPosTagMap.put("X", "Другое (иностранное слово, опечатка)");
    }

    private void fillEnPosTagMap() {
        enPosTagMap.put("CC", "Coordinating conjunction");
        enPosTagMap.put("CD", "Cardinal number");
        enPosTagMap.put("DT", "Determiner");
        enPosTagMap.put("EX", "Existential there");
        enPosTagMap.put("FW", "Foreign word");
        enPosTagMap.put("IN", "Preposition or subordinating conjunction");
        enPosTagMap.put("JJ", "Adjective");
        enPosTagMap.put("JJR", "Adjective, comparative");
        enPosTagMap.put("JJS", "Adjective, superlative");
        enPosTagMap.put("LS", "List item marker");
        enPosTagMap.put("MD", "Modal");
        enPosTagMap.put("NN", "Noun, singular or mass");
        enPosTagMap.put("NNS", "Noun, plural");
        enPosTagMap.put("NNP", "Proper noun, singular");
        enPosTagMap.put("NNPS", "Proper noun, plural");
        enPosTagMap.put("PDT", "Predeterminer");
        enPosTagMap.put("POS", "Possessive ending");
        enPosTagMap.put("PRP", "Personal pronoun");
        enPosTagMap.put("PRP$", "Possessive pronoun");
        enPosTagMap.put("RB", "Adverb");
        enPosTagMap.put("RBR", "Adverb, comparative");
        enPosTagMap.put("RBS", "Adverb, superlative");
        enPosTagMap.put("RP", "Particle");
        enPosTagMap.put("SYM", "Symbol");
        enPosTagMap.put("TO", "to");
        enPosTagMap.put("UH", "Interjection");
        enPosTagMap.put("VB", "Verb, base form");
        enPosTagMap.put("VBD", "Verb, past tense");
        enPosTagMap.put("VBG", "Verb, gerund or present participle");
        enPosTagMap.put("VBN", "Verb, past participle");
        enPosTagMap.put("VBP", "Verb, non-3rd person singular present");
        enPosTagMap.put("VBZ", "Verb, 3rd person singular present");
        enPosTagMap.put("WDT", "Wh-determiner");
        enPosTagMap.put("WP", "Wh-pronoun");
        enPosTagMap.put("WP$", "Possessive wh-pronoun");
        enPosTagMap.put("WRB", "Wh-adverb");
    }
}