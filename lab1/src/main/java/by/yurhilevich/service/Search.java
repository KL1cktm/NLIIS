package by.yurhilevich.service;

import by.yurhilevich.model.SearchResult;
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
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.*;

/**
 * Класс, отвечающий за поиск и ранжирование документов.
 * Соответствует диаграмме из лабораторной работы.
 */
public class Search {
    private String searchQuery;
    private Directory index;

    public Search(String searchQuery, Directory index) {
        this.searchQuery = searchQuery;
        this.index = index;
    }

    /**
     * Вычисляет скалярное произведение двух векторов.
     * Здесь это заглушка, так как Lucene делает это автоматически.
     * @param a Первый вектор.
     * @param b Второй вектор.
     * @return Скалярное произведение.
     */
    public double ScalarProduct(Map<Integer, Double> a, Map<Integer, Double> b) {
        // Lucene делает это автоматически, но метод оставлен для соответствия заданию.
        return 0.0;
    }

    /**
     * Вычисляет евклидову норму вектора.
     * Здесь это заглушка, так как Lucene делает это автоматически.
     * @param a Вектор.
     * @return Евклидова норма.
     */
    public double EuclideanNorm(Map<Integer, Double> a) {
        // Lucene делает это автоматически, но метод оставлен для соответствия заданию.
        return 0.0;
    }

    /**
     * Выполняет поиск и возвращает список результатов.
     * Использует Lucene для эффективного поиска.
     * @return Список результатов поиска, отсортированный по релевантности.
     */
    public List<SearchResult> GetSearchResult() {
        List<SearchResult> searchResults = new ArrayList<>();
        try (IndexReader reader = DirectoryReader.open(index)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Analyzer analyzer = new RussianAnalyzer();

            // Используем QueryParser для создания запроса
            Query query = new QueryParser("text", analyzer).parse(searchQuery);

            TopDocs docs = searcher.search(query, 10);
            ScoreDoc[] hits = docs.scoreDocs;

            for (ScoreDoc hit : hits) {
                Document d = searcher.doc(hit.doc);
                searchResults.add(new SearchResult(
                        Integer.parseInt(d.get("documentId")),
                        d.get("title"),
                        d.get("text").substring(0, Math.min(d.get("text").length(), 300)),
                        hit.score,
                        d.get("date")
                ));
            }
        } catch (Exception e) {
            System.err.println("Проблема при поиске: " + e.getMessage());
        }

        // Сортируем результаты по убыванию релевантности (рангу)
        searchResults.sort(Comparator.comparingDouble(SearchResult::getRank).reversed());
        return searchResults;
    }
}
