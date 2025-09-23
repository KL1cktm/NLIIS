package by.yurhilevich;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Value("${app.index.dir}")
    private String indexDir;

    public List<SearchResult> search(String queryString, int maxResults) throws Exception {
        List<SearchResult> results = new ArrayList<>();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new ClassicSimilarity()); // Используем классическую TF-IDF модель

        Analyzer analyzer = new RussianAnalyzer();
        QueryParser parser = new QueryParser("contents", analyzer);

        String preprocessedQuery = TextProcessor.preprocess(queryString);
        if (preprocessedQuery.isEmpty()) {
            return results;
        }

        Query query = parser.parse(preprocessedQuery);
        TopDocs docs = searcher.search(query, maxResults);
        ScoreDoc[] hits = docs.scoreDocs;

        Set<String> queryTerms = Arrays.stream(preprocessedQuery.split("\\s+")).collect(Collectors.toSet());

        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            String title = doc.get("title");
            String fullText = doc.get("contents");
            String snippet = fullText.length() > 300 ? fullText.substring(0, 300) + "..." : fullText;
            double rank = hit.score;

            List<String> presentTerms = queryTerms.stream()
                    .filter(term -> fullText.contains(term))
                    .collect(Collectors.toList());

            results.add(new SearchResult(title, snippet, rank, presentTerms));
        }

        reader.close();
        return results;
    }
}
