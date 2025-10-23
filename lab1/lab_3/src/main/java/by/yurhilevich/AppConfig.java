package by.yurhilevich;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AppConfig {

    @Value("${models.path.ru.sent}")
    private String ruSentModel;
    @Value("${models.path.ru.token}")
    private String ruTokenModel;
    @Value("${models.path.en.sent}")
    private String enSentModel;
    @Value("${models.path.en.token}")
    private String enTokenModel;

    @Bean
    public Preprocessor preprocessor() throws IOException {
        try {
            return new Preprocessor(ruSentModel, ruTokenModel, enSentModel, enTokenModel);
        } catch (IOException e) {
            System.err.println("!!! КРИТИЧЕСКАЯ ОШИБКА: Не удалось загрузить модели NLP.");
            System.err.println("Проверьте пути в 'application.properties' и наличие файлов.");
            throw e;
        }
    }

    @Bean
    public CorpusStats corpusStats(Preprocessor preprocessor) {
        return new CorpusStats(preprocessor);
    }

    @Bean
    public LlamaService llamaService() {
        return new LlamaService();
    }

    @Bean
    public Summarizer summarizer(Preprocessor preprocessor, CorpusStats corpusStats, LlamaService llamaService) {
        return new Summarizer(preprocessor, corpusStats, llamaService);
    }
}