package by.yurhilevich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Summarizer {

    private final Preprocessor preprocessor;
    private final CorpusStats corpusStats;
    private final LlamaService llamaService;

    public Summarizer(Preprocessor preprocessor, CorpusStats corpusStats, LlamaService llamaService) {
        this.preprocessor = preprocessor;
        this.corpusStats = corpusStats;
        this.llamaService = llamaService;
    }

    public String generateKeywordReferat(String fullText, String lang) throws Exception {
        return llamaService.getKeywords(fullText, lang);
    }

    public String generateClassicReferat(String fullText, String lang) throws Exception {
        List<Sentence> allSentences = new ArrayList<>();
        String[] paragraphs = fullText.split("\n\n");
        int totalDocChars = fullText.length();
        int sentenceCounter = 0;
        int docCharCounter = 0;

        for (String par : paragraphs) {
            int totalParChars = par.length();
            int parCharCounter = 0;
            String[] sentencesInPar = preprocessor.splitSentences(par, lang);

            for (int i = 0; i < sentencesInPar.length; i++) {
                String sentText = sentencesInPar[i];
                List<String> stems = preprocessor.preprocessSentence(sentText, lang);

                Sentence sentence = new Sentence(
                        sentText,
                        sentenceCounter++,
                        i,
                        docCharCounter,
                        parCharCounter,
                        stems,
                        totalParChars
                );
                // -------------------------

                allSentences.add(sentence);
                parCharCounter += sentText.length();
                docCharCounter += sentText.length();
            }
            docCharCounter += 2;
        }

        if (allSentences.isEmpty()) {
            return "Текст слишком короткий для реферирования.";
        }

        Map<String, Long> tfDoc = allSentences.stream()
                .flatMap(s -> s.getStems().stream())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        for (Sentence s : allSentences) {
            double fPosD = s.getFposD(totalDocChars);

            double fPosP = s.getFposP();

            Map<String, Long> tfSentence = s.getStems().stream()
                    .collect(Collectors.groupingBy(s_stem -> s_stem, Collectors.counting()));

            double fTfidf = 0.0;
            for (Map.Entry<String, Long> entry : tfSentence.entrySet()) {
                String stem = entry.getKey();
                long tf_si = entry.getValue();
                long tf_d = tfDoc.getOrDefault(stem, 1L);
                double idf = corpusStats.getIdf(stem);

                fTfidf += ((double) tf_si / tf_d) * idf;
            }

            double weight = fPosD * fPosP * fTfidf;
            s.setWeight(weight);
        }

        Collections.sort(allSentences);
        List<Sentence> top10 = allSentences.stream().limit(10).collect(Collectors.toList());

        top10.sort((s1, s2) -> Integer.compare(s1.getOriginalIndex(), s2.getOriginalIndex()));

        String draftReferat = top10.stream()
                .map(Sentence::getOriginalText)
                .collect(Collectors.joining(" "));

        return draftReferat;
//        return llamaService.transformReferat(draftReferat, lang);
    }
}