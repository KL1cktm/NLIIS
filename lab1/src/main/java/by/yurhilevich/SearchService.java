package by.yurhilevich;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final String indexDir;
    private final OllamaService ollamaService;

    public SearchService(@Value("${app.index.dir}") String indexDir, OllamaService ollamaService) {
        this.indexDir = indexDir;
        this.ollamaService = ollamaService;
    }

    public List<SearchResult> search(String queryString, int maxResults) throws Exception {
        List<SearchResult> results = new ArrayList<>();
        Analyzer analyzer = new RussianAnalyzer();

        try (IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)))) {
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new ClassicSimilarity());

            QueryParser parser = new QueryParser("contents", analyzer);
            Query query = parser.parse(queryString);

            TopDocs docs = searcher.search(query, maxResults);

            if (docs.scoreDocs.length == 0) {
                System.out.println("Локальный поиск не дал результатов. Обращаемся к локальной модели Ollama...");
                String aiResponse = ollamaService.ask(queryString); // <-- ВЫЗЫВАЕМ НОВЫЙ СЕРВИС

                SearchResult aiResult = new SearchResult(
                        "Ответ от локальной нейросети", // <-- Меняем заголовок
                        aiResponse,
                        0.0,
                        List.of(),
                        "AI_RESPONSE"
                );
                results.add(aiResult);
            } else {
                ScoreDoc[] hits = docs.scoreDocs;
                List<String> queryTerms = analyze(queryString, analyzer);

                for (ScoreDoc hit : hits) {
                    Document doc = searcher.doc(hit.doc);
                    String title = doc.get("title");
                    String fullText = doc.get("contents");
                    double rank = hit.score;
                    String filePath = doc.get("path");

                    List<String> docTerms = analyze(fullText, analyzer);
                    List<String> presentTerms = queryTerms.stream()
                            .filter(docTerms::contains)
                            .distinct()
                            .collect(Collectors.toList());

                    results.add(new SearchResult(title, "", rank, presentTerms, filePath));
                }
            }
        }
        return results;
    }

    private List<String> analyze(String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        tokenStream.close();
        return result;
    }
}