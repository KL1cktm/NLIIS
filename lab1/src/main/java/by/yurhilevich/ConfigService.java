package by.yurhilevich;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    private final String configPath;
    private final Properties properties = new Properties();

    public ConfigService(@Value("${app.config.path}") String configPath) {
        this.configPath = configPath;
        loadProperties();
    }

    private void loadProperties() {
        try {
            if (Files.exists(Paths.get(configPath))) {
                try (FileInputStream fis = new FileInputStream(configPath)) {
                    properties.load(fis);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getSearchDirectories() {
        String dirs = properties.getProperty("app.search.directories", "documents");
        return Arrays.stream(dirs.split("\\s*,\\s*|\\s*\\n\\s*")) // Разделяем по запятой или новой строке
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public int getReindexIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("app.reindex.interval.minutes", "2"));
    }

    public void saveSettings(String directories, int interval) {
        System.out.println("Настройки успешно изменены! \nДиректория: " + directories + "\nВремя обновления: " + interval);
        properties.setProperty("app.search.directories", directories);
        properties.setProperty("app.reindex.interval.minutes", String.valueOf(interval));
        try (FileOutputStream fos = new FileOutputStream(configPath)) {
            properties.store(fos, "Search Engine Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}