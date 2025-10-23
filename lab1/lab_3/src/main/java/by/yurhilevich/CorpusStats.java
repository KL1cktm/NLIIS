package by.yurhilevich;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorpusStats {

    private Map<String, Double> idfScores = new HashMap<>();
    private int docCount = 0;
    private final Preprocessor preprocessor;

    public CorpusStats(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    /**
     * Обрабатывает все .txt файлы в папке.
     * Определяет язык по имени файла (например, "ru_...txt" или "en_...txt")
     */
    public String processCorpus(File corpusDir) throws IOException {
        idfScores.clear();
        Map<String, Set<Integer>> docFrequencies = new HashMap<>();
        docCount = 0;

        File[] files = corpusDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null) {
            throw new IOException("Corpus directory not found or is empty: " + corpusDir.getAbsolutePath());
        }

        for (File file : files) {
            docCount++;
            int docId = docCount;
            String lang = file.getName().startsWith("ru") ? "ru" : "en";
            String content = Files.readString(file.toPath());

            Set<String> uniqueStemsInDoc = new HashSet<>(preprocessor.preprocessText(content, lang));

            for (String stem : uniqueStemsInDoc) {
                docFrequencies.putIfAbsent(stem, new HashSet<>());
                docFrequencies.get(stem).add(docId);
            }
        }

        if (docCount == 0) {
            throw new IOException("No .txt files found in corpus directory.");
        }

        for (Map.Entry<String, Set<Integer>> entry : docFrequencies.entrySet()) {
            String stem = entry.getKey();
            int df = entry.getValue().size();
            double idf = Math.log((double) docCount / df);
            idfScores.put(stem, idf);
        }

        return "Corpus processed: " + docCount + " documents, " + idfScores.size() + " unique terms.";
    }

    public double getIdf(String stem) {
        return idfScores.getOrDefault(stem, 0.0);
    }
}
