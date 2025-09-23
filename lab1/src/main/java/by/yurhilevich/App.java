package by.yurhilevich;

import by.yurhilevich.model.Document;
import by.yurhilevich.model.SearchResult;
import by.yurhilevich.service.Search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.sql.Time;
import java.util.Date;
import java.util.List;

/**
 * Основной класс для запуска информационно-поисковой системы.
 * Этот класс отвечает за индексацию документов из БД,
 * выполнение поиска и отображение результатов.
 */
public class App {
    public static void main(String[] args) {
        // RAMDirectory для индексации в памяти. Для реального приложения используйте FSDirectory.
        try (Directory index = new RAMDirectory()) {
            Analyzer analyzer = new RussianAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            // Шаг 1: Индексация документов из БД в Lucene
            System.out.println("Индексирование документов...");
            try (IndexWriter w = new IndexWriter(index, config)) {
                // Создаем и сохраняем документы в PostgreSQL
                Document doc1 = new Document("Лабораторная работа по ИПС", "Информационный поиск это процесс поиска в большой коллекции некоего неструктурированного материала.", new Date(), new Time(System.currentTimeMillis()));
                if (doc1.AddDocumentToBase()) {
                    // Если документ добавлен в БД, индексируем его в Lucene
                    org.apache.lucene.document.Document luceneDoc1 = new org.apache.lucene.document.Document();
                    luceneDoc1.add(new TextField("text", doc1.getText(), Field.Store.YES));
                    luceneDoc1.add(new StringField("documentId", String.valueOf(doc1.getDocumentID()), Field.Store.YES));
                    luceneDoc1.add(new StringField("title", doc1.getTitle(), Field.Store.YES));
                    luceneDoc1.add(new StringField("date", doc1.getDate().toString(), Field.Store.YES));
                    w.addDocument(luceneDoc1);
                    System.out.println("Документ 1 добавлен в БД и Lucene. ID: " + doc1.getDocumentID());
                }

                Document doc2 = new Document("Весовые коэффициенты терминов", "Весовые коэф терминов и инверсная частота термина.", new Date(), new Time(System.currentTimeMillis()));
                if (doc2.AddDocumentToBase()) {
                    org.apache.lucene.document.Document luceneDoc2 = new org.apache.lucene.document.Document();
                    luceneDoc2.add(new TextField("text", doc2.getText(), Field.Store.YES));
                    luceneDoc2.add(new StringField("documentId", String.valueOf(doc2.getDocumentID()), Field.Store.YES));
                    luceneDoc2.add(new StringField("title", doc2.getTitle(), Field.Store.YES));
                    luceneDoc2.add(new StringField("date", doc2.getDate().toString(), Field.Store.YES));
                    w.addDocument(luceneDoc2);
                    System.out.println("Документ 2 добавлен в БД и Lucene. ID: " + doc2.getDocumentID());
                }

                Document doc3 = new Document("Кэфы", "коэффициенты значимости важны.", new Date(), new Time(System.currentTimeMillis()));
                if (doc3.AddDocumentToBase()) {
                    org.apache.lucene.document.Document luceneDoc2 = new org.apache.lucene.document.Document();
                    luceneDoc2.add(new TextField("text", doc3.getText(), Field.Store.YES));
                    luceneDoc2.add(new StringField("documentId", String.valueOf(doc3.getDocumentID()), Field.Store.YES));
                    luceneDoc2.add(new StringField("title", doc3.getTitle(), Field.Store.YES));
                    luceneDoc2.add(new StringField("date", doc3.getDate().toString(), Field.Store.YES));
                    w.addDocument(luceneDoc2);
                    System.out.println("Документ 3 добавлен в БД и Lucene. ID: " + doc3.getDocumentID());
                }

                System.out.println("Индексирование завершено.");
            } // IndexWriter автоматически закрывается, и изменения сохраняются в индексе.

            // Шаг 2: Выполнение поиска по проиндексированным документам
            String userQuery = "коэффициенты значимости";
            System.out.println("\nВыполняется поиск по запросу: '" + userQuery + "'");
            Search searchEngine = new Search(userQuery, index);
            List<SearchResult> results = searchEngine.GetSearchResult();

            // Шаг 3: Отображение результатов поиска
            System.out.println("Результаты поиска:");
            if (results.isEmpty()) {
                System.out.println("Документы не найдены.");
            } else {
                for (SearchResult result : results) {
                    System.out.println("------------------------------------");
                    System.out.println("Document ID: " + result.getDocumentId());
                    System.out.println("Title: " + result.getTitle());
                    System.out.println("Relevance Score: " + result.getRank());
                    System.out.println("Snippet: " + result.getSnippet() + "...");
                    System.out.println("Date: " + result.getDate());
                    System.out.println("------------------------------------");
                }
            }

            System.out.println("\nПрограмма завершена.");

        } catch (IOException e) {
            System.err.println("Произошла ошибка при работе с индексом Lucene.");
            e.printStackTrace();
        }
    }
}
