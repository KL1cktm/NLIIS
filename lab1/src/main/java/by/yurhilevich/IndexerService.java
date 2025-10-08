package by.yurhilevich;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Service
public class IndexerService {

    private final String indexDir;
    private final ConfigService configService;

    public IndexerService(@Value("${app.index.dir}") String indexDir, ConfigService configService) {
        this.indexDir = indexDir;
        this.configService = configService;
        rebuildIndex();
    }

    /**
     * --- ИСПРАВЛЕНИЕ: Это теперь главный публичный метод для индексации. ---
     * Он полностью перестраивает индекс на основе текущих настроек.
     * Его вызывает конструктор при старте и планировщик для обновлений.
     */
    public void rebuildIndex() {
        try {
            System.out.println("--- Начало полной переиндексации ---");
            List<String> directories = configService.getSearchDirectories();
            Directory dir = FSDirectory.open(Paths.get(indexDir));
            Analyzer analyzer = new RussianAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // Всегда полностью пересоздаем индекс

            try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                for (String docPath : directories) {
                    indexDocs(writer, Paths.get(docPath.trim()));
                }
            }
            updateLastIndexTime();
            System.out.println("--- Переиндексация завершена ---");
        } catch (IOException e) {
            System.err.println("Критическая ошибка при переиндексации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void indexDocs(final IndexWriter writer, Path path) throws IOException {
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            System.err.println("Директория для индексации не найдена или не является папкой: " + path);
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

    /**
     * --- НОВЫЙ МЕТОД: Получает время последней успешной индексации. ---
     * Нужен для планировщика, чтобы сравнивать с датами изменения файлов.
     */
    public long getLastIndexTime() {
        File timestampFile = new File(indexDir, "index.timestamp");
        if (timestampFile.exists()) {
            return timestampFile.lastModified();
        }
        return 0L;
    }

    /**
     * --- НОВЫЙ МЕТОД: Создает или обновляет файл-метку времени. ---
     * Вызывается после каждой успешной переиндексации.
     */
    private void updateLastIndexTime() throws IOException {
        File timestampFile = new File(indexDir, "index.timestamp");
        if (!timestampFile.exists()) {
            Files.createFile(timestampFile.toPath());
        }
        timestampFile.setLastModified(System.currentTimeMillis());
    }
}