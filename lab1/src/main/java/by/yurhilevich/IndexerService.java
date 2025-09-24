package by.yurhilevich;

import jakarta.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class IndexerService {

    @Value("${app.index.dir}")
    private String indexDir;

    @Value("${app.docs.dir}")
    private String docsDir;

    @PostConstruct
    public void startIndexing() {
        try {
            System.out.println("--- Начало индексации ---");
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            Analyzer analyzer = new RussianAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

            try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                indexDocs(writer, Paths.get(docsDir));
            }

            System.out.println("--- Индексация завершена ---");

        } catch (IOException e) {
            System.err.println("Ошибка при индексации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (!Files.exists(path)) {
            System.err.println("Директория для индексации не найдена: " + path);
            return;
        }

        try (Stream<Path> stream = Files.walk(path)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".txt"))
                    .forEach(file -> {
                        try {
                            indexDoc(writer, file);
                        } catch (IOException e) {
                            System.err.println("Не удалось проиндексировать файл: " + file);
                        }
                    });
        }
    }

    private void indexDoc(IndexWriter writer, Path file) throws IOException {
        System.out.println("Индексируется файл: " + file);
        String content = Files.readString(file);

        Document doc = new Document();
        doc.add(new StringField("path", file.toAbsolutePath().toString(), Field.Store.YES));
        doc.add(new TextField("title", file.getFileName().toString().replace(".txt", ""), Field.Store.YES));
        doc.add(new TextField("contents", TextProcessor.preprocess(content), Field.Store.YES));

        writer.addDocument(doc);
    }
}